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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.MediaType;

import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.causeway.viewer.restfulobjects.applib.util.Parser;

/**
 * @since 1.x {@index}
 */
public class RestfulResponse<T> {

    public static final class HttpStatusCode {

        private static final Map<Status, HttpStatusCode> statii = _Maps.newHashMap();
        private static final Map<Integer, HttpStatusCode> statusCodes = _Maps.newHashMap();

        private static class StatusTypeImpl implements StatusType {

            private final int statusCode;
            private final Family family;
            private final String reasonPhrase;

            private StatusTypeImpl(final int statusCode, final Family family, final String reasonPhrase) {
                this.statusCode = statusCode;
                this.family = family;
                this.reasonPhrase = reasonPhrase;
            }

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public Family getFamily() {
                return family;
            }

            @Override
            public String getReasonPhrase() {
                return reasonPhrase;
            }
        }

        public static HttpStatusCode lookup(final int status) {
            return statusCodes.get(status);
        }

        public static Family lookupFamily(final int statusCode) {
            switch (statusCode / 100) {
            case 1:
                return Family.INFORMATIONAL;
            case 2:
                return Family.SUCCESSFUL;
            case 3:
                return Family.REDIRECTION;
            case 4:
                return Family.CLIENT_ERROR;
            case 5:
                return Family.SERVER_ERROR;
            default:
                return Family.OTHER;
            }
        }

        // public static final int SC_CONTINUE = 100;
        // public static final int SC_SWITCHING_PROTOCOLS = 101;
        // public static final int SC_PROCESSING = 102;

        public static final HttpStatusCode OK = new HttpStatusCode(200, Status.OK);
        public static final HttpStatusCode CREATED = new HttpStatusCode(201, Status.CREATED);

        // public static final int SC_ACCEPTED = 202;
        // public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

        public static final HttpStatusCode NO_CONTENT = new HttpStatusCode(204, Status.NO_CONTENT);

        // public static final int SC_RESET_CONTENT = 205;
        // public static final int SC_PARTIAL_CONTENT = 206;
        // public static final int SC_MULTI_STATUS = 207;
        // public static final int SC_MULTIPLE_CHOICES = 300;
        // public static final int SC_MOVED_PERMANENTLY = 301;
        // public static final int SC_MOVED_TEMPORARILY = 302;
        // public static final int SC_SEE_OTHER = 303;
        public static final HttpStatusCode NOT_MODIFIED = new HttpStatusCode(304, Status.BAD_REQUEST);

        // public static final int SC_NOT_MODIFIED = 304;
        // public static final int SC_USE_PROXY = 305;
        // public static final int SC_TEMPORARY_REDIRECT = 307;

        public static final HttpStatusCode BAD_REQUEST = new HttpStatusCode(400, Status.BAD_REQUEST);
        public static final HttpStatusCode UNAUTHORIZED = new HttpStatusCode(401, Status.UNAUTHORIZED);

        // public static final int SC_PAYMENT_REQUIRED = 402;
        public static final HttpStatusCode FORBIDDEN = new HttpStatusCode(403, Status.FORBIDDEN);

        public static final HttpStatusCode NOT_FOUND = new HttpStatusCode(404, Status.NOT_FOUND);
        public static final HttpStatusCode METHOD_NOT_ALLOWED = new HttpStatusCode(405, new StatusTypeImpl(405, Family.CLIENT_ERROR, "Method not allowed"));
        public static final HttpStatusCode NOT_ACCEPTABLE = new HttpStatusCode(406, Status.NOT_ACCEPTABLE);

        // public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
        // public static final int SC_REQUEST_TIMEOUT = 408;

        public static final HttpStatusCode CONFLICT = new HttpStatusCode(409, Status.CONFLICT);

        // public static final int SC_GONE = 410;
        // public static final int SC_LENGTH_REQUIRED = 411;
        // public static final int SC_PRECONDITION_FAILED = 412;
        // public static final int SC_REQUEST_TOO_LONG = 413;
        // public static final int SC_REQUEST_URI_TOO_LONG = 414;
        // public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

        public static final HttpStatusCode UNSUPPORTED_MEDIA_TYPE = new HttpStatusCode(415, Status.UNSUPPORTED_MEDIA_TYPE);

        // public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        // public static final int SC_EXPECTATION_FAILED = 417;
        // public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;

        public static final HttpStatusCode METHOD_FAILURE = new HttpStatusCode(420, new StatusTypeImpl(420, Family.CLIENT_ERROR, "Method failure"));

        // public static final int SC_UNPROCESSABLE_ENTITY = 422;
        public static final HttpStatusCode VALIDATION_FAILED = new HttpStatusCode(422, new StatusTypeImpl(422, Family.CLIENT_ERROR, "Validation failed"));

        // public static final int SC_LOCKED = 423;
        // public static final int SC_FAILED_DEPENDENCY = 424;

        public static final HttpStatusCode PRECONDITION_HEADER_MISSING = new HttpStatusCode(428, new StatusTypeImpl(428, Family.CLIENT_ERROR, "Precondition header missing"));

        public static final HttpStatusCode INTERNAL_SERVER_ERROR = new HttpStatusCode(500, Status.INTERNAL_SERVER_ERROR);
        public static final HttpStatusCode NOT_IMPLEMENTED = new HttpStatusCode(501, new StatusTypeImpl(501, Family.SERVER_ERROR, "Not implemented"));

