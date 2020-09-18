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
package org.apache.isis.testing.unittestsupport.applib.core.jmocking;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.lib.JavaReflectionImposteriser;

import org.apache.isis.commons.internal.proxy._ProxyFactory;
import org.apache.isis.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;

class ImposteriserUsingCodegenPlugin implements Imposteriser {

    public static final Imposteriser INSTANCE = new ImposteriserUsingCodegenPlugin();

    private final Imposteriser reflectionImposteriser = new JavaReflectionImposteriser();

    private ImposteriserUsingCodegenPlugin() {
    }


    @Override
    public boolean canImposterise(Class<?> mockedType) {

        if(mockedType.isInterface()) {
            return reflectionImposteriser.canImposterise(mockedType);
        }

        return !mockedType.isPrimitive() &&
                !Modifier.isFinal(mockedType.getModifiers()) &&
                !toStringMethodIsFinal(mockedType);
    }

    @Override
    public <T> T imposterise(final Invokable mockObject, final Class<T> mockedType, Class<?>... ancilliaryTypes) {
        if (!canImposterise(mockedType)) {
            throw new IllegalArgumentException(mockedType.getName() + " cannot be imposterized (either a primitive, or a final type or has final toString method)");
        }

        if(mockedType.isInterface()) {
            return reflectionImposteriser.imposterise(mockObject, mockedType, ancilliaryTypes);
        }


        final _ProxyFactory<T> factory = _ProxyFactory.builder(mockedType)
                .interfaces(ancilliaryTypes)
                .build(new ProxyFactoryServiceByteBuddy());

        final boolean initialize = false;

        return factory.createInstance(
                (obj, method, args)->mockObject.invoke(new Invocation(obj, method, args)),
                initialize);
    }

    // //////////////////////////////////////

    private static boolean toStringMethodIsFinal(Class<?> type) {
        try {
            Method toString = type.getMethod("toString");
            return Modifier.isFinal(toString.getModifiers());

        }
        catch (SecurityException e) {
            throw new IllegalStateException("not allowed to reflect on toString method", e);
        }
        catch (NoSuchMethodException e) {
            throw new Error("no public toString method found", e);
        }
    }

}
