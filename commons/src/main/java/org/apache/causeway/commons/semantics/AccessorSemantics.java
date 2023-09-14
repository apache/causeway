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

import java.util.function.Predicate;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

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

    public static boolean isNonBooleanGetter(final ResolvedMethod method, final Predicate<Class<?>> typeFilter) {
        return GET.isPrefixOf(method.name())
                && method.isNoArg()
                && !method.isStatic()
                && typeFilter.test(method.returnType());
    }

    public static boolean isNonBooleanGetter(final ResolvedMethod method, final Class<?> expectedType) {
        return isNonBooleanGetter(method, type->
            expectedType.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(type)));
    }

    public static boolean isGetter(final ResolvedMethod method) {
        return isBooleanGetter(method)
                || isNonBooleanGetter(method, type->type != void.class);
    }
}
