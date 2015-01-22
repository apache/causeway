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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class ActionDomainEventFacetAbstract
        extends SingleClassValueFacetAbstract implements ActionDomainEventFacet {

    static Class<? extends Facet> type() {
        return ActionDomainEventFacet.class;
    }

    private final DomainEventHelper domainEventHelper;

    /**
     * Pass event from validate to executing phases.
     *
     * <p>
     * A new event is created for the hide, disable and validate phases.  But when the validate completes (and if does
     * not invalidate), then the event is passed through to the executing phase using this thread-local.
     * </p>
     */
    final static ThreadLocal<ActionDomainEvent<?>> currentInteraction = new ThreadLocal<>();

    public ActionDomainEventFacetAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        super(type(), holder, eventType, specificationLoader);
        domainEventHelper = new DomainEventHelper(servicesInjector);
    }


    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        eventType(), null, null, AbstractDomainEvent.Phase.HIDE,
                        getIdentified(), ic.getTarget(), null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(UsabilityContext<? extends UsabilityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        eventType(), null, null, AbstractDomainEvent.Phase.DISABLE,
                        getIdentified(), ic.getTarget(), null);
        if (event != null && event.isDisabled()) {
            return event.getDisabledReason();
        }
        return null;
    }

    @Override
    public String invalidates(ValidityContext<? extends ValidityEvent> ic) {
        if(!domainEventHelper.hasEventBusService()) {
            return null;
        }

        // reset (belt-n-braces)
        currentInteraction.set(null);

        final ActionInvocationContext aic = (ActionInvocationContext) ic;
        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        eventType(), null, null, AbstractDomainEvent.Phase.VALIDATE,
                        getIdentified(), ic.getTarget(), aic.getArgs());
        if (event != null && event.isInvalid()) {
            return event.getInvalidityReason();
        }

        // make available for next phases (executing/executed)
        currentInteraction.set(event);
        return null;
    }

    protected Class eventType() {
        return value();
    }

    /**
     * For testing only.
     */
    public Class<? extends ActionDomainEvent<?>> getEventType() {
        //noinspection unchecked
        return eventType();
    }

}
