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
package org.apache.isis.commons.internal.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;

import lombok.NonNull;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public final class _Generics {

    // -- STREAMING TYPE ARGUMENTS

    /**
     * Returns a Stream of the actual type arguments for given {@code genericType}.
     * @param owner - the corresponding class that declares the field or method,
     *     which uses the {@code genericType}
     * @param genericType
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOf(
            final @NonNull Class<?> owner,
            final @NonNull Type genericType) {

        return (genericType instanceof ParameterizedType)
                ? Stream.of(((ParameterizedType) genericType).getActualTypeArguments())
                        .flatMap(type->streamClassesOfType(owner, type))
                : Stream.empty();
    }

    /**
     * Returns a Stream of the actual type arguments for given {@code genericTypes}.
     * @param owner - the corresponding class that declares the method,
     *     which uses the {@code genericTypes}
     * @param genericTypes
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOf(
            final @NonNull Class<?> owner,
            final @Nullable Type[] genericTypes) {

        return _NullSafe.stream(genericTypes)
                .flatMap(type->streamGenericTypeArgumentsOf(owner, type));
    }

    // -- SHORTCUTS

    /**
     * Returns a Stream of the actual type arguments for given type {@code cls}.
     * @implNote always returns {@link Stream#empty()}, as we don't know how to do this
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfType(
            final @NonNull Class<?> cls) {
        // maybe the best one could do is to extract any bounds on the type argument
        return Stream.empty();
    }

    /**
     * Returns a Stream of the actual type arguments for given {@code field}.
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfField(
            final @NonNull Field field) {
        return streamGenericTypeArgumentsOf(
                field.getDeclaringClass(),
                field.getGenericType());
    }

    /**
     * Returns a Stream of the actual type arguments for given {@code method}'s return type.
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfMethodParameterTypes(
            final @NonNull Method method) {
        return streamGenericTypeArgumentsOf(
                method.getDeclaringClass(),
                method.getGenericParameterTypes());
    }

    /**
     * Returns a Stream of the actual type arguments for given {@code method}'s parameter types.
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfMethodReturnType(
            final @NonNull Method method) {
        return streamGenericTypeArgumentsOf(
                method.getDeclaringClass(),
                method.getGenericReturnType());
    }

    /**
     * Returns a Stream of the actual type arguments for given {@code param}.
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfParameter(
            final @NonNull Parameter param) {
        return streamGenericTypeArgumentsOf(
                param.getDeclaringExecutable().getDeclaringClass(),
                param.getParameterizedType());
    }

    // -- HELPER

    private static Stream<Class<?>> streamClassesOfType(
            final Class<?> owner,
            final Type type) {

        if (type instanceof Class) {
            return Stream.of((Class<?>) type);
        }
        if (type instanceof WildcardType) {
            val wildcardType = (WildcardType) type;
            return Stream.concat(
                        Stream.of(wildcardType.getLowerBounds()),
                        Stream.of(wildcardType.getUpperBounds()))
                    .flatMap(x->streamClassesOfType(owner, x)); // recursive call
        }

        if (type instanceof TypeVariable) {

            // try to match up with the actual type argument of the owner's generic superclass.
            final Type genericSuperclass = owner.getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType) {
                val parameterizedTypeOfSuperclass = (ParameterizedType)genericSuperclass;
                val genericDeclaration = ((TypeVariable<?>) type).getGenericDeclaration();
                if(parameterizedTypeOfSuperclass.getRawType() == genericDeclaration) {
                    return Stream.of(parameterizedTypeOfSuperclass.getActualTypeArguments())
                        .flatMap(actualType->streamClassesOfType(owner, actualType)); // recursive call
                }
            }
            // otherwise, what to do?
        }

        return Stream.empty();
    }

}
