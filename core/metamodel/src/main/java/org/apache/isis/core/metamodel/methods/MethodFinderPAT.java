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
package org.apache.isis.core.metamodel.methods;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.util.ClassUtils;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.MethodUtil;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * In support of <i>Parameters as a Tuple</i> (PAT).
 */
@UtilityClass
public final class MethodFinderPAT {

    // -- PAT SUPPORT

    @Value(staticConstructor = "of")
    public static class MethodAndPatConstructor {
        @NonNull Method supportingMethod;
        @NonNull Constructor<?> patConstructor;
    }

    // -- SEARCH FOR MULTIPLE NAME CANDIDATES (PAT)

    public Stream<MethodAndPatConstructor> findMethodWithPATArg(
            final MethodFinder finder,
            final Class<?>[] signature,
            final Can<Class<?>> additionalParamTypes) {

        return finder.streamMethodsIgnoringSignature()
            .filter(MethodUtil.Predicates.paramCount(1 + additionalParamTypes.size()))
            .filter(MethodUtil.Predicates.matchParamTypes(1, additionalParamTypes))
            .map(method->lookupPatConstructor(finder, method, signature))
            .flatMap(Optional::stream);
    }

    // -- HELPER

    private Optional<MethodAndPatConstructor> lookupPatConstructor(
            final MethodFinder finder,
            final Method supportingMethod,
            final Class<?>[] signature) {

        val patCandidate = supportingMethod.getParameterTypes()[0];

        // just an optimization, not strictly required
        if(ClassUtils.isPrimitiveOrWrapper(patCandidate)
                || ClassExtensions.isJavaClass(patCandidate)) {
            return Optional.empty();
        }

        val classCache = _ClassCache.getInstance();

        return classCache
                .lookupPublicConstructor(patCandidate, signature)
                .map(constructor->MethodAndPatConstructor.of(supportingMethod, constructor));
    }


}
