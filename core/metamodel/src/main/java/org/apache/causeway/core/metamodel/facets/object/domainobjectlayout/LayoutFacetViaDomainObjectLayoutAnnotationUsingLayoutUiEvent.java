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
package org.apache.causeway.core.metamodel.facets.object.domainobjectlayout;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.ui.LayoutUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.util.EventUtil;

public class LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent
extends LayoutFacetAbstract
implements LayoutFacet {

    public static Optional<LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::layoutUiEvent)
                .filter(layoutUiEvent -> EventUtil.eventTypeIsPostable(
                        layoutUiEvent,
                        LayoutUiEvent.Noop.class,
                        LayoutUiEvent.Default.class,
                        facetHolder.getConfiguration().getApplib().getAnnotation()
                            .getDomainObjectLayout().getLayoutUiEvent().isPostForDefault()))
                .map(layoutUiEvent -> {

                    return new LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
                            layoutUiEvent, metamodelEventService, facetHolder);
                });
    }

    private final Class<? extends LayoutUiEvent<Object>> layoutUiEventClass;
    private final MetamodelEventService metamodelEventService;

    private LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
            final Class<? extends LayoutUiEvent<?>> layoutUiEventClass,
                    final MetamodelEventService metamodelEventService,
                    final FacetHolder holder) {
        super(holder, Precedence.EVENT);
        this.layoutUiEventClass = _Casts.uncheckedCast(layoutUiEventClass);
        this.metamodelEventService = metamodelEventService;
    }

    @Override
    public String layout(final ManagedObject owningAdapter) {

        if(owningAdapter == null) {
            return null;
        }

        final LayoutUiEvent<Object> layoutUiEvent = newLayoutUiEvent(owningAdapter);

        metamodelEventService.fireLayoutUiEvent(layoutUiEvent);

        final String layout = layoutUiEvent.getLayout();

        if(layout == null) {
            // ie no subscribers out there...

            final LayoutFacet underlyingLayoutFacet = getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(LayoutFacet.class))
            .orElse(null);

            if(underlyingLayoutFacet!=null) {
                return underlyingLayoutFacet.layout(owningAdapter);
            }
        }

        return layout;
    }

    private LayoutUiEvent<Object> newLayoutUiEvent(final ManagedObject owningAdapter) {
        return EventObjectBase.getInstanceWithSourceSupplier(layoutUiEventClass, owningAdapter::getPojo).orElseThrow();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("layoutUiEventClass", layoutUiEventClass);
    }
}
