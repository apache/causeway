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
package org.apache.isis.viewer.json.applib.blocks;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.util.UrlEncodingUtils;
import org.jboss.resteasy.client.ClientRequest;

public enum Method {
    GET(ArgStrategy.QUERY_STRING),
    PUT(ArgStrategy.BODY),
    POST(ArgStrategy.BODY),
    DELETE(ArgStrategy.QUERY_STRING);

    private enum ArgStrategy {
        QUERY_STRING,
        BODY;
        void setUpArgs(ClientRequest restEasyRequest, JsonRepresentation requestArgs) {
            if(this == QUERY_STRING) {
                final MultivaluedMap<String, String> queryParameters = restEasyRequest.getQueryParameters();
                for(Map.Entry<String, JsonRepresentation> entry: requestArgs.mapIterable()) {
                    final String param = entry.getKey();
                    final JsonRepresentation argRepr = entry.getValue();
                    final String arg = UrlEncodingUtils.asUrlEncoded(argRepr.asArg());
                    queryParameters.add(param, arg);
                }
            } else {
                restEasyRequest.body(MediaType.APPLICATION_JSON_TYPE, requestArgs.toString());
            }
        }
    }
    
    private final ArgStrategy argStrategy;

    private Method(ArgStrategy argStrategy) {
        this.argStrategy = argStrategy;
    }

    public void setUp(ClientRequest restEasyRequest, JsonRepresentation requestArgs) {
        restEasyRequest.setHttpMethod(name());
        if(requestArgs == null) {
            return;
        }
        if(!requestArgs.isMap()) {
            throw new IllegalArgumentException("requestArgs must be a map; instead got: " + requestArgs);
        }
        argStrategy.setUpArgs(restEasyRequest, requestArgs);
    }

}
