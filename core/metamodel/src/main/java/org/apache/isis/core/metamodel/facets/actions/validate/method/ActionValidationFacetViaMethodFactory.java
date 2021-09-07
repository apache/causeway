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

import javax.inject.Inject;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ActionSupport.ActionSupportingMethodSearchResult;
import org.apache.isis.core.metamodel.facets.ActionSupport.SearchAlgorithm;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.support.MemberSupportFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.param.validate.method.ActionParameterValidationFacetViaMethod;

import lombok.val;

/**
 * Sets up {@link ActionParameterValidationFacetViaMethod}.
 */
public class ActionValidationFacetViaMethodFactory
extends MemberSupportFacetFactoryAbstract {

    @Inject
    public ActionValidationFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY, MemberSupportPrefix.VALIDATE, searchOptions->
                searchOptions
                .searchAlgorithms(EnumSet.of(SearchAlgorithm.PPM, SearchAlgorithm.ALL_PARAM_TYPES)));
    }

    @Override
    protected void onSearchResult(
            final FacetedMethod facetHolder,
            final ActionSupportingMethodSearchResult searchResult) {
        val validateMethod = searchResult.getSupportingMethod();
        val ppmFactory = searchResult.getPpmFactory();
        addFacet(
                new ActionValidationFacetViaMethod(
                        validateMethod, ppmFactory, facetHolder));
    }

}
