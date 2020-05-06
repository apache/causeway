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
package org.apache.isis.core.metamodel.facets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.reflection._MethodCache;
import org.apache.isis.core.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;

import static org.apache.isis.core.commons.internal.reflection._Reflect.Filter.paramSignatureMatch;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class MethodFinderUtils {

    private MethodFinderUtils() {
    }

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
            final String name,
            final Class<?> expectedReturnType,
            final Class<?>[] paramTypes) {
        
        val methodCache = _MethodCache.getInstance();
        
        val method = methodCache.lookupMethod(type, name, paramTypes);
        if(method == null) {
            return null;
        }

        if (!MethodUtil.isPublic(method)) {
            return null;
        }

        if(MethodUtil.isStatic(method)) {
            return null;
        }

        if (!method.getName().equals(name)) {
            return null;
        }

        if (expectedReturnType != null && !expectedReturnType.isAssignableFrom(method.getReturnType())) {
            return null;
        }

        if (paramTypes != null) {
            val parameterTypes = method.getParameterTypes();
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
    
    public static Method findMethod_returningAnyOf(
            final Class<?>[] returnTypes,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes) {
        
        for (val returnType : returnTypes) {
            val method = findMethod(type, name, returnType, paramTypes);
            if(method != null) {
                return method;
            }
        }
        return null;
    }
    
    public static Optional<Method> findNoArgMethod(final Class<?> type, final String name, final Class<?> returnType) {
        return streamMethods(type, Can.ofSingleton(name), returnType)
                .filter(MethodUtil.Predicates.paramCount(0))
                .findFirst();
    }
    
    public static Optional<Method> findSingleArgMethod(final Class<?> type, final String name, final Class<?> returnType) {
        return streamMethods(type, Can.ofSingleton(name), returnType)
                .filter(MethodUtil.Predicates.paramCount(1))
                .findFirst();
    }
    
    public static Stream<Method> streamMethods(final Class<?> type, final Can<String> names, final Class<?> returnType) {
        try {
            final Method[] methods = type.getMethods();
            return Arrays.stream(methods)
                    .filter(MethodUtil::isPublic)
                    .filter(MethodUtil::isNotStatic)
                    .filter(method -> names.contains(method.getName()))
                    .filter(method -> returnType == null ||
                                      returnType.isAssignableFrom(method.getReturnType()))
                    ;
        } catch (final SecurityException e) {
            return null;
        }
    }

    public static List<Method> findMethodsWithAnnotation(
            final Class<?> type, 
            final Class<? extends Annotation> annotationClass) {

        // Validate arguments
        if (type == null || annotationClass == null) {
            throw new IllegalArgumentException("One or more arguments are 'null' valued");
        }

        // Find methods annotated with the specified annotation
        return Arrays.stream(type.getMethods())
                .filter(method -> !MethodUtil.isStatic(method))
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
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

        val clz = pojo.getClass();
        val annotatedMethodIfAny = 
                methods.computeIfAbsent(clz, __->search(clz, annotationClass, methods));
        return annotatedMethodIfAny.orElse(null);
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
        return nullableMethod;
    }

    // -- SHORTCUTS
    
    private static final Class<?>[] BOOLEAN_TYPES = new Class<?>[]{
        boolean.class};

    
    public static Method findMethod_returningBoolean(
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes) {
        return findMethod_returningAnyOf(BOOLEAN_TYPES, type, name, paramTypes);
    }
    
    private static final Class<?>[] TEXT_TYPES = new Class<?>[]{
        String.class, 
        TranslatableString.class};

    
    public static Method findMethod_returningText(
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes) {
        return findMethod_returningAnyOf(TEXT_TYPES, type, name, paramTypes);
    }
    
    public static Method findMethod_returningNonScalar(
            final Class<?> type,
            final String name, 
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes) {
        
        val nonScalarTypes = new Class<?>[]{
            Collection.class, 
            Array.newInstance(elementReturnType, 0).getClass()};
        
        return findMethod_returningAnyOf(nonScalarTypes, type, name, paramTypes);
    }
    
    // -- PPM SUPPORT
    
    
    @Value(staticConstructor = "of")
    public static class MethodAndPpmConstructor {
        @NonNull Method supportingMethod;
        @NonNull Constructor<?> ppmFactory;
    }
    
    @Value(staticConstructor = "of")
    private static class MethodAndPpmCandidate {
        @NonNull Method supportingMethod;
        @NonNull Class<?> ppmCandidate;
        Optional<MethodAndPpmConstructor> lookupConstructor(Class<?>[] paramTypes) {
            return _Reflect.getPublicConstructors(getPpmCandidate()).stream()
            .filter(paramSignatureMatch(paramTypes))
            .map(constructor->MethodAndPpmConstructor.of(supportingMethod, constructor))
            .findFirst();
        }
    }
    
    public static MethodAndPpmConstructor findMethodWithPPMArg(
            final Class<?> type, 
            final String name, 
            final Class<?> returnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {
        
        return streamMethods(type, Can.ofSingleton(name), returnType)
            .filter(MethodUtil.Predicates.paramCount(additionalParamTypes.size()+1))
            .filter(MethodUtil.Predicates.matchParamTypes(1, additionalParamTypes))
            .map(method->MethodAndPpmCandidate.of(method, method.getParameterTypes()[0]))
            .map(ppmCandidate->ppmCandidate.lookupConstructor(paramTypes))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
    }
    
    public static MethodAndPpmConstructor findMethodWithPPMArg_returningAnyOf(
            final Class<?>[] returnTypes,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {
        
        for (val returnType : returnTypes) {
            val result = findMethodWithPPMArg(type, name, returnType, paramTypes, additionalParamTypes);
            if(result != null) {
                return result;
            }
        }
        return null;
    } 

    public static MethodAndPpmConstructor findMethodWithPPMArg_returningBoolean(
            final Class<?> type, 
            final String name,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {
        
        return findMethodWithPPMArg_returningAnyOf(BOOLEAN_TYPES, type, name, paramTypes, additionalParamTypes);
    }

    public static MethodAndPpmConstructor findMethodWithPPMArg_returningText(
            final Class<?> type, 
            final String name,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {
        
        return findMethodWithPPMArg_returningAnyOf(TEXT_TYPES, type, name, paramTypes, additionalParamTypes);
    }

    public static MethodAndPpmConstructor findMethodWithPPMArg_returningNonScalar(
            final Class<?> type, 
            final String name, 
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes, 
            final Can<Class<?>> additionalParamTypes) {
        
        val nonScalarTypes = new Class<?>[]{
            Collection.class, 
            Array.newInstance(elementReturnType, 0).getClass()};
        
        return findMethodWithPPMArg_returningAnyOf(nonScalarTypes, type, name, paramTypes, additionalParamTypes);
    }
    



}
