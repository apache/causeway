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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearingAccessor;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySettingAccessor;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

/**
 * Handles modifications for (non-mixed-in) properties and accompanied {@link PropertyDomainEvent}(s).
 */
public abstract class PropertyModifyFacetAbstract
extends DomainEventFacetAbstract<PropertyDomainEvent<?, ?>>
implements
    PropertyClearingAccessor,
    PropertySettingAccessor {

    // -- CONSTRUCTION

    private final @NonNull PropertyOrCollectionAccessorFacet getterFacet;
    private final @Nullable PropertySetterFacet setterFacet; // either this
    private final @Nullable PropertyClearFacet clearFacet; // or that
    private final @NonNull MemberExecutorService memberExecutorService;

    protected PropertyModifyFacetAbstract(
            final Class<? extends Facet> facetType,
            final DomainEventHolder<PropertyDomainEvent<?, ?>> domainEventHolder,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final PropertyClearFacet clearFacet,
            final FacetHolder holder) {

        super(facetType, domainEventHolder, holder);
        this.getterFacet = getterFacet;
        this.setterFacet = setterFacet;
        this.clearFacet = clearFacet;
        this.memberExecutorService = getServiceRegistry().lookupServiceElseFail(MemberExecutorService.class);
    }

    @Override
    public ManagedObject clearProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionHead head = InteractionHead.regular(targetAdapter);

        return memberExecutorService.clearProperty(getFacetHolder(), interactionInitiatedBy,
                head, owningProperty, getterFacet, clearFacet, this);
    }

    @Override
    public ManagedObject setProperty(
            final OneToOneAssociation owningProperty,
            final ManagedObject targetAdapter,
            final ManagedObject newValueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionHead head = InteractionHead.regular(targetAdapter);

        return memberExecutorService.setProperty(getFacetHolder(), interactionInitiatedBy,
                head, newValueAdapter, owningProperty, getterFacet, setterFacet, this);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("getterFacet", getterFacet);
        if(setterFacet!=null) {
            visitor.accept("setterFacet", setterFacet);
        }
        if(clearFacet!=null) {
            visitor.accept("clearFacet", clearFacet);
        }
    }

}
