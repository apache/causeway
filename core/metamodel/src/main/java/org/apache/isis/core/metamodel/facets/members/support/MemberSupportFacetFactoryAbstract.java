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
package org.apache.isis.core.metamodel.facets.members.support;

import java.util.function.UnaryOperator;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ActionSupport;
import org.apache.isis.core.metamodel.facets.ActionSupport.ActionSupportingMethodSearchRequest.ActionSupportingMethodSearchRequestBuilder;
import org.apache.isis.core.metamodel.facets.ActionSupport.ActionSupportingMethodSearchResult;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

import lombok.NonNull;
import lombok.val;

public abstract class MemberSupportFacetFactoryAbstract
extends MemberAndPropertySupportFacetFactoryAbstract {

    private final UnaryOperator<ActionSupportingMethodSearchRequestBuilder>
        searchRefiner;

    protected MemberSupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull ImmutableEnumSet<FeatureType> featureTypes,
            final @NonNull MemberSupportPrefix memberSupportPrefix) {
        this(mmc, featureTypes, memberSupportPrefix, UnaryOperator.identity());
    }

    protected MemberSupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull ImmutableEnumSet<FeatureType> featureTypes,
            final @NonNull MemberSupportPrefix memberSupportPrefix,
            final @NonNull UnaryOperator<ActionSupportingMethodSearchRequestBuilder> searchRefiner) {
        super(mmc, featureTypes, memberSupportPrefix);
        this.searchRefiner = searchRefiner;
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val methodNameCandidates = memberSupportPrefix.getMethodNamePrefixes()
                .flatMap(processMethodContext::memberSupportCandidates);

        val searchRequest = searchRefiner
                .apply(
                        ActionSupport.ActionSupportingMethodSearchRequest.builder()
                        .processMethodContext(processMethodContext)
                        .methodNames(methodNameCandidates)
                        .returnType(memberSupportPrefix.getParameterSearchReturnType()))
                .build();

        ActionSupport.findActionSupportingMethods(searchRequest, searchResult -> {
            processMethodContext.removeMethod(searchResult.getSupportingMethod());
            onSearchResult(processMethodContext.getFacetHolder(), searchResult);
        });

    }

    protected abstract void onSearchResult(
            FacetedMethod facetHolder,
            ActionSupportingMethodSearchResult searchResult);

}
