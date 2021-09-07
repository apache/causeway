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
package org.apache.isis.core.metamodel.facets.param.hide.method;

import java.util.EnumSet;

import javax.inject.Inject;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ParameterSupport;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchRequest.ReturnType;
import org.apache.isis.core.metamodel.facets.ParameterSupport.SearchAlgorithm;
import org.apache.isis.core.metamodel.facets.param.hide.ActionParameterHiddenFacet;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

/**
 * Sets up {@link ActionParameterHiddenFacet}.
 */
public class ActionParameterHiddenFacetViaMethodFactory
extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String PREFIX = ProgrammingModelConstants.HIDE_PREFIX;

    @Inject
    public ActionParameterHiddenFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetedMethod = processMethodContext.getFacetHolder();
        val parameters = facetedMethod.getParameters();

        if (parameters.isEmpty()) {
            return;
        }

        // attach ActionParameterHiddenFacet if hideNumMethod is found ...

        val methodNameCandidates = processMethodContext.parameterSupportCandidates(PREFIX);

        val searchRequest = ParameterSupport.ParamSupportingMethodSearchRequest.builder()
                .processMethodContext(processMethodContext)
                .returnType(ReturnType.BOOLEAN)
                .paramIndexToMethodNameProviders(methodNameCandidates)
                .searchAlgorithms(EnumSet.of(SearchAlgorithm.PPM, SearchAlgorithm.SWEEP))
                .build();

        ParameterSupport.findParamSupportingMethods(searchRequest, searchResult -> {

            val hideMethod = searchResult.getSupportingMethod();
            val paramIndex = searchResult.getParamIndex();

            processMethodContext.removeMethod(hideMethod);

            if (facetedMethod.containsNonFallbackFacet(ActionParameterHiddenFacet.class)) {
                val cls = processMethodContext.getCls();
                throw new MetaModelException(cls + " uses both old and new 'hide' syntax - "
                        + "must use one or other");
            }

            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramIndex);
            val ppmFactory = searchResult.getPpmFactory();

            addFacet(
                    new ActionParameterHiddenFacetViaMethod(hideMethod, ppmFactory, paramAsHolder));
        });

    }


}
