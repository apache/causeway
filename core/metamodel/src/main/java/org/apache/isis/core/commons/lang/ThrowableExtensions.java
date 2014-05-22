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

package org.apache.isis.core.commons.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;

public final class ThrowableExtensions {

    public static String stackTraceFor(final Throwable extendee) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            extendee.printStackTrace(new PrintStream(baos));
            return baos.toString();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (final IOException ignore) {
                }
            }
        }
    }

    public static void throwWithinIsisException(final InvocationTargetException e, final String error) {
        final Throwable targetException = e.getTargetException();
        if (targetException instanceof RecoverableException) {
            // an application exception from the domain code is re-thrown as an
            // IsisException with same semantics
            // TODO: should probably be using ApplicationException here
            throw new IsisApplicationException(targetException);
        }
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        } else {
            throw new MetaModelException(targetException);
        }
    }
}
