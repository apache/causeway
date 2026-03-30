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

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record PropertyModifier(
	    ExecutionContext executionContext,
	    FacetHolder facetHolder,
	    ModificationVariant executionVariant,
	    InteractionInitiatedBy interactionInitiatedBy,
	    InteractionHead head,
	    OneToOneAssociation owningProperty,
	    ManagedObject newValue,
	    PropertyOrCollectionAccessorFacet getterFacet,
	    PropertySetterFacet setterFacet,
	    PropertyModifyFacet propertySetterOrClearFacetForDomainEventAbstract)
implements
    HasMetaModelContext,
    InteractionInternal.MemberExecutor<PropertyEdit> {

    // -- FACTORIES

	@Deprecated
    public static PropertyModifier forPropertyClear(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            // property specifics
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull PropertyOrCollectionAccessorFacet getterFacet,
            final @NonNull PropertySetterFacet setterFacet,
            final @NonNull PropertyModifyFacet propertySetterOrClearFacetForDomainEventAbstract) {
        var emptyValueAdapter = ManagedObject.empty(owningProperty.getElementType());
        return new PropertyModifier(
        		facetHolder.lookupServiceElseFail(ExecutionContext.class), facetHolder,
                ModificationVariant.CLEAR, interactionInitiatedBy, head,
                owningProperty, emptyValueAdapter, getterFacet, setterFacet,
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
            final @NonNull PropertyModifyFacet propertySetterOrClearFacetForDomainEventAbstract) {
        return new PropertyModifier(
        		facetHolder.lookupServiceElseFail(ExecutionContext.class), facetHolder,
                ModificationVariant.SET, interactionInitiatedBy, head,
                owningProperty, newValueAdapter, getterFacet, setterFacet,
                propertySetterOrClearFacetForDomainEventAbstract);
    }

    // -- ENUMS

    public enum ModificationVariant {
        /** clearing a property */
        CLEAR,
        /** setting a property (to a new value) */
        SET;
        public boolean isClear() { return this == CLEAR; }
        public boolean isSet() { return this == SET; }
    }

    @Override public MetaModelContext getMetaModelContext() { return facetHolder.getMetaModelContext(); }

    @Override
    public Object execute(final PropertyEdit currentExecution) {
    	
        // update the current execution with the DTO (memento)
        //
        // but ... no point in attempting this if no bookmark is yet available.
        // One way this might occur is if using excel module to populate an entity representing each line of the spreadsheet;
        // but the entity will be transient at the point.  But there's probably very little value in creating DTOs in such a scenario.
        //
        var ownerAdapter = head.owner();
        var ownerHasBookmark = ManagedObjects.bookmark(ownerAdapter).isPresent();

        if (ownerHasBookmark) {
            var propertyEditDto = executionContext.interactionDtoFactory()
            		.asPropertyEditDto(owningProperty, head, newValue);
            currentExecution.setDto(propertyEditDto);
        }

        if(!isPostable()) {
            // don't emit domain events
            executeClearOrSetWithoutEvents(newValue);
            return head.target().getPojo();
        }

        // ... post the executing event
        var oldValuePojo = getterFacet.getAssociationValueAsPojo(head.target(), interactionInitiatedBy);
        var newValuePojo = MmUnwrapUtils.single(newValue);

        var propertyDomainEvent = executionContext.domainEventHelper()
        		.postEventForProperty(
                    AbstractDomainEvent.Phase.EXECUTING,
                    getEventType(), null,
                    propertySetterOrClearFacetForDomainEventAbstract.facetHolder(), head,
                    oldValuePojo, newValuePojo);

        var newValuePojoPossiblyUpdated = propertyDomainEvent.getNewValue();
        var isValueModifiedByEvent = !Objects.equals(newValuePojoPossiblyUpdated, newValuePojo);

        final ManagedObject newValueAfterEventPolling =
                isValueModifiedByEvent
                        // properly handles the pojo==null case
                        ? ManagedObject.adaptSingular(newValue.objSpec(), newValuePojoPossiblyUpdated)
                        : newValue;

        // set event onto the execution
        currentExecution.setEvent(propertyDomainEvent);

        // invoke method
        executeClearOrSetWithoutEvents(newValueAfterEventPolling);

        // reading the actual value from the target object, playing it safe...
        var actualNewValue = getterFacet.getAssociationValueAsPojo(head.target(), interactionInitiatedBy);
        if (!Objects.equals(oldValuePojo, actualNewValue)) {

            // ... post the executed event
        	executionContext.domainEventHelper()
        		.postEventForProperty(
                    AbstractDomainEvent.Phase.EXECUTED,
                    getEventType(),
                    uncheckedCast(propertyDomainEvent),
                    propertySetterOrClearFacetForDomainEventAbstract.facetHolder(), head,
                    oldValuePojo, actualNewValue);
        }

        // with action invocations, we inject services in the returned pojo at this point.
        // for property sets, though, there's no need, as we're just returning the targetPojo itself
        return head.target().getPojo();

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
        	setterFacet.clearProperty(
                    owningProperty, head.target(), interactionInitiatedBy);
        } else {
            setterFacet.setProperty(
                    owningProperty, head.target(), newValue, interactionInitiatedBy);
        }
    }

    // -- HELPER

    private boolean isPostable() {
    	return propertySetterOrClearFacetForDomainEventAbstract.isPostable();
    }
    
    private final <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return uncheckedCast(propertySetterOrClearFacetForDomainEventAbstract.getEventType());
    }

}
