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
package org.apache.causeway.core.metamodel.facets.object.layout;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.ui.LayoutUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEventUtils;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;

public record LayoutPrefixFacetForUiEvent(
    @NonNull String origin,
    @NonNull Class<? extends LayoutUiEvent<Object>> layoutUiEventClass,
    @NonNull MetamodelEventService metamodelEventService,
    @NonNull FacetHolder facetHolder,
    Facet.@NonNull Precedence precedence
) implements LayoutPrefixFacet {

    // -- FACTORIES

    public static Optional<LayoutPrefixFacetForUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::layoutUiEvent)
                .filter(layoutUiEvent -> MmEventUtils.eventTypeIsPostable(
                        layoutUiEvent,
                        LayoutUiEvent.Noop.class,
                        LayoutUiEvent.Default.class,
                        facetHolder.getConfiguration().getApplib().getAnnotation()
                            .getDomainObjectLayout().getLayoutUiEvent().isPostForDefault()))
                .map(layoutUiEvent -> new LayoutPrefixFacetForUiEvent("DomainObjectLayoutAnnotationWithLayoutUiEvent",
                    _Casts.uncheckedCast(layoutUiEvent), metamodelEventService,
                    facetHolder, Precedence.EVENT));
    }

    // -- METHODS

    @Override public Class<? extends Facet> facetType() { return LayoutPrefixFacet.class; }
    @Override public Precedence getPrecedence() { return precedence(); }
    @Override public FacetHolder getFacetHolder() { return facetHolder(); }

    @Override
    public String layoutPrefix(final ManagedObject managedObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) return null;

        final LayoutUiEvent<Object> layoutUiEvent = newLayoutUiEvent(managedObject);

        metamodelEventService.fireLayoutUiEvent(layoutUiEvent);

        final String layout = layoutUiEvent.getLayout();
        if(layout != null) return layout;

        // ie no subscribers out there, then fallback to the underlying ...
        return getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(LayoutPrefixFacet.class))
            .map(underlyingLayoutFacet->underlyingLayoutFacet.layoutPrefix(managedObject))
            .orElse(null);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("origin", origin());
        visitor.accept("precedence", getPrecedence().name());
        visitor.accept("layoutUiEventClass", layoutUiEventClass);
    }

    // -- HELPER

    private LayoutUiEvent<Object> newLayoutUiEvent(final ManagedObject owningAdapter) {
        return EventObjectBase.getInstanceWithSourceSupplier(layoutUiEventClass, owningAdapter::getPojo).orElseThrow();
    }

}
