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
package org.apache.isis.viewer.json.viewer;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;

public class JsonApplicationException extends RuntimeException implements HasHttpStatusCode {

    public static final JsonApplicationException create(final HttpStatusCode httpStatusCode) {
        return create(httpStatusCode, null);
    }

    public static JsonApplicationException create(final HttpStatusCode httpStatusCode, final String message, final Object... args) {
        return create(httpStatusCode, (Exception) null, message, args);
    }

    public static JsonApplicationException create(final HttpStatusCode httpStatusCode, final Exception cause) {
        return create(httpStatusCode, cause, null);
    }

    public static JsonApplicationException create(final HttpStatusCode httpStatusCode, final Exception cause, final String message, final Object... args) {
        return new JsonApplicationException(httpStatusCode, formatString(message, args), cause, null);
    }

    public static JsonApplicationException create(final HttpStatusCode httpStatusCode, final JsonRepresentation repr, final String message, final Object... args) {
        return new JsonApplicationException(httpStatusCode, formatString(message, args), null, repr);
    }

    private static String formatString(final String formatStr, final Object... args) {
        return formatStr != null ? String.format(formatStr, args) : null;
    }

    private static final long serialVersionUID = 1L;
    private final HttpStatusCode httpStatusCode;
    private final JsonRepresentation jsonRepresentation;

    private JsonApplicationException(final HttpStatusCode httpStatusCode, final String message, final Throwable ex, final JsonRepresentation jsonRepresentation) {
        super(message, ex);
        this.httpStatusCode = httpStatusCode;
        this.jsonRepresentation = jsonRepresentation;
    }

    @Override
    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    public JsonRepresentation getJsonRepresentation() {
        return jsonRepresentation;
    }

}
