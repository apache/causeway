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

package org.apache.isis.core.metamodel.postprocessors.all;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberDerivedFromType;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Replaces some of the functionality in {@link DescribedAsFacetOnMemberFactory} and
 * {@link DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory}.
 */
public class DeriveDescribedAsFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
        // no-op
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if(objectAction.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        objectAction.getReturnType()
        .lookupNonFallbackFacet(DescribedAsFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new DescribedAsFacetOnMemberDerivedFromType(specFacet,
                                    facetedMethodFor(objectAction))));
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        final ObjectSpecification paramSpec = parameter.getSpecification();
        final DescribedAsFacet specFacet = paramSpec.getFacet(DescribedAsFacet.class);

        //TODO: this ought to check if a do-op; if you come across this, you can probably change it (just taking smaller steps for now)
        //if(existsAndIsDoOp(specFacet)) {
        if(specFacet != null) {
            FacetUtil.addFacetIfPresent(new DescribedAsFacetOnParameterDerivedFromType(specFacet, peerFor(parameter)));
        }
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToOneAssociation prop) {
        handle(prop);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
        handle(coll);
    }

    private void handle(final ObjectAssociation objectAssociation) {
        if(objectAssociation.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        objectAssociation.getSpecification()
        .lookupNonFallbackFacet(DescribedAsFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new DescribedAsFacetOnMemberDerivedFromType(
                                    specFacet, facetedMethodFor(objectAssociation))));
    }

}
