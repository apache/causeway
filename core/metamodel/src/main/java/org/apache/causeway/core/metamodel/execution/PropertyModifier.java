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
package org.apache.causeway.core.metamodel.execution;

import java.util.Objects;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacetAbstract;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public final class PropertyModifier
implements
    HasMetaModelContext,
    InteractionInternal.MemberExecutor<PropertyEdit> {

    // -- FACTORIES

    public static PropertyModifier forPropertyClear(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            // property specifics
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull PropertyOrCollectionAccessorFacet getterFacet,
            final @NonNull PropertyClearFacet clearFacet,
            final @NonNull PropertyModifyFacetAbstract propertySetterOrClearFacetForDomainEventAbstract) {
        var emptyValueAdapter = ManagedObject.empty(owningProperty.getElementType());
        return new PropertyModifier(owningProperty.getMetaModelContext(), facetHolder,
                ModificationVariant.CLEAR, interactionInitiatedBy, head,
                owningProperty, emptyValueAdapter, getterFacet, null, clearFacet,
                propertySetterOrClearFacetForDomainEventAbstract);
    }

    public static PropertyModifier forPropertySet(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            final @NonNull ManagedObject newValueAdapter,
            // property specifics
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull PropertyOrCollectionAccessorFacet getterFacet,
            final @NonNull PropertySetterFacet setterFacet,
            final @NonNull PropertyModifyFacetAbstract propertySetterOrClearFacetForDomainEventAbstract) {
        return new PropertyModifier(owningProperty.getMetaModelContext(), facetHolder,
                ModificationVariant.SET, interactionInitiatedBy, head,
                owningProperty, newValueAdapter, getterFacet, setterFacet, null,
                propertySetterOrClearFacetForDomainEventAbstract);
    }

    // -- ENUMS

    public static enum ModificationVariant {
        /** clearing a property */
        CLEAR,
        /** setting a property (to a new value) */
        SET;
        public boolean isClear() { return this == CLEAR; }
        public boolean isSet() { return this == SET; }
    }

    // -- CONSTRUCTION

    @Getter(onMethod_={@Override})
    private final @NonNull MetaModelContext metaModelContext;

    @Getter
    private final @NonNull FacetHolder facetHolder;
    private final @NonNull ModificationVariant executionVariant;
    @Getter
    private final @NonNull InteractionInitiatedBy interactionInitiatedBy;

    @Getter
    private final @NonNull InteractionHead head;

    @Getter
    private final @NonNull OneToOneAssociation owningProperty;

    @Getter
    private final @NonNull ManagedObject newValue;

    // -- REFACTOR ...
    private final PropertyOrCollectionAccessorFacet getterFacet;
    private final PropertySetterFacet setterFacet; // either this
    private final PropertyClearFacet clearFacet; // or that
    private final PropertyModifyFacetAbstract propertySetterOrClearFacetForDomainEventAbstract;

    @Getter(lazy=true)
    private final InteractionDtoFactory interactionDtoServiceInternal =
        getServiceRegistry().lookupServiceElseFail(InteractionDtoFactory.class);

    @Getter(lazy=true)
    private final DomainEventHelper domainEventHelper =
        DomainEventHelper.ofServiceRegistry(getServiceRegistry());

    private boolean isPostable() {
        return propertySetterOrClearFacetForDomainEventAbstract.isPostable();
    }

    // --

    @Override
    public Object execute(final PropertyEdit currentExecution) {

        // update the current execution with the DTO (memento)
        //
        // but ... no point in attempting this if no bookmark is yet available.
        // One way this might occur is if using excel module to populate an entity representing each line of the spreadsheet;
        // but the entity will be transient at the point.  But there's probably very little value in creating DTOs in such a scenario.
        //
        var ownerAdapter = head.getOwner();
        var ownerHasBookmark = ManagedObjects.bookmark(ownerAdapter).isPresent();

        if (ownerHasBookmark) {
            var propertyEditDto =
                    getInteractionDtoServiceInternal().asPropertyEditDto(owningProperty, head, newValue);
            currentExecution.setDto(propertyEditDto);
        }

        if(!isPostable()) {
            // don't emit domain events
            executeClearOrSetWithoutEvents(newValue);
            return head.getTarget().getPojo();
        }

        // ... post the executing event
        var oldValuePojo = getterFacet.getProperty(head.getTarget(), interactionInitiatedBy);
        var newValuePojo = MmUnwrapUtils.single(newValue);

        var propertyDomainEvent =
                getDomainEventHelper().postEventForProperty(
                        AbstractDomainEvent.Phase.EXECUTING,
                        getEventType(), null,
                        propertySetterOrClearFacetForDomainEventAbstract.getFacetHolder(), head,
                        oldValuePojo, newValuePojo);

        var newValuePojoPossiblyUpdated = propertyDomainEvent.getNewValue();
        var isValueModifiedByEvent = !Objects.equals(newValuePojoPossiblyUpdated, newValuePojo);

        final ManagedObject newValueAfterEventPolling =
                isValueModifiedByEvent
                        // properly handles the pojo==null case
                        ? ManagedObject.adaptSingular(newValue.getSpecification(), newValuePojoPossiblyUpdated)
                        : newValue;

        // set event onto the execution
        currentExecution.setEvent(propertyDomainEvent);

        // invoke method
        executeClearOrSetWithoutEvents(newValueAfterEventPolling);

        // reading the actual value from the target object, playing it safe...
        var actualNewValue = getterFacet.getProperty(head.getTarget(), interactionInitiatedBy);
        if (!Objects.equals(oldValuePojo, actualNewValue)) {

            // ... post the executed event
            getDomainEventHelper().postEventForProperty(
                    AbstractDomainEvent.Phase.EXECUTED,
                    getEventType(),
                    uncheckedCast(propertyDomainEvent),
                    propertySetterOrClearFacetForDomainEventAbstract.getFacetHolder(), head,
                    oldValuePojo, actualNewValue);
        }

        // with action invocations, we inject services in the returned pojo at this point.
        // for property sets, though, there's no need, as we're just returning the targetPojo itself
        return head.getTarget().getPojo();

        //
        // REVIEW: the corresponding action has a whole bunch of error handling here.
        // we probably should do something similar...
        //
    }

    /**
     * Executes the change using underlying setter or getter facets, without triggering any events.
     * <p>
     * @implNote Reassesses whether this is a clear or a set operation, based on actual {@code newValue},
     * which might have been modified during EXECUTING phase (event polling).
     */
    public void executeClearOrSetWithoutEvents(final @NonNull ManagedObject newValue) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(newValue)) {
            clearFacet.clearProperty(
                    owningProperty, head.getTarget(), interactionInitiatedBy);
        } else {
            setterFacet.setProperty(
                    owningProperty, head.getTarget(), newValue, interactionInitiatedBy);
        }
    }

    // -- HELPER

    private final <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return uncheckedCast(propertySetterOrClearFacetForDomainEventAbstract.getEventType());
    }

}
