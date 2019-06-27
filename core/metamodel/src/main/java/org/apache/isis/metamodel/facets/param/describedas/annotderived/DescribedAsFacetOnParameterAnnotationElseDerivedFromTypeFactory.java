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

package org.apache.isis.metamodel.facets.param.describedas.annotderived;

import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory extends FacetFactoryAbstract {

    public DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final int paramNum = processParameterContext.getParamNum();
        final Class<?> parameterType = processParameterContext.getMethod().getParameterTypes()[paramNum];

        // fall back to a description on the parameter's type, if
        // available
        final DescribedAsFacet parameterTypeDescribedAsFacet = getDescribedAsFacet(parameterType);
        if (parameterTypeDescribedAsFacet != null) {
            FacetUtil.addFacet(new DescribedAsFacetOnParameterDerivedFromType(parameterTypeDescribedAsFacet, processParameterContext.getFacetHolder()));
        }

    }

    private DescribedAsFacet getDescribedAsFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(DescribedAsFacet.class);
    }

}
