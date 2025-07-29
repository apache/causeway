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
package org.apache.causeway.viewer.commons.model.error;

import java.io.Serializable;

public record StackTraceDetail(
        Type type,
        String line) implements Serializable {

    public enum Type {
        EXCEPTION_CLASS_NAME,
        EXCEPTION_MESSAGE,
        STACKTRACE_ELEMENT,
        LITERAL
    }

    public static StackTraceDetail exceptionClassName(final Throwable cause) {
        return new StackTraceDetail(StackTraceDetail.Type.EXCEPTION_CLASS_NAME, cause.getClass().getName());
    }

    public static StackTraceDetail exceptionMessage(final Throwable cause) {
        return new StackTraceDetail(StackTraceDetail.Type.EXCEPTION_MESSAGE, cause.getMessage());
    }

    public static StackTraceDetail element(final StackTraceElement el) {
        var buf = new StringBuilder();
        buf.append("    ")
            .append(el.getClassName())
            .append("#")
            .append(el.getMethodName())
            .append("(")
            .append(el.getFileName())
            .append(":")
            .append(el.getLineNumber())
            .append(")\n");

        return new StackTraceDetail(StackTraceDetail.Type.STACKTRACE_ELEMENT, buf.toString());
    }

    public static StackTraceDetail spacer() {
        return new StackTraceDetail(Type.LITERAL, "");
    }

    public static StackTraceDetail causedBy() {
        return new StackTraceDetail(Type.LITERAL, "Caused by:");
    }

}