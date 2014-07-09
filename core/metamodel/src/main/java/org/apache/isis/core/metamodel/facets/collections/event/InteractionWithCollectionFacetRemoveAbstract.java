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

package org.apache.isis.core.metamodel.facets.collections.event;

import java.util.Collection;
import com.google.common.collect.Lists;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;


public abstract class InteractionWithCollectionFacetRemoveAbstract
    extends SingleValueFacetAbstract<Class<? extends CollectionInteractionEvent<?,?>>>
    implements InteractionWithCollectionRemoveFacet {

	public static Class<? extends Facet> type() {
	    
	    // the "primary" type is CollectionAddToFacet rather than InteractionWithCollectionRemoveFacet
	    // so that this facet can wrap an existing (via setUnderlying).
	    
        //return InteractionWithCollectionRemoveFacet.class;
	    return CollectionRemoveFromFacet.class;
    }

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final CollectionRemoveFromFacet collectionRemoveFromFacet;
    private final ServicesInjector servicesInjector;

    private EventBusService eventBusService;
    private boolean searchedForEventBusService = false;

    public InteractionWithCollectionFacetRemoveAbstract(
            final Class<? extends CollectionInteractionEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final CollectionRemoveFromFacet collectionRemoveFromFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.collectionRemoveFromFacet = collectionRemoveFromFacet;
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void remove(ObjectAdapter targetAdapter,
                       ObjectAdapter referencedObjectAdapter) {
        if (this.collectionRemoveFromFacet == null) {
            return;
        }
        eventBusService = getEventBusService();
        if (eventBusService == null) {
            collectionRemoveFromFacet.remove(targetAdapter,
                    referencedObjectAdapter);
            return;
        }

        final Object referencedObject = AdapterUtils
                .unwrap(referencedObjectAdapter);

        // get hold of underlying collection
        final Object collection = getterFacet.getProperty(targetAdapter);

        // don't post event if the collections does not contain object
        if (!((Collection<?>) collection).contains(referencedObject)) {
            return;
        }

        // contains the element. So the event must be posted.
        collectionRemoveFromFacet
                .remove(targetAdapter, referencedObjectAdapter);

        postEvent(targetAdapter, getIdentified().getIdentifier(),
                referencedObject);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void postEvent(final ObjectAdapter targetAdapter,
                           final Identifier identifier, final Object removedReference) {

        try {
            final Class type = value();
            final Object source = AdapterUtils.unwrap(targetAdapter);
            final CollectionInteractionEvent<?, ?> event = Util.newEvent(type, source, identifier, removedReference);
            eventBusService.post(event);
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    private EventBusService getEventBusService() {
        if (!searchedForEventBusService) {
            eventBusService = this.servicesInjector.lookupService(EventBusService.class);
        }
        searchedForEventBusService = true;
        return eventBusService;
    }

    // //////////////////////////////////////
    // MultiTypedFacet
    // //////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] facetTypes() {
        return Lists.newArrayList(type(), // ie CollectionRemoveFromFacet
                InteractionWithCollectionRemoveFacet.class).toArray(new Class[] {});
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T getFacet(Class<T> facet) {
        return (T) this;
    }



}
