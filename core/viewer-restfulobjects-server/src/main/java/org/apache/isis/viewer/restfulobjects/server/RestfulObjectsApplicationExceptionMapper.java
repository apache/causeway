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
package org.apache.isis.viewer.restfulobjects.server;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.exceptions.ExceptionUtils;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.rendering.HasHttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

//@Path("/") // FIXME: workaround for TomEE ... but breaks the RestEasy TCK tests so commented out:-(
@Provider
public class RestfulObjectsApplicationExceptionMapper implements ExceptionMapper<RestfulObjectsApplicationException> {

    @Override
    public Response toResponse(final RestfulObjectsApplicationException ex) {
        final ResponseBuilder builder = Response.status(ex.getHttpStatusCode().getJaxrsStatusType());

        // body and content-type
        final JsonRepresentation bodyRepr = ex.getBody();
        final Throwable cause = ex.getCause();
        if (bodyRepr != null) {
            final String body = bodyRepr.toString();
            builder.entity(body);
            builder.type(MediaType.APPLICATION_JSON); // generic; the spec doesn't define what the media type should be
        } else if(cause == null) {
            builder.type(MediaType.APPLICATION_JSON); // generic; the spec doesn't define what the media type should be
        } else { 
            String body;
            try {
                body = JsonMapper.instance().write(ExceptionPojo.create(cause));
            } catch (final Exception e) {
                // fallback
                body = "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(cause) + "\" }";
            }
            builder.entity(body);
            builder.type(RestfulMediaType.APPLICATION_JSON_ERROR);
        }

        final String message = ex.getMessage();
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }
        return builder.build();
    }

    private static class ExceptionPojo {

        public static ExceptionPojo create(final Throwable ex) {
            return new ExceptionPojo(ex);
        }

        private static String format(final StackTraceElement stackTraceElement) {
            return stackTraceElement.toString();
        }

        private final int httpStatusCode;
        private final String message;
        private final List<String> stackTrace = Lists.newArrayList();
        private ExceptionPojo causedBy;

        public ExceptionPojo(final Throwable ex) {
            httpStatusCode = getHttpStatusCodeIfAny(ex);
            this.message = ex.getMessage();
            final StackTraceElement[] stackTraceElements = ex.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                this.stackTrace.add(format(stackTraceElement));
            }
            final Throwable cause = ex.getCause();
            if (cause != null && cause != ex) {
                this.causedBy = new ExceptionPojo(cause);
            }
        }

        private int getHttpStatusCodeIfAny(final Throwable ex) {
            if (!(ex instanceof HasHttpStatusCode)) {
                return 0;
            }
            final HasHttpStatusCode hasHttpStatusCode = (HasHttpStatusCode) ex;
            return hasHttpStatusCode.getHttpStatusCode().getStatusCode();
        }

        @SuppressWarnings("unused")
        public int getHttpStatusCode() {
            return httpStatusCode;
        }

        @SuppressWarnings("unused")
        public String getMessage() {
            return message;
        }

        @SuppressWarnings("unused")
        public List<String> getStackTrace() {
            return stackTrace;
        }

        @SuppressWarnings("unused")
        public ExceptionPojo getCausedBy() {
            return causedBy;
        }

    }


}
