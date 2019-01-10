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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.LayoutUiEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.util.EventUtil;

public class LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent extends FacetAbstract implements
        LayoutFacet {

    private static final Logger LOG = LoggerFactory.getLogger(
            LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent.class);

    public static Facet create(
            final DomainObjectLayout domainObjectLayout,
            final ServicesInjector servicesInjector,
            final IsisConfiguration configuration, final FacetHolder facetHolder) {
        if(domainObjectLayout == null) {
            return null;
        }
        final Class<? extends LayoutUiEvent<?>> layoutUiEventClass = domainObjectLayout.layoutUiEvent();

        if(!EventUtil.eventTypeIsPostable(
                layoutUiEventClass,
                LayoutUiEvent.Noop.class,
                LayoutUiEvent.Default.class,
                "isis.reflector.facet.domainObjectLayoutAnnotation.layoutUiEvent.postForDefault",
                configuration)) {
            return null;
        }

        final EventBusService eventBusService = servicesInjector.lookupService(EventBusService.class);

        return new LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
                layoutUiEventClass, eventBusService, facetHolder);
    }

    private final Class<? extends LayoutUiEvent<?>> layoutUiEventClass;
    private final EventBusService eventBusService;

    public LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent(
            final Class<? extends LayoutUiEvent<?>> layoutUiEventClass,
            final EventBusService eventBusService,
            final FacetHolder holder) {
        super(LayoutFacetAbstract.type(), holder, Derivation.NOT_DERIVED);
        this.layoutUiEventClass = layoutUiEventClass;
        this.eventBusService = eventBusService;
    }

    @Override
    public String layout(final ObjectAdapter owningAdapter) {

        if(owningAdapter == null) {
            return null;
        }

        final LayoutUiEvent<Object> layoutUiEvent = newLayoutUiEvent(owningAdapter);

        eventBusService.post(layoutUiEvent);

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

    private LayoutUiEvent<Object> newLayoutUiEvent(final ObjectAdapter owningAdapter) {
        final Object domainObject = owningAdapter.getObject();
        return newLayoutUiEvent(domainObject);
    }

    private LayoutUiEvent<Object> newLayoutUiEvent(final Object domainObject) {
        try {
            final LayoutUiEvent<Object> layoutUiEvent = (LayoutUiEvent<Object>) layoutUiEventClass.newInstance();
            layoutUiEvent.setSource(domainObject);
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
