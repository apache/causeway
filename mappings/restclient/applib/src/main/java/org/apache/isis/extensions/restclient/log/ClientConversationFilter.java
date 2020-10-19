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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.apache.isis.commons.internal.base._Bytes;

import lombok.val;

public interface ClientConversationFilter 
extends ClientRequestFilter, ClientResponseFilter {
    
    void onRequest(
            String endpoint,
            String method,
            String acceptHeaderParsing,
            Map<String, List<String>> headers, 
            String body);
    
    void onResponse(int httpReturnCode, Map<String, List<String>> headers, String body);

    @Override
    default void filter(ClientRequestContext requestContext) throws IOException {
        val endpoint = requestContext.getUri().toString();
        val method = requestContext.getMethod();

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

        final String requestBody = requestContext.getEntity().toString();

        onRequest(
                endpoint, method, acceptHeaderParsing,
                requestContext.getStringHeaders(), 
                requestBody);
    }
    
    @Override
    default void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

        val inputStream = responseContext.getEntityStream();
        final String responseBody;
        if(inputStream!=null) {
            val bytes = _Bytes.ofKeepOpen(inputStream);
            responseBody = new String(bytes, StandardCharsets.UTF_8);
            responseContext.setEntityStream(new ByteArrayInputStream(bytes));
        } else {
            responseBody = "null";
        }

        onResponse(responseContext.getStatusInfo().getStatusCode(), responseContext.getHeaders(), responseBody);
    }
  
    
}
