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
import org.apache.causeway.applib.events.ui.IconUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.util.EventUtil;

public class IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent
extends IconFacetAbstract {

    public static Optional<IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::iconUiEvent)
                .filter(iconUiEvent -> EventUtil.eventTypeIsPostable(
                        iconUiEvent,
                        IconUiEvent.Noop.class,
                        IconUiEvent.Default.class,
                        facetHolder.getConfiguration().getApplib().getAnnotation()
                            .getDomainObjectLayout().getIconUiEvent().isPostForDefault()))
                .map(iconUiEvent -> {
                    return new IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
                            iconUiEvent, metamodelEventService, facetHolder);
                });
    }

    private final Class<? extends IconUiEvent<Object>> iconUiEventClass;
    private final MetamodelEventService metamodelEventService;

    public IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
            final Class<? extends IconUiEvent<?>> iconUiEventClass,
                    final MetamodelEventService metamodelEventService,
                    final FacetHolder holder) {
        super(holder, Precedence.EVENT);
        this.iconUiEventClass = _Casts.uncheckedCast(iconUiEventClass);
        this.metamodelEventService = metamodelEventService;
    }

    @Override
    public String iconName(final ManagedObject owningAdapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(owningAdapter)) {
            return null;
        }

        final IconUiEvent<Object> iconUiEvent = newIconUiEvent(owningAdapter);

        metamodelEventService.fireIconUiEvent(iconUiEvent);

        final String iconName = iconUiEvent.getIconName();

        if(iconName == null) {
            // ie no subscribers out there...

            final IconFacet underlyingIconFacet = getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(IconFacet.class))
            .orElse(null);

            if(underlyingIconFacet!=null) {
                return underlyingIconFacet.iconName(owningAdapter);
            }
        }

        return iconName; // could be null
    }

    private IconUiEvent<Object> newIconUiEvent(final ManagedObject owningAdapter) {
        return EventObjectBase.getInstanceWithSourceSupplier(iconUiEventClass, owningAdapter::getPojo).orElseThrow();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("iconUiEventClass", iconUiEventClass);
    }

}
