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

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.causeway.core.metamodel.util.EventUtil;

import lombok.NonNull;
import lombok.val;

public class CollectionDomainEventFacet
extends DomainEventFacetAbstract<CollectionDomainEvent<?, ?>>
implements HidingInteractionAdvisor {

    // -- FACTORIES

    /**
     * For regular (non mixed-in) members only.
     * <p>
     * @return empty, if event is not post-able
     */
    public static Optional<CollectionDomainEventFacet> createRegular(
            final Optional<Collection> collectionIfAny,
            final ObjectSpecification typeSpec,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final FacetHolder facetHolder) {

        val collectionDomainEventFacet = collectionIfAny
                .map(Collection::domainEvent)
                .filter(domainEvent -> domainEvent != CollectionDomainEvent.Default.class)
                .map(domainEvent ->
                        new CollectionDomainEventFacet(
                                defaultFromDomainObjectIfRequired(typeSpec, domainEvent),
                                EventTypeOrigin.ANNOTATED_MEMBER, facetHolder))
                .orElse(
                        new CollectionDomainEventFacet(
                                defaultFromDomainObjectIfRequired(typeSpec, CollectionDomainEvent.Default.class),
                                EventTypeOrigin.DEFAULT, facetHolder));

        return EventUtil.eventTypeIsPostable(
                collectionDomainEventFacet.getEventType(),
                CollectionDomainEvent.Noop.class,
                CollectionDomainEvent.Default.class,
                facetHolder.getConfiguration().getApplib().getAnnotation().getCollection().getDomainEvent().isPostForDefault())
                    ? Optional.of(collectionDomainEventFacet)
                    : Optional.empty();
    }

    /**
     * For mixed-in members.
     */
    public static Optional<CollectionDomainEventFacet> createMixedIn(
            final @NonNull ObjectSpecification mixeeSpecification,
            final @NonNull OneToManyAssociationMixedIn mixedInCollection) {

        val facetedMethod = mixedInCollection.getFacetedMethod();
        final Method method = facetedMethod.getMethod().asMethodElseFail(); // no-arg method, should have a regular facade

        //TODO[CAUSEWAY-3409] what if the @Collection annotation is not on the method but on the (mixin) type
        final Collection collectionAnnot =
                _Annotations.synthesize(method, Collection.class)
                        .orElse(null);

        if(collectionAnnot != null) {
            final Class<? extends CollectionDomainEvent<?, ?>> collectionDomainEventType =
                    defaultFromDomainObjectIfRequired(
                            mixeeSpecification, collectionAnnot.domainEvent());
            return Optional.of(
                    new CollectionDomainEventFacet(
                            collectionDomainEventType, EventTypeOrigin.ANNOTATED_MEMBER, mixedInCollection));
        }
        return Optional.empty();
    }

    // --

    private static Class<? extends Facet> type() {
        return CollectionDomainEventFacet.class;
    }

    private static Class<? extends CollectionDomainEvent<?,?>> defaultFromDomainObjectIfRequired(
            final ObjectSpecification typeSpec,
            final Class<? extends CollectionDomainEvent<?,?>> collectionDomainEventType) {
        if (collectionDomainEventType == CollectionDomainEvent.Default.class) {
            final CollectionDomainEventDefaultFacetForDomainObjectAnnotation typeFromDomainObject =
                    typeSpec.getFacet(CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class);
            if (typeFromDomainObject != null) {
                return typeFromDomainObject.getEventType();
            }
        }
        return collectionDomainEventType;
    }

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
