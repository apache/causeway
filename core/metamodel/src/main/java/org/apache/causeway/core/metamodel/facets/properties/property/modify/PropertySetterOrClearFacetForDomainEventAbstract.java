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
package org.apache.causeway.core.metamodel.facets.properties.property.modify;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.SingleValueFacetAbstract;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearingAccessor;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySettingAccessor;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public abstract class PropertySetterOrClearFacetForDomainEventAbstract
extends SingleValueFacetAbstract<Class<? extends PropertyDomainEvent<?,?>>>
implements
    PropertyClearingAccessor,
    PropertySettingAccessor {

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet;
    private final PropertyClearFacet clearFacet;

    protected PropertySetterOrClearFacetForDomainEventAbstract(
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

    public enum EditingVariant {
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

    @Override
    public ManagedObject clearProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val emptyValueAdapter = ManagedObject.empty(owningProperty.getElementType());

        return setOrClearProperty(EditingVariant.CLEAR,
                owningProperty, targetAdapter, emptyValueAdapter, interactionInitiatedBy);

    }

    @Override
    public ManagedObject setProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return setOrClearProperty(EditingVariant.SET,
                owningProperty, targetAdapter, newValueAdapter, interactionInitiatedBy);

    }

    private ManagedObject setOrClearProperty(
            final @NonNull EditingVariant style,
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull ManagedObject targetAdapter,
            final @NonNull ManagedObject newValueAdapter,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy) {

        return getTransactionService()
                .callWithinCurrentTransactionElseCreateNew(() ->
                    doSetOrClearProperty(
                            style,
                            owningProperty,
                            InteractionHead.regular(targetAdapter),
                            newValueAdapter,
                            interactionInitiatedBy))
                .ifFailureFail()
                .getValue().orElse(null);
        }

    @RequiredArgsConstructor
    private final class DomainEventMemberExecutor
            implements InteractionInternal.MemberExecutor<PropertyEdit> {

        private final OneToOneAssociation owningProperty;
        private final InteractionHead head;
        private final ManagedObject newValueAdapter;
        private final InteractionInitiatedBy interactionInitiatedBy;
        private final EditingVariant style;

        @Override
        public Object execute(final PropertyEdit currentExecution) {

            // TODO: REVIEW - is this safe to do?
            ManagedObject newValueAdapterMutatable = newValueAdapter;

            try {

                // update the current execution with the DTO (memento)
                val propertyEditDto =
                        PropertySetterOrClearFacetForDomainEventAbstract.this.getInteractionDtoServiceInternal().asPropertyEditDto(
                                owningProperty, head.getOwner(), newValueAdapterMutatable, head);
                currentExecution.setDto(propertyEditDto);

                // ... post the executing event
                val oldValuePojo = getterFacet.getProperty(head.getTarget(), interactionInitiatedBy);
                val newValuePojo = MmUnwrapUtil.single(newValueAdapterMutatable);

                val propertyDomainEvent =
                        domainEventHelper.postEventForProperty(
                                AbstractDomainEvent.Phase.EXECUTING,
                                PropertySetterOrClearFacetForDomainEventAbstract.this.getEventType(), null,
                                PropertySetterOrClearFacetForDomainEventAbstract.this.getFacetHolder(), head,
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
                        head.getTarget(), newValueAdapterMutatable, interactionInitiatedBy);


                // reading the actual value from the target object, playing it safe...
                val actualNewValue = getterFacet.getProperty(head.getTarget(), interactionInitiatedBy);
                if (!Objects.equals(oldValuePojo, actualNewValue)) {

                    // ... post the executed event
                    domainEventHelper.postEventForProperty(
                            AbstractDomainEvent.Phase.EXECUTED,
                            PropertySetterOrClearFacetForDomainEventAbstract.this.getEventType(), uncheckedCast(propertyDomainEvent),
                            PropertySetterOrClearFacetForDomainEventAbstract.this.getFacetHolder(), head,
                            oldValuePojo, actualNewValue);
                }

                // with action invocations, we inject services in the returned pojo at this point.
                // for property sets, though, there's no need, as we're just returning the targetPojo itself
                return head.getTarget().getPojo();

                //
                // REVIEW: the corresponding action has a whole bunch of error handling here.
                // we probably should do something similar...
                //

            } finally {

            }
        }

    }

    private ManagedObject doSetOrClearProperty(
            final EditingVariant editingVariant,
            final OneToOneAssociation owningProperty,
            final InteractionHead head,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // similar code in ActionInvocationFacetFDEA

        val domainObject = head.getTarget();

        if(!editingVariant.hasCorrespondingFacet(this)) {
            return domainObject;
        }

        if(interactionInitiatedBy.isPassThrough()) {
            /* directly access property setter to prevent triggering of domain events
             * or change tracking, eg. when called in the context of serialization */
            editingVariant.invoke(this, owningProperty, domainObject, newValueAdapter, interactionInitiatedBy);
            return domainObject;
        }

        return getMemberExecutor().setOrClearProperty(
                owningProperty,
                head,
                newValueAdapter,
                interactionInitiatedBy,
                DomainEventMemberExecutor::new,
                getFacetHolder(),
                editingVariant
                );
    }

    public <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return uncheckedCast(value());
    }

    private InteractionDtoFactory getInteractionDtoServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(InteractionDtoFactory.class);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("getterFacet", getterFacet);
        visitor.accept("setterFacet", setterFacet);
        visitor.accept("clearFacet", clearFacet);
    }
}
