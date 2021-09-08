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

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.MethodUtil;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramSignatureMatch;

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
            final MethodFinderOptions options,
            final Class<?> returnType,
            final Class<?>[] signature,
            final Can<Class<?>> additionalParamTypes) {

        return options.streamMethodsIgnoringSignature()
            .filter(method -> returnType == null
                || returnType.isAssignableFrom(method.getReturnType()))
            .filter(MethodUtil.Predicates.paramCount(1 + additionalParamTypes.size()))
            .filter(MethodUtil.Predicates.matchParamTypes(1, additionalParamTypes))
            .map(method->lookupPatConstructor(method, signature))
            .flatMap(Optional::stream);
    }

    public Stream<MethodAndPatConstructor> findMethodWithPATArg_returningAnyOf(
            final MethodFinderOptions options,
            final Can<Class<?>> returnTypes,
            final Class<?>[] signature,
            final Can<Class<?>> additionalParamTypes) {

        return returnTypes.stream()
        .flatMap(returnType->findMethodWithPATArg(options, returnType, signature, additionalParamTypes));
    }

    // -- HELPER

    private Optional<MethodAndPatConstructor> lookupPatConstructor(
            final Method supportingMethod,
            final Class<?>[] signature) {

        val patCandidate = supportingMethod.getParameterTypes()[0];
        return _Reflect.getPublicConstructors(patCandidate).stream()
                .filter(paramSignatureMatch(signature))
                .map(constructor->MethodAndPatConstructor.of(supportingMethod, constructor))
                .findFirst();
    }


}
