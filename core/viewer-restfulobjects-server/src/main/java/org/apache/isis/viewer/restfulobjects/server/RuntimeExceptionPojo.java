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
import javax.jdo.JDOException;
import com.google.common.collect.Lists;

class RuntimeExceptionPojo {

    public static RuntimeExceptionPojo create(final Exception ex) {
        return new RuntimeExceptionPojo(ex);
    }

    private static String format(final StackTraceElement stackTraceElement) {
        return stackTraceElement.toString();
    }

    private final String className;
    private final String message;
    private final List<String> stackTrace = Lists.newArrayList();
    private RuntimeExceptionPojo causedBy;

    public RuntimeExceptionPojo(final Throwable ex) {
        this.className = ex.getClass().getName();
        this.message = messageFor(ex);
        final StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            this.stackTrace.add(format(stackTraceElement));
        }

        final Throwable cause = causeOf(ex);
        if (cause != null && cause != ex) {
            this.causedBy = new RuntimeExceptionPojo(cause);
        }
    }

    private static Throwable causeOf(Throwable ex) {
        if (ex instanceof JDOException) {
            final JDOException jdoException = (JDOException) ex;
            final Throwable[] nestedExceptions = jdoException.getNestedExceptions();
            return nestedExceptions.length > 0? nestedExceptions[0]: null;
        }
        else {
            return ex.getCause();
        }
    }

    private static String messageFor(final Throwable ex) {
        final String message = ex.getMessage();
        return message != null ? message : ex.getClass().getName();
    }

    @SuppressWarnings("unused")
    public String getClassName() {
        return className;
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
    public RuntimeExceptionPojo getCausedBy() {
        return causedBy;
    }
}
