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

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public abstract class PropertySetterOrClearFacetForDomainEventAbstract
        extends SingleValueFacetAbstract<Class<? extends PropertyDomainEvent<?,?>>> {

    public static Class<? extends Facet> type() {
        return PropertySetterFacet.class;
    }

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyClearFacet clearFacet;
    private final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

    private final ServicesInjector servicesInjector;


    public PropertySetterOrClearFacetForDomainEventAbstract(
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final PropertyClearFacet clearFacet,
            final PropertyDomainEventFacetAbstract propertyDomainEventFacet,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {
        super(type(), eventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.clearFacet = clearFacet;
        this.propertyDomainEventFacet = propertyDomainEventFacet;
        this.servicesInjector = servicesInjector;
        this.domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    enum Type {
        SET {
            @Override
            boolean meetsPrereqs(final PropertySetterOrClearFacetForDomainEventAbstract facet) {
                return facet.setterFacet != null;
            }

            @Override
            void invoke(
                    final PropertySetterOrClearFacetForDomainEventAbstract facet,
                    final OneToOneAssociation owningProperty,
                    final ObjectAdapter targetAdapter,
                    final ObjectAdapter valueAdapterOrNull,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                facet.setterFacet.setProperty(
                        owningProperty, targetAdapter, valueAdapterOrNull, interactionInitiatedBy);

            }
        },
        CLEAR {
            @Override
            boolean meetsPrereqs(final PropertySetterOrClearFacetForDomainEventAbstract facet) {
                return facet.clearFacet != null;
            }

            @Override
            void invoke(
                    final PropertySetterOrClearFacetForDomainEventAbstract facet,
                    final OneToOneAssociation owningProperty,
                    final ObjectAdapter targetAdapter,
                    final ObjectAdapter valueAdapterOrNull,
                    final InteractionInitiatedBy interactionInitiatedBy) {

                facet.clearFacet.clearProperty(
                        owningProperty, targetAdapter, interactionInitiatedBy);

            }
        };

        abstract boolean meetsPrereqs(final PropertySetterOrClearFacetForDomainEventAbstract facet);

        abstract void invoke(
                final PropertySetterOrClearFacetForDomainEventAbstract facet,
                final OneToOneAssociation owningProperty,
                final ObjectAdapter targetAdapter,
                final ObjectAdapter valueAdapterOrNull,
                final InteractionInitiatedBy interactionInitiatedBy);
    }

    public void clearProperty(
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setOrClearProperty(Type.CLEAR,
                owningProperty, targetAdapter, null, interactionInitiatedBy);

    }

    public void setProperty(
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setOrClearProperty(Type.SET,
                owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);

    }

    private void setOrClearProperty(
            final Type type,
            final OneToOneAssociation owningAssociation,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        if(!type.meetsPrereqs(this)) {
            return;
        }

        // ... post the executing event
        final Object oldValue = getterFacet.getProperty(targetAdapter, interactionInitiatedBy);
        final Object newValue = ObjectAdapter.Util.unwrap(newValueAdapter);

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.EXECUTING,
                        eventType(), null,
                        getIdentified(), targetAdapter,
                        oldValue, newValue);

        setPropertyInternal(type, owningAssociation, targetAdapter, newValueAdapter, interactionInitiatedBy);

        // reading the actual value from the target object, playing it safe...
        final Object actualNewValue = getterFacet.getProperty(targetAdapter, interactionInitiatedBy);
        if (!Objects.equal(oldValue, actualNewValue)) {

            // ... post the executed event
            domainEventHelper.postEventForProperty(
                    AbstractDomainEvent.Phase.EXECUTED,
                    eventType(), verify(event),
                    getIdentified(), targetAdapter,
                    oldValue, actualNewValue);
        }
    }

    public void setPropertyInternal(
            final Type type,
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        owningProperty.setupCommand(targetAdapter, newValueAdapter);

        invokeThruCommand(type, owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);
    }

    private void invokeThruCommand(
            final Type type,
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter valueAdapterOrNull,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();

        final String propertyId = owningProperty.getIdentifier().toClassAndNameIdentityString();

        if( command.getExecutor() == Command.Executor.USER &&
                command.getExecuteIn() == org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND) {

            // deal with background commands

            // persist command so can it can subsequently be invoked in the 'background'
            final CommandService commandService = getCommandService();
            if (!commandService.persistIfPossible(command)) {
                throw new IsisException(String.format(
                        "Unable to persist command for property '%s'; CommandService does not support persistent commands ",
                        propertyId));
            }

        } else {

            // otherwise, go ahead and execute action in the 'foreground'

            final Object target = ObjectAdapter.Util.unwrap(targetAdapter);
            final Object argValue = ObjectAdapter.Util.unwrap(valueAdapterOrNull);

            final Interaction.PropertyArgs propertyArgs = new Interaction.PropertyArgs(propertyId, target, argValue);
            final Interaction.MemberCallable<?> callable = new Interaction.MemberCallable<Interaction.PropertyArgs>() {
                        @Override public Object call(final Interaction.PropertyArgs propertyArgs11) {

                            type.invoke(PropertySetterOrClearFacetForDomainEventAbstract.this, owningProperty, targetAdapter, valueAdapterOrNull, interactionInitiatedBy);
                            return null;
                        }
                    };

            interaction.execute(callable, propertyArgs, getClockService(), command);

            final Interaction.Execution priorExecution = interaction.getPriorExecution();

            final RuntimeException executionExceptionIfAny = priorExecution.getException();
            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny;
            }

            //
            // at this point in ActionInvocationFacetFDEA, the action is optionally published via the
            // PublishingServiceInternal.  However, we currently do not support the concept of publishing simple
            // property modifications.
            //
        }
    }

    private Class<? extends PropertyDomainEvent<?, ?>> eventType() {
        return value();
    }

    /**
     * Optional hook to allow the facet implementation for the deprecated {@link org.apache.isis.applib.annotation.PostsPropertyChangedEvent} annotation
     * to discard the event if of a different type.
     */
    protected PropertyDomainEvent<?, ?> verify(PropertyDomainEvent<?, ?> event) {
        return event;
    }



    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    private CommandContext getCommandContext() {
        return lookupService(CommandContext.class);
    }

    private InteractionContext getInteractionContext() {
        return lookupService(InteractionContext.class);
    }

    private CommandService getCommandService() {
        return lookupService(CommandService.class);
    }

    private ClockService getClockService() {
        return lookupService(ClockService.class);
    }

    private <T> T lookupService(final Class<T> serviceClass) {
        T service = lookupServiceIfAny(serviceClass);
        if(service == null) {
            throw new IllegalStateException("The '" + serviceClass.getName() + "' service is not registered!");
        }
        return service;
    }
    private <T> T lookupServiceIfAny(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }

}
