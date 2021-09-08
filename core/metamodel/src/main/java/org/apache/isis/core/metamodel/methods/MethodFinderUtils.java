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

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.MethodUtil;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramSignatureMatch;

import lombok.NonNull;
import lombok.Value;

public final class MethodFinderUtils {

    private MethodFinderUtils() {
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
        Optional<MethodAndPpmConstructor> lookupConstructor(final Class<?>[] paramTypes) {
            return _Reflect.getPublicConstructors(getPpmCandidate()).stream()
            .filter(paramSignatureMatch(paramTypes))
            .map(constructor->MethodAndPpmConstructor.of(supportingMethod, constructor))
            .findFirst();
        }
    }

    // -- HELPER

    static Stream<MethodAndPpmConstructor> findMethodWithPPMArg(
            final MethodFinderOptions options,
            final Class<?> returnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return streamMethods(options, returnType)
            .filter(MethodUtil.Predicates.paramCount(additionalParamTypes.size()+1))
            .filter(MethodUtil.Predicates.matchParamTypes(1, additionalParamTypes))
            .map(method->MethodAndPpmCandidate.of(method, method.getParameterTypes()[0]))
            .map(ppmCandidate->ppmCandidate.lookupConstructor(paramTypes))
            .flatMap(Optional::stream);
    }

    static Stream<MethodAndPpmConstructor> findMethodWithPPMArg_returningAnyOf(
            final MethodFinderOptions options,
            final Can<Class<?>> returnTypes,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return returnTypes.stream()
        .flatMap(returnType->findMethodWithPPMArg(options, returnType, paramTypes, additionalParamTypes));
    }

    private static Stream<Method> streamMethods(
            final MethodFinderOptions options,
            final @Nullable Class<?> returnType) {

        return options.streamMethodsIgnoringSignature()
            .filter(method -> returnType == null
                || returnType.isAssignableFrom(method.getReturnType()));
    }


}
