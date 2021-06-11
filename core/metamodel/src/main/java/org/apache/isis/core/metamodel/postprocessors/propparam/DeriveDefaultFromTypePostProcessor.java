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

package org.apache.isis.core.metamodel.postprocessors.propparam;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetInferredFromTypeFacets;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;


/**
 * Replaces {@link ActionParameterDefaultFacetDerivedFromTypeFactory}
 * and  {@link PropertyDefaultFacetDerivedFromTypeFactory}
 */
public class DeriveDefaultFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, final ObjectActionParameter parameter) {

        if (parameter.containsNonFallbackFacet(ActionDefaultsFacet.class)) {
            return;
        }

        // this loop within the outer loop (for every param) is really weird,
        // but arises from porting the old facet factory
        val parameterSpecs = objectAction.getParameterTypes();
        final DefaultedFacet[] parameterTypeDefaultedFacets = new DefaultedFacet[parameterSpecs.size()];
        boolean hasAtLeastOneDefault = false;
        for (int i = 0; i < parameterSpecs.size(); i++) {
            final ObjectSpecification parameterSpec = parameterSpecs.getElseFail(i);
            parameterTypeDefaultedFacets[i] = parameterSpec.getFacet(DefaultedFacet.class);
            hasAtLeastOneDefault = hasAtLeastOneDefault | (parameterTypeDefaultedFacets[i] != null);
        }
        if (hasAtLeastOneDefault) {
            FacetUtil.addFacetIfPresent(new ActionParameterDefaultFacetInferredFromTypeFacets(parameterTypeDefaultedFacets, peerFor(parameter)));
        }
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(PropertyDefaultFacet.class)) {
            return;
        }
        property.getSpecification()
        .lookupNonFallbackFacet(DefaultedFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new PropertyDefaultFacetDerivedFromDefaultedFacet(
                                    specFacet, facetedMethodFor(property))));
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
    }

}
