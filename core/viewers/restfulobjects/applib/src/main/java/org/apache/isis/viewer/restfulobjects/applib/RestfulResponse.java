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
package org.apache.isis.viewer.restfulobjects.applib;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;

public class RestfulResponse<T> {

    public final static class HttpStatusCode {

        private final static Map<Status, HttpStatusCode> statii = _Maps.newHashMap();
        private final static Map<Integer, HttpStatusCode> statusCodes = _Maps.newHashMap();

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

        public final static HttpStatusCode OK = new HttpStatusCode(200, Status.OK);
        public final static HttpStatusCode CREATED = new HttpStatusCode(201, Status.CREATED);

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
        public final static HttpStatusCode NOT_MODIFIED = new HttpStatusCode(304, Status.BAD_REQUEST);

        // public static final int SC_NOT_MODIFIED = 304;
        // public static final int SC_USE_PROXY = 305;
        // public static final int SC_TEMPORARY_REDIRECT = 307;

        public final static HttpStatusCode BAD_REQUEST = new HttpStatusCode(400, Status.BAD_REQUEST);
        public final static HttpStatusCode UNAUTHORIZED = new HttpStatusCode(401, Status.UNAUTHORIZED);

        // public static final int SC_PAYMENT_REQUIRED = 402;
        public static final HttpStatusCode FORBIDDEN = new HttpStatusCode(403, Status.FORBIDDEN);

        public final static HttpStatusCode NOT_FOUND = new HttpStatusCode(404, Status.NOT_FOUND);
        public final static HttpStatusCode METHOD_NOT_ALLOWED = new HttpStatusCode(405, new StatusTypeImpl(405, Family.CLIENT_ERROR, "Method not allowed"));
        public final static HttpStatusCode NOT_ACCEPTABLE = new HttpStatusCode(406, Status.NOT_ACCEPTABLE);

        // public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
        // public static final int SC_REQUEST_TIMEOUT = 408;

        public final static HttpStatusCode CONFLICT = new HttpStatusCode(409, Status.CONFLICT);

        // public static final int SC_GONE = 410;
        // public static final int SC_LENGTH_REQUIRED = 411;
        // public static final int SC_PRECONDITION_FAILED = 412;
        // public static final int SC_REQUEST_TOO_LONG = 413;
        // public static final int SC_REQUEST_URI_TOO_LONG = 414;
        // public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

        public final static HttpStatusCode UNSUPPORTED_MEDIA_TYPE = new HttpStatusCode(415, Status.UNSUPPORTED_MEDIA_TYPE);

        // public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        // public static final int SC_EXPECTATION_FAILED = 417;
        // public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;

        public final static HttpStatusCode METHOD_FAILURE = new HttpStatusCode(420, new StatusTypeImpl(420, Family.CLIENT_ERROR, "Method failure"));

        // public static final int SC_UNPROCESSABLE_ENTITY = 422;
        public final static HttpStatusCode VALIDATION_FAILED = new HttpStatusCode(422, new StatusTypeImpl(422, Family.CLIENT_ERROR, "Validation failed"));

        // public static final int SC_LOCKED = 423;
        // public static final int SC_FAILED_DEPENDENCY = 424;

        public final static HttpStatusCode PRECONDITION_HEADER_MISSING = new HttpStatusCode(428, new StatusTypeImpl(428, Family.CLIENT_ERROR, "Precondition header missing"));

        public final static HttpStatusCode INTERNAL_SERVER_ERROR = new HttpStatusCode(500, Status.INTERNAL_SERVER_ERROR);
        public final static HttpStatusCode NOT_IMPLEMENTED = new HttpStatusCode(501, new StatusTypeImpl(501, Family.SERVER_ERROR, "Not implemented"));

        // public static final int SC_BAD_GATEWAY = 502;
        // public static final int SC_SERVICE_UNAVAILABLE = 503;
        // public static final int SC_GATEWAY_TIMEOUT = 504;
        // public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
        // public static final int SC_INSUFFICIENT_STORAGE = 507;

        public final static HttpStatusCode statusFor(final int statusCode) {
            final HttpStatusCode httpStatusCode = statusCodes.get(statusCode);
            if (httpStatusCode != null) {
                return httpStatusCode;
            }
            return statusForSynchronized(statusCode);
        }

        public final static HttpStatusCode statusFor(final Status status) {
            return statii.get(status);
        }

        private final static synchronized HttpStatusCode statusForSynchronized(final int statusCode) {
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

        public final static Header<String> WARNING = new Header<String>("Warning", warningParser());
        public final static Header<Date> LAST_MODIFIED = new Header<Date>("Last-Modified", Parser.forDate());
        public final static Header<CacheControl> CACHE_CONTROL = new Header<CacheControl>("Cache-Control", Parser.forCacheControl());
        public final static Header<MediaType> CONTENT_TYPE = new Header<MediaType>("Content-Type", Parser.forJaxRsMediaType());
        public final static Header<Integer> CONTENT_LENGTH = new Header<Integer>("Content-Length", Parser.forInteger());
        public final static Header<String> ETAG = new Header<String>("ETag", Parser.forETag());

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
