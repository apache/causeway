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

import java.util.Map;
import java.util.Objects;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.val;

public abstract class PropertySetterOrClearFacetForDomainEventAbstract
extends SingleValueFacetAbstract<Class<? extends PropertyDomainEvent<?,?>>> {

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyClearFacet clearFacet;

    public PropertySetterOrClearFacetForDomainEventAbstract(
            final Class<? extends Facet> facetType,
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
                    final PropertyOrCollectionAccessorFacet getterFacet,
                    final PropertySetterFacet setterFacet,
                    final PropertyClearFacet clearFacet,
                    final PropertyDomainEventFacetAbstract propertyDomainEventFacet,
                    final FacetHolder holder) {

        super(facetType, eventType, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.clearFacet = clearFacet;
        this.domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
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
                    final ManagedObject targetAdapter,
                    final ManagedObject valueAdapterOrNull,
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
                    final ManagedObject targetAdapter,
                    final ManagedObject valueAdapterOrNull,
                    final InteractionInitiatedBy interactionInitiatedBy) {

                facet.clearFacet.clearProperty(
                        owningProperty, targetAdapter, interactionInitiatedBy);

            }
        };

        abstract boolean hasCorrespondingFacet(final PropertySetterOrClearFacetForDomainEventAbstract facet);

        abstract void invoke(
                final PropertySetterOrClearFacetForDomainEventAbstract facet,
                final OneToOneAssociation owningProperty,
                final ManagedObject targetAdapter,
                final ManagedObject valueAdapterOrNull,
                final InteractionInitiatedBy interactionInitiatedBy);
    }

    public ManagedObject clearProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return setOrClearProperty(Style.CLEAR,
                owningProperty, targetAdapter, /*newValueAdapter*/ null, interactionInitiatedBy);

    }

    public ManagedObject setProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return setOrClearProperty(Style.SET,
                owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);

    }

    private ManagedObject setOrClearProperty(
            final Style style,
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return getTransactionService().executeWithinTransaction(() ->
                doSetOrClearProperty(style, owningProperty, InteractionHead.simple(targetAdapter), newValueAdapter, interactionInitiatedBy));
        }

    private ManagedObject doSetOrClearProperty(
            final Style style,
            final OneToOneAssociation owningProperty,
            final InteractionHead head,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        if(!style.hasCorrespondingFacet(this)) {
            return head.getTarget();
        }

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();
        if( command==null ) {
            return head.getTarget();
        }

        final InteractionContext interactionContext = getInteractionContext();
        final Interaction interaction = interactionContext.getInteraction();

        final String propertyId = owningProperty.getIdentifier().toClassAndNameIdentityString();

        if( command.getExecutor() == Command.Executor.USER
                && command.getExecuteIn() == org.apache.isis.applib.annotation.CommandExecuteIn.BACKGROUND) {

            // deal with background commands

            // persist command so can it can subsequently be invoked in the 'background'
            final CommandService commandService = getCommandService();
            if (!commandService.persistIfPossible(command)) {
                throw new IsisException(String.format(
                        "Unable to persist command for property '%s'; CommandService does not support persistent commands ",
                        propertyId));
            }

            return head.getTarget();

        } else {

            val targetAdapter = head.getTarget();
            final Object target = UnwrapUtil.single(targetAdapter);
            final Object argValue = UnwrapUtil.single(newValueAdapter);

            final String targetMember = CommandUtil.targetMemberNameFor(owningProperty);
            final String targetClass = CommandUtil.targetClassNameFor(targetAdapter);

            final Interaction.PropertyEdit execution =
                    new Interaction.PropertyEdit(interaction, propertyId, target, argValue, targetMember, targetClass);
            final Interaction.MemberExecutor<Interaction.PropertyEdit> executor =
                    new Interaction.MemberExecutor<Interaction.PropertyEdit>() {
                @Override
                public Object execute(final Interaction.PropertyEdit currentExecution) {

                    // TODO: REVIEW - is this safe to do?
                    ManagedObject newValueAdapterMutatable = newValueAdapter;

                    try {

                        // update the current execution with the DTO (memento)
                        final PropertyEditDto editDto =
                                getInteractionDtoServiceInternal().asPropertyEditDto(
                                        owningProperty, targetAdapter, newValueAdapterMutatable);
                        currentExecution.setDto(editDto);


                        // set the startedAt (and update command if this is the top-most member execution)
                        // (this isn't done within Interaction#execute(...) because it requires the DTO
                        // to have been set on the current execution).
                        val startedAt = execution.start(getClockService(), getMetricsService());
                        if(command.getStartedAt() == null) {
                            command.internal().setStartedAt(startedAt);
                        }

                        // ... post the executing event
                        final Object oldValue = getterFacet.getProperty(targetAdapter, interactionInitiatedBy);
                        final Object newValue = UnwrapUtil.single(newValueAdapterMutatable);

                        final PropertyDomainEvent<?, ?> event =
                                domainEventHelper.postEventForProperty(
                                        AbstractDomainEvent.Phase.EXECUTING,
                                        getEventType(), null,
                                        getIdentified(), head,
                                        oldValue, newValue);

                        Object newValuePossiblyUpdated = event.getNewValue();
                        if(!Objects.equals(newValuePossiblyUpdated, newValue)) {
                            newValueAdapterMutatable = newValuePossiblyUpdated != null
                                    ? getObjectManager().adapt(newValuePossiblyUpdated)
                                    : null;
                        }

                        // set event onto the execution
                        currentExecution.setEvent(event);

                        // invoke method
                        style.invoke(PropertySetterOrClearFacetForDomainEventAbstract.this, owningProperty,
                                targetAdapter, newValueAdapterMutatable, interactionInitiatedBy);


                        // reading the actual value from the target object, playing it safe...
                        final Object actualNewValue = getterFacet.getProperty(targetAdapter, interactionInitiatedBy);
                        if (!Objects.equals(oldValue, actualNewValue)) {

                            // ... post the executed event
                            domainEventHelper.postEventForProperty(
                                    AbstractDomainEvent.Phase.EXECUTED,
                                    getEventType(), uncheckedCast(event),
                                    getIdentified(), head,
                                    oldValue, actualNewValue);
                        }

                        ManagedObject targetAdapterPossiblyCloned =
                                cloneIfViewModelCloneable(targetAdapter);

                        return targetAdapterPossiblyCloned.getPojo();

                        //
                        // REVIEW: the corresponding action has a whole bunch of error handling here.
                        // we probably should do something similar...
                        //

                    } finally {

                    }
                }
            };

            // sets up startedAt and completedAt on the execution, also manages the execution call graph
            Object targetPojo = interaction.execute(executor, execution, getClockService(), getMetricsService());

            // handle any exceptions
            final Interaction.Execution<?, ?> priorExecution = interaction.getPriorExecution();

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

            return getObjectManager().adapt(targetPojo);
        }
    }

    private ManagedObject cloneIfViewModelCloneable(final ManagedObject adapter) {

        if (!adapter.getSpecification().isViewModelCloneable(adapter)) {
            return adapter;
        }

        final ViewModelFacet viewModelFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        final Object clone = viewModelFacet.clone(adapter.getPojo());

        final ManagedObject clonedAdapter = getObjectManager().adapt(clone);
        return clonedAdapter;
    }

    public <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return uncheckedCast(value());
    }

    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(InteractionDtoServiceInternal.class);
    }

    private CommandContext getCommandContext() {
        return getServiceRegistry().lookupServiceElseFail(CommandContext.class);
    }

    private InteractionContext getInteractionContext() {
        return getServiceRegistry().lookupServiceElseFail(InteractionContext.class);
    }

    private CommandService getCommandService() {
        return getServiceRegistry().lookupServiceElseFail(CommandService.class);
    }

    private ClockService getClockService() {
        return getServiceRegistry().lookupServiceElseFail(ClockService.class);
    }

    private MetricsService getMetricsService() {
        return getServiceRegistry().lookupServiceElseFail(MetricsService.class);
    }

    private PublisherDispatchService getPublishingServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(PublisherDispatchService.class);
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("getterFacet", getterFacet);
        attributeMap.put("setterFacet", setterFacet);
        attributeMap.put("clearFacet", clearFacet);
    }
}
