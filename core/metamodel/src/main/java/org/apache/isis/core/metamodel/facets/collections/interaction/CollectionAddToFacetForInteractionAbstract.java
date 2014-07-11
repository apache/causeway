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

package org.apache.isis.core.metamodel.facets.collections.interaction;

import java.util.Set;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.InteractionHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;


public abstract class CollectionAddToFacetForInteractionAbstract
    extends SingleValueFacetAbstract<Class<? extends CollectionInteractionEvent<?,?>>>
    implements CollectionAddToFacet {

    private final InteractionHelper interactionHelper;

    public static Class<? extends Facet> type() {
	    return CollectionAddToFacet.class;
    }

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final CollectionAddToFacet collectionAddToFacet;
    private final CollectionInteractionFacetAbstract collectionInteractionFacet;

    public CollectionAddToFacetForInteractionAbstract(
            final Class<? extends CollectionInteractionEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final CollectionAddToFacet collectionAddToFacet,
            final CollectionInteractionFacetAbstract collectionInteractionFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.collectionAddToFacet = collectionAddToFacet;
        this.collectionInteractionFacet = collectionInteractionFacet;
        this.interactionHelper = new InteractionHelper(servicesInjector);
    }

    @Override
    public void add(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter referencedObjectAdapter) {
        if (this.collectionAddToFacet == null) {
            return;
        }
        if(!interactionHelper.hasEventBusService()) {
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
            final CollectionInteractionEvent<?, ?> existingEvent = collectionInteractionFacet.currentInteraction.get();

            // ... post the executing event
            final CollectionInteractionEvent<?, ?> event = interactionHelper.postEventForCollection(
                    value(), existingEvent, AbstractInteractionEvent.Phase.EXECUTING,
                    getIdentified(), targetAdapter, CollectionInteractionEvent.Of.ADD_TO, referencedObject);

            // ... perform add
            collectionAddToFacet.add(targetAdapter, referencedObjectAdapter);

            // ... post the executed event
            interactionHelper.postEventForCollection(
                    value(), verify(event), AbstractInteractionEvent.Phase.EXECUTED,
                    getIdentified(), targetAdapter, CollectionInteractionEvent.Of.ADD_TO, referencedObject);

        } finally {
            // clean up
            collectionInteractionFacet.currentInteraction.set(null);
        }
    }

    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsCollectionAddedToEvent} annotation
     * to discard the event if of a different type.
     */
    protected CollectionInteractionEvent<?, ?> verify(CollectionInteractionEvent<?, ?> event) {
        return event;
    }

}
