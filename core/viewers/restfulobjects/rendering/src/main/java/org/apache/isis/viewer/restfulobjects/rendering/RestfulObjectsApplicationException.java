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
package org.apache.isis.viewer.restfulobjects.rendering;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;

public class RestfulObjectsApplicationException
extends RuntimeException
implements ExceptionWithHttpStatusCode, ExceptionWithBody {

    private static final long serialVersionUID = 1L;

    public static final RestfulObjectsApplicationException create(final HttpStatusCode httpStatusCode) {
        return createWithCause(httpStatusCode, null);
    }

    public static RestfulObjectsApplicationException createWithMessage(
            final HttpStatusCode httpStatusCode,
            final String message, final Object... args) {
        return createWithCauseAndMessage(httpStatusCode, null, message, args);
    }

    public static RestfulObjectsApplicationException createWithCause(
            final HttpStatusCode httpStatusCode,
            final Exception cause) {
        return createWithCauseAndMessage(httpStatusCode, cause, null);
    }

    public static RestfulObjectsApplicationException createWithCauseAndMessage(
            final HttpStatusCode httpStatusCode,
            final Exception cause,
            final String message, final Object... args) {
        return new RestfulObjectsApplicationException(httpStatusCode, formatString(message, args), cause, null);
    }

    public static RestfulObjectsApplicationException createWithBody(
            final HttpStatusCode httpStatusCode,
            final JsonRepresentation body,
            final String message, final Object... args) {
        return new RestfulObjectsApplicationException(httpStatusCode, formatString(message, args), null, body);
    }

    private static String formatString(final String formatStr, final Object... args) {
        return formatStr != null ? String.format(formatStr, args) : null;
    }

    private final HttpStatusCode httpStatusCode;
    private final JsonRepresentation body;

    protected RestfulObjectsApplicationException(
            final HttpStatusCode httpStatusCode,
            final String message,
            final Throwable cause,
            final JsonRepresentation body) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.body = body;
    }

    @Override
    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public JsonRepresentation getBody() {
        return body;
    }

}
