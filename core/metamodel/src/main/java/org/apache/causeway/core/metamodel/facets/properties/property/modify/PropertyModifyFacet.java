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

import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.execution.PropertyModifier;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Handles modifications for (non-mixed-in) properties and accompanied {@link PropertyDomainEvent}(s).
 */
public final class PropertyModifyFacet
extends DomainEventFacetAbstract<PropertyDomainEvent<?, ?>>
implements
	PropertySetterFacet {

    // -- CONSTRUCTION

    private final @NonNull PropertyOrCollectionAccessorFacet getterFacet;
    private final @NonNull PropertySetterFacet setterFacet;
    private final @NonNull MemberExecutorService memberExecutorService;

    public PropertyModifyFacet(
            final Class<? extends Facet> facetType,
            final DomainEventHolder<PropertyDomainEvent<?, ?>> domainEventHolder,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final FacetHolder holder) {

        super(facetType, domainEventHolder, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.memberExecutorService = getServiceRegistry().lookupServiceElseFail(MemberExecutorService.class);
    }

    @Override
    public ManagedObject clearProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var emptyValueAdapter = ManagedObject.empty(owningProperty.getElementType());
        return setProperty(owningProperty, targetAdapter, emptyValueAdapter, interactionInitiatedBy);
    }

    @Override
    public ManagedObject setProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var modifier = PropertyModifier.forProperty(
        		facetHolder(),
        		interactionInitiatedBy, InteractionHead.regular(targetAdapter), owningProperty,
        		newValueAdapter, getterFacet, setterFacet, this);
        return memberExecutorService
        		.setOrClearProperty(modifier);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("getterFacet", getterFacet);
        visitor.accept("setterFacet", setterFacet);
    }

}
