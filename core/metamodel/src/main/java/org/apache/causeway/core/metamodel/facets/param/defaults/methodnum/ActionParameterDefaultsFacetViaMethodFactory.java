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
package org.apache.causeway.core.metamodel.facets.param.defaults.methodnum;

import jakarta.inject.Inject;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethodParameter;
import org.apache.causeway.core.metamodel.facets.ParameterSupport.ParamSupportingMethodSearchResult;
import org.apache.causeway.core.metamodel.facets.param.support.ActionParameterSupportFacetFactoryAbstract;

public class ActionParameterDefaultsFacetViaMethodFactory
extends ActionParameterSupportFacetFactoryAbstract {

    @Inject
    public ActionParameterDefaultsFacetViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, MemberSupportPrefix.DEFAULT);
    }

    @Override
    protected void onSearchResult(
            final FacetedMethodParameter paramAsHolder,
            final ParamSupportingMethodSearchResult searchResult) {
        var defaultMethod = searchResult.supportingMethod();
        var paramIndex = searchResult.paramIndex();
        var patConstructor = searchResult.patConstructor();
        addFacet(
                new ActionParameterDefaultsFacetViaMethod(
                        defaultMethod, paramIndex, patConstructor, paramAsHolder));
    }

}