        // public static final int SC_BAD_GATEWAY = 502;
        // public static final int SC_SERVICE_UNAVAILABLE = 503;
        // public static final int SC_GATEWAY_TIMEOUT = 504;
        // public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
        // public static final int SC_INSUFFICIENT_STORAGE = 507;

        public static final HttpStatusCode statusFor(final int statusCode) {
            final HttpStatusCode httpStatusCode = statusCodes.get(statusCode);
            if (httpStatusCode != null) {
                return httpStatusCode;
            }
            return statusForSynchronized(statusCode);
        }

        public static final HttpStatusCode statusFor(final Status status) {
            return statii.get(status);
        }

        private static final synchronized HttpStatusCode statusForSynchronized(final int statusCode) {
            HttpStatusCode httpStatusCode = statusCodes.get(statusCode);
            if (httpStatusCode != null) {
                return httpStatusCode;
            }
            httpStatusCode = new HttpStatusCode(statusCode, null);
            statusCodes.put(statusCode, httpStatusCode);
            return httpStatusCode;
        }

        private final int statusCode;
        private final Family family;
        private final StatusType jaxrsStatusType;

        private HttpStatusCode(final int statusCode, final StatusType status) {
            this.statusCode = statusCode;
            this.jaxrsStatusType = status;
            family = lookupFamily(statusCode);
            statusCodes.put(statusCode, this);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public StatusType getJaxrsStatusType() {
            return jaxrsStatusType;
        }

        public Family getFamily() {
            return family;
        }

        @Override
        public int hashCode() {
            return statusCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HttpStatusCode other = (HttpStatusCode) obj;
            if (statusCode != other.statusCode) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "HttpStatusCode " + statusCode + ", " + family;
        }

    }

    public static class Header<X> {

        public static final Header<String> WARNING = new Header<String>("Warning", warningParser());
        public static final Header<Date> LAST_MODIFIED = new Header<Date>("Last-Modified", Parser.forDate());
        public static final Header<CacheControl> CACHE_CONTROL = new Header<CacheControl>("Cache-Control", Parser.forCacheControl());
        public static final Header<MediaType> CONTENT_TYPE = new Header<MediaType>("Content-Type", Parser.forJaxRsMediaType());
        public static final Header<Integer> CONTENT_LENGTH = new Header<Integer>("Content-Length", Parser.forInteger());
        public static final Header<String> ETAG = new Header<String>("ETag", Parser.forETag());

        private final String name;
        private final Parser<X> parser;

        private Header(final String name, final Parser<X> parser) {
            this.name = name;
            this.parser = parser;
        }

        public String getName() {
            return name;
        }

        public X parse(final String value) {
            return value != null? parser.valueOf(value): null;
        }

        public String render(X message) {
            return parser.asString(message);
        }

        private static Parser<String> warningParser() {
            return new Parser<String>(){
                private static final String PREFIX = "199 RestfulObjects ";

                @Override
                public String valueOf(String str) {
                    return stripPrefix(str, PREFIX);
                }

                @Override
                public String asString(String str) {
                    return PREFIX + str;
                }
                private String stripPrefix(String str, String prefix) {
                    return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
                }
            };
        }

    }

    private final Response response;
    private final HttpStatusCode httpStatusCode;
    private final Class<T> returnType;
    private T entity;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RestfulResponse<JsonRepresentation> of(final Response response) {
        final MediaType jaxRsMediaType = getHeader(response, Header.CONTENT_TYPE);
        final RepresentationType representationType = RepresentationType.lookup(jaxRsMediaType);
        final Class<? extends JsonRepresentation> returnType = representationType.getRepresentationClass();
        return new RestfulResponse(response, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T extends JsonRepresentation> RestfulResponse<T> ofT(final Response response) {
        return (RestfulResponse<T>) of(response);
    }

    private RestfulResponse(final Response response, final Class<T> returnType) {
        this.response = response;
        this.httpStatusCode = HttpStatusCode.statusFor(response.getStatus());
        this.returnType = returnType;
    }

    public HttpStatusCode getStatus() {
        return httpStatusCode;
    }

    public T getEntity() throws JsonParseException, JsonMappingException, IOException {
        if(entity == null) {
            // previously this was good enough, but no longer it seems
            //entity = JsonMapper.instance().read(response, returnType);

            // instead, we do it manually
            final JsonNode jsonNode = JsonMapper.instance().read(response, JsonNode.class);
            try {
                final Constructor<T> constructor = returnType.getConstructor(JsonNode.class);
                entity = constructor.newInstance(jsonNode);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return entity;
    }

    public <V> V getHeader(final Header<V> header) {
        return getHeader(response, header);
    }

    private static <V> V getHeader(final Response response, final Header<V> header) {
        final MultivaluedMap<String, Object> metadata = response.getMetadata();
        // always returns a String
        final String value = (String) metadata.getFirst(header.getName());
        return header.parse(value);
    }

    /**
     * Convenience that recasts this response as wrapping some other
     * representation.
     *
     * <p>
     * This would typically be as the results of a content type being an
     * error rather than a representation returned on success.
     */
    @SuppressWarnings("unchecked")
    public <Q extends JsonRepresentation> RestfulResponse<Q> wraps(Class<Q> cls) {
        return (RestfulResponse<Q>) this;
    }

    @Override
    public String toString() {
        return "RestfulResponse [httpStatusCode=" + httpStatusCode + "]";
    }

}
