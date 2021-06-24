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

package org.apache.isis.core.metamodel.facets.param.defaults.methodnum;

import java.util.EnumSet;

import javax.inject.Inject;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ParameterSupport;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchRequest.ReturnType;
import org.apache.isis.core.metamodel.facets.ParameterSupport.SearchAlgorithm;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionParameterDefaultsFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.DEFAULT_PREFIX;

    @Inject
    public ActionParameterDefaultsFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetedMethod = processMethodContext.getFacetHolder();
        val parameters = facetedMethod.getParameters();

        if (parameters.isEmpty()) {
            return;
        }

        // attach DefaultFacetForParameters if defaultNumMethod is found ...

        val actionMethod = processMethodContext.getMethod();
        val namingConvention = processMethodContext.parameterSupportCandidates(PREFIX);

        val searchRequest = ParameterSupport.ParamSupportingMethodSearchRequest.builder()
                .processMethodContext(processMethodContext)
                .returnType(ReturnType.SAME_AS_PARAMETER_TYPE)
                .paramIndexToMethodNameProviders(namingConvention)
                .searchAlgorithms(EnumSet.of(SearchAlgorithm.PPM, SearchAlgorithm.SWEEP))
                .build();

        ParameterSupport.findParamSupportingMethods(searchRequest, searchResult -> {

            val defaultMethod = searchResult.getSupportingMethod();
            val paramIndex = searchResult.getParamIndex();

            processMethodContext.removeMethod(defaultMethod);

            if (facetedMethod.containsNonFallbackFacet(ActionDefaultsFacet.class)) {
                val cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new default syntax for "
                        + actionMethod.getName() + "(...) - must use one or other");
            }

            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramIndex);
            //val translationContext = paramAsHolder.getIdentifier().toFullIdentityString();
            val ppmFactory = searchResult.getPpmFactory();

            addFacet(new ActionParameterDefaultsFacetViaMethod(
                    defaultMethod, paramIndex, ppmFactory, paramAsHolder));
        });
    }


}
