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
package org.apache.isis.core.metamodel.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodExtensions {

    private MethodExtensions() {
    }

    public static boolean isStatic(final Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    // //////////////////////////////////////

    public static <T> T construct(final Constructor<T> con) {
        final Object[] parameters = MethodExtensions.getNullOrDefaultArgs(con);
        return MethodExtensions.construct(con, parameters);
    }

    public static <T> T construct(final Constructor<T> con, final Object[] arguments) {
        try {
            Object[] defaultAnyPrimitive = defaultAnyPrimitive(con.getParameterTypes(), arguments);
            return CanonicalParameterUtil.construct(con, defaultAnyPrimitive);
        } catch (Exception e) {
             ThrowableExtensions.handleInvocationException(e, con.getName());
             return null;
        }
    }

    public static Object invoke(final Method method, final Object object) {
        final Object[] parameters = MethodExtensions.getNullOrDefaultArgs(method);
        return MethodExtensions.invoke(method, object, parameters);
    }

    public static Object invoke(final Method method, final Object object, final Object[] arguments) {
        try {
            Object[] defaultAnyPrimitive = defaultAnyPrimitive(method.getParameterTypes(), arguments);
            return CanonicalParameterUtil.invoke(method, object, defaultAnyPrimitive);
        } catch (Exception e) {
            return ThrowableExtensions.handleInvocationException(e, method.getName());
        }
    }

    private static Object[] defaultAnyPrimitive(final Class<?>[] parameterTypes, final Object[] arguments) {
        if(parameterTypes == null || arguments == null || parameterTypes.length != arguments.length) {
            return arguments;
        }
        final Object[] argumentsWithPrimitivesDefaulted = new Object[arguments.length];
        for(int i=0; i<argumentsWithPrimitivesDefaulted.length; i++) {
            argumentsWithPrimitivesDefaulted[i] = valueIfPrimitiveThenDefaulted(parameterTypes[i], arguments[i]);
        }
        return argumentsWithPrimitivesDefaulted;
    }

    private static Object valueIfPrimitiveThenDefaulted(final Class<?> cls, final Object argument) {
        if(argument != null) {
            return argument;
        }
        if(!cls.isPrimitive()) {
            return argument;
        }
        return ClassUtil.defaultByPrimitive.get(cls);
    }

    public static Object invokeStatic(final Method method, final Object[] parameters) {
        return invoke(method, null, parameters);
    }

    public static Object invokeStatic(final Method method) {
        return invoke(method, null, MethodExtensions.getNullOrDefaultArgs(method));
    }

    // //////////////////////////////////////

    public static Object[] getNullOrDefaultArgs(final Executable method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = ClassExtensions.getNullOrDefault(paramTypes[i]);
        }
        return parameters;
    }




}
