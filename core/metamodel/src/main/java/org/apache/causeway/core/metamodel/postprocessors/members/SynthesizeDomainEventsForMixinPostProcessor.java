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
package org.apache.causeway.core.metamodel.postprocessors.members;

import javax.inject.Inject;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Mixed-in members use the domain-event type as specified with the mixee type,
 * <pre>@DomainObject()</pre>
 * unless overwritten by the mixin type.
 */
public class SynthesizeDomainEventsForMixinPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public SynthesizeDomainEventsForMixinPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if(objectAction.isMixedIn()) {
            objectAction
                .lookupFacet(ActionDomainEventFacet.class)
                .orElseThrow(()->_Exceptions
                        .illegalState("framework bug: "
                                + "ActionDomainEventFacet for %s should have already been created via "
                                + "ActionAnnotationFacetFactory, yet was not.",
                                objectAction.getFeatureIdentifier()))
                .initWithMixee(objectSpecification);
        }
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if(property.isMixedIn()) {
            property
                .lookupFacet(PropertyDomainEventFacet.class)
                .orElseThrow(()->_Exceptions
                        .illegalState("framework bug: "
                                + "PropertyDomainEventFacet for %s should have already been created via "
                                + "PropertyAnnotationFacetFactory, yet was not.",
                                property.getFeatureIdentifier()))
                .initWithMixee(objectSpecification);
        }
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {
        if(collection.isMixedIn()) {
            collection
                .lookupFacet(CollectionDomainEventFacet.class)
                .orElseThrow(()->_Exceptions
                        .illegalState("framework bug: "
                                + "CollectionDomainEventFacet for %s should have already been created via "
                                + "CollectionAnnotationFacetFactory, yet was not.",
                                collection.getFeatureIdentifier()))
                .initWithMixee(objectSpecification);

        }
    }

}
