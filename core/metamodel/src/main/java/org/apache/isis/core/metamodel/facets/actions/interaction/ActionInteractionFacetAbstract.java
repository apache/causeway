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

package org.apache.isis.core.metamodel.facets.actions.interaction;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.InteractionHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.interaction.CollectionInteractionFacet;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class ActionInteractionFacetAbstract
        extends SingleClassValueFacetAbstract implements ActionInteractionFacet {

    private final InteractionHelper interactionHelper;

    final static ThreadLocal<ActionInteractionEvent<?>> currentInteraction = new ThreadLocal<ActionInteractionEvent<?>>();

    public ActionInteractionFacetAbstract(
            final Class<? extends ActionInteractionEvent<?>> eventType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        super(CollectionInteractionFacet.class, holder, eventType, specificationLoader);
        interactionHelper = new InteractionHelper(servicesInjector);
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        if(!interactionHelper.hasEventBusService()) {
            return null;
        }

        final ActionInteractionEvent<?> event =
                interactionHelper.postEventForAction(
                        eventType(), null, null, AbstractInteractionEvent.Phase.HIDE,
                        getIdentified(), ic.getTarget(), null);
        if (event == null || !event.isHidden()) {
            // make available for next phase
            currentInteraction.set(event);
            return null;
        } else {
            // clean up
            currentInteraction.set(null);
            return "Hidden by subscriber";
        }
    }

    @Override
    public String disables(UsabilityContext<? extends UsabilityEvent> ic) {
        final ActionInteractionEvent<?> existingEvent = currentInteraction.get();
        final ActionInteractionEvent<?> event =
                interactionHelper.postEventForAction(
                        eventType(), existingEvent, null, AbstractInteractionEvent.Phase.DISABLE,
                        getIdentified(), ic.getTarget(), null);
        if (event == null || !event.isDisabled()) {
            // make available for next phase
            currentInteraction.set(event);
            return null;
        } else {
            // clean up
            currentInteraction.set(null);
            return event.getDisabledReason();
        }
    }

    @Override
    public String invalidates(ValidityContext<? extends ValidityEvent> ic) {

        final ActionInteractionEvent<?> existingEvent = currentInteraction.get();

        final ActionInvocationContext aic = (ActionInvocationContext) ic;

        final ActionInteractionEvent<?> event =
                interactionHelper.postEventForAction(
                        eventType(), existingEvent, null, AbstractInteractionEvent.Phase.VALIDATE,
                        getIdentified(), ic.getTarget(), aic.getArgs());
        if(event == null || !event.isInvalid()) {
            // make available for next phase
            currentInteraction.set(event);
            return null;
        } else {
            // clean up
            currentInteraction.set(null);
            return event.getInvalidityReason();
        }
    }


    private Class<?> eventType() {
        return value();
    }


}
