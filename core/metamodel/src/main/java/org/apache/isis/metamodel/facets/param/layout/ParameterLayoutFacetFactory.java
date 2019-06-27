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

package org.apache.isis.metamodel.facets.param.layout;

import java.util.List;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;

public class ParameterLayoutFacetFactory extends FacetFactoryAbstract {

    public ParameterLayoutFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        if (processParameterContext.getParamNum() >= parameterTypes.length) {
            // ignore
            return;
        }

        final List<ParameterLayout> parameterLayouts =
                Annotations.getAnnotations(
                        processParameterContext.getMethod(),
                        processParameterContext.getParamNum(),
                        ParameterLayout.class);
        addFacets(processParameterContext, parameterLayouts);
    }

    protected void addFacets(ProcessParameterContext processParameterContext, List<ParameterLayout> parameterLayouts) {
        final FacetedMethodParameter facetHolder = processParameterContext.getFacetHolder();

        FacetUtil.addFacet(CssClassFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(DescribedAsFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(LabelAtFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(MultiLineFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(NamedFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(RenderedAdjustedFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));
        FacetUtil.addFacet(TypicalLengthFacetForParameterLayoutAnnotation.create(parameterLayouts, facetHolder));

    }

}
