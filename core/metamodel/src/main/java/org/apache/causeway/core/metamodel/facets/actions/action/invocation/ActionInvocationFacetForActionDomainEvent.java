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
package org.apache.causeway.core.metamodel.facets.actions.action.invocation;

import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;

/**
 * Handles both, regular and mixed-in actions,
 * but not mix-in properties nor mix-in collections.
 * (that's for the sibling facet {@link ActionInvocationFacetForPropertyOrCollectionDomainEvent})
 */
public class ActionInvocationFacetForActionDomainEvent
extends ActionInvocationFacetAbstract {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    @Getter(onMethod_ = {@Override}) private final ObjectSpecification declaringType;
    @Getter(onMethod_ = {@Override}) private final ObjectSpecification returnType;
    private final MemberExecutorService memberExecutorService;

    public ActionInvocationFacetForActionDomainEvent(
            final DomainEventHolder<ActionDomainEvent<?>> domainEventHolder,
            final MethodFacade method,
            final ObjectSpecification declaringType,
            final ObjectSpecification returnType,
            final FacetHolder holder) {

        super(domainEventHolder, holder);
        this.methods = ImperativeFacet.singleMethod(method);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.memberExecutorService = getServiceRegistry().lookupServiceElseFail(MemberExecutorService.class);
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return memberExecutorService.invokeAction(getFacetHolder(), interactionInitiatedBy,
                head, argumentAdapters, owningAction, this);
    }

}
