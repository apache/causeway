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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderPAT;
import org.apache.isis.core.metamodel.methods.MethodFinderPAT.MethodAndPatConstructor;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

//@Log4j2
public final class ActionSupport {

    @Value @Builder
    public static class ActionSupportingMethodSearchRequest {

        @NonNull FacetFactory.ProcessMethodContext processMethodContext;
        @Getter @NonNull MethodFinder methodFinder;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms;

        Class<?> additionalParamType;

        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();
    }

    @FunctionalInterface
    public static interface SearchFunction {
        void search(
                ActionSupportingMethodSearchRequest searchRequest,
                Consumer<ActionSupportingMethodSearchResult> onMethodFound);
    }

    @RequiredArgsConstructor
    public static enum SearchAlgorithm
    implements SearchFunction {
        /** Parameter as a Tuple */
        PAT(ActionSupport::findActionSupportingMethodWithPATArg),
        ALL_PARAM_TYPES(ActionSupport::findActionSupportingMethodWithAllParamTypes),
        ;
        private final SearchFunction searchFunction;
        @Override
        public void search(
                final ActionSupportingMethodSearchRequest searchRequest,
                final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {
            searchFunction.search(searchRequest, onMethodFound);
        }
    }

    @Value(staticConstructor = "of")
    public static class ActionSupportingMethodSearchResult {
        Method supportingMethod;
        Class<?> returnType;
        Optional<Constructor<?>> patConstructor;
    }

    public static void findActionSupportingMethods(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        for (val searchAlgorithm : searchRequest.searchAlgorithms) {
            searchAlgorithm.search(searchRequest, onMethodFound);
        }

    }

    // -- SEARCH ALGORITHMS

    private final static void findActionSupportingMethodWithPATArg(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        val paramTypes = searchRequest.getParamTypes();
        val finderOptions = searchRequest.getMethodFinder();
        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());

        MethodFinderPAT
        .findMethodWithPATArg(
                finderOptions,
                paramTypes, additionalParamTypes)
        .map(ActionSupport::toSearchResult)
        .forEach(onMethodFound);
    }

    private static ActionSupportingMethodSearchResult toSearchResult(
            final MethodAndPatConstructor supportingMethodAndPatConstructor) {
        return ActionSupportingMethodSearchResult
                .of(
                        supportingMethodAndPatConstructor.getSupportingMethod(),
                        supportingMethodAndPatConstructor.getSupportingMethod().getReturnType(),
                        Optional.of(supportingMethodAndPatConstructor.getPatConstructor()));
    }

    private final static void findActionSupportingMethodWithAllParamTypes(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        val paramTypes = searchRequest.getParamTypes();
        val finderOptions = searchRequest.getMethodFinder();

        val additionalParamType = searchRequest.getAdditionalParamType();
        val additionalParamCount = additionalParamType!=null ? 1 : 0;

        final int paramsConsideredCount = paramTypes.length + additionalParamCount;
        if(paramsConsideredCount>=0) {

            val signature = concat(paramTypes, paramsConsideredCount, additionalParamType);

            finderOptions
            .streamMethodsMatchingSignature(signature)
            .map(ActionSupport::toSearchResult)
            .forEach(onMethodFound);

        }
    }

    private static ActionSupportingMethodSearchResult toSearchResult(
            final Method supportingMethod) {
        return ActionSupportingMethodSearchResult
                .of(supportingMethod, supportingMethod.getReturnType(), Optional.empty());
    }

    // -- PARAM UTIL

    private static Class<?>[] concat(
            final Class<?>[] paramTypes,
            final int paramsConsidered,
            final @Nullable Class<?> additionalParamType) {

        if(paramsConsidered>paramTypes.length) {
            val msg = String.format("paramsConsidered %d exceeds size of paramTypes %d",
                    paramsConsidered, paramTypes.length);
            throw new IllegalArgumentException(msg);
        }

        val paramTypesConsidered = paramsConsidered<paramTypes.length
                ? Arrays.copyOf(paramTypes, paramsConsidered)
                : paramTypes;

        val withAdditional = additionalParamType!=null
                ? _Arrays.combine(paramTypesConsidered, additionalParamType)
                : paramTypesConsidered;

        return withAdditional;
    }


}
