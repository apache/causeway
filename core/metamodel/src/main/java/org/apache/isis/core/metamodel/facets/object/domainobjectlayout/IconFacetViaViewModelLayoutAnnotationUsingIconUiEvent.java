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

package org.apache.isis.core.metamodel.facets.object.domainobjectlayout;

import java.util.Map;
import java.util.Optional;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacetAbstract;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.EventUtil;

public class IconFacetViaViewModelLayoutAnnotationUsingIconUiEvent extends IconFacetAbstract {

    public static Facet create(
            final Optional<ViewModelLayout> viewModelLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final IsisConfiguration configuration,
            final FacetHolder facetHolder) {

        return viewModelLayoutIfAny
                .map(ViewModelLayout::iconUiEvent)
                .filter(iconUiEvent -> EventUtil.eventTypeIsPostable(
                        iconUiEvent,
                        IconUiEvent.Noop.class,
                        IconUiEvent.Default.class,
                        configuration.getReflector().getFacet().getViewModelLayoutAnnotation().getIconUiEvent().isPostForDefault()))
                .map(iconUiEvent -> {

                    return new IconFacetViaViewModelLayoutAnnotationUsingIconUiEvent(
                            iconUiEvent, metamodelEventService, facetHolder);
                })
                .orElse(null);
    }

    private final Class<? extends IconUiEvent<?>> iconUiEventClass;
    private final MetamodelEventService metamodelEventService;

    public IconFacetViaViewModelLayoutAnnotationUsingIconUiEvent(
            final Class<? extends IconUiEvent<?>> iconUiEventClass,
                    final MetamodelEventService metamodelEventService,
                    final FacetHolder holder) {
        super(holder);
        this.iconUiEventClass = iconUiEventClass;
        this.metamodelEventService = metamodelEventService;
    }

    @Override
    public String iconName(final ManagedObject owningAdapter) {

        if(owningAdapter == null) {
            return null;
        }

        final IconUiEvent<Object> iconUiEvent = newIconUiEvent(owningAdapter);

        metamodelEventService.fireIconUiEvent(iconUiEvent);

        final String iconName = iconUiEvent.getIconName();

        if(iconName == null) {
            // ie no subscribers out there...
            final Facet underlyingFacet = getUnderlyingFacet();
            if(underlyingFacet instanceof IconFacet) {
                final IconFacet underlyingIconFacet = (IconFacet) underlyingFacet;
                return underlyingIconFacet.iconName(owningAdapter);
            }
        }

        return iconName; // could be null
    }

    private IconUiEvent<Object> newIconUiEvent(final ManagedObject owningAdapter) {
        final Object domainObject = owningAdapter.getPojo();
        return newIconUiEventForPojo(domainObject);
    }

    private IconUiEvent<Object> newIconUiEventForPojo(final Object domainObject) {
        try {
            final IconUiEvent<Object> iconUiEvent = _Casts.uncheckedCast(iconUiEventClass.newInstance());
            iconUiEvent.initSource(domainObject);
            return iconUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new NonRecoverableException(ex);
        }
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("iconUiEventClass", iconUiEventClass);
    }

}
