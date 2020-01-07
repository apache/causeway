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
package org.apache.isis.legacy.restclient.lib;

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public interface ClientExecutor {

    ClientRequest createRequest(UriBuilder uriBuilder);
    WebTarget webTarget(URI baseUri);

    static ClientExecutor of(final Client client) {
        
        return new ClientExecutor() {

            @Override
            public ClientRequest createRequest(UriBuilder uriBuilder) {

                return new ClientRequest() {

                    final WebTarget target = client.target(uriBuilder);

                    final List<MediaType> accept = new ArrayList<>();
                    final List<Map.Entry<String, String>> header = new ArrayList<>();

                    private Entity<String> payload;

                    // TODO [andi-huber] just a wild guess
                    private String method = "get";
                    
                    @Override
                    public String getHttpMethod() {
                        return method;
                    }

                    @Override
                    public void setHttpMethod(String method) {
                        this.method = method;
                    }

                    @Override
                    public void accept(MediaType mediaType) {
                        accept.add(mediaType);
                    }

                    @Override
                    public void header(String headerName, String value) {
                        header.add(new AbstractMap.SimpleEntry<String, String>(headerName, value));
                    }

                    @Override
                    public void addQueryParameter(String param, String arg) {
                        target.queryParam(param, arg);
                    }

                    @Override
                    public void jsonPayload(String jsonString) {
                        payload = Entity.json(jsonString);
                    }

                    @Override
                    public Response execute() {
                        final Builder builder = target.request();

                        accept.stream()
                        .forEach(builder::accept);
                        header.stream()
                        .forEach(e->builder.header(e.getKey(), e.getValue()));

                        final Invocation invocation = payload==null
                                ? builder.build(method)
                                        : builder.build(method, payload);

                                return invocation.invoke();
                    }

                };
            }

            @Override
            public WebTarget webTarget(URI baseUri) {
                return client.target(baseUri);
            }

        };
    }


}
