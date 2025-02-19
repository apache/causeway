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
package org.apache.causeway.core.metamodel.facets;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.methods.MethodFinder;
import org.apache.causeway.core.metamodel.methods.MethodFinderPAT;
import org.apache.causeway.core.metamodel.methods.MethodFinderPAT.MethodAndPatConstructor;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

public final class ActionSupport {

    @Value @Builder
    public static class ActionSupportingMethodSearchRequest {

        FacetFactory.@NonNull ProcessMethodContext processMethodContext;
        @Getter @NonNull MethodFinder methodFinder;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms;

        @Builder.Default
        final @NonNull Can<Class<?>> additionalParamTypes = Can.empty();

        @Getter(lazy = true) Class<?>[] paramTypes =
                getProcessMethodContext().getMethod().getParameterTypes();
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
        ResolvedMethod supportingMethod;
        Class<?> returnType;
        Optional<ResolvedConstructor> patConstructor;
    }

    public static void findActionSupportingMethods(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        for (var searchAlgorithm : searchRequest.searchAlgorithms) {
            searchAlgorithm.search(searchRequest, onMethodFound);
        }

    }

    // -- SEARCH ALGORITHMS

    private final static void findActionSupportingMethodWithPATArg(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        MethodFinderPAT
        .findMethodWithPATArg(
                searchRequest.getMethodFinder(),
                searchRequest.getParamTypes(),
                searchRequest.getAdditionalParamTypes())
        .map(ActionSupport::toSearchResult)
        .forEach(onMethodFound);
    }

    private static ActionSupportingMethodSearchResult toSearchResult(
            final MethodAndPatConstructor supportingMethodAndPatConstructor) {
        return ActionSupportingMethodSearchResult
                .of(
                        supportingMethodAndPatConstructor.supportingMethod(),
                        supportingMethodAndPatConstructor.supportingMethod().returnType(),
                        Optional.of(supportingMethodAndPatConstructor.patConstructor()));
    }

    private final static void findActionSupportingMethodWithAllParamTypes(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        var paramTypes = searchRequest.getParamTypes();
        var finderOptions = searchRequest.getMethodFinder();

        var additionalParamTypes = searchRequest.getAdditionalParamTypes();
        var additionalParamCount = additionalParamTypes.size();

        final int paramsConsideredCount = paramTypes.length + additionalParamCount;
        if(paramsConsideredCount>=0) {

            var signature = concat(paramTypes, paramsConsideredCount, additionalParamTypes);

            finderOptions
                .streamMethodsMatchingSignature(signature)
                .map(ActionSupport::toSearchResult)
                .forEach(onMethodFound);
        }
    }

    private static ActionSupportingMethodSearchResult toSearchResult(
            final ResolvedMethod supportingMethod) {
        return ActionSupportingMethodSearchResult
                .of(supportingMethod, supportingMethod.returnType(), Optional.empty());
    }

    // -- PARAM UTIL

    private static Class<?>[] concat(
            final Class<?>[] paramTypes,
            final int paramsConsidered,
            final Can<Class<?>> additionalParamTypes) {

        if(paramsConsidered>paramTypes.length) {
            var msg = String.format("paramsConsidered %d exceeds size of paramTypes %d",
                    paramsConsidered, paramTypes.length);
            throw new IllegalArgumentException(msg);
        }

        var paramTypesConsidered = paramsConsidered<paramTypes.length
                ? Arrays.copyOf(paramTypes, paramsConsidered)
                : paramTypes;

        var withAdditional = additionalParamTypes.isNotEmpty()
                ? _Arrays.combine(paramTypesConsidered, additionalParamTypes.toArray(_Constants.emptyClasses))
                : paramTypesConsidered;

        return withAdditional;
    }

}
