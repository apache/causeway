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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils.MethodAndPpmConstructor;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@UtilityClass 
@Log4j2
public class ParameterSupport {
    
    @Value @Builder
    public static class ParamSupportingMethodSearchRequest {
        
        public static enum ReturnType {
            NON_SCALAR,
            TEXT,
            BOOLEAN, 
            SAME_AS_PARAMETER_TYPE,
        }
        
        @NonNull FacetFactory.ProcessMethodContext processMethodContext;
        @NonNull Can<IntFunction<String>> paramIndexToMethodNameProviders;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms; 
        @NonNull ReturnType returnType;
        
        Class<?> additionalParamType;
        
        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();
        
        Can<String> getSupporingMethodNameCandidates(final int paramNr) {
            //TODO optimization make unique, without loosing order
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
        Optional<Constructor<?>> ppmFactory;
    }
    
    @FunctionalInterface
    public static interface SearchFunction {
        ParameterSupport.ParamSupportingMethodSearchResult search(
                final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
                final int paramNum);
    }
    
    @RequiredArgsConstructor
    public static enum SearchAlgorithm
    implements SearchFunction {
        PPM(ParameterSupport::findParamSupportingMethodWithPPMArg),
        SWEEP(ParameterSupport::findParamSupportingMethod),
        SINGLEARG_BEING_PARAMTYPE(ParameterSupport::singleArgBeingParamType)
        ;
        private final SearchFunction searchFunction;
        public ParameterSupport.ParamSupportingMethodSearchResult search(
                final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
                final int paramNum) {
            return searchFunction.search(searchRequest, paramNum);
        }
    }

    public static void findParamSupportingMethods(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest, 
            final Consumer<ParameterSupport.ParamSupportingMethodSearchResult> onMethodFound) {
        
        val actionMethod = searchRequest.getProcessMethodContext().getMethod();
        val paramCount = actionMethod.getParameterCount();
        
        for (int i = 0; i < paramCount; i++) {
            for (val searchAlgorithm : searchRequest.searchAlgorithms) { 

                val paramNum = i;
                val searchResult = searchAlgorithm.search(searchRequest, paramNum); 
                
                if(log.isDebugEnabled()) {
                    log.debug("search algorithm={} {}{}",
                            searchAlgorithm.name(),
                            searchResult != null ? "FOUND " : "",
                            toString(searchRequest, paramNum));
                }
    
                if (searchResult != null) {
                    onMethodFound.accept(searchResult);
                }
            }
        }
        
    }

    private static ParameterSupport.ParamSupportingMethodSearchResult findParamSupportingMethodWithPPMArg(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex) {
        
        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
                
        val paramType = paramTypes[paramIndex];
        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());
        
        final MethodAndPpmConstructor supportingMethodAndPpmConstructor;
        
        switch(searchRequest.getReturnType()) {
        case BOOLEAN:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg_returningBoolean(type, methodNames, paramTypes, additionalParamTypes);
            break;
        case TEXT:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg_returningText(type, methodNames, paramTypes, additionalParamTypes);
            break;
        case NON_SCALAR:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg_returningNonScalar(type, methodNames, paramType, paramTypes, additionalParamTypes);
            break;
        case SAME_AS_PARAMETER_TYPE:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg(type, methodNames, paramType, paramTypes, additionalParamTypes);
            break;
        default:
            supportingMethodAndPpmConstructor = null;
        }
        
        if(log.isDebugEnabled()) {
            
            log.debug(". signature (<any>, {}) {}", 
                    toString(additionalParamTypes.toArray(_Constants.emptyClasses)),
                    supportingMethodAndPpmConstructor != null 
                        ? "found -> " + supportingMethodAndPpmConstructor.getSupportingMethod() 
                        : "");
        }
        
        if(supportingMethodAndPpmConstructor != null) {
            val searchResult = ParamSupportingMethodSearchResult
                    .of(paramIndex, paramType, 
                            supportingMethodAndPpmConstructor.getSupportingMethod(), 
                            supportingMethodAndPpmConstructor.getSupportingMethod().getReturnType(),
                            Optional.of(supportingMethodAndPpmConstructor.getPpmFactory()));
            return searchResult;
        }
        
        return null;
        
    }
    
    private static ParameterSupport.ParamSupportingMethodSearchResult singleArgBeingParamType(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex) {
        
        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getSupporingMethodNameCandidates(paramIndex);
        val paramType = paramTypes[paramIndex];
        val singleArg = new Class<?>[]{paramType};
        
        final Method supportingMethod;
        
        switch(searchRequest.getReturnType()) {
        case BOOLEAN:
            supportingMethod = MethodFinder2
                .findMethod_returningBoolean(type, methodNames, singleArg);
            break;
        case TEXT:
            supportingMethod = MethodFinder2
                .findMethod_returningText(type, methodNames, singleArg);
            break;
        case NON_SCALAR:
            supportingMethod = MethodFinder2
                .findMethod_returningNonScalar(type, methodNames, paramType, singleArg);
            break;
        case SAME_AS_PARAMETER_TYPE:
            supportingMethod = MethodFinder2
                .findMethod(type, methodNames, paramType, singleArg);
            break;
        default:
            supportingMethod = null;
        }

        if(supportingMethod != null) {
            val searchResult = ParamSupportingMethodSearchResult
                    .of(paramIndex, paramType, supportingMethod, supportingMethod.getReturnType(), Optional.empty());
            return searchResult;
        }
     
        return null;
    }
    
    /*
     * search successively for the supporting method, trimming number of param types each loop
     */
    private static ParameterSupport.ParamSupportingMethodSearchResult findParamSupportingMethod(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex) {

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
            
            switch(searchRequest.getReturnType()) {
            case BOOLEAN:
                supportingMethod = MethodFinder2
                    .findMethod_returningBoolean(type, methodNames, paramTypesToLookFor);
                break;
            case TEXT:
                supportingMethod = MethodFinder2
                    .findMethod_returningText(type, methodNames, paramTypesToLookFor);
                break;
            case NON_SCALAR:
                supportingMethod = MethodFinder2
                    .findMethod_returningNonScalar(type, methodNames, paramType, paramTypesToLookFor);
                break;
            case SAME_AS_PARAMETER_TYPE:
                supportingMethod = MethodFinder2
                    .findMethod(type, methodNames, paramType, paramTypesToLookFor);
                break;
            default:
                supportingMethod = null;
            }
            
            if(log.isDebugEnabled()) {
                log.debug(". signature ({}) {}", 
                        toString(paramTypesToLookFor),
                        supportingMethod != null ? "found -> " + supportingMethod : "");
            }
            
            if(supportingMethod != null) {
                val searchResult = ParamSupportingMethodSearchResult
                        .of(paramIndex, paramType, supportingMethod, supportingMethod.getReturnType(), Optional.empty());
                return searchResult;
            }

            // remove last, and search again
            paramsConsideredCount--;
        }

        return null;
    }
    
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
    
    private String toString(
            ParameterSupport.ParamSupportingMethodSearchRequest searchRequest, 
            int paramIndex) {
        
        return String.format("%s.%s(%s) : %s",
                searchRequest.getProcessMethodContext().getCls().getSimpleName(),
                searchRequest.getSupporingMethodNameCandidates(paramIndex),
                toString(searchRequest.getParamTypes()),
                searchRequest.getReturnType().name()
                );
    }
    
    private String toString(Class<?>[] types) {
        return _NullSafe.stream(types)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
    }
    
    
}