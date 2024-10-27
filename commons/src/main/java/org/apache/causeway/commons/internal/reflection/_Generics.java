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
package org.apache.causeway.commons.internal.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.NonNull;

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
     * @apiNote may return {@link Stream#empty()}, as we don't know how to do this in the general case
     * @implNote will work for simple cases, but NOT for wildcards or non trivial bounds
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfType(final @NonNull Class<?> cls) {
        return streamGenericTypeArgumentsOfType(cls, cls);
    }

    /**
     * Returns a Stream of the actual type arguments for given type {@code cls} after up-castingn to
     * {@code stopAtSuperClass}.
     * @apiNote may return {@link Stream#empty()}, as we don't know how to do this in the general case
     * @implNote will work for simple cases, but NOT for wildcards or non trivial bounds
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOfType(
            final @NonNull Class<?> cls, final @NonNull Class<?> stopAtSuperClass) {
        var superClass = genericUpCast(cls, stopAtSuperClass);
        if(superClass instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) superClass;
            final Type[] typeArgs = pt.getActualTypeArguments();
            final int typeArgCount = _NullSafe.size(typeArgs);
            var extractedGenericTypes = Can.ofArray(typeArgs)
                        .<Class<?>>map(typeArg->typeToClass(typeArg));
            return extractedGenericTypes.size()==typeArgCount
                ? extractedGenericTypes.stream()
                : Stream.empty(); // if any of the type to class conversions failed, return an empty stream
        }
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
            var wildcardType = (WildcardType) type;
            return Stream.concat(
                        Stream.of(wildcardType.getLowerBounds()),
                        Stream.of(wildcardType.getUpperBounds()))
                    .flatMap(x->streamClassesOfType(owner, x)); // recursive call
        }

        if (type instanceof TypeVariable) {

            // try to match up with the actual type argument of the owner's generic superclass.
            final Type genericSuperclass = owner.getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType) {
                var parameterizedTypeOfSuperclass = (ParameterizedType)genericSuperclass;
                var genericDeclaration = ((TypeVariable<?>) type).getGenericDeclaration();
                if(parameterizedTypeOfSuperclass.getRawType() == genericDeclaration) {
                    return Stream.of(parameterizedTypeOfSuperclass.getActualTypeArguments())
                        .flatMap(actualType->streamClassesOfType(owner, actualType)); // recursive call
                }
            }
            // otherwise, what to do?
        }

        return Stream.empty();
    }

    @Nullable
    private static Class<?> typeToClass(final @NonNull Type type) {
        if(type instanceof Class) {
            return (Class<?>) type;
        }
        if(type instanceof ParameterizedType) {
            var pType = (ParameterizedType) type;
            return typeToClass(pType.getRawType());
        }
        // don't know how to do this otherwise
        return null;
    }

    private static Type genericUpCast(final @NonNull Class<?> cls, final @NonNull Class<?> stopAtSuperClass) {
        var superClass = cls.getGenericSuperclass();
        if(cls.equals(stopAtSuperClass)) {
            // don't know how to get a ParameterizedType for cls, so we assume there is a super type to the rescue
            superClass = cls.getGenericSuperclass();
        }
        while (superClass instanceof ParameterizedType
                && stopAtSuperClass != ((ParameterizedType)superClass).getRawType()) {
            superClass = ((Class<?>) ((ParameterizedType) superClass).getRawType()).getGenericSuperclass();
        }
        return superClass;
    }

}
