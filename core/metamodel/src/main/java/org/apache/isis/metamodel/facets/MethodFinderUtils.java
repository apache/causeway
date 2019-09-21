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
package org.apache.isis.metamodel.facets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.isis.commons.internal.reflection._MethodCache;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.methodutils.MethodScope;

import lombok.val;

public final class MethodFinderUtils {

    private MethodFinderUtils() {
    }

    public static Method findMethodWithOrWithoutParameters(final Class<?> type, final MethodScope classMethod, final String name, final Class<?> returnType, final Class<?>[] paramTypes) {
        Method method = MethodFinderUtils.findMethod(type, classMethod, name, returnType, paramTypes);
        if (method == null) {
            method = MethodFinderUtils.findMethod(type, classMethod, name, returnType, MethodPrefixBasedFacetFactoryAbstract.NO_PARAMETERS_TYPES);
        }
        return method;
    }
    
    //private static final _MethodCache methodCache = new _MethodCache(); //TODO could be share on the context

    /**
     * Returns a specific public methods that: have the specified prefix; have
     * the specified return type (or some subtype), and has the
     * specified number of parameters.
     *
     * <p>
     * If the returnType is specified as null then the return type is ignored.
     * If void.class is passed in, then searches for void methods.
     *
     * <p>
     * If the parameter type array is null, is also not checked.
     */
    public static Method findMethod(
            final Class<?> type,
            final MethodScope methodScope,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        
        
        val methodCache = _MethodCache.getInstance();
        
        val method = methodCache.lookupMethod(type, name, paramTypes);
        if(method == null) {
            return null;
        }

        final int modifiers = method.getModifiers();

        if (!Modifier.isPublic(modifiers)) {
            return null;
        }

        // check for scope modifier
        if (!methodScope.matchesScopeOf(method)) {
            return null;
        }

        if (!method.getName().equals(name)) {
            return null;
        }

        if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
            return null;
        }

        if (paramTypes != null) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (paramTypes.length != parameterTypes.length) {
                return null;
            }

            for (int c = 0; c < paramTypes.length; c++) {
                if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                    return null;
                }
            }
        }

        return method;
    }
    

    public static Method findMethod(
            final Class<?> type,
            final MethodScope methodScope,
            final String name,
            final Class<?>[] returnTypes,
            final Class<?>[] paramTypes) {
        for (Class<?> returnType : returnTypes) {
            final Method method = findMethod(type, methodScope, name, returnType, paramTypes);
            if(method != null) {
                return method;
            }
        }
        return null;
    }

    protected static boolean doesNotMatchScope(final MethodScope methodScope, final Method method) {
        return methodScope.doesNotMatchScope(method);
    }

    public static Method findMethod(final Class<?> type, final MethodScope methodScope, final String name, final Class<?> returnType) {
        try {
            final Method[] methods = type.getMethods();
            for (final Method method2 : methods) {
                final Method method = method2;
                final int modifiers = method.getModifiers();
                // check for public modifier
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }

                // check correct scope (static vs instance)
                if (!methodScope.matchesScopeOf(method)) {
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
                return method;
            }
        } catch (final SecurityException e) {
            return null;
        }
        return null;
    }

    public static List<Method> findMethodsWithAnnotation(final Class<?> type, final MethodScope methodScope, final Class<? extends Annotation> annotationClass) {

        final List<Method> methods = new ArrayList<Method>();

        // Validate arguments
        if ((type == null) || (methodScope == null) || (annotationClass == null)) {
            throw new IllegalArgumentException("One or more arguments are 'null' valued");
        }

        // Find methods annotated with the specified annotation
        for (final Method method : type.getMethods()) {
            if (!methodScope.matchesScopeOf(method)) {
                continue;
            }

            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }

        return methods;
    }

    public static void removeMethod(final MethodRemover methodRemover, final Method method) {
        if (methodRemover != null && method != null) {
            methodRemover.removeMethod(method);
        }
    }

    public static Class<?>[] paramTypesOrNull(final Class<?> type) {
        return type == null ? null : new Class[] { type };
    }

    public static boolean allParametersOfSameType(final Class<?>[] params) {
        final Class<?> firstParam = params[0];
        for (int i = 1; i < params.length; i++) {
            if (params[i] != firstParam) {
                return false;
            }
        }
        return true;
    }


    public static Method findAnnotatedMethod(
            final Object pojo,
            final Class<? extends Annotation> annotationClass,
            final Map<Class<?>, Optional<Method>> methods) {

        final Class<?> clz = pojo.getClass();
        Optional<Method> nullableMethod = methods.get(clz);
        if(nullableMethod == null) {
            nullableMethod = search(clz, annotationClass, methods);
        }
        return nullableMethod.orElse(null);
    }

    private static Optional<Method> search(
            final Class<?> clz,
            final Class<? extends Annotation> annotationClass,
            final Map<Class<?>, Optional<Method>> postConstructMethods) {

        final Method[] methods = clz.getMethods();

        Optional<Method> nullableMethod = Optional.empty();
        for (final Method method : methods) {
            final Annotation annotation = method.getAnnotation(annotationClass);
            if(annotation != null) {
                nullableMethod = Optional.of(method);
                break;
            }
        }
        postConstructMethods.put(clz, nullableMethod);
        return nullableMethod;
    }

}
