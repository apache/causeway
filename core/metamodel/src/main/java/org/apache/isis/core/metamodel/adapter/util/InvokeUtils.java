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

package org.apache.isis.core.metamodel.adapter.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.commons.lang.WrapperUtils;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.methodutils.MethodFinderUtils;

/**
 * TODO: remove duplication with {@link WrapperUtils} and
 * {@link MethodFinderUtils}.
 */
public class InvokeUtils {

    // //////////////////////////////////////////////////////////////
    // invoke
    // //////////////////////////////////////////////////////////////

    public static void invoke(final List<Method> methods, final Object object) {
        for (final Method method : methods) {
            invoke(method, object);
        }
    }

    public static Object invoke(final Method method, final Object object) {
        final Object[] parameters = nullOrDefaultArgsFor(method);
        return invoke(method, object, parameters);
    }

    public static Object invoke(final Method method, final Object object, final Object[] parameters) {
        try {
            return method.invoke(object, parameters);
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            invocationException("Exception executing " + method, e);
            return null;
        } catch (final IllegalAccessException e) {
            throw new MetaModelException("illegal access of " + method, e);
        }
    }

    // //////////////////////////////////////////////////////////////
    // invokeStatic
    // //////////////////////////////////////////////////////////////

    public static Object invokeStatic(final Method method, final Object[] parameters) {
        return invoke(method, null, parameters);
    }

    public static Object invokeStatic(final Method method) {
        return invoke(method, null, nullOrDefaultArgsFor(method));
    }

    // //////////////////////////////////////////////////////////////
    // invocationException
    // //////////////////////////////////////////////////////////////

    public static void invocationException(final String error, final InvocationTargetException e) {
        final Throwable targetException = e.getTargetException();
        if (targetException instanceof ApplicationException) {
            // an application exception from the domain code is re-thrown as an
            // IsisException with same semantics
            throw new IsisApplicationException(targetException);
        }
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        } else {
            throw new MetaModelException(targetException);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////

    private static Map<Class<?>, Object> defaultByPrimitiveType = new HashMap<Class<?>, Object>();
    static {
        defaultByPrimitiveType.put(byte.class, (byte) 0);
        defaultByPrimitiveType.put(short.class, (short) 0);
        defaultByPrimitiveType.put(int.class, 0);
        defaultByPrimitiveType.put(long.class, 0L);
        defaultByPrimitiveType.put(char.class, 0);
        defaultByPrimitiveType.put(float.class, 0.0F);
        defaultByPrimitiveType.put(double.class, 0.0);
        defaultByPrimitiveType.put(boolean.class, false);
    }

    private static Object[] nullOrDefaultArgsFor(final Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = nullOrDefaultFor(paramTypes[i]);
        }
        return parameters;
    }

    /**
     * Returns the corresponding 'null' value for the primitives, or just
     * <tt>null</tt> if the class represents a non-primitive type.
     */
    private static Object nullOrDefaultFor(final Class<?> type) {
        return defaultByPrimitiveType.get(type);
    }

}
