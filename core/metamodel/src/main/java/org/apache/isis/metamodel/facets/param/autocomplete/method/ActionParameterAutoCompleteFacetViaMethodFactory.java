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

package org.apache.isis.metamodel.facets.param.autocomplete.method;

import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.DependentArgUtils;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;

import lombok.val;

public class ActionParameterAutoCompleteFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.AUTO_COMPLETE_PREFIX);

    public ActionParameterAutoCompleteFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachAutoCompleteFacetForParametersIfAutoCompleteNumMethodIsFound(processMethodContext, holderList);

    }
    
    private void attachAutoCompleteFacetForParametersIfAutoCompleteNumMethodIsFound(
            final ProcessMethodContext processMethodContext, 
            final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        val actionMethod = processMethodContext.getMethod();
        val capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());

        val searchRequest = DependentArgUtils.ParamSupportingMethodSearchRequest.of(
                processMethodContext,
                String.class,
                paramIndex -> MethodLiteralConstants.AUTO_COMPLETE_PREFIX + paramIndex + capitalizedName);    

        DependentArgUtils.findParamSupportingMethods(searchRequest, searchResult -> {
            
            val autoCompleteMethod = searchResult.getSupportingMethod();
            val paramIndex = searchResult.getParamIndex();
            val paramType = searchResult.getParamType();
            
            processMethodContext.removeMethod(autoCompleteMethod);

            val facetedMethod = processMethodContext.getFacetHolder();
            if (facetedMethod.containsNonFallbackFacet(ActionParameterAutoCompleteFacet.class)) {
                val cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new autoComplete syntax - "
                        + "must use one or other");
            }
            
            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramIndex);
            super.addFacet(
                    new ActionParameterAutoCompleteFacetViaMethod(
                            autoCompleteMethod, paramType, paramAsHolder));
        });
        
    }
    
// legacy of ...
//    private void attachAutoCompleteFacetForParametersIfAutoCompleteNumMethodIsFound(
//            final ProcessMethodContext processMethodContext, 
//            final List<FacetedMethodParameter> parameters) {
//
//        if (parameters.isEmpty()) {
//            return;
//        }
//
//        val actionMethod = processMethodContext.getMethod();
//        val paramTypes = actionMethod.getParameterTypes();
//
//        for (int i = 0; i < paramTypes.length; i++) {
//
//            final Class<?> paramType = paramTypes[i];
//            final Class<?> arrayOfParamType = Array.newInstance(paramType, 0).getClass();
//            final Class<?>[] returnTypes = { arrayOfParamType, List.class, Set.class, Collection.class };
//            
//            val autoCompleteMethod = findAutoCompleteNumMethodReturning(processMethodContext, i, returnTypes);
//            if (autoCompleteMethod == null) {
//                continue;
//            }
//            processMethodContext.removeMethod(autoCompleteMethod);
//
//            // add facets directly to parameters, not to actions
//            final FacetedMethodParameter paramAsHolder = parameters.get(i);
//            super.addFacet(
//                    new ActionParameterAutoCompleteFacetViaMethod(
//                            autoCompleteMethod, paramType, paramAsHolder));
//        }
//    }
//
//    private Method findAutoCompleteNumMethodReturning(
//            final ProcessMethodContext processMethodContext,
//            final int paramNum,
//            final Class<?>[] returnTypes) {
//
//        final Class<?> cls = processMethodContext.getCls();
//        final Method actionMethod = processMethodContext.getMethod();
//        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
//        final String name = MethodLiteralConstants.AUTO_COMPLETE_PREFIX + paramNum + capitalizedName;
//        return MethodFinderUtils.findMethod_returningAnyOf(
//                returnTypes,
//                cls,
//                name,
//                STRING_ARG);
//    }

}
