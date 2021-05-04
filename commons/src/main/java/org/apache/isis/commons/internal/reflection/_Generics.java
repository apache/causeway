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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.functions._Predicates;

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

    /**
     * Visits given {@code genericType} for its actual type arguments
     * and calls back {@code onTypeArgument} on each type argument found. 
     * @param genericType
     * @param onTypeArgument
     */
    public static void visitGenericTypeArgumentsOf(
            final @NonNull Type genericType,
            final @NonNull Consumer<Class<?>> onTypeArgument) {
        visitGenericTypeArgumentsOf(genericType, _Predicates.alwaysTrue(), onTypeArgument);
    }
    
    /**
     * Visits given {@code genericTypes} for their actual type arguments
     * and calls back {@code onTypeArgument} on each type argument found. 
     * @param genericTypes
     * @param onTypeArgument
     */
    public static void visitGenericTypeArgumentsOf(
            final @Nullable Type[] genericTypes,
            final @NonNull Consumer<Class<?>> onTypeArgument) {
        visitGenericTypeArgumentsOf(genericTypes, _Predicates.alwaysTrue(), onTypeArgument);
    }
    
    /**
     * Visits given {@code genericType} for its actual type arguments
     * and calls back {@code onTypeArgument} on each type argument found, that passes
     * given {@code typeArgumentFilter}. 
     * @param genericType
     * @param typeArgumentFilter
     * @param onTypeArgument
     */
    public static void visitGenericTypeArgumentsOf(
            final @NonNull Type genericType,
            final @NonNull Predicate<Class<?>> typeArgumentFilter,
            final @NonNull Consumer<Class<?>> onTypeArgument) {
        
        if (genericType instanceof ParameterizedType) {
            for (val type : ((ParameterizedType) genericType).getActualTypeArguments()) {
                visitTypeArgument(type, typeArgumentFilter, onTypeArgument);
            }
        }
    }
    
    /**
     * Visits given {@code genericTypes} for their actual type arguments
     * and calls back {@code onTypeArgument} on each type argument found, that passes
     * given {@code typeArgumentFilter}. 
     * @param genericTypes
     * @param typeArgumentFilter
     * @param onTypeArgument
     */
    public static void visitGenericTypeArgumentsOf(
            final @Nullable Type[] genericTypes,
            final @NonNull Predicate<Class<?>> typeArgumentFilter,
            final @NonNull Consumer<Class<?>> onTypeArgument) {
        
        if(genericTypes==null) {
            return; // no-op
        }
        for (val genericType : genericTypes) {
            visitGenericTypeArgumentsOf(genericType, typeArgumentFilter, onTypeArgument);
        }
    }
    
    // -- HELPER

    private static void visitTypeArgument(
            final Type type,
            final Predicate<Class<?>> filter,
            final Consumer<Class<?>> onClass) {
        
        if (type instanceof WildcardType) {
            acceptWildcardType((WildcardType) type, filter, onClass);
        }
        if (type instanceof Class) {
            acceptNonWildcardType((Class<?>) type, filter, onClass);
        }
        // unexpected code reach
    }
    
    private static void acceptWildcardType(
            final WildcardType wildcardType, 
            final Predicate<Class<?>> filter,
            final Consumer<Class<?>> onClass) {
        
        for (val lower : wildcardType.getLowerBounds()) {
            if (lower instanceof Class) {
                visitTypeArgument((Class<?>) lower, filter, onClass);
            }
        }
        for (val upper : wildcardType.getUpperBounds()) {
            if (upper instanceof Class) {
                visitTypeArgument((Class<?>) upper, filter, onClass);
            }
        }
    }
    
    private static void acceptNonWildcardType(
            final Class<?> cls,
            final Predicate<Class<?>> filter,
            final Consumer<Class<?>> onClass) {
        
        if(filter.test(cls)) {
            onClass.accept(cls);
        }    
        
    }
    
}
