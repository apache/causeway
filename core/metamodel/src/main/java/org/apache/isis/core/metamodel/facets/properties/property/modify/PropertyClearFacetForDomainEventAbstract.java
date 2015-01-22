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

package org.apache.isis.core.metamodel.facets.properties.property.modify;

import com.google.common.base.Objects;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

public abstract class PropertyClearFacetForDomainEventAbstract
        extends SingleValueFacetAbstract<Class<? extends PropertyDomainEvent<?,?>>>
        implements PropertyClearFacet {


    public static Class<? extends Facet> type() {
        return PropertyClearFacet.class;
    }

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertyClearFacet clearFacet;
    private final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

    public PropertyClearFacetForDomainEventAbstract(
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertyClearFacet clearFacet,
            final PropertyDomainEventFacetAbstract propertyDomainEventFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.clearFacet = clearFacet;
        this.propertyDomainEventFacet = propertyDomainEventFacet;
        this.domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    @Override
    public void clearProperty(ObjectAdapter targetAdapter) {
        if(clearFacet == null) {
            return;
        }

        if(!domainEventHelper.hasEventBusService()) {
            clearFacet.clearProperty(targetAdapter);
            return;
        }


        try {
            // pick up existing event (saved in thread local during the validation phase)
            final PropertyDomainEvent<?, ?> existingEvent = propertyDomainEventFacet.currentInteraction.get();

            // ... post the executing event
            final Object oldValue = getterFacet.getProperty(targetAdapter);
            domainEventHelper.postEventForProperty(
                    value(), existingEvent, AbstractDomainEvent.Phase.EXECUTING,
                    getIdentified(), targetAdapter, oldValue, null);

            // ... perform the property clear
            clearFacet.clearProperty(targetAdapter);

            // reading the actual value from the target object, playing it safe...
            final Object actualNewValue = getterFacet.getProperty(targetAdapter);
            if(Objects.equal(oldValue, actualNewValue)) {
                // do nothing.
                return;
            }

            // ... and post the event (reusing existing event if available)
            final PropertyDomainEvent<?, ?> event = propertyDomainEventFacet.currentInteraction.get();
            domainEventHelper.postEventForProperty(value(), verify(event), AbstractDomainEvent.Phase.EXECUTED, getIdentified(), targetAdapter, oldValue, actualNewValue);

        } finally {
            // clean up
            propertyDomainEventFacet.currentInteraction.set(null);
        }
    }

    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsPropertyChangedEvent} annotation
     * to discard the event if of a different type.
     */
    protected PropertyDomainEvent<?, ?> verify(PropertyDomainEvent<?, ?> event) {
        return event;
    }

    private Class<? extends PropertyDomainEvent<?, ?>> eventType() {
        return value();
    }

}
