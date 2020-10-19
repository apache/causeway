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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 */
@Log4j2
public class ClientConversationLogger implements ClientConversationFilter {
    
    @Override
    public void onRequest(String endpoint, String method, String acceptHeaderParsing,
            Map<String, List<String>> headers, String body) {
        
        val headersAsText = headers.entrySet().stream()
                .map(this::toKeyValueString)
                .map(this::obscureAuthHeader)
                .collect(Collectors.joining(",\n\t"));

        val sb = new StringBuilder();
        sb.append("\n")
        .append("---------- JAX-RS REQUEST -------------\n")
        .append("uri: ").append(endpoint).append("\n")
        .append("method: ").append(method).append("\n")
        .append("accept-header-parsing: ").append(acceptHeaderParsing).append("\n")
        .append("headers: \n\t").append(headersAsText).append("\n")
        .append("request-body: ").append(body).append("\n")
        .append("----------------------------------------\n")
        ;

        log.info(sb.toString());
    }

    @Override
    public void onResponse(int httpReturnCode, Map<String, List<String>> headers, String body) {
        val headersAsText = headers.entrySet().stream()
                .map(this::toKeyValueString)
                .map(this::obscureAuthHeader)
                .collect(Collectors.joining(",\n\t"));
        
        val sb = new StringBuilder();
        sb.append("\n")
        .append("---------- JAX-RS RESPONSE -------------\n")
        .append("http-return-code: \n\t").append(httpReturnCode).append("\n")
        .append("headers: \n\t").append(headersAsText).append("\n")
        .append("response-body: ").append(body).append("\n")
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