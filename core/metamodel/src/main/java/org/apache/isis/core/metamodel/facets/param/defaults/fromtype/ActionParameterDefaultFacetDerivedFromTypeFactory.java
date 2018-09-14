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

package org.apache.isis.core.metamodel.facets.param.defaults.fromtype;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ActionParameterDefaultFacetDerivedFromTypeFactory extends FacetFactoryAbstract {

    public ActionParameterDefaultFacetDerivedFromTypeFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    /**
     * If there is a {@link DefaultedFacet} on any of the action's parameter
     * types, then installs a {@link ActionDefaultsFacet} for the action.
     */
    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        // don't overwrite any defaults already picked up
        if (processParameterContext.getFacetHolder().getFacet(ActionDefaultsFacet.class) != null) {
            return;
        }

        // try to infer defaults from any of the parameter's underlying types
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        final DefaultedFacet[] parameterTypeDefaultedFacets = new DefaultedFacet[parameterTypes.length];
        boolean hasAtLeastOneDefault = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> paramType = parameterTypes[i];
            parameterTypeDefaultedFacets[i] = getDefaultedFacet(paramType);
            hasAtLeastOneDefault = hasAtLeastOneDefault | (parameterTypeDefaultedFacets[i] != null);
        }
        if (hasAtLeastOneDefault) {
            FacetUtil.addFacet(new ActionParameterDefaultFacetDerivedFromTypeFacets(parameterTypeDefaultedFacets, processParameterContext.getFacetHolder()));
        }
    }

    private DefaultedFacet getDefaultedFacet(final Class<?> paramType) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(paramType);
        return paramTypeSpec.getFacet(DefaultedFacet.class);
    }

}
