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
        @NonNull IntFunction<String> paramIndexToMethodName;
        @NonNull ReturnType returnType;
        Class<?> additionalParamType;
        
        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();
    }
    
    @Value(staticConstructor = "of")
    public static class ParamSupportingMethodSearchResult {
        int paramIndex;
        Class<?> paramType;
        Method supportingMethod;
        Class<?> returnType;
        Optional<Constructor<?>> ppmFactory;
    }

    public static void findParamSupportingMethods(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest, 
            final Consumer<ParameterSupport.ParamSupportingMethodSearchResult> onMethodFound) {
        
        val actionMethod = searchRequest.getProcessMethodContext().getMethod();
        val paramCount = actionMethod.getParameterCount();
        
        for (int i = 0; i < paramCount; i++) {
            for (int j = 0; j < 2; j++) { // account for 2 different search algorithms

                val algorithmIndex = j;
                val paramIndex = i;
                val searchResult = algorithmIndex==0
                        ? findParamSupportingMethodWithPPMArg(searchRequest, paramIndex)
                        : findParamSupportingMethod(searchRequest, paramIndex);
                
                if(log.isDebugEnabled()) {
                    log.debug("search algorithm={} {}{}",
                            algorithmIndex,
                            searchResult != null ? "FOUND " : "",
                            toString(searchRequest, paramIndex));
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
        val methodName = searchRequest.getParamIndexToMethodName().apply(paramIndex);
        val paramType = paramTypes[paramIndex];
        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());
        
        final MethodAndPpmConstructor supportingMethodAndPpmConstructor;
        
        switch(searchRequest.getReturnType()) {
        case BOOLEAN:
            supportingMethodAndPpmConstructor = MethodFinderUtils
                .findMethodWithPPMArg_returningBoolean(type, methodName, paramTypes, additionalParamTypes);
            break;
        case TEXT:
            supportingMethodAndPpmConstructor = MethodFinderUtils
                .findMethodWithPPMArg_returningText(type, methodName, paramTypes, additionalParamTypes);
            break;
        case NON_SCALAR:
            supportingMethodAndPpmConstructor = MethodFinderUtils
                .findMethodWithPPMArg_returningNonScalar(type, methodName, paramType, paramTypes, additionalParamTypes);
            break;
        case SAME_AS_PARAMETER_TYPE:
            supportingMethodAndPpmConstructor = MethodFinderUtils
                .findMethodWithPPMArg(type, methodName, paramType, paramTypes, additionalParamTypes);
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
    
    /*
     * search successively for the supporting method, trimming number of param types each loop
     */
    private static ParameterSupport.ParamSupportingMethodSearchResult findParamSupportingMethod(
            final ParameterSupport.ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodName = searchRequest.getParamIndexToMethodName().apply(paramIndex);
        val paramType = paramTypes[paramIndex];
        val additionalParamType = searchRequest.getAdditionalParamType();
        val additionalParamCount = additionalParamType!=null ? 1 : 0;
        
        int paramsConsideredCount = paramIndex + additionalParamCount; 
        while(paramsConsideredCount>=0) {
        
            val paramTypesToLookFor = concat(paramTypes, paramsConsideredCount, additionalParamType);
            
            final Method supportingMethod;
            
            switch(searchRequest.getReturnType()) {
            case BOOLEAN:
                supportingMethod = MethodFinderUtils
                    .findMethod_returningBoolean(type, methodName, paramTypesToLookFor);
                break;
            case TEXT:
                supportingMethod = MethodFinderUtils
                    .findMethod_returningText(type, methodName, paramTypesToLookFor);
                break;
            case NON_SCALAR:
                supportingMethod = MethodFinderUtils
                    .findMethod_returningNonScalar(type, methodName, paramType, paramTypesToLookFor);
                break;
            case SAME_AS_PARAMETER_TYPE:
                supportingMethod = MethodFinderUtils
                    .findMethod(type, methodName, paramType, paramTypesToLookFor);
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
                searchRequest.getParamIndexToMethodName().apply(paramIndex),
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