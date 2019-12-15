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
package org.apache.isis.metamodel.facets;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;

import lombok.Getter;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * 
 * @since 2.0
 *
 */
@UtilityClass
public class DependentArgUtils {
    
    @Value(staticConstructor = "of")
    public static class ParamSupportingMethodSearchRequest {
        ProcessMethodContext processMethodContext;
        Class<?> additionalParamType;
        IntFunction<String> paramIndexToMethodName;
        
        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();
    }
    
    @Value(staticConstructor = "of")
    public static class ParamSupportingMethodSearchResult {
        int paramIndex;
        Class<?> paramType;
        Method supportingMethod;
        Class<?> returnType;
    }

    public void findParamSupportingMethods(
            final DependentArgUtils.ParamSupportingMethodSearchRequest searchRequest, 
            final Consumer<DependentArgUtils.ParamSupportingMethodSearchResult> onMethodFound) {
        
        val actionMethod = searchRequest.getProcessMethodContext().getMethod();
        val paramCount = actionMethod.getParameterCount();
        
        for (int i = 0; i < paramCount; i++) {

            val paramIndex = i;
            val searchResult = findParamSupportingMethod(searchRequest, paramIndex);

            if (searchResult != null) {
                onMethodFound.accept(searchResult);
            }
        }
        
    }

    /*
     * search successively for the supporting method, trimming number of param types each loop
     */
    private static DependentArgUtils.ParamSupportingMethodSearchResult findParamSupportingMethod(
            final DependentArgUtils.ParamSupportingMethodSearchRequest searchRequest,
            final int paramIndex) {

        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodName = searchRequest.getParamIndexToMethodName().apply(paramIndex);
        val paramType = paramTypes[paramIndex];
        val additionalParamType = searchRequest.getAdditionalParamType();
        
        int paramsConsidered = paramTypes.length - 1; 
        while(paramsConsidered>=0) {
        
            val paramTypesToLookFor = concat(paramTypes, paramsConsidered, additionalParamType);
            
            val supportingMethod = MethodFinderUtils
                    .findMethod_returningNonScalar(type, methodName, paramType, paramTypesToLookFor);
            
            if(supportingMethod != null) {
                val searchResult = ParamSupportingMethodSearchResult.of(
                        paramIndex, paramType, supportingMethod, supportingMethod.getReturnType());
                return searchResult;
            }

            // remove last, and search again
            paramsConsidered--;
        }

        return null;
    }
    
    public static Class<?>[] concat(
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