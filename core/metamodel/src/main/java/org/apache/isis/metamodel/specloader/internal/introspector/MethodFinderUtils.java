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


package org.apache.isis.metamodel.specloader.internal.introspector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.commons.lang.JavaClassUtils;
import org.apache.isis.commons.lang.WrapperUtils;
import org.apache.isis.metamodel.facets.MethodScope;


/**
 * TODO: duplication with {@link WrapperUtils} and {@link PrimitiveUtils}.
 */
public final class MethodFinderUtils {
    private static Map<Class<?>, Class<?>> boxedClasses = new HashMap<Class<?>, Class<?>>();

    static {
        // TODO: there is a better way of doing this in 1.6 using TypeMirror - this is just
        // for java 1.1 compatibility - replace after code fork.
        boxedClasses.put(boolean.class, Boolean.class);
        boxedClasses.put(char.class, Character.class);
        boxedClasses.put(byte.class, Byte.class);
        boxedClasses.put(short.class, Short.class);
        boxedClasses.put(int.class, Integer.class);
        boxedClasses.put(long.class, Long.class);
        boxedClasses.put(float.class, Float.class);
        boxedClasses.put(double.class, Double.class);
        boxedClasses.put(void.class, Void.class);
    }

    private MethodFinderUtils() {}

    /**
     * Searches the supplied array of methods for specific method and returns it, also removing it from
     * supplied array if found (by setting to <tt>null</tt>).
     * 
     * <p>
     * Any methods that do not meet the search criteria are left in the array of methods.
     * 
     * <p>
     * The search algorithm is:
     * <ul>
     * <li>has the specified prefix</li>
     * <li>has the specified return type, or <tt>void</tt> if canBeVoid is <tt>true</tt> (but see below)</li>
     * <li>has the specified number of parameters</li>
     * </ul>
     * If the returnType is specified as null then the return type is ignored.
     * 
     * @param forClass
     * @param name
     * @param returnType
     * @param paramTypes
     *            the set of parameters the method should have, if null then is ignored
     * @return Method
     */
    public static Method removeMethod(
            final Method[] methods,
            final MethodScope methodScope,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        method: for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }

            final Method method = methods[i];
            final int modifiers = method.getModifiers();

            // check for public modifier
            if (!Modifier.isPublic(modifiers)) {
                continue;
            }

            // check for static modifier
            if (!inScope(methodScope, method)) {
                continue;
            }

            // check for name
            if (!method.getName().equals(name)) {
                continue;
            }

            // check for return type
            if (returnType != null && returnType != method.getReturnType()) {
                continue;
            }

            // check params (if required)
            if (paramTypes != null) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (paramTypes.length != parameterTypes.length) {
                    continue;
                }

                for (int c = 0; c < paramTypes.length; c++) {
                    if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                        continue method;
                    }
                }
            }
            methods[i] = null;

            return method;
        }

        return null;
    }

    public static boolean inScope(final MethodScope methodScope, final Method method) {
        final boolean isStatic = JavaClassUtils.isStatic(method);
        return isStatic && methodScope == MethodScope.CLASS || !isStatic && methodScope == MethodScope.OBJECT;
    }

    /**
     * Searches the supplied array of methods for all specific methods and returns them, also removing them
     * from supplied array if found.
     * 
     * <p>
     * Any methods that do not meet the search criteria are left in the array of methods.
     * 
     * <p>
     * The search algorithm is:
     * <ul>
     * <li>has the specified prefix</li>
     * <li>has the specified return type, or <tt>void</tt> if canBeVoid is <tt>true</tt> (but see below)</li>
     * <li>has the specified number of parameters</li>
     * </ul>
     * If the returnType is specified as null then the return type is ignored.
     * 
     * @param forClass
     * @param name
     * @param returnType
     * @param paramTypes
     *            the set of parameters the method should have, if null then is ignored
     * @return Method
     */
    public static List<Method> removeMethods(
            final Method[] methods,
            final MethodScope forClass,
            final String prefix,
            final Class<?> returnType,
            final boolean canBeVoid,
            final int paramCount) {

        final List<Method> validMethods = new ArrayList<Method>();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }

            final Method method = methods[i];

            if (!inScope(forClass, method)) {
                continue;
            }

            final boolean goodPrefix = method.getName().startsWith(prefix);

            final boolean goodCount = method.getParameterTypes().length == paramCount;
            final Class<?> type = method.getReturnType();
            final boolean goodReturn = returnTypeCompatible(returnType, canBeVoid, type);

            if (goodPrefix && goodCount && goodReturn) {
                validMethods.add(method);
                methods[i] = null;
            }
        }
        return validMethods;
    }

    private static boolean returnTypeCompatible(final Class<?> returnType, final boolean canBeVoid, final Class<?> type) {
        if (returnType == null) {
            return true;
        }
        if (canBeVoid && (type == void.class)) {
            return true;
        }

        if (type.isPrimitive()) {
            return returnType.isAssignableFrom(boxedClasses.get(type));
        }

        return (returnType.isAssignableFrom(type));
    }

    /**
     * From the supplied method array, finds but <i>does not remove</i> methods that have the required prefix,
     * and adds to the supplied candidates vector.
     */
    public static void findPrefixedInstanceMethods(final Method[] methods, final String prefix, final List<Method> candidates) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }

            final Method method = methods[i];
            if (JavaClassUtils.isStatic(method)) {
                continue;
            }

            final boolean goodPrefix = method.getName().startsWith(prefix);
            final boolean goodCount = method.getParameterTypes().length == 0;

            if (goodPrefix && goodCount) {
                candidates.add(method);
            }
        }
    }
}

