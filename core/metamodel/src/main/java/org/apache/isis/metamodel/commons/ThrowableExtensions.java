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

package org.apache.isis.metamodel.commons;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.commons.exceptions.IsisApplicationException;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.specloader.ReflectiveActionException;

public final class ThrowableExtensions {

    public static Object handleInvocationException(
            final Throwable e,
            final String memberName) {
        return handleInvocationException(e, memberName, null);
    }

    public static Object handleInvocationException(
            final Throwable e,
            final String memberName,
            final Consumer<RecoverableException> recovery) {

        if(e instanceof InvocationTargetException) {
            return handleInvocationException(((InvocationTargetException) e).getTargetException(), memberName, recovery);
        }
        if(e instanceof WrongMethodTypeException) {
            throw new MetaModelException("Wrong method type access of " + memberName, e);
        }
        if(e instanceof IllegalAccessException) {
            throw new ReflectiveActionException("Illegal access of " + memberName, e);
        }
        if(e instanceof IllegalStateException) {
            throw new ReflectiveActionException( String.format(
                    "IllegalStateException thrown while invoking %s %s",
                    memberName, e.getMessage()), e);
        }
        if(e instanceof RecoverableException) {
            return handleRecoverableException((RecoverableException)e, memberName, recovery);
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new MetaModelException("Exception invoking " + memberName, e);
    }


    private static Object handleRecoverableException(
            final RecoverableException e,
            final String memberName,
            final Consumer<RecoverableException> recovery) {

        if(recovery!=null)
            recovery.accept(e);

        // an application exception from the domain code is re-thrown as an
        // IsisException with same semantics
        // TODO: should probably be using ApplicationException here
        throw new IsisApplicationException("Exception invoking " + memberName, e);
    }

}
