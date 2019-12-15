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

package org.apache.isis.metamodel.facets.param.choices.methodnum;

import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.DependentArgUtils;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.param.choices.ActionChoicesFacet;

import lombok.val;

public class ActionParameterChoicesFacetViaMethodFactory 
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterChoicesFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachChoicesFacetForParametersIfChoicesNumMethodIsFound(processMethodContext, holderList);

    }

    private void attachChoicesFacetForParametersIfChoicesNumMethodIsFound(
            final ProcessMethodContext processMethodContext, 
            final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        val actionMethod = processMethodContext.getMethod();
        val capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());

        val searchRequest = DependentArgUtils.ParamSupportingMethodSearchRequest.of(
                processMethodContext, 
                null,
                paramIndex -> MethodLiteralConstants.CHOICES_PREFIX + paramIndex + capitalizedName);    

        DependentArgUtils.findParamSupportingMethods(searchRequest, searchResult -> {
            
            val choicesMethod = searchResult.getSupportingMethod();
            val paramIndex = searchResult.getParamIndex();
            val returnType = searchResult.getReturnType();
            
            processMethodContext.removeMethod(choicesMethod);

            val facetedMethod = processMethodContext.getFacetHolder();
            if (facetedMethod.containsNonFallbackFacet(ActionChoicesFacet.class)) {
                val cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new choices syntax - "
                        + "must use one or other");
            }
            
            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramIndex);
            super.addFacet(
                    new ActionParameterChoicesFacetViaMethod(
                            choicesMethod, returnType, paramAsHolder));
        });
        
    }
    
    

}
