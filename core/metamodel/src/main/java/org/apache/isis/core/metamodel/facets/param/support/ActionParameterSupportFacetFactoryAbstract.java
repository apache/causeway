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
package org.apache.isis.core.metamodel.facets.param.support;

import java.util.EnumSet;
import java.util.function.UnaryOperator;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.ParameterSupport;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchRequest;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchRequest.ParamSupportingMethodSearchRequestBuilder;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchResult;
import org.apache.isis.core.metamodel.facets.ParameterSupport.SearchAlgorithm;
import org.apache.isis.core.metamodel.facets.members.support.MemberAndPropertySupportFacetFactoryAbstract;

import lombok.NonNull;
import lombok.val;

public abstract class ActionParameterSupportFacetFactoryAbstract
extends MemberAndPropertySupportFacetFactoryAbstract {

    private final UnaryOperator<ParamSupportingMethodSearchRequest.ParamSupportingMethodSearchRequestBuilder>
        searchRefiner;

    protected ActionParameterSupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull MemberSupportPrefix memberSupportPrefix) {
        this(mmc, memberSupportPrefix, UnaryOperator.identity());
    }

    protected ActionParameterSupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull MemberSupportPrefix memberSupportPrefix,
            final @NonNull UnaryOperator<ParamSupportingMethodSearchRequestBuilder> searchRefiner) {
        super(mmc, FeatureType.ACTIONS_ONLY, memberSupportPrefix);
        this.searchRefiner = searchRefiner;
    }

    @Override
    public final void process(final ProcessMethodContext processMethodContext) {

        val facetedMethod = processMethodContext.getFacetHolder();
        val parameters = facetedMethod.getParameters();

        if (parameters.isEmpty()) {
            return;
        }

        val methodNameCandidates = memberSupportPrefix.getMethodNamePrefixes()
                .flatMap(processMethodContext::parameterSupportCandidates);

        val searchRequest = searchRefiner
                .apply(
                        ParameterSupport.ParamSupportingMethodSearchRequest.builder()
                        .processMethodContext(processMethodContext)
                        .paramIndexToMethodNameProviders(methodNameCandidates)
                        .searchAlgorithms(EnumSet.of(SearchAlgorithm.PAT, SearchAlgorithm.SWEEP))
                        .returnTypePattern(memberSupportPrefix.getSupportMethodReturnType()))
                .build();

        ParameterSupport.findParamSupportingMethods(searchRequest, searchResult -> {
            processMethodContext.removeMethod(searchResult.getSupportingMethod());
            val paramIndex = searchResult.getParamIndex();
            // add facets directly to parameters, not to actions
            val paramAsHolder = parameters.get(paramIndex);
            onSearchResult(paramAsHolder, searchResult);
        });

    }

    protected abstract void onSearchResult(
            FacetedMethodParameter paramAsHolder,
            ParamSupportingMethodSearchResult searchResult);

}
