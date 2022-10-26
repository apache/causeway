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

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

public class ReprRendererException extends RuntimeException {

    public static ReprRendererException create(final String message, final Object... args) {
        return create((Exception) null, message, args);
    }

    public static ReprRendererException create(final Exception cause) {
        return create(cause, null);
    }

    public static ReprRendererException create(final Exception cause, final String message, final Object... args) {
        return new ReprRendererException(formatString(message, args), cause, null);
    }

    public static ReprRendererException create(final JsonRepresentation repr, final String message, final Object... args) {
        return new ReprRendererException(formatString(message, args), null, repr);
    }

    private static String formatString(final String formatStr, final Object... args) {
        return formatStr != null ? String.format(formatStr, args) : null;
    }

    private static final long serialVersionUID = 1L;
    private final JsonRepresentation jsonRepresentation;

    private ReprRendererException(final String message, final Throwable ex, final JsonRepresentation jsonRepresentation) {
        super(message, ex);
        this.jsonRepresentation = jsonRepresentation;
    }

    public JsonRepresentation getJsonRepresentation() {
        return jsonRepresentation;
    }

}
