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

package org.apache.isis.core.metamodel.facets.param.layout;

import java.util.Optional;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class ParameterLayoutFacetFactory extends FacetFactoryAbstract {

    public ParameterLayoutFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        val parameterLayoutIfAny = processParameterContext.synthesizeOnParameter(ParameterLayout.class);
        addFacets(processParameterContext, parameterLayoutIfAny);
    }

    protected void addFacets(
            ProcessParameterContext processParameterContext, 
            Optional<ParameterLayout> parameterLayoutIfAny) {
        
        val facetHolder = processParameterContext.getFacetHolder();

        super.addFacet(CssClassFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(DescribedAsFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(LabelAtFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(MultiLineFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(NamedFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(RenderedAdjustedFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));
        super.addFacet(TypicalLengthFacetForParameterLayoutAnnotation.create(parameterLayoutIfAny, facetHolder));

    }

}
