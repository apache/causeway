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
package org.apache.isis.core.metamodel.execution;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.isis.applib.services.iactn.Interaction.ActionInvocation;
import org.apache.isis.applib.services.iactn.Interaction.PropertyEdit;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterOrClearFacetForDomainEventAbstract.EditingVariant;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

/**
 * Used by ActionInvocationFacets and PropertySetterOrClearFacets to submit their executions.
 * <p>
 * That is, invoke a domain action or edit a domain property.
 * 
 * @since 2.0
 */
public interface MemberExecutorService {
    
    @Deprecated // just a refactoring step
    @FunctionalInterface
    interface ActionExecutorFactory {
        InternalInteraction.MemberExecutor<ActionInvocation> createExecutor(
                Can<ManagedObject> argumentAdapters,
                ManagedObject targetAdapter,
                ObjectAction owningAction,
                ManagedObject mixinElseRegularAdapter,
                ManagedObject mixedInAdapter);
    }
    
    @Deprecated // just a refactoring step
    @FunctionalInterface
    interface PropertyExecutorFactory {
        InternalInteraction.MemberExecutor<PropertyEdit> createExecutor(
                ManagedObject newValueAdapter,
                OneToOneAssociation owningProperty,
                ManagedObject targetManagedObject,
                InteractionInitiatedBy interactionInitiatedBy,
                InteractionHead head,
                EditingVariant editingVariant);
    }
    
    /**
     * Optionally, the currently active {@link InternalInteraction} for the calling thread.
     */
    Optional<InternalInteraction> getInteraction();

    // -- SHORTCUTS
    
    default InternalInteraction getInteractionIfAny() {
        return getInteraction().orElse(null);
    }
    
    default InternalInteraction getInteractionElseFail() {
        return getInteraction().orElseThrow(()->_Exceptions
                .unrecoverable("needs an InteractionSession on current thread"));
    }
    
    // -- REFACTORING

    //TODO implementations of this service should also handle domain object events, don't delegate this responsibility to facets
    ManagedObject invokeAction(
            @NonNull ObjectAction owningAction, 
            @NonNull InteractionHead head,
            @NonNull Can<ManagedObject> argumentAdapters, 
            @NonNull InteractionInitiatedBy interactionInitiatedBy,
            @NonNull Method method,
            @NonNull ActionExecutorFactory actionExecutorFactory, 
            @NonNull FacetHolder facetHolder, 
            @NonNull IdentifiedHolder identifiedHolder);

    //TODO implementations of this service should also handle domain object events, don't delegate this responsibility to facets
    ManagedObject setOrClearProperty(
            @NonNull OneToOneAssociation owningProperty, 
            @NonNull InteractionHead head, 
            @NonNull ManagedObject newValueAdapter,
            @NonNull InteractionInitiatedBy interactionInitiatedBy,
            @NonNull PropertyExecutorFactory propertyExecutorFactory,
            @NonNull FacetHolder facetHolder,
            @NonNull IdentifiedHolder identifiedHolder,
            @NonNull EditingVariant editingVariant);
         
}
