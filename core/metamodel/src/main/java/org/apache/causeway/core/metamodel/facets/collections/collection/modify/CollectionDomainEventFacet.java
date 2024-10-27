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
package org.apache.causeway.core.metamodel.facets.collections.collection.modify;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

public class CollectionDomainEventFacet
extends DomainEventFacetAbstract<CollectionDomainEvent<?, ?>>
implements HidingInteractionAdvisor {

    // -- FACET TYPE

    private static Class<? extends Facet> type() {
        return CollectionDomainEventFacet.class;
    }

    // -- FACTORIES

    /**
     * Inspect {@link Collection#domainEvent()} if present, else use the default event type.
     */
    public static CollectionDomainEventFacet create(
            final @NonNull Optional<Collection> collectionIfAny,
            final @NonNull Class<?> classBeingIntrospected,
            final @NonNull FacetHolder facetHolder) {

        var collectionDomainEventFacet = collectionIfAny
                .map(Collection::domainEvent)
                .filter(domainEvent -> domainEvent != CollectionDomainEvent.Default.class)
                .map(domainEvent ->
                        new CollectionDomainEventFacet(domainEvent,
                                EventTypeOrigin.ANNOTATED_MEMBER, facetHolder))
                .orElseGet(()->{

                    /* only used to lookup {@link CollectionDomainEventDefaultFacetForDomainObjectAnnotation} */
                    var typeSpec = facetHolder.getSpecificationLoader().loadSpecification(classBeingIntrospected);
                    var typeFromDomainObject = typeSpec.getFacet(CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class);

                    return typeFromDomainObject != null
                            ? new CollectionDomainEventFacet(
                                    typeFromDomainObject.getEventType(),
                                    EventTypeOrigin.ANNOTATED_OBJECT, facetHolder)
                            : new CollectionDomainEventFacet(
                                    CollectionDomainEvent.Default.class,
                                    EventTypeOrigin.DEFAULT, facetHolder);
                });

        return collectionDomainEventFacet;
    }

    // -- CONSTRUCTION

    private final DomainEventHelper domainEventHelper;

    protected CollectionDomainEventFacet(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final FacetHolder holder) {
        super(type(), eventType, eventTypeOrigin, holder);
        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public void initWithMixee(final ObjectSpecification mixeeSpec) {
        if(!getEventTypeOrigin().isDefault()) return; // skip if already set explicitly
        mixeeSpec
        .lookupFacet(CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class)
        .ifPresent(facetOnMixee->
            super.updateEventType(facetOnMixee.getEventType(), EventTypeOrigin.ANNOTATED_OBJECT));
    }

    @Override
    public String hides(final VisibilityContext ic) {
        if(!isPostable()) return null; // bale out

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        AbstractDomainEvent.Phase.HIDE,
                        _Casts.uncheckedCast(getEventType()),
                        getFacetHolder(), ic.getHead()
                );
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

}
