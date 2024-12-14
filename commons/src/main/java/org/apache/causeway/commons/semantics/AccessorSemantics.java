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
package org.apache.causeway.commons.semantics;

import java.beans.Introspector;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccessorSemantics {
    GET("get"),
    IS("is"),
    SET("set");
    private final String prefix;

    public String prefix(final @Nullable String input) {
        return input!=null
                ? prefix + input
                : prefix;
    }

    public boolean isPrefixOf(final @Nullable String input) {
        return input!=null
                ? input.startsWith(prefix)
                : false;
    }

    // -- HIGH LEVEL PREDICATES

    public static boolean isPropertyAccessor(final ResolvedMethod method) {
        return isPropertyAccessorCandidate(method)
            ? !hasCollectionSemantics(method.returnType())
            : false;
    }

    public static boolean isCollectionAccessor(final ResolvedMethod method) {
        return isCollectionAccessorCandidate(method)
            ? hasCollectionSemantics(method.returnType())
            : false;
    }

    public static boolean hasSupportedNonScalarMethodReturnType(final ResolvedMethod method) {
        return isNonBooleanGetter(method, Iterable.class)
            && CollectionSemantics.valueOf(method.returnType()).isPresent();
    }

    // -- LOW LEVEL PREDICATES

    public static boolean isPropertyAccessorCandidate(final ResolvedMethod method) {
        return isRecordComponentAccessor(method)
            || isGetter(method);
    }

    public static boolean isCollectionAccessorCandidate(final ResolvedMethod method) {
        return isRecordComponentAccessor(method)
            || isNonBooleanGetter(method);
    }


    public static boolean isCandidateGetterName(final @Nullable String name) {
        return GET.isPrefixOf(name)
                || IS.isPrefixOf(name);
    }

    public static boolean isBooleanGetter(final ResolvedMethod method) {
        return IS.isPrefixOf(method.name())
                && method.isNoArg()
                && !method.isStatic()
                && (method.returnType() == boolean.class
                || method.returnType() == Boolean.class);
    }

    public static boolean isGetter(final ResolvedMethod method) {
        return isBooleanGetter(method)
                || isNonBooleanGetter(method);
    }

    public static boolean isNonBooleanGetter(final ResolvedMethod method, final Class<?> expectedType) {
        return isNonBooleanGetter(method, type->
            expectedType.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(type)));
    }

    public static boolean isNonBooleanGetter(final ResolvedMethod method) {
        return isNonBooleanGetter(method, type->type != void.class);
    }

    private static boolean isNonBooleanGetter(final ResolvedMethod method, final Predicate<Class<?>> typeFilter) {
        return GET.isPrefixOf(method.name())
                && method.isNoArg()
                && !method.isStatic()
                && typeFilter.test(method.returnType());
    }

    /**
     * @since 3.0.0
     */
    public static boolean isRecordComponentAccessor(final ResolvedMethod method) {
        var recordClass = method.implementationClass();
        if(!recordClass.isRecord()) return false;
        for(var recordComponent : recordClass.getRecordComponents()) {
            if(method.name().equals(recordComponent.getName())) return true;
        }
        return false;
    }

    public static String associationIdentifierFor(final ResolvedMethod method) {
        return AccessorSemantics.isRecordComponentAccessor(method)
                ? method.name()
                : Introspector.decapitalize(_Strings.baseName(method.name()));
    }

    // -- HELPER

    private static boolean hasCollectionSemantics(final Class<?> cls) {
        return CollectionSemantics.valueOf(cls)
                .isPresent();
    }
}
