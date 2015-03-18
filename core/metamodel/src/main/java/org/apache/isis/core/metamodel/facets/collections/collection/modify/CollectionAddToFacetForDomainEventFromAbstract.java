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

package org.apache.isis.core.metamodel.facets.collections.collection.modify;

import java.util.Set;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;


public abstract class CollectionAddToFacetForDomainEventFromAbstract
    extends SingleValueFacetAbstract<Class<? extends CollectionDomainEvent<?,?>>>
    implements CollectionAddToFacet {

    private final DomainEventHelper domainEventHelper;

    public static Class<? extends Facet> type() {
	    return CollectionAddToFacet.class;
    }

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final CollectionAddToFacet collectionAddToFacet;
    private final CollectionDomainEventFacetAbstract collectionDomainEventFacet;

    public CollectionAddToFacetForDomainEventFromAbstract(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final CollectionAddToFacet collectionAddToFacet,
            final CollectionDomainEventFacetAbstract collectionDomainEventFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.collectionAddToFacet = collectionAddToFacet;
        this.collectionDomainEventFacet = collectionDomainEventFacet;
        this.domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    @Override
    public void add(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter referencedObjectAdapter) {
        if (this.collectionAddToFacet == null) {
            return;
        }
        if(!domainEventHelper.hasEventBusService()) {
            collectionAddToFacet.add(targetAdapter, referencedObjectAdapter);
            return;
        }

        try {
            final Object referencedObject = ObjectAdapter.Util.unwrap(referencedObjectAdapter);

            // get hold of underlying collection
            final Object collection = getterFacet.getProperty(targetAdapter);

            // don't post event if has set semantics and already contains object
            if(collection instanceof Set) {
                Set<?> set = (Set<?>) collection;
                if(set.contains(referencedObject)) {
                    return;
                }
            }


            // either doesn't contain object, or doesn't have set semantics, so
            // execute the add wrapped between the executing and executed events ...

            // pick up existing event (saved in thread local during the validation phase)
            final CollectionDomainEvent<?, ?> existingEvent = collectionDomainEventFacet.currentInteraction.get();

            // ... post the executing event
            final CollectionDomainEvent<?, ?> event = domainEventHelper.postEventForCollection(
                    value(), existingEvent, AbstractDomainEvent.Phase.EXECUTING,
                    getIdentified(), targetAdapter, CollectionDomainEvent.Of.ADD_TO, referencedObject);

            // ... perform add
            collectionAddToFacet.add(targetAdapter, referencedObjectAdapter);

            // ... post the executed event
            domainEventHelper.postEventForCollection(
                    value(), verify(event), AbstractDomainEvent.Phase.EXECUTED,
                    getIdentified(), targetAdapter, CollectionDomainEvent.Of.ADD_TO, referencedObject);

        } finally {
            // clean up
            collectionDomainEventFacet.currentInteraction.set(null);
        }
    }

    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsCollectionAddedToEvent} annotation
     * to discard the event if of a different type.
     */
    protected CollectionDomainEvent<?, ?> verify(CollectionDomainEvent<?, ?> event) {
        return event;
    }

}
