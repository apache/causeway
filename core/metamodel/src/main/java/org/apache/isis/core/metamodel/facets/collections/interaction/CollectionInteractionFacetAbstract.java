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

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.InteractionHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.*;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class CollectionInteractionFacetAbstract extends SingleClassValueFacetAbstract implements CollectionInteractionFacet {

    private final InteractionHelper interactionHelper;

    final static ThreadLocal<CollectionInteractionEvent<?,?>> currentInteraction = new ThreadLocal<CollectionInteractionEvent<?,?>>();

    public CollectionInteractionFacetAbstract(
            final Class<? extends CollectionInteractionEvent<?, ?>> eventType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        super(CollectionInteractionFacet.class, holder, eventType, specificationLoader);
        interactionHelper = new InteractionHelper(servicesInjector);
    }

    @Override
    public String hides(VisibilityContext<? extends VisibilityEvent> ic) {
        if(!interactionHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final CollectionInteractionEvent<?, ?> event =
                interactionHelper.postEventForCollection(
                        eventType(), null, AbstractInteractionEvent.Phase.HIDE, getIdentified(), ic.getTarget(), CollectionInteractionEvent.Of.ACCESS, null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(UsabilityContext<? extends UsabilityEvent> ic) {
        if(!interactionHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final CollectionInteractionEvent<?, ?> event =
                interactionHelper.postEventForCollection(
                    eventType(), null, AbstractInteractionEvent.Phase.DISABLE, getIdentified(), ic.getTarget(), CollectionInteractionEvent.Of.ACCESS, null);
        if (event != null && event.isDisabled()) {
            return event.getDisabledReason();
        }
        return null;
    }

    @Override
    public String invalidates(ValidityContext<? extends ValidityEvent> ic) {
        if(!interactionHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final ProposedHolder catc = (ProposedHolder) ic;
        final Object proposed = catc.getProposed().getObject();

        final CollectionInteractionEvent<?, ?> event =
                interactionHelper.postEventForCollection(
                        eventType(), null, AbstractInteractionEvent.Phase.VALIDATE, getIdentified(), ic.getTarget(), CollectionInteractionEvent.Of.ADD_TO, proposed);
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

}
