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

package org.apache.isis.core.metamodel.facets;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.services.eventbus.*;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInteractionFacet;
import org.apache.isis.core.metamodel.facets.collections.interaction.InteractionWithCollectionAddFacet;
import org.apache.isis.core.metamodel.facets.collections.interaction.InteractionWithCollectionRemoveFacet;
import org.apache.isis.core.metamodel.facets.properties.interaction.InteractionWithPropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.interaction.InteractionWithPropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;


public class InteractionHelper {

    private final ServicesInjector servicesInjector;
    private final AbstractInteractionEvent.Mode mode;

    private EventBusService eventBusService;
    private boolean searchedForEventBusService = false;

    public InteractionHelper(
            final ServicesInjector servicesInjector,
            final AbstractInteractionEvent.Mode mode) {
        this.servicesInjector = servicesInjector;
        this.mode = mode;
    }

    @SuppressWarnings({ "rawtypes" })
    public ActionInteractionEvent<?> postEventForAction(
            final Class eventType,
            final IdentifiedHolder identified,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final Object source = AdapterUtils.unwrap(targetAdapter);
            final Object[] arguments = AdapterUtils.unwrap(argumentAdapters);
            final ActionInteractionEvent<?> event =
                    ActionInteractionFacet.Util.newEvent(
                            eventType, source, identified.getIdentifier(), arguments);
            event.setMode(mode);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public PropertyInteractionEvent<?, ?> postEventForPropertySet(
            final Class eventType,
            final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final Object oldValue,
            final Object newValue) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
            final PropertyInteractionEvent<?, ?> event =
                    InteractionWithPropertySetterFacet.Util.newEvent(
                            eventType, source, identified.getIdentifier(), oldValue, newValue);
            event.setMode(mode);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public PropertyInteractionEvent<?, ?> postEventForPropertyClear(
            final Class eventType,
            final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final Object oldValue,
            final Object newValue) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final Object source = ObjectAdapter.Util.unwrap(targetAdapter);
            final PropertyInteractionEvent<?, ?> event =
                    InteractionWithPropertyClearFacet.Util.newEvent(
                            eventType, source, identified.getIdentifier(), oldValue, newValue);
            event.setMode(mode);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CollectionInteractionEvent<?, ?> postEventForCollectionAdd(
            final Class eventType,
            final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final Object addedReference) {

        if(!hasEventBusService()) {
            return null;
        }
        try {
            final Object source = AdapterUtils.unwrap(targetAdapter);
            final CollectionInteractionEvent<?, ?> event =
                    InteractionWithCollectionAddFacet.Util.newEvent(
                            eventType, source, identified.getIdentifier(), addedReference);
            event.setMode(mode);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CollectionInteractionEvent<?, ?> postEventForCollectionRemove(
            final Class eventType,
            final ObjectAdapter targetAdapter,
            final IdentifiedHolder identified,
            final Object removedReference) {
        if(!hasEventBusService()) {
            return null;
        }
        try {
            final Object source = AdapterUtils.unwrap(targetAdapter);
            final CollectionInteractionEvent<?, ?> event =
                    InteractionWithCollectionRemoveFacet.Util.newEvent(
                            eventType, source, identified.getIdentifier(), removedReference);
            event.setMode(mode);
            getEventBusService().post(event);
            return event;
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    public boolean hasEventBusService() {
        return getEventBusService() != null;
    }

    private EventBusService getEventBusService() {
        if (!searchedForEventBusService) {
            eventBusService = this.servicesInjector.lookupService(EventBusService.class);
        }
        searchedForEventBusService = true;
        return eventBusService;
    }

}
