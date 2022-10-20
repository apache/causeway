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
package org.apache.causeway.core.metamodel.commons;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.causeway.applib.exceptions.unrecoverable.ReflectiveActionException;

public final class ThrowableExtensions {

    public static Exception handleInvocationException(
            final Throwable e,
            final String memberName) {

        if(e instanceof InvocationTargetException) {
            return handleInvocationException(((InvocationTargetException) e).getTargetException(), memberName);
        }
        if(e instanceof WrongMethodTypeException) {
            return new MetaModelException("Wrong method type access of " + memberName, e);
        }
        if(e instanceof IllegalAccessException) {
            return new ReflectiveActionException("Illegal access of " + memberName, e);
        }
        if(e instanceof IllegalStateException) {
            return new ReflectiveActionException( String.format(
                    "IllegalStateException thrown while invoking %s %s",
                    memberName, e.getMessage()), e);
        }
        if(e instanceof RecoverableException) {
            return new RecoverableException("Exception invoking " + memberName, e);
        }
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new MetaModelException("Exception invoking " + memberName, e);
    }

}
