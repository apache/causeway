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
import java.util.function.IntFunction;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ReturnTypePattern;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderPAT;
import org.apache.isis.core.metamodel.methods.MethodFinderPAT.MethodAndPatConstructor;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public final class ParameterSupport {

    @Value @Builder
    public static class ParamSupportingMethodSearchRequest {

        @NonNull FacetFactory.ProcessMethodContext processMethodContext;
        @NonNull Can<IntFunction<String>> paramIndexToMethodNameProviders;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms;
        @NonNull ReturnTypePattern returnTypePattern;

        Class<?> additionalParamType;

        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();

        Can<String> getSupporingMethodNameCandidates(final int paramNr) {
            return getParamIndexToMethodNameProviders()
                    .map(provider->provider.apply(paramNr));
        }
    }

    @Value(staticConstructor = "of")
    public static class ParamSupportingMethodSearchResult {
        int paramIndex;
        Class<?> paramType;
        Method supportingMethod;
        Class<?> returnType;
        Optional<Constructor<?>> patConstructor;
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
        PAT(ParameterSupport::findParamSupportingMethodWithPATArg),
        SWEEP(ParameterSupport::findParamSupportingMethod),
        SINGLEARG_BEING_PARAMTYPE(ParameterSupport::singleArgBeingParamType)
        ;
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

        val actionMethod = searchRequest.getProcessMethodContext().getMethod();
        val paramCount = actionMethod.getParameterCount();

        for (int i = 0; i < paramCount; i++) {
            for (val searchAlgorithm : searchRequest.searchAlgorithms) {
                val paramNum = i;
                searchAlgorithm.search(searchRequest, paramNum, onMethodFound);
            }
        }

    }

    private static void findParamSupportingMethodWithPATArg(
            final ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);

        val paramType = paramTypes[paramIndex];
        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());

        MethodFinderPAT
        .findMethodWithPATArg_returningAnyOf(
                MethodFinderOptions
                .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy()),
                searchRequest.getReturnTypePattern().matchingTypes(paramType),
                paramTypes, additionalParamTypes)
        .map(methodAndPatConstructor->toSearchResult(paramIndex, paramType, methodAndPatConstructor))
        .forEach(onMethodFound);
    }

    private static ParamSupportingMethodSearchResult toSearchResult(
            final int paramIndex,
            final Class<?> paramType,
            final MethodAndPatConstructor supportingMethodAndPatConstructor) {
        return ParamSupportingMethodSearchResult
                .of(paramIndex, paramType,
                        supportingMethodAndPatConstructor.getSupportingMethod(),
                        supportingMethodAndPatConstructor.getSupportingMethod().getReturnType(),
                        Optional.of(supportingMethodAndPatConstructor.getPatConstructor()));
    }

    private static void singleArgBeingParamType(
            final ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex,
            final Consumer<ParamSupportingMethodSearchResult> onMethodFound) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
        val paramType = paramTypes[paramIndex];
        val singleArg = new Class<?>[]{paramType};

        MethodFinder
        .findMethod_returningAnyOf(
                MethodFinderOptions
                .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy()),
                searchRequest.getReturnTypePattern().matchingTypes(paramType),
                singleArg)
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

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
        val paramType = paramTypes[paramIndex];
        val additionalParamType = searchRequest.getAdditionalParamType();
        val additionalParamCount = additionalParamType!=null ? 1 : 0;

        int paramsConsideredCount = paramIndex + additionalParamCount;
        while(paramsConsideredCount>=0) {

            val paramTypesToLookFor = concat(paramTypes, paramsConsideredCount, additionalParamType);

            final Method supportingMethod;
            supportingMethod = MethodFinder
                    .findMethod_returningAnyOf(
                            MethodFinderOptions
                            .memberSupport(type, methodNames, processMethodContext.getIntrospectionPolicy()),
                            searchRequest.getReturnTypePattern().matchingTypes(paramType),
                            paramTypesToLookFor)
                    .findFirst()
                    .orElse(null);

            if(supportingMethod != null) {
                onMethodFound.accept(toSearchResult(paramIndex, paramType, supportingMethod));
                return;
            }

            // remove last, and search again
            paramsConsideredCount--;
        }

    }

    private static ParamSupportingMethodSearchResult toSearchResult(
            final int paramIndex,
            final Class<?> paramType,
            final Method supportingMethod) {
        return ParamSupportingMethodSearchResult
                .of(paramIndex, paramType,
                        supportingMethod,
                        supportingMethod.getReturnType(),
                        Optional.empty());
    }

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