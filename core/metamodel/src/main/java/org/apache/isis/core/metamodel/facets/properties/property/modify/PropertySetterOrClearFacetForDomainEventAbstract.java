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
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.publish.ExecutionDispatchPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.ExecutionDispatcher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.RequiredArgsConstructor;
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

    @RequiredArgsConstructor
    private final class DomainEventMemberExecutor
            implements Interaction.MemberExecutor<Interaction.PropertyEdit> {

        private final ManagedObject newValueAdapter;
        private final OneToOneAssociation owningProperty;
        private final ManagedObject targetManagedObject;
        private final Interaction.PropertyEdit propertyEdit;
        private final Command command;
        private final InteractionInitiatedBy interactionInitiatedBy;
        private final InteractionHead head;
        private final Style style;

        public Object execute(Interaction.PropertyEdit currentExecution) {

            // TODO: REVIEW - is this safe to do?
            ManagedObject newValueAdapterMutatable = newValueAdapter;

            try {

                // update the current execution with the DTO (memento)
                val propertyEditDto =
                        PropertySetterOrClearFacetForDomainEventAbstract.this.getInteractionDtoServiceInternal().asPropertyEditDto(
                                owningProperty, targetManagedObject, newValueAdapterMutatable);
                currentExecution.setDto(propertyEditDto);

                // ... post the executing event
                val oldValuePojo = getterFacet.getProperty(targetManagedObject, interactionInitiatedBy);
                val newValuePojo = UnwrapUtil.single(newValueAdapterMutatable);

                val propertyDomainEvent =
                        domainEventHelper.postEventForProperty(
                                AbstractDomainEvent.Phase.EXECUTING,
                                PropertySetterOrClearFacetForDomainEventAbstract.this.getEventType(), null,
                                PropertySetterOrClearFacetForDomainEventAbstract.this.getIdentified(), head,
                                oldValuePojo, newValuePojo);

                val newValuePojoPossiblyUpdated = propertyDomainEvent.getNewValue();
                if (!Objects.equals(newValuePojoPossiblyUpdated, newValuePojo)) {
                    newValueAdapterMutatable = newValuePojoPossiblyUpdated != null
                            ? PropertySetterOrClearFacetForDomainEventAbstract.this.getObjectManager().adapt(newValuePojoPossiblyUpdated)
                            : null;
                }

                // set event onto the execution
                currentExecution.setEvent(propertyDomainEvent);

                // invoke method
                style.invoke(PropertySetterOrClearFacetForDomainEventAbstract.this, owningProperty,
                        targetManagedObject, newValueAdapterMutatable, interactionInitiatedBy);


                // reading the actual value from the target object, playing it safe...
                val actualNewValue = getterFacet.getProperty(targetManagedObject, interactionInitiatedBy);
                if (!Objects.equals(oldValuePojo, actualNewValue)) {

                    // ... post the executed event
                    domainEventHelper.postEventForProperty(
                            AbstractDomainEvent.Phase.EXECUTED,
                            PropertySetterOrClearFacetForDomainEventAbstract.this.getEventType(), uncheckedCast(propertyDomainEvent),
                            PropertySetterOrClearFacetForDomainEventAbstract.this.getIdentified(), head,
                            oldValuePojo, actualNewValue);
                }

                val targetManagedObjectPossiblyCloned =
                        PropertySetterOrClearFacetForDomainEventAbstract.this.cloneIfViewModelCloneable(targetManagedObject);

                return targetManagedObjectPossiblyCloned.getPojo();

                //
                // REVIEW: the corresponding action has a whole bunch of error handling here.
                // we probably should do something similar...
                //

            } finally {

            }
        }

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

        val interactionContext = getInteractionContext();
        val interaction = interactionContext.getInteractionElseFail();
        val command = interaction.getCommand();
        if( command==null ) {
            return head.getTarget();
        }
        
        command.updater().setDispatchingEnabled(
                CommandFacet.isDispatchingEnabled(getFacetHolder()));

        val propertyId = owningProperty.getIdentifier().toClassAndNameIdentityString();

        val targetManagedObject = head.getTarget();
        val target = UnwrapUtil.single(targetManagedObject);
        val argValue = UnwrapUtil.single(newValueAdapter);

        val targetMemberName = CommandUtil.targetMemberNameFor(owningProperty);
        val targetClass = CommandUtil.targetClassNameFor(targetManagedObject);

        val propertyEdit = new Interaction.PropertyEdit(interaction, propertyId, target, argValue, targetMemberName, targetClass);
        val executor = new DomainEventMemberExecutor(newValueAdapter, owningProperty, targetManagedObject, propertyEdit, command, interactionInitiatedBy, head, style);

        // sets up startedAt and completedAt on the execution, also manages the execution call graph
        val targetPojo = interaction.execute(executor, propertyEdit, getClockService(), getMetricsService(), command);

        // handle any exceptions
        final Interaction.Execution<?, ?> priorExecution = interaction.getPriorExecution();

        // TODO: should also sync DTO's 'threw' attribute here...?

        val executionExceptionIfAny = priorExecution.getThrew();
        if(executionExceptionIfAny != null) {
            throw executionExceptionIfAny instanceof RuntimeException
            ? ((RuntimeException)executionExceptionIfAny)
                    : new RuntimeException(executionExceptionIfAny);
        }

        // publish (if not a contributed association, query-only mixin)
        val publishedPropertyFacet = getIdentified().getFacet(ExecutionDispatchPropertyFacet.class);
        if (publishedPropertyFacet != null) {
            getExecutionDispatcher().dispatchPropertyChangeExecution(priorExecution);
        }

        return getObjectManager().adapt(targetPojo);
    }

    private ManagedObject cloneIfViewModelCloneable(final ManagedObject adapter) {

        if (!adapter.getSpecification().isViewModelCloneable(adapter)) {
            return adapter;
        }

        final ViewModelFacet viewModelFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        final Object clone = viewModelFacet.clone(adapter.getPojo());

        return getObjectManager().adapt(clone);
    }

    public <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return uncheckedCast(value());
    }

    private InteractionDtoServiceInternal getInteractionDtoServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(InteractionDtoServiceInternal.class);
    }

    private InteractionContext getInteractionContext() {
        return getServiceRegistry().lookupServiceElseFail(InteractionContext.class);
    }

    private ClockService getClockService() {
        return getServiceRegistry().lookupServiceElseFail(ClockService.class);
    }

    private MetricsService getMetricsService() {
        return getServiceRegistry().lookupServiceElseFail(MetricsService.class);
    }

    private ExecutionDispatcher getExecutionDispatcher() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionDispatcher.class);
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("getterFacet", getterFacet);
        attributeMap.put("setterFacet", setterFacet);
        attributeMap.put("clearFacet", clearFacet);
    }
}
