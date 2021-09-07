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

import javax.inject.Inject;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchResult;
import org.apache.isis.core.metamodel.facets.param.hide.ActionParameterHiddenFacet;
import org.apache.isis.core.metamodel.facets.param.support.ActionParameterSupportFacetAbstract;

import lombok.val;

/**
 * Sets up {@link ActionParameterHiddenFacet}.
 */
public class ActionParameterHiddenFacetViaMethodFactory
extends ActionParameterSupportFacetAbstract {

    @Inject
    public ActionParameterHiddenFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, MemberSupportPrefix.HIDE);
    }

    @Override
    protected void onSearchResult(
            final FacetedMethodParameter paramAsHolder,
            final ParamSupportingMethodSearchResult searchResult) {
        val hideMethod = searchResult.getSupportingMethod();
        val ppmFactory = searchResult.getPpmFactory();
        addFacet(
                new ActionParameterHiddenFacetViaMethod(
                        hideMethod, ppmFactory, paramAsHolder));
    }


}
