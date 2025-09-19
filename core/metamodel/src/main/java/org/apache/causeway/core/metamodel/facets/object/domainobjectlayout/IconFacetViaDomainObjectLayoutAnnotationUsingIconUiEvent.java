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

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.ui.IconUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEventUtils;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;

public record IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
    Class<? extends IconUiEvent<Object>> iconUiEventClass,
    MetamodelEventService metamodelEventService,
    FacetHolder facetHolder)
implements IconFacet {

    public static Optional<IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
            .map(DomainObjectLayout::iconUiEvent)
            .filter(iconUiEvent -> MmEventUtils.eventTypeIsPostable(
                    iconUiEvent,
                    IconUiEvent.Noop.class,
                    IconUiEvent.Default.class,
                    facetHolder.getConfiguration().applib().annotation()
                        .domainObjectLayout().iconUiEvent().postForDefault()))
            .map(iconUiEvent -> {
                return new IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
                    _Casts.uncheckedCast(iconUiEvent), metamodelEventService, facetHolder);
            });
    }

    @Override public FacetHolder getFacetHolder() { return facetHolder; }
    @Override public Class<? extends Facet> facetType() { return IconFacet.class; }
    @Override public Precedence getPrecedence() { return Precedence.EVENT; }


    @Override
    public Optional<ObjectSupport.IconResource> icon(ManagedObject domainObject, ObjectSupport.IconSize iconSize) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)) return Optional.empty();

        final IconUiEvent<Object> iconUiEvent = newIconUiEvent(domainObject, iconSize);

        metamodelEventService.fireIconUiEvent(iconUiEvent);

        var icon = iconUiEvent.getIcon();

        if(icon == null) {
            // ie no subscribers out there...

            icon = underlyingIconFacet()
                .flatMap(underlyingIconFacet->underlyingIconFacet.icon(domainObject, iconSize))
                .orElse(null);
        }

        return Optional.ofNullable(icon);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", ClassUtils.getShortName(getClass()));
        visitor.accept("precedence", getPrecedence().name());
        visitor.accept("iconUiEventClass", iconUiEventClass);
    }

    private IconUiEvent<Object> newIconUiEvent(final ManagedObject owningAdapter, ObjectSupport.IconSize iconSize) {
        var iconUiEvent = EventObjectBase.getInstanceWithSourceSupplier(iconUiEventClass, owningAdapter::getPojo)
            .orElseThrow();
        return iconUiEvent.iconSize(iconSize);
    }

    private Optional<IconFacet> underlyingIconFacet() {
        return getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(IconFacet.class));
    }


}
