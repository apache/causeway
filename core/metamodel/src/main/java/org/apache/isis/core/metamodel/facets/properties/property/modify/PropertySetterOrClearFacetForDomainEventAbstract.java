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

import java.sql.Timestamp;

import com.google.common.base.Objects;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;

public abstract class PropertySetterOrClearFacetForDomainEventAbstract
extends SingleValueFacetAbstract<Class<? extends PropertyDomainEvent<?,?>>> {

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyClearFacet clearFacet;
    private final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

    private final ServicesInjector servicesInjector;
    private final PersistenceSessionServiceInternal persistenceSessionServiceInternal;


    public PropertySetterOrClearFacetForDomainEventAbstract(
            final Class<? extends Facet> facetType,
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
                    final PropertyOrCollectionAccessorFacet getterFacet,
                    final PropertySetterFacet setterFacet,
                    final PropertyClearFacet clearFacet,
                    final PropertyDomainEventFacetAbstract propertyDomainEventFacet,
                    final ServicesInjector servicesInjector,
                    final FacetHolder holder) {
        super(facetType, eventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.clearFacet = clearFacet;
        this.propertyDomainEventFacet = propertyDomainEventFacet;
        this.persistenceSessionServiceInternal = servicesInjector.getPersistenceSessionServiceInternal();
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

        getPersistenceSessionServiceInternal().executeWithinTransaction(()->{
            doSetOrClearProperty(style, owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);
        });

    }

    private void doSetOrClearProperty(
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

        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();

        final String propertyId = owningProperty.getIdentifier().toClassAndNameIdentityString();

        if( command.getExecutor() == Command.Executor.USER &&
                command.getExecuteIn() == org.apache.isis.applib.annotation.CommandExecuteIn.BACKGROUND) {

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

            final String targetMember = CommandUtil.targetMemberNameFor(owningProperty);
            final String targetClass = CommandUtil.targetClassNameFor(targetAdapter);

            final Interaction.PropertyEdit execution =
                    new Interaction.PropertyEdit(interaction, propertyId, target, argValue, targetMember, targetClass);
            final Interaction.MemberExecutor<Interaction.PropertyEdit> executor =
                    new Interaction.MemberExecutor<Interaction.PropertyEdit>() {
                @Override
                public Object execute(final Interaction.PropertyEdit currentExecution) {

                    try {

                        // update the current execution with the DTO (memento)
                        final PropertyEditDto editDto =
                                getInteractionDtoServiceInternal().asPropertyEditDto(
                                        owningProperty, targetAdapter, newValueAdapter);
                        currentExecution.setDto(editDto);


                        // set the startedAt (and update command if this is the top-most member execution)
                        // (this isn't done within Interaction#execute(...) because it requires the DTO
                        // to have been set on the current execution).
                        final Timestamp startedAt = getClockService().nowAsJavaSqlTimestamp();
                        execution.setStartedAt(startedAt);
                        if(command.getStartedAt() == null) {
                            command.setStartedAt(startedAt);
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
            interaction.execute(executor, execution);

            // handle any exceptions
            final Interaction.Execution priorExecution = interaction.getPriorExecution();

            // TODO: should also sync DTO's 'threw' attribute here...?

            final Exception executionExceptionIfAny = priorExecution.getThrew();
            if(executionExceptionIfAny != null) {
                throw executionExceptionIfAny instanceof RuntimeException
                ? ((RuntimeException)executionExceptionIfAny)
                        : new RuntimeException(executionExceptionIfAny);
            }


            // publish (if not a contributed association, query-only mixin)
            final PublishedPropertyFacet publishedPropertyFacet = getIdentified().getFacet(PublishedPropertyFacet.class);
            if (publishedPropertyFacet != null) {
                getPublishingServiceInternal().publishProperty(priorExecution);
            }

        }
    }

    private Class<? extends PropertyDomainEvent<?, ?>> eventType() {
        return value();
    }

    /**
     * Optional hook to allow the facet implementation for now-deleted annotations to discard an event if incompatible.
     * Now in effect redundant.
     */
    protected PropertyDomainEvent<?, ?> verify(PropertyDomainEvent<?, ?> event) {
        return event;
    }


    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return servicesInjector.lookupServiceElseFail(InteractionDtoServiceInternal.class);
    }

    private CommandContext getCommandContext() {
        return servicesInjector.lookupServiceElseFail(CommandContext.class);
    }

    private InteractionContext getInteractionContext() {
        return servicesInjector.lookupServiceElseFail(InteractionContext.class);
    }

    private CommandService getCommandService() {
        return servicesInjector.lookupServiceElseFail(CommandService.class);
    }

    private ClockService getClockService() {
        return servicesInjector.lookupServiceElseFail(ClockService.class);
    }

    private PublishingServiceInternal getPublishingServiceInternal() {
        return servicesInjector.lookupServiceElseFail(PublishingServiceInternal.class);
    }

    private PersistenceSessionServiceInternal getPersistenceSessionServiceInternal() {
        return persistenceSessionServiceInternal;
    }


}
