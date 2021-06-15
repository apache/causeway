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

package org.apache.isis.core.metamodel.facets.param.typicallen.fromtype;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        val type = processParameterContext.getParameterType();
        val facetHolder = processParameterContext.getFacetHolder();
        addFacetDerivedFromTypeIfPresent(facetHolder, type);
    }

    private void addFacetDerivedFromTypeIfPresent(final FacetHolder holder, final Class<?> type) {
        final TypicalLengthFacet facet = getTypicalLengthFacet(type);
        if (facet != null) {
            FacetUtil.addFacetIfPresent(new TypicalLengthFacetOnParameterInferredFromType(facet, holder));
        }
    }

    private TypicalLengthFacet getTypicalLengthFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(TypicalLengthFacet.class);
    }

}
