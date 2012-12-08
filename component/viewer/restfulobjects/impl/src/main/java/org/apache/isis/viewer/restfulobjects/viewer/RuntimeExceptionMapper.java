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
package org.apache.isis.viewer.restfulobjects.viewer;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Lists;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(final RuntimeException ex) {
        final ResponseBuilder builder = Response.status(HttpStatusCode.INTERNAL_SERVER_ERROR.getJaxrsStatusType()).type(RestfulMediaType.APPLICATION_JSON_ERROR).entity(jsonFor(ex));
        return builder.build();
    }

    private static class ExceptionPojo {

        public static ExceptionPojo create(final Exception ex) {
            return new ExceptionPojo(ex);
        }

        private static String format(final StackTraceElement stackTraceElement) {
            return stackTraceElement.toString();
        }

        private final String message;
        private final List<String> stackTrace = Lists.newArrayList();
        private ExceptionPojo causedBy;

        public ExceptionPojo(final Throwable ex) {
            this.message = messageFor(ex);
            final StackTraceElement[] stackTraceElements = ex.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                this.stackTrace.add(format(stackTraceElement));
            }
            final Throwable cause = ex.getCause();
            if (cause != null && cause != ex) {
                this.causedBy = new ExceptionPojo(cause);
            }
        }

        private static String messageFor(final Throwable ex) {
            final String message = ex.getMessage();
            return message != null ? message : ex.getClass().getName();
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

    static String jsonFor(final Exception ex) {
        try {
            return JsonMapper.instance().write(ExceptionPojo.create(ex));
        } catch (final Exception e) {
            // fallback
            return "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(ex) + "\" }";
        }
    }
}
