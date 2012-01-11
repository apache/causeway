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
package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.util.UrlEncodingUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;

import com.google.common.collect.Maps;

public final class RestfulRequest {

    public enum DomainModel {
        NONE,
        SIMPLE,
        FORMAL,
        SELECTABLE;
        
        public static Parser<DomainModel> parser() {
            return new Parser<RestfulRequest.DomainModel>() {
                
                @Override
                public DomainModel valueOf(String str) {
                    return DomainModel.valueOf(str.toUpperCase());
                }
                
                @Override
                public String asString(DomainModel t) {
                    return t.name().toLowerCase();
                }
            };
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    public static class RequestParameter<Q> {

        public static RequestParameter<List<List<String>>> FOLLOW_LINKS = new RequestParameter<List<List<String>>>("x-ro-follow-links", Parser.forListOfListOfStrings(), Collections.<List<String>>emptyList());
        public static RequestParameter<Integer> PAGE = new RequestParameter<Integer>("x-ro-page", Parser.forInteger(), 1);
        public static RequestParameter<Integer> PAGE_SIZE = new RequestParameter<Integer>("x-ro-page-size", Parser.forInteger(), 25);
        public static RequestParameter<List<String>> SORT_BY = new RequestParameter<List<String>>("x-ro-sort-by", Parser.forListOfStrings(), Collections.<String>emptyList());
        public static RequestParameter<DomainModel> DOMAIN_MODEL = new RequestParameter<DomainModel>("x-ro-domain-model", DomainModel.parser(), DomainModel.SIMPLE);
        public static RequestParameter<Boolean> VALIDATE_ONLY = new RequestParameter<Boolean>("x-ro-validate-only", Parser.forBoolean(), false);
        
        private final String name;
        private final Parser<Q> parser;
        private final Q defaultValue;
        
        private RequestParameter(String name, Parser<Q> parser, Q defaultValue) {
            this.name = name;
            this.parser = parser;
            this.defaultValue = defaultValue;
        }
        
        public String getName() {
            return name;
        }

        public Parser<Q> getParser() {
            return parser;
        }

        public Q valueOf(Map<?, ?> parameterMap) {
            if(parameterMap == null) {
                return defaultValue;
            }
            @SuppressWarnings("unchecked")
            Map<String, String[]> parameters = (Map<String, String[]>) parameterMap; 
            final String[] values = parameters.get(getName());
            if(values == null) {
                return defaultValue;
            }
            // special case processing
            if(values.length == 1) {
                return getParser().valueOf(urlDecode(values[0]));
            }
            return getParser().valueOf(urlDecode(values));
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }

    public static class Header<X> {
        public static Header<String> IF_MATCH = new Header<String>("If-Match", Parser.forString());
        public static Header<List<MediaType>> ACCEPT = new Header<List<MediaType>>("Accept", Parser.forListOfMediaTypes());
            
        private final String name;
        private final Parser<X> parser;
        private Header(String name, Parser<X> parser) {
            this.name = name;
            this.parser = parser;
        }

        public String getName() {
            return name;
        }
        
        public Parser<X> getParser() {
            return parser;
        }
        
        void setHeader(ClientRequest clientRequest, X t) {
            clientRequest.header(getName(), parser.asString(t));
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }

    private final ClientRequest clientRequest;
    private final HttpMethod httpMethod;
    private final Map<RequestParameter<?>, Object> args = Maps.newLinkedHashMap();
    
    public RestfulRequest(ClientRequest clientRequest, HttpMethod httpMethod) {
        this.clientRequest = clientRequest;
        this.httpMethod = httpMethod;
    }


    /**
     * Exposed primarily for testing.
     */
    public ClientRequest getClientRequest() {
        return clientRequest;
    }

    public <T> RestfulRequest withHeader(Header<T> header, T t) {
        header.setHeader(clientRequest, t);
        return this;
    }

    public <T> RestfulRequest withHeader(Header<List<T>> header, T... ts) {
        header.setHeader(clientRequest, Arrays.asList(ts));
        return this;
    }

    public <Q> RestfulRequest withArg(RestfulRequest.RequestParameter<Q> queryParam, String argStrFormat, Object... args) {
        String argStr = String.format(argStrFormat, args);
        final Q arg = queryParam.getParser().valueOf(argStr);
        return withArg(queryParam, arg);
    }

    public <Q> RestfulRequest withArg(RestfulRequest.RequestParameter<Q> queryParam, Q arg) {
        args.put(queryParam, arg);
        return this;
    }

    public RestfulResponse<JsonRepresentation> execute() {
        try {
            switch (httpMethod) {
            case GET:
            case DELETE:
                setQueryArgs();
                break;
            case POST:
            case PUT:
                setBody();
                break;
            }

            Response executeJaxrs = clientRequest.execute();
            return RestfulResponse.ofT(executeJaxrs);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setQueryArgs() {
        for (RequestParameter requestParam : args.keySet()) {
            clientRequest.queryParameter(requestParam.getName(), requestParam.parser.asString(args.get(requestParam)));
        }
    }

    private void setBody() {
        final JsonRepresentation bodyArgs = JsonRepresentation.newMap();
        for (RequestParameter<?> requestParam : args.keySet()) {
            bodyArgs.mapPut(requestParam.getName(), args.get(requestParam));
        }
        clientRequest.body(MediaType.APPLICATION_JSON, bodyArgs.toString());
    }


    @SuppressWarnings("unchecked")
    public <T extends JsonRepresentation> RestfulResponse<T> executeT() {
        final RestfulResponse<JsonRepresentation> restfulResponse = execute();
        return (RestfulResponse<T>) restfulResponse;
    }


}
