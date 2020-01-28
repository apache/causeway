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
package org.apache.isis.extensions.restclient.log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._Strings;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 */
@Priority(999) @Log4j2
public class RestfulLoggingFilter implements ClientRequestFilter, ClientResponseFilter {
    
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        final String endpoint = requestContext.getUri().toString();
        final String method = requestContext.getMethod();

        Exception acceptableMediaTypeParsingFailure;
        try {
            @SuppressWarnings("unused")
            final String acceptableMediaTypes = requestContext.getAcceptableMediaTypes().toString();
            acceptableMediaTypeParsingFailure = null;
        } catch (Exception e) {
            acceptableMediaTypeParsingFailure = e;
        }
        final String acceptHeaderParsing = acceptableMediaTypeParsingFailure != null
                ? "Failed to parse accept header, cause: " + acceptableMediaTypeParsingFailure.getMessage()
                    : "OK";

        final String headers = requestContext.getStringHeaders().entrySet().stream()
                .map(this::toKeyValueString)
                .map(this::obscureAuthHeader)
                .collect(Collectors.joining(",\n\t"));

        final String requestBody = requestContext.getEntity().toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("\n")
        .append("---------- JAX-RS REQUEST -------------\n")
        .append("uri: ").append(endpoint).append("\n")
        .append("method: ").append(method).append("\n")
        .append("accept-header-parsing: ").append(acceptHeaderParsing).append("\n")
        .append("headers: \n\t").append(headers).append("\n")
        .append("request-body: ").append(requestBody).append("\n")
        .append("----------------------------------------\n")
        ;

        log.info(sb.toString());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

        final InputStream inputStream = responseContext.getEntityStream();
        final String responseBody;
        if(inputStream!=null) {
            val bytes = _Bytes.ofKeepOpen(inputStream);
            responseBody = new String(bytes, StandardCharsets.UTF_8);
            responseContext.setEntityStream(new ByteArrayInputStream(bytes));
        } else {
            responseBody = "null";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("\n")
        .append("---------- JAX-RS RESPONSE -------------\n")
        .append("response-body: ").append(responseBody).append("\n")
        .append("----------------------------------------\n")
        ;

        log.info(sb.toString());

    }

    // -- HELPER

    private final String basicAuthMagic = "Authorization: [Basic "; 

    private String toKeyValueString(Map.Entry<?, ?> entry) {
        return "" + entry.getKey() + ": " + entry.getValue();
    }
    
    private String obscureAuthHeader(String keyValueLiteral) {
        if(_Strings.isEmpty(keyValueLiteral)) {
            return keyValueLiteral;
        }
        if(keyValueLiteral.startsWith(basicAuthMagic)) {

            final String obscured = _Strings.padEnd(basicAuthMagic, keyValueLiteral.length() - 1, '*') + "]";
            return obscured;

        }
        return keyValueLiteral;
    }


}