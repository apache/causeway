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

package org.apache.isis.core.commons.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

/**
 * General exception raised by the framework, typically a system exception.
 */
public class IsisException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static boolean THROWABLE_SUPPORTS_CAUSE;

    static {
        // Java 1.4, and after, holds a cause; Java 1.1, 1.2 and .Net do not, so
        // we need to
        // do the work ourselves.
        final Class<?> c = Throwable.class;
        try {
            THROWABLE_SUPPORTS_CAUSE = c.getMethod("getCause", new Class[0]) != null;
        } catch (final Exception ignore) {
            // this should never occur in proper Java environment
            THROWABLE_SUPPORTS_CAUSE = false;
        }
    }

    private final Throwable cause;

    public IsisException() {
        super("");
        cause = null;
    }

    public IsisException(final String message) {
        super(message);
        cause = null;
    }

    public IsisException(final String messageFormat, final Object... args) {
        super(MessageFormat.format(messageFormat, args));
        cause = null;
    }

    public IsisException(final String message, final Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public IsisException(final Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return (cause == this ? null : cause);
    }

    @Override
    public void printStackTrace(final PrintStream s) {
        if (THROWABLE_SUPPORTS_CAUSE) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                super.printStackTrace(s);
                final Throwable c = getCause();
                if (c != null) {
                    s.print("Root cause: ");
                    c.printStackTrace(s);
                }
            }
        }
    }

    @Override
    public void printStackTrace(final PrintWriter s) {
        if (THROWABLE_SUPPORTS_CAUSE) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                super.printStackTrace(s);
                final Throwable c = getCause();
                if (c != null) {
                    s.println("Root cause: ");
                    c.printStackTrace(s);
                }
            }
        }
    }
}
