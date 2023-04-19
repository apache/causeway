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

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

public class TweakDomainEventsForMixinPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public TweakDomainEventsForMixinPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if(objectAction.isMixedIn()) {

 //TODO[CAUSEWAY-3409] yet already created in ActionAnnotationFacetFactory
            //FacetUtil.addFacetIfPresent(
                    //ActionDomainEventFacet.createMixedIn(objectSpecification, (ObjectActionMixedIn)objectAction));
 //TODO[CAUSEWAY-3409] even when this lookup returns empty, we still might need an event-type holding facet
            objectAction
                .lookupFacet(ActionDomainEventFacet.class)
                .ifPresent(actionDomainEventFacet->
                    actionDomainEventFacet.initWithMixee(objectSpecification));
        }
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation property) {

        if(property.isMixedIn()) {

            FacetUtil.addFacetIfPresent(
                    PropertyDomainEventFacet.createMixedIn(objectSpecification, (OneToOneAssociationMixedIn)property));
//TODO[CAUSEWAY-3409] even when this lookup returns empty, we still might need an event-type holding facet
            property
                .lookupFacet(PropertyDomainEventFacet.class)
                .ifPresent(propertyDomainEventFacet->
                    propertyDomainEventFacet.initWithMixee(objectSpecification));
        }
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {

        if(collection.isMixedIn()) {

            FacetUtil.addFacetIfPresent(
                    CollectionDomainEventFacet.createMixedIn(objectSpecification, (OneToManyAssociationMixedIn)collection));
//TODO[CAUSEWAY-3409] even when this lookup returns empty, we still might need an event-type holding facet
            collection
                .lookupFacet(CollectionDomainEventFacet.class)
                .ifPresent(collectionDomainEventFacet->
                    collectionDomainEventFacet.initWithMixee(objectSpecification));

        }
    }

}
