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

package org.apache.isis.core.metamodel.facets.actions.validate.method;

import java.util.EnumSet;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ActionSupport;
import org.apache.isis.core.metamodel.facets.ActionSupport.SearchAlgorithm;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.facets.param.validate.method.ActionParameterValidationFacetViaMethod;

import lombok.val;

/**
 * Sets up {@link ActionValidationFacet} and {@link ActionParameterValidationFacetViaMethod}.
 */
public class ActionValidationFacetViaMethodFactory 
extends MethodPrefixBasedFacetFactoryAbstract  {
    
    private static final String PREFIX = MethodLiteralConstants.VALIDATE_PREFIX;

    public ActionValidationFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        handleValidateAllArgsMethod(processMethodContext);
    }

    private void handleValidateAllArgsMethod(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();

        val namingConvention = getNamingConventionForActionSupport(processMethodContext, PREFIX);
        
        val searchRequest = ActionSupport.ActionSupportingMethodSearchRequest.builder()
                .processMethodContext(processMethodContext)
                .returnType(ActionSupport.ActionSupportingMethodSearchRequest.ReturnType.TEXT)
                .methodNames(namingConvention)
                .searchAlgorithms(EnumSet.of(SearchAlgorithm.PPM, SearchAlgorithm.ALL_PARAM_TYPES))
                .build();
        
        ActionSupport.findActionSupportingMethods(searchRequest, searchResult -> {
            val validateMethod = searchResult.getSupportingMethod();
            
            processMethodContext.removeMethod(validateMethod);
            
            if (facetHolder.containsNonFallbackFacet(ActionValidationFacetViaMethod.class)) {
                throw new MetaModelException( processMethodContext.getCls() + " uses both old and new 'validate' syntax - "
                        + "must use one or other");
            }

            val ppmFactory = searchResult.getPpmFactory();
            val translationService = getTranslationService();
            val translationContext = facetHolder.getIdentifier().getTranslationContext();
            super.addFacet(
                    new ActionValidationFacetViaMethod(
                            validateMethod, translationService, translationContext, ppmFactory, facetHolder));
        });

    }



}
