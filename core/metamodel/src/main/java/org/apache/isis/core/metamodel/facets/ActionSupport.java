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
import org.apache.isis.core.metamodel.methods.MethodFinderUtils.MethodAndPpmConstructor;

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

        public static enum ReturnType {
            TEXT,
            BOOLEAN,
        }

        @NonNull FacetFactory.ProcessMethodContext processMethodContext;
        @NonNull Can<String> methodNames;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms;
        @NonNull ReturnType returnType;

        Class<?> additionalParamType;

        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();

        Can<String> getSupporingMethodNameCandidates() {
            return methodNames;
        }
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
        PPM(ActionSupport::findActionSupportingMethodWithPPMArg),
        ALL_PARAM_TYPES(ActionSupport::findActionSupportingMethodWithAllParamTypes),
        ;
        private final SearchFunction searchFunction;
        @Override
        public void search(
                final ActionSupportingMethodSearchRequest searchRequest,
                Consumer<ActionSupportingMethodSearchResult> onMethodFound) {
            searchFunction.search(searchRequest, onMethodFound);
        }
    }

    @Value(staticConstructor = "of")
    public static class ActionSupportingMethodSearchResult {
        Method supportingMethod;
        Class<?> returnType;
        Optional<Constructor<?>> ppmFactory;
    }

    public static void findActionSupportingMethods(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        for (val searchAlgorithm : searchRequest.searchAlgorithms) {
            searchAlgorithm.search(searchRequest, onMethodFound);
        }

    }

    // -- SEARCH ALGORITHMS

    private final static void findActionSupportingMethodWithPPMArg(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getMethodNames();

        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());

        switch(searchRequest.getReturnType()) {
        case BOOLEAN:
            MethodFinder
                .findMethodWithPPMArg_returningBoolean(type, methodNames, paramTypes, additionalParamTypes)
                .map(ActionSupport::toSearchResult)
                .forEach(onMethodFound);
            break;
        case TEXT:
            MethodFinder
                .findMethodWithPPMArg_returningText(type, methodNames, paramTypes, additionalParamTypes)
                .map(ActionSupport::toSearchResult)
                .forEach(onMethodFound);
            break;
        default:

        }

    }

    private static ActionSupportingMethodSearchResult toSearchResult(
            final MethodAndPpmConstructor supportingMethodAndPpmConstructor) {
        return ActionSupportingMethodSearchResult
                .of(
                        supportingMethodAndPpmConstructor.getSupportingMethod(),
                        supportingMethodAndPpmConstructor.getSupportingMethod().getReturnType(),
                        Optional.of(supportingMethodAndPpmConstructor.getPpmFactory()));
    }

    private final static void findActionSupportingMethodWithAllParamTypes(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getMethodNames();

        val additionalParamType = searchRequest.getAdditionalParamType();
        val additionalParamCount = additionalParamType!=null ? 1 : 0;

        final int paramsConsideredCount = paramTypes.length + additionalParamCount;
        if(paramsConsideredCount>=0) {

            val paramTypesToLookFor = concat(paramTypes, paramsConsideredCount, additionalParamType);

            switch(searchRequest.getReturnType()) {
            case BOOLEAN:
                MethodFinder
                    .findMethod_returningBoolean(type, methodNames, paramTypesToLookFor)
                    .map(ActionSupport::toSearchResult)
                    .forEach(onMethodFound);
                break;
            case TEXT:
                MethodFinder
                    .findMethod_returningText(type, methodNames, paramTypesToLookFor)
                    .map(ActionSupport::toSearchResult)
                    .forEach(onMethodFound);
                break;
            default:
            }

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
            @Nullable final Class<?> additionalParamType) {

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
