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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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
     * @param genericType
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOf(
            final @NonNull Type genericType) {

        return (genericType instanceof ParameterizedType) 
                ? Stream.of(((ParameterizedType) genericType).getActualTypeArguments())
                        .flatMap(_Generics::streamClassesOfType)
                : Stream.empty();
    }
    
    /**
     * Streams given {@code genericTypes} for their actual type arguments
     * and calls back {@code onTypeArgument} on each type argument found. 
     * @param genericTypes
     */
    public static Stream<Class<?>> streamGenericTypeArgumentsOf(
            final @Nullable Type[] genericTypes) {
        
        return _NullSafe.stream(genericTypes)
                .flatMap(_Generics::streamGenericTypeArgumentsOf);
    }
    
    // -- SHORTCUTS
    
    public static Stream<Class<?>> streamGenericTypeArgumentsOfField(
            final @NonNull Field field) {
        return streamGenericTypeArgumentsOf(field.getGenericType());
    }
    
    public static Stream<Class<?>> streamGenericTypeArgumentsOfMethodParameterTypes(
            final @NonNull Method method) {
        return streamGenericTypeArgumentsOf(method.getGenericParameterTypes());
    }
    
    public static Stream<Class<?>> streamGenericTypeArgumentsOfMethodReturnType(
            final @NonNull Method method) {
        return streamGenericTypeArgumentsOf(method.getGenericReturnType());
    }
    
    // -- HELPER
    
    private static Stream<Class<?>> streamClassesOfType(final Type type) {
        if (type instanceof Class) {
            return Stream.of((Class<?>) type);
        }
        if (type instanceof WildcardType) {
            val wildcardType = (WildcardType) type;
            return Stream.concat(
                        Stream.of(wildcardType.getLowerBounds()),
                        Stream.of(wildcardType.getUpperBounds()))
                    .flatMap(_Generics::streamClassesOfType);
        }
        return Stream.empty();
    }
    
    
}
