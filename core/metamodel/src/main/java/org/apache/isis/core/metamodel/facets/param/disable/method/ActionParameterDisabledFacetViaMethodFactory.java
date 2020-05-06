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

package org.apache.isis.core.metamodel.facets.param.disable.method;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ParameterSupport;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchRequest.ReturnType;
import org.apache.isis.core.metamodel.facets.param.disable.ActionParameterDisabledFacet;

import lombok.val;

/**
 * Sets up {@link ActionParameterDisabledFacet}.
 */
public class ActionParameterDisabledFacetViaMethodFactory 
extends MethodPrefixBasedFacetFactoryAbstract  {
    
    private static final String PREFIX = MethodLiteralConstants.DISABLE_PREFIX;

    public ActionParameterDisabledFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetedMethod = processMethodContext.getFacetHolder();
        val parameters = facetedMethod.getParameters();

        if (parameters.isEmpty()) {
            return;
        }

        // attach ActionParameterDisabledFacet if disableNumMethod is found ...
        
        val actionMethod = processMethodContext.getMethod();
        val namingConvention = PREFIX_BASED_NAMING.providerForParam(actionMethod, PREFIX);

        val searchRequest = ParameterSupport.ParamSupportingMethodSearchRequest.builder()
                .processMethodContext(processMethodContext)
                .returnType(ReturnType.TEXT)
                .paramIndexToMethodName(namingConvention)
                .build();
        
        ParameterSupport.findParamSupportingMethods(searchRequest, searchResult -> {
            
            val disableMethod = searchResult.getSupportingMethod();
            val paramNum = searchResult.getParamIndex();
            
            processMethodContext.removeMethod(disableMethod);

            if (facetedMethod.containsNonFallbackFacet(ActionParameterDisabledFacet.class)) {
                val cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new 'disable' syntax - "
                        + "must use one or other");
            }
            
            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramNum);
            val translationContext = paramAsHolder.getIdentifier().toFullIdentityString();
            val ppmFactory = searchResult.getPpmFactory();
            val translationService = getMetaModelContext().getTranslationService();
            
            super.addFacet(
                    new ActionParameterDisabledFacetViaMethod(
                            disableMethod, translationService, translationContext, ppmFactory, paramAsHolder));
        });
        
    }

}
