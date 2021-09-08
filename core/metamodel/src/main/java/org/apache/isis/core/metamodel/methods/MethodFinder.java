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

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ReturnTypeCategory;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils.MethodAndPpmConstructor;

/**
 * An extension to {@link MethodFinderUtils} in support of multiple simultaneous naming conventions.
 * @apiNote each method name candidate is processed in sequence as given by {@code Can<String> names}
 */
//@Log4j2
public final class MethodFinder {

    public static Stream<Method> findMethod(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?> expectedReturnType,
            final Class<?>[] paramTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethod(options, type, name, expectedReturnType, paramTypes))
        .filter(_NullSafe::isPresent);
    }

    // -- SEARCH FOR MULTIPLE NAME CANDIDATES

    public static Stream<Method> findMethod_returningCategory(
            final MethodFinderOptions options,
            final ReturnTypeCategory returnTypeCategory,
            final Class<?> type,
            final Class<?>[] paramTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethod_returningCategory(options, returnTypeCategory, type, name, paramTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<Method> findMethod_returningBoolean(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?>[] paramTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethod_returningBoolean(options, type, name, paramTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<Method> findMethod_returningText(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?>[] paramTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethod_returningText(options, type, name, paramTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<Method> findMethod_returningNonScalar(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethod_returningNonScalar(options, type, name, elementReturnType, paramTypes))
        .filter(_NullSafe::isPresent);
    }

    // -- SEARCH FOR MULTIPLE NAME CANDIDATES (PPM)

    public static Stream<MethodAndPpmConstructor> findMethodWithPPMArg(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?> returnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethodWithPPMArg(options, type, name, returnType, paramTypes, additionalParamTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<MethodAndPpmConstructor> findMethodWithPPMArg_returningAnyOf(
            final MethodFinderOptions options,
            final Can<Class<?>> returnTypes,
            final Class<?> type,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethodWithPPMArg_returningAnyOf(options, returnTypes, type, name, paramTypes, additionalParamTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<MethodAndPpmConstructor> findMethodWithPPMArg_returningBoolean(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethodWithPPMArg_returningBoolean(options, type, name, paramTypes, additionalParamTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<MethodAndPpmConstructor> findMethodWithPPMArg_returningText(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethodWithPPMArg_returningText(options, type, name, paramTypes, additionalParamTypes))
        .filter(_NullSafe::isPresent);
    }

    public static Stream<MethodAndPpmConstructor> findMethodWithPPMArg_returningNonScalar(
            final MethodFinderOptions options,
            final Class<?> type,
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes,
            final Can<Class<?>> additionalParamTypes) {

        return options.getMethodNameCandidates().stream()
        .distinct()
        .map(name->MethodFinderUtils
                .findMethodWithPPMArg_returningNonScalar(options, type, name, elementReturnType, paramTypes, additionalParamTypes))
        .filter(_NullSafe::isPresent);
    }


}
