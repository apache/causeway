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

package org.apache.isis.core.metamodel.postprocessors.properties;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyInferredFromRecreatableObject;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;


/**
 * Replaces {@link DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory}
 */
public class DeriveDisabledFromViewModelPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, ObjectActionParameter param) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(DisabledFacet.class)){
            return;
        }
        property.getOnType()
        .lookupNonFallbackFacet(ViewModelFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new DisabledFacetOnPropertyInferredFromRecreatableObject(
                                    facetedMethodFor(property), inferSemanticsFrom(specFacet))));
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
    }

    static DisabledFacetAbstract.Semantics inferSemanticsFrom(final ViewModelFacet facet) {
        return facet.isImplicitlyImmutable() ?
                DisabledFacetAbstract.Semantics.DISABLED :
                DisabledFacetAbstract.Semantics.ENABLED;
    }

}
