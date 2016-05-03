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
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;

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

    enum Style {
        SET {
            @Override
            boolean hasCorrespondingFacet(final PropertySetterOrClearFacetForDomainEventAbstract facet) {
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
            boolean hasCorrespondingFacet(final PropertySetterOrClearFacetForDomainEventAbstract facet) {
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

        abstract boolean hasCorrespondingFacet(final PropertySetterOrClearFacetForDomainEventAbstract facet);

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

        setOrClearProperty(Style.CLEAR,
                owningProperty, targetAdapter, null, interactionInitiatedBy);

    }

    public void setProperty(
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setOrClearProperty(Style.SET,
                owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);

    }

    private void setOrClearProperty(
            final Style style,
            final OneToOneAssociation owningProperty,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        if(!style.hasCorrespondingFacet(this)) {
            return;
        }

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        owningProperty.setupCommand(targetAdapter, newValueAdapter);


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

            final Object target = ObjectAdapter.Util.unwrap(targetAdapter);
            final Object argValue = ObjectAdapter.Util.unwrap(newValueAdapter);

            final Interaction.PropertyModification execution =
                    new Interaction.PropertyModification(propertyId, target, argValue);
            final Interaction.MemberExecutor<Interaction.PropertyModification> executor =
                    new Interaction.MemberExecutor<Interaction.PropertyModification>() {
                        @Override
                        public Object execute(final Interaction.PropertyModification currentExecution) {

                            try {

                                // update the current execution with the DTO (memento)
                                final PropertyEditDto editDto =
                                        getInteractionDtoServiceInternal().asPropertyEditDto(
                                                owningProperty, targetAdapter, newValueAdapter);
                                currentExecution.setDto(editDto);


                                // ... post the executing event
                                final Object oldValue = getterFacet.getProperty(targetAdapter, interactionInitiatedBy);
                                final Object newValue = ObjectAdapter.Util.unwrap(newValueAdapter);

                                final PropertyDomainEvent<?, ?> event =
                                        domainEventHelper.postEventForProperty(
                                                AbstractDomainEvent.Phase.EXECUTING,
                                                eventType(), null,
                                                getIdentified(), targetAdapter,
                                                oldValue, newValue);


                                // set event onto the execution
                                currentExecution.setEvent(event);

                                // invoke method
                                style.invoke(PropertySetterOrClearFacetForDomainEventAbstract.this, owningProperty,
                                        targetAdapter, newValueAdapter, interactionInitiatedBy);



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

                                return null;

                                //
                                // REVIEW: the corresponding action has a whole bunch of error handling here.
                                // we probably should do something similar...
                                //

                            } finally {

                            }
                        }
                    };

            // sets up startedAt and completedAt on the execution, also manages the execution call graph
            interaction.execute(executor, execution, getClockService(), command);

            // handle any exceptions
            final Interaction.Execution priorExecution = interaction.getPriorExecution();

            // TODO: should also sync DTO's threw here...

            final Exception executionExceptionIfAny = priorExecution.getThrew();
            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny instanceof RuntimeException
                        ? ((RuntimeException)executionExceptionIfAny)
                        : new RuntimeException(executionExceptionIfAny);
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


    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return lookupService(InteractionDtoServiceInternal.class);
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
