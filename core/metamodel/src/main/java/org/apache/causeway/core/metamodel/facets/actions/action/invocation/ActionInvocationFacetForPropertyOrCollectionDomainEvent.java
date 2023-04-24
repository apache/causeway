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

import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;

/**
 * Handles mixed-in properties and collections.
 * <p>
 * This facet is expected to be always installed
 * together with a {@link PropertyDomainEventFacet} or {@link CollectionDomainEventFacet}.
 */
public class ActionInvocationFacetForPropertyOrCollectionDomainEvent
extends ActionInvocationFacetAbstract {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    @Getter(onMethod_ = {@Override}) private final ObjectSpecification declaringType;
    @Getter(onMethod_ = {@Override}) private final ObjectSpecification returnType;
    private final ServiceRegistry serviceRegistry;
    private final DomainEventHelper domainEventHelper;

    public ActionInvocationFacetForPropertyOrCollectionDomainEvent(
            final MethodFacade method,
            final ObjectSpecification declaringType,
            final ObjectSpecification returnType,
            final FacetHolder holder) {

        /* DomainEventHolder is empty, because we are using that of the
         * PropertyDomainEventFacet or CollectionDomainEventFacet instead */
        super(DomainEventHolder.empty(), holder);
        this.methods = ImperativeFacet.singleMethod(method);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.serviceRegistry = getServiceRegistry();
        this.domainEventHelper = DomainEventHelper.ofServiceRegistry(serviceRegistry);
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        //FIXME [CAUSEWAY-3409] do the action invocation, but emit property execution events
        return ManagedObject.empty(owningAction.getReturnType());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        //TODO[CAUSEWAY-3409] not sure how to report this hybrid kind of facet
        /*
         *  <mml:facet id="org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet" fqcn="org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForPropertyOrCollectionDomainEvent">
            <mml:attr name="declaringType" value="org.apache.causeway.extensions.pdfjs.metamodel.domains.mixin.SomeViewModel_pdf"/>
            <mml:attr name="eventType" value="org.apache.causeway.applib.events.domain.ActionDomainEvent.Default"/>
            <mml:attr name="eventTypeOrigin" value="DEFAULT"/>
            <mml:attr name="facet" value="ActionInvocationFacetForPropertyOrCollectionDomainEvent"/>
            <mml:attr name="intent.prop" value="EXECUTE"/>
            <mml:attr name="isPostable" value="true"/>
            <mml:attr name="methods" value="public org.apache.causeway.applib.value.Blob org.apache.causeway.extensions.pdfjs.metamodel.domains.mixin.SomeViewModel_pdf.prop()"/>
            <mml:attr name="precedence" value="DEFAULT"/>
            <mml:attr name="returnType" value="org.apache.causeway.applib.value.Blob"/>
          </mml:facet>
         */
    }

}
