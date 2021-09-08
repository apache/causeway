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
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ReturnTypeCategory;
import org.apache.isis.core.metamodel.commons.MethodUtil;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramSignatureMatch;

import lombok.NonNull;
import lombok.Value;

/**
 * In support of <i>Parameters as a Tuple</i> (PAT).
 */
public final class MethodFinderPAT {

    private MethodFinderPAT() {
    }

    // -- PAT SUPPORT

    @Value(staticConstructor = "of")
    public static class MethodAndPatConstructor {
        @NonNull Method supportingMethod;
        @NonNull Constructor<?> patConstructor;
    }

    // -- SEARCH FOR MULTIPLE NAME CANDIDATES (PAT)

    public static Stream<MethodAndPatConstructor> findMethodWithPATArg(
            final MethodFinderOptions options,
            final Class<?> returnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.streamMethodsIgnoringSignature()
            .filter(method -> returnType == null
                || returnType.isAssignableFrom(method.getReturnType()))
            .filter(MethodUtil.Predicates.paramCount(additionalParamTypes.size()+1))
            .filter(MethodUtil.Predicates.matchParamTypes(1, additionalParamTypes))
            .map(method->MethodAndPatCandidate.of(method, method.getParameterTypes()[0]))
            .map(ppmCandidate->ppmCandidate.lookupConstructor(paramTypes))
            .flatMap(Optional::stream);
    }

    public static Stream<MethodAndPatConstructor> findMethodWithPATArg_returningBoolean(
            final MethodFinderOptions options,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return MethodFinderPAT
        .findMethodWithPATArg_returningAnyOf(
                options, ReturnTypeCategory.BOOLEAN.getReturnTypes(), paramTypes, additionalParamTypes);
    }

    public static Stream<MethodAndPatConstructor> findMethodWithPATArg_returningText(
            final MethodFinderOptions options,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return MethodFinderPAT
        .findMethodWithPATArg_returningAnyOf(
                options, ReturnTypeCategory.TRANSLATABLE.getReturnTypes(), paramTypes, additionalParamTypes);
    }

    public static Stream<MethodAndPatConstructor> findMethodWithPATArg_returningNonScalar(
            final MethodFinderOptions options,
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return MethodFinderPAT
        .findMethodWithPATArg_returningAnyOf(
                options, ReturnTypeCategory.nonScalar(elementReturnType), paramTypes, additionalParamTypes);
    }

    // -- HELPER

    @Value(staticConstructor = "of")
    private static class MethodAndPatCandidate {
        @NonNull Method supportingMethod;
        @NonNull Class<?> patCandidate;
        Optional<MethodAndPatConstructor> lookupConstructor(final Class<?>[] paramTypes) {
            return _Reflect.getPublicConstructors(getPatCandidate()).stream()
            .filter(paramSignatureMatch(paramTypes))
            .map(constructor->MethodAndPatConstructor.of(supportingMethod, constructor))
            .findFirst();
        }
    }

    static Stream<MethodAndPatConstructor> findMethodWithPATArg_returningAnyOf(
            final MethodFinderOptions options,
            final Can<Class<?>> returnTypes,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return returnTypes.stream()
        .flatMap(returnType->findMethodWithPATArg(options, returnType, paramTypes, additionalParamTypes));
    }

}
