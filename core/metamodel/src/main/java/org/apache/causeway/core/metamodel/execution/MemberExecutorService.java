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

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacetAbstract;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

/**
 * Used by ActionInvocationFacets and PropertySetterOrClearFacets to submit their executions.
 * <p>
 * That is, invoke a domain action or edit a domain property.
 *
 * @since 2.0
 */
public interface MemberExecutorService {

    /**
     * Optionally, the currently active {@link InteractionInternal} for the calling thread.
     */
    Optional<InteractionInternal> getInteraction();

    ManagedObject invokeAction(
            @NonNull ActionExecutor actionExecutor);

    ManagedObject setOrClearProperty(
            @NonNull PropertyModifier propertyExecutor);

    // -- SHORTCUTS

    default InteractionInternal getInteractionIfAny() {
        return getInteraction().orElse(null);
    }

    default InteractionInternal getInteractionElseFail() {
        return getInteraction().orElseThrow(()->_Exceptions
                .unrecoverable("needs an InteractionSession on current thread"));
    }

    default ManagedObject invokeAction(
            @NonNull final FacetHolder facetHolder,
            @NonNull final InteractionInitiatedBy interactionInitiatedBy,
            @NonNull final InteractionHead head,
            // action specifics
            @NonNull final Can<ManagedObject> argumentAdapters,
            @NonNull final ObjectAction owningAction,
            @NonNull final ActionInvocationFacetAbstract actionInvocationFacetAbstract) {
        var actionExecutor = ActionExecutor.forAction(
                facetHolder,
                interactionInitiatedBy,
                head,
                argumentAdapters,
                owningAction,
                actionInvocationFacetAbstract);
        return invokeAction(actionExecutor);
    }

    default ManagedObject clearProperty(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            // property specifics
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull PropertyOrCollectionAccessorFacet getterFacet,
            final @NonNull PropertyClearFacet clearFacet,
            final @NonNull PropertyModifyFacetAbstract propertySetterOrClearFacetForDomainEventAbstract) {

        var propertyExecutor = PropertyModifier.forPropertyClear(
                facetHolder, interactionInitiatedBy, head,
                owningProperty, getterFacet, clearFacet,
                propertySetterOrClearFacetForDomainEventAbstract);
        return setOrClearProperty(propertyExecutor);
    }

    default ManagedObject setProperty(
            final @NonNull FacetHolder facetHolder,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy,
            final @NonNull InteractionHead head,
            // property specifics
            final @NonNull ManagedObject newValueAdapter,
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull PropertyOrCollectionAccessorFacet getterFacet,
            final @NonNull PropertySetterFacet setterFacet,
            final @NonNull PropertyModifyFacetAbstract propertySetterOrClearFacetForDomainEventAbstract) {

        var propertyExecutor = PropertyModifier.forPropertySet(facetHolder,
                interactionInitiatedBy, head, newValueAdapter,
                owningProperty, getterFacet, setterFacet,
                propertySetterOrClearFacetForDomainEventAbstract);

        return setOrClearProperty(propertyExecutor);
    }

}
