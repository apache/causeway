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
package org.apache.causeway.viewer.restfulobjects.rendering;

import org.springframework.http.HttpStatus;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import lombok.Getter;
import lombok.experimental.Accessors;

public class RestfulObjectsApplicationException
extends RuntimeException
implements ExceptionWithHttpStatusCode, ExceptionWithBody {

    private static final long serialVersionUID = 1L;

    public static final RestfulObjectsApplicationException create(final HttpStatus httpStatus) {
        return createWithCause(httpStatus, null);
    }

    public static RestfulObjectsApplicationException createWithMessage(
            final HttpStatus httpStatus,
            final String message) {
        return createWithCauseAndMessage(httpStatus, null, message);
    }

    public static RestfulObjectsApplicationException createWithCause(
            final HttpStatus httpStatus,
            final Exception cause) {
        return createWithCauseAndMessage(httpStatus, cause, null);
    }

    public static RestfulObjectsApplicationException createWithCauseAndMessage(
            final HttpStatus httpStatus,
            final Exception cause,
            final String message) {
        return new RestfulObjectsApplicationException(httpStatus, message, cause, null);
    }

    public static RestfulObjectsApplicationException createWithBody(
            final HttpStatus httpStatus,
            final JsonRepresentation body,
            final String message) {
        return new RestfulObjectsApplicationException(httpStatus, message, null, body);
    }

    @Getter(onMethod_={@Override}) @Accessors(fluent=true) private final HttpStatus httpStatus;
    @Getter(onMethod_={@Override}) @Accessors(fluent=true) private final JsonRepresentation body;

    protected RestfulObjectsApplicationException(
            final HttpStatus httpStatus,
            final String message,
            final Throwable cause,
            final JsonRepresentation body) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.body = body;
    }

}
