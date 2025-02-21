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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ReturnTypePattern;
import org.apache.causeway.core.metamodel.methods.MethodFinder;
import org.apache.causeway.core.metamodel.methods.MethodFinderPAT;
import org.apache.causeway.core.metamodel.methods.MethodFinderPAT.MethodAndPatConstructor;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * @since 2.0
 */
public record ParameterSupport() {

    @Builder(builderMethodName = "builderInternal")
    public record ParamSupportingMethodSearchRequest(
            FacetFactory.@NonNull ProcessMethodContext processMethodContext,
            @NonNull Can<IntFunction<String>> paramIndexToMethodNameProviders,
            @NonNull Can<SearchAlgorithm> searchAlgorithms,
            @NonNull ReturnTypePattern returnTypePattern,
            @NonNull Can<Class<?>> additionalParamTypes) {

        public static ParamSupportingMethodSearchRequestBuilder builder() {
            return builderInternal()
                    .additionalParamTypes(Can.empty());
        }

        public Class<?>[] paramTypes() {
            return processMethodContext().getMethod().getParameterTypes();
        }

        private Can<String> getSupporingMethodNameCandidates(final int paramIndex) {
            return paramIndexToMethodNameProviders()
                    .map(provider->provider.apply(paramIndex));
        }
    }

    public record ParamSupportingMethodSearchResult(
            int paramIndex,
            Class<?> paramType,
            ResolvedMethod supportingMethod,
            Optional<ResolvedConstructor> patConstructor,
            ResolvedType paramSupportReturnType) {
    }

    @FunctionalInterface
    public static interface SearchFunction {
        void search(
                ParamSupportingMethodSearchRequest searchRequest,
                int paramNum,
                Consumer<ParamSupportingMethodSearchResult> onMethodFound);
    }

    @RequiredArgsConstructor
    public static enum SearchAlgorithm
    implements SearchFunction {
        /** In support of <i>Parameters as a Tuple</i> (PAT).*/
        PAT(ParameterSupport::findParamSupportingMethodWithPATArg),
        /** Starting with all-args working its way down to no-arg.*/
        SWEEP(ParameterSupport::findParamSupportingMethod),
        /** Argument matches return type.*/
        SINGLEARG_BEING_PARAMTYPE(ParameterSupport::singleArgBeingParamType);

        private final SearchFunction searchFunction;
        @Override
        public void search(
                final ParamSupportingMethodSearchRequest searchRequest,
                final int paramNum,
                final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {
            searchFunction.search(searchRequest, paramNum, onMethodFound);
        }
    }

    public static void findParamSupportingMethods(
            final ParamSupportingMethodSearchRequest searchRequest,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        var actionMethod = searchRequest.processMethodContext().getMethod();
        var paramCount = actionMethod.getParameterCount();

        for (int i = 0; i < paramCount; i++) {
            for (var searchAlgorithm : searchRequest.searchAlgorithms) {
                var paramNum = i;
                searchAlgorithm.search(searchRequest, paramNum, onMethodFound);
            }
        }
    }

    private static void findParamSupportingMethodWithPATArg(
            final ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        var processMethodContext = searchRequest.processMethodContext();
        var type = processMethodContext.getCls();
        var paramTypes = searchRequest.paramTypes();
        var methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);

        var paramType = paramTypes[paramIndex];

        MethodFinderPAT
        .findMethodWithPATArg(
                MethodFinder
                .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy())
                .withReturnTypeAnyOf(searchRequest.returnTypePattern().matchingTypes(paramType)),
                paramTypes,
                searchRequest.additionalParamTypes())
        .map(methodAndPatConstructor->toSearchResult(type, paramIndex, paramType, methodAndPatConstructor))
        .forEach(onMethodFound);
    }

    private static ParamSupportingMethodSearchResult toSearchResult(
            final Class<?> declaringClass,
            final int paramIndex,
            final Class<?> paramType,
            final MethodAndPatConstructor supportingMethodAndPatConstructor) {
        return new ParamSupportingMethodSearchResult(paramIndex, paramType,
                    supportingMethodAndPatConstructor.supportingMethod(),
                    Optional.of(supportingMethodAndPatConstructor.patConstructor()),
                    _GenericResolver.forMethodReturn(
                            supportingMethodAndPatConstructor.supportingMethod()));
    }

    private static void singleArgBeingParamType(
            final ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        var processMethodContext = searchRequest.processMethodContext();
        var type = processMethodContext.getCls();
        var paramTypes = searchRequest.paramTypes();
        var methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
        var paramType = paramTypes[paramIndex];
        var signature = new Class<?>[]{paramType};

        MethodFinder
            .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy())
            .withReturnTypeAnyOf(searchRequest.returnTypePattern().matchingTypes(paramType))
            .streamMethodsMatchingSignature(signature)
            .map(supportingMethod->toSearchResult(paramIndex, paramType, supportingMethod))
            .forEach(onMethodFound);
    }

    /*
     * search successively for the supporting method, trimming number of param types each loop
     */
    private static void findParamSupportingMethod(
            final ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        var processMethodContext = searchRequest.processMethodContext();
        var type = processMethodContext.getCls();
        var paramTypes = searchRequest.paramTypes();
        var methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
        var paramType = paramTypes[paramIndex];
        var additionalParamTypes = searchRequest.additionalParamTypes();

        //limit: [0 .. paramIndex + 1]
        for(int limit = paramIndex + 1; limit>=0; --limit) {
            var signature = concat(paramTypes, limit, additionalParamTypes);
            var supportingMethod =
                    MethodFinder
                    .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy())
                    .withReturnTypeAnyOf(searchRequest.returnTypePattern().matchingTypes(paramType))
                    .streamMethodsMatchingSignature(signature)
                    .findFirst()
                    .orElse(null);
            if(supportingMethod != null) {
                onMethodFound.accept(toSearchResult(paramIndex, paramType, supportingMethod));
                return;
            }
        }
    }

    private static ParamSupportingMethodSearchResult toSearchResult(
            final int paramIndex,
            final Class<?> paramType,
            final ResolvedMethod supportingMethod) {
        return new ParamSupportingMethodSearchResult(paramIndex, paramType,
                    supportingMethod,
                    Optional.empty(),
                    _GenericResolver.forMethodReturn(supportingMethod));
    }

    /**
     * @param paramTypes - all available
     * @param limit - params considered count (without any additional)
     * @param additionalParamTypes - append regardless
     */
    private static Class<?>[] concat(
            final Class<?>[] paramTypes,
            final int limit,
            final Can<Class<?>> additionalParamTypes) {

        if(limit>paramTypes.length) {
            var msg = String.format("limit %d exceeds size of paramTypes %d",
                    limit, paramTypes.length);
            throw new IllegalArgumentException(msg);
        }

        var paramTypesConsidered = limit<paramTypes.length
                ? Arrays.copyOf(paramTypes, limit)
                : paramTypes;

        var withAdditional = additionalParamTypes.isNotEmpty()
                ? _Arrays.combine(paramTypesConsidered, additionalParamTypes.toArray(_Constants.emptyClasses))
                : paramTypesConsidered;

        return withAdditional;
    }

}