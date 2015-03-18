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

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class CollectionDomainEventFacetAbstract extends SingleClassValueFacetAbstract implements CollectionDomainEventFacet {

    private final DomainEventHelper domainEventHelper;

    final static ThreadLocal<CollectionDomainEvent<?,?>> currentInteraction = new ThreadLocal<>();

    public CollectionDomainEventFacetAbstract(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        super(CollectionDomainEventFacet.class, holder, eventType, specificationLoader);
        domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        eventType(), null, AbstractDomainEvent.Phase.HIDE, getIdentified(), ic.getTarget(), CollectionDomainEvent.Of.ACCESS, null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                    eventType(), null, AbstractDomainEvent.Phase.DISABLE, getIdentified(), ic.getTarget(), CollectionDomainEvent.Of.ACCESS, null);
        if (event != null && event.isDisabled()) {
            return event.getDisabledReason();
        }
        return null;
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final ProposedHolder catc = (ProposedHolder) ic;
        final Object proposed = catc.getProposed().getObject();

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        eventType(), null, AbstractDomainEvent.Phase.VALIDATE, getIdentified(), ic.getTarget(), CollectionDomainEvent.Of.ADD_TO, proposed);
        if (event != null && event.isInvalid()) {
            return event.getInvalidityReason();
        }

        // make available for next phases (executing/executed)
        currentInteraction.set(event);
        return null;
    }

    private Class<?> eventType() {
        return value();
    }

    /**
     * For testing.
     */
    public Class<? extends CollectionDomainEvent<?, ?>> getEventType() {
        Class eventType = eventType();
        return eventType;
    }

}
