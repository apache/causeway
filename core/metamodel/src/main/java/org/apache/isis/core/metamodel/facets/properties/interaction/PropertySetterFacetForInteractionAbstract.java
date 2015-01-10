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

package org.apache.isis.core.metamodel.facets.properties.interaction;

import com.google.common.base.Objects;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.InteractionHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

public abstract class PropertySetterFacetForInteractionAbstract
        extends SingleValueFacetAbstract<Class<? extends PropertyInteractionEvent<?,?>>>
        implements PropertySetterFacet {

    public static Class<? extends Facet> type() {
        return PropertySetterFacet.class;
    }

    private final InteractionHelper interactionHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyInteractionFacetAbstract propertyInteractionFacet;

    public PropertySetterFacetForInteractionAbstract(
            final Class<? extends PropertyInteractionEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final PropertyInteractionFacetAbstract propertyInteractionFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.propertyInteractionFacet = propertyInteractionFacet;
        this.interactionHelper = new InteractionHelper(servicesInjector);
    }

    @Override
    public void setProperty(ObjectAdapter targetAdapter, ObjectAdapter newValueAdapter) {
        if(setterFacet == null) {
            return;
        }
        if(!interactionHelper.hasEventBusService()) {
            setterFacet.setProperty(targetAdapter, newValueAdapter);
            return;
        }


        try {
            // pick up existing event (saved in thread local during the validation phase)
            final PropertyInteractionEvent<?, ?> existingEvent = propertyInteractionFacet.currentInteraction.get();

            // ... post the executing event
            final Object oldValue = getterFacet.getProperty(targetAdapter);
            final Object newValue = ObjectAdapter.Util.unwrap(newValueAdapter);

            interactionHelper.postEventForProperty(
                    value(), existingEvent, AbstractInteractionEvent.Phase.EXECUTING,
                    getIdentified(), targetAdapter, oldValue, newValue);

            // ... perform the property modification
            setterFacet.setProperty(targetAdapter, newValueAdapter);

            // reading the actual value from the target object, playing it safe...
            final Object actualNewValue = getterFacet.getProperty(targetAdapter);
            if(Objects.equal(oldValue, actualNewValue)) {
                return;
            }

            // ... post the executed event
            final PropertyInteractionEvent<?, ?> event = propertyInteractionFacet.currentInteraction.get();
            interactionHelper.postEventForProperty(value(), verify(event), AbstractInteractionEvent.Phase.EXECUTED, getIdentified(), targetAdapter, oldValue, actualNewValue);

        } finally {
            // clean up
            propertyInteractionFacet.currentInteraction.set(null);
        }
    }

    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsPropertyChangedEvent} annotation
     * to discard the event if of a different type.
     */
    protected PropertyInteractionEvent<?, ?> verify(PropertyInteractionEvent<?, ?> event) {
        return event;
    }

}
