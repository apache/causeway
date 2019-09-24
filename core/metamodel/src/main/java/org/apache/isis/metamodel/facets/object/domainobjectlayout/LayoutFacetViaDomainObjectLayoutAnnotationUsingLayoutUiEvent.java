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

package org.apache.isis.metamodel.facets.object.domainobjectlayout;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.metamodel.facets.object.layout.LayoutFacetAbstract;
import org.apache.isis.metamodel.services.events.MetamodelEventService;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.util.EventUtil;

public class LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent 
extends LayoutFacetAbstract 
implements LayoutFacet {

    public static Facet create(
            final List<DomainObjectLayout> domainObjectLayouts,
            final MetamodelEventService metamodelEventService,
            final IsisConfiguration configuration, 
            final FacetHolder facetHolder) {

        return domainObjectLayouts.stream()
                .map(DomainObjectLayout::layoutUiEvent)
                .filter(layoutUiEvent -> EventUtil.eventTypeIsPostable(
                        layoutUiEvent,
                        LayoutUiEvent.Noop.class,
                        LayoutUiEvent.Default.class,
                        "isis.reflector.facet.domainObjectLayoutAnnotation.layoutUiEvent.postForDefault",
                        configuration))
                .findFirst()
                .map(layoutUiEvent -> {

                    return new LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
                            layoutUiEvent, metamodelEventService, facetHolder);
                })
                .orElse(null);
    }

    private final Class<? extends LayoutUiEvent<?>> layoutUiEventClass;
    private final MetamodelEventService metamodelEventService;

    public LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
            final Class<? extends LayoutUiEvent<?>> layoutUiEventClass,
                    final MetamodelEventService metamodelEventService,
                    final FacetHolder holder) {
        super(holder);
        this.layoutUiEventClass = layoutUiEventClass;
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
            final Facet underlyingFacet = getUnderlyingFacet();
            if(underlyingFacet instanceof LayoutFacet) {
                final LayoutFacet underlyingLayoutFacet = (LayoutFacet) underlyingFacet;
                return underlyingLayoutFacet.layout(owningAdapter);
            }
        }

        return layout;
    }

    private LayoutUiEvent<Object> newLayoutUiEvent(final ManagedObject owningAdapter) {
        final Object domainObject = owningAdapter.getPojo();
        return newLayoutUiEvent(domainObject);
    }

    private LayoutUiEvent<Object> newLayoutUiEvent(final Object domainObject) {
        try {
            final LayoutUiEvent<Object> layoutUiEvent = (LayoutUiEvent<Object>) layoutUiEventClass.newInstance();
            layoutUiEvent.initSource(domainObject);
            return layoutUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new NonRecoverableException(ex);
        }
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("layoutUiEventClass", layoutUiEventClass);
    }
}
