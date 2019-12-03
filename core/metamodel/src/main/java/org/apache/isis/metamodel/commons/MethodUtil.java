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

import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class MethodUtil {

    private MethodUtil(){}

    public static void invoke(final Collection<Method> methods, final Object object) {
        methods.forEach(method->MethodExtensions.invoke(method, object));
    }

    /**
     * Searches the supplied array of methods for specific method and returns
     * it, also removing it from supplied array if found (by setting to
     * <tt>null</tt>).
     *
     * <p>
     * Any methods that do not meet the search criteria are left in the array of
     * methods.
     */
    public static Method removeMethod(
            final Set<Method> methods,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        
        val methodIterator = methods.iterator();
        while(methodIterator.hasNext()) {
            val method = methodIterator.next();
            if(matches(method, name, returnType, paramTypes)){
                methodIterator.remove();
                return method;
            }
        }
        
        return null;
    }

    /**
     * Searches the supplied array of methods for specific method and returns
     * its index, otherwise returns <tt>-1</tt>.
     *
     * <p>
     * The search algorithm is:
     * <ul>
     * <li>has the specified prefix</li>
     * <li>has the specified return type, or <tt>void</tt> if canBeVoid is
     * <tt>true</tt> (but see below)</li>
     * <li>has the specified number of parameters</li>
     * </ul>
     * If the returnType is specified as null then the return type is ignored.
     */
    private static boolean matches(
            final Method method,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        
        final int modifiers = method.getModifiers();

        // check for public modifier
        if (!Modifier.isPublic(modifiers)) {
            return false;
        }

        if (isStatic(method)) {
            return false;
        }

        // check for name
        if (!method.getName().equals(name)) {
            return false;
        }

        // check for return type
        if (returnType != null && returnType != method.getReturnType()) {
            return false;
        }

        // check params (if required)
        if (paramTypes != null) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (paramTypes.length != parameterTypes.length) {
                return false;
            }

            for (int c = 0; c < paramTypes.length; c++) {
                if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                    return false;
                }
            }
        }
        
        return true;
    }


    /**
     * Searches the supplied array of methods for all specific methods and
     * returns them, also removing them from supplied array if found.
     *
     * <p>
     * Any methods that do not meet the search criteria are left in the array of
     * methods.
     *
     * <p>
     * The search algorithm is:
     * <ul>
     * <li>has the specified prefix</li>
     * <li>has the specified return type, or <tt>void</tt> if canBeVoid is
     * <tt>true</tt> (but see below)</li>
     * <li>has the specified number of parameters</li>
     * </ul>
     * If the returnType is specified as null then the return type is ignored.
     *
     * @param name
     * @param onRemoval
     * @param paramTypes
     *            the set of parameters the method should have, if null then is
     *            ignored
     * @param returnType
     * @param canBeVoid
     * @return Method
     */
    public static void removeMethods(
            Set<Method> methods,
            String prefix,
            Class<?> returnType,
            CanBeVoid canBeVoid,
            int paramCount,
            Consumer<Method> onMatch) {

        methods.removeIf(method -> 
            matches(method, prefix, returnType, canBeVoid, paramCount, onMatch));
        
    }

    private static boolean matches(
            Method method,
            String prefix,
            Class<?> returnType,
            CanBeVoid canBeVoid,
            int paramCount,
            Consumer<Method> onMatch) {

        if (isStatic(method)) {
            return false;
        }

        val goodPrefix = method.getName().startsWith(prefix);
        val goodCount = method.getParameterTypes().length == paramCount;
        val type = method.getReturnType();
        val goodReturn = ClassExtensions.isCompatibleAsReturnType(returnType, canBeVoid, type);

        if (goodPrefix && goodCount && goodReturn) {
            onMatch.accept(method);
            return true;
        }
        
        return false;
        
    }


    public static boolean isStatic(final Method method) {
        final int modifiers = method.getModifiers();
        final boolean isStatic = Modifier.isStatic(modifiers);
        return isStatic;
    }
}
