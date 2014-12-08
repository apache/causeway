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
import com.google.common.collect.Lists;
import org.apache.isis.viewer.restfulobjects.rendering.HasHttpStatusCode;

class RestfulObjectsApplicationExceptionPojo {

    public static RestfulObjectsApplicationExceptionPojo create(final Throwable ex) {
        return new RestfulObjectsApplicationExceptionPojo(ex);
    }

    private static String format(final StackTraceElement stackTraceElement) {
        return stackTraceElement.toString();
    }

    private final String className;
    private final int httpStatusCode;
    private final String message;
    private final List<String> stackTrace = Lists.newArrayList();
    private RestfulObjectsApplicationExceptionPojo causedBy;

    public RestfulObjectsApplicationExceptionPojo(final Throwable ex) {
        this.className = ex.getClass().getName();
        this.httpStatusCode = getHttpStatusCodeIfAny(ex);
        this.message = ex.getMessage();
        final StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            this.stackTrace.add(format(stackTraceElement));
        }
        final Throwable cause = ex.getCause();
        if (cause != null && cause != ex) {
            this.causedBy = new RestfulObjectsApplicationExceptionPojo(cause);
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
    public String getClassName() {
        return className;
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
    public RestfulObjectsApplicationExceptionPojo getCausedBy() {
        return causedBy;
    }

}
