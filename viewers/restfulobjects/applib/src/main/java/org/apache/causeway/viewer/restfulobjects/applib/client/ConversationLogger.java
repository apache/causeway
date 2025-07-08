/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.restfulobjects.applib.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.function.ThrowingSupplier;
import org.springframework.web.client.RestClient;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;

/**
 * Provides conversation logging for the {@link RestClient}.
 *
 * <p><strong>Note:</strong> buffering must be enabled through
 * {@link org.springframework.web.client.RestClient.Builder#bufferContent(BiPredicate)}.
 *
 * @since 4.0
 */
public record ConversationLogger(Consumer<String> logAppender)
implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        return onResponse(execution.execute(onRequest(request, body), body));
    }

    // -- HELPER

    private HttpRequest onRequest(final HttpRequest request, final byte[] body) {

        var uri = request.getURI();
        var headers = request.getHeaders();
        var headersAsText = headersAsText(headers);
        var method = request.getMethod().name();

        var sb = new StringBuilder().append("\n")
            .append("---------- REST REQUEST -------------\n")
            .append("uri: ").append(uri).append("\n")
            .append("method: ").append(method).append("\n")
            .append("headers: \n\t").append(headersAsText).append("\n")
            .append("body-size: ").append(_NullSafe.size(body)).append(" bytes\n")
            .append("request-body: ").append(bodyAsText(body)).append("\n")
            .append("----------------------------------------\n");

        logAppender.accept(sb.toString());

        return request;
    }

    private ClientHttpResponse onResponse(final ClientHttpResponse response) {

        var headersAsText = headersAsText(response.getHeaders());
        var statusCode = Try.call(()->response.getStatusCode())
            .getValue()
            .map(HttpStatusCode::toString)
            .orElse("failure retrieving status code");
        var body = bodyAsBytes(response::getBody);

        var sb = new StringBuilder().append("\n")
            .append("---------- REST RESPONSE -------------\n")
            .append("http-return-code: \n\t").append(statusCode).append("\n")
            .append("headers: \n\t").append(headersAsText).append("\n")
            .append("body-size: ").append(_NullSafe.size(body)).append(" bytes\n")
            .append("response-body: ").append(bodyAsText(body)).append("\n")
            .append("----------------------------------------\n");

        logAppender.accept(sb.toString());

        return response;
    }

    private byte[] bodyAsBytes(final ThrowingSupplier<InputStream> bodySupplier) {
        try {
            return DataSource.ofInputStreamEagerly(bodySupplier.get()).bytes();
        } catch (Exception e) {
            return "failed to read response body".getBytes(StandardCharsets.UTF_8);
        }
    }

    private String bodyAsText(final byte[] body) {
        if(_NullSafe.isEmpty(body)) return "";
        try {
            var raw = new String(body, StandardCharsets.UTF_8);
            return _Strings.ellipsifyAtEnd(raw, 4*1024, "...truncated");
        } catch (Exception e) {
            return "failed to interpret response body as String";
        }
    }

    private String headersAsText(final HttpHeaders headers) {
        return headers.toSingleValueMap().entrySet().stream()
            .map(this::toKeyValueString)
            .map(this::obscureAuthHeader)
            .collect(Collectors.joining(",\n\t"));
    }

    private final static String BASIC_AUTH_MAGIC = "Authorization: Basic ";

    private String toKeyValueString(final Map.Entry<?, ?> entry) {
        return "" + entry.getKey() + ": " + entry.getValue();
    }

    private String obscureAuthHeader(final String keyValueLiteral) {
        if(_Strings.isEmpty(keyValueLiteral)) {
            return keyValueLiteral;
        }
        if(keyValueLiteral.startsWith(BASIC_AUTH_MAGIC)) {
            final String obscured = _Strings.padEnd(BASIC_AUTH_MAGIC, keyValueLiteral.length() - 1, '*');
            return obscured;
        }
        return keyValueLiteral;
    }

}
