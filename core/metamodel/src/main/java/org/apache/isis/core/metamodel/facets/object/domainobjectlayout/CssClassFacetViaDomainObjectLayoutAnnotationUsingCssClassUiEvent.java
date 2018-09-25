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

import java.util.List;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.util.EventUtil;

public class CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent extends FacetAbstract implements
CssClassFacet {

    //private static final Logger LOG = LoggerFactory.getLogger(CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent.class);

    public static Facet create(
            final List<DomainObjectLayout> domainObjectLayouts,
            final ServicesInjector servicesInjector,
            final IsisConfiguration configuration, final FacetHolder facetHolder) {

        return domainObjectLayouts.stream()
                .map(DomainObjectLayout::cssClassUiEvent)
                .filter(cssClassUiEventClass -> EventUtil.eventTypeIsPostable(
                        cssClassUiEventClass,
                        CssClassUiEvent.Noop.class,
                        CssClassUiEvent.Default.class,
                        "isis.reflector.facet.domainObjectLayoutAnnotation.cssClassUiEvent.postForDefault",
                        configuration))
                .findFirst()
                .map(cssClassUiEventClass -> {
                    final EventBusService eventBusService = servicesInjector.lookupService(EventBusService.class);
                    return new CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent(
                            cssClassUiEventClass, eventBusService, facetHolder);
                })
                .orElse(null);

    }

    private final Class<? extends CssClassUiEvent<?>> cssClassUiEventClass;
    private final EventBusService eventBusService;

    public CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent(
            final Class<? extends CssClassUiEvent<?>> cssClassUiEventClass,
                    final EventBusService eventBusService,
                    final FacetHolder holder) {
        super(CssClassFacetAbstract.type(), holder, Derivation.NOT_DERIVED);
        this.cssClassUiEventClass = cssClassUiEventClass;
        this.eventBusService = eventBusService;
    }

    @Override
    public String cssClass(final Instance owningAdapter) {

        final CssClassUiEvent<Object> cssClassUiEvent = newCssClassUiEvent(owningAdapter);

        eventBusService.post(cssClassUiEvent);

        final String cssClass = cssClassUiEvent.getCssClass();

        if(cssClass == null) {
            // ie no subscribers out there...
            final Facet underlyingFacet = getUnderlyingFacet();
            if(underlyingFacet instanceof CssClassFacet) {
                final CssClassFacet underlyingCssClassFacet = (CssClassFacet) underlyingFacet;
                return underlyingCssClassFacet.cssClass(owningAdapter);
            }
        }

        return cssClass;
    }

    private CssClassUiEvent<Object> newCssClassUiEvent(final Instance owningAdapter) {
        final Object domainObject = owningAdapter.getObject();
        return newCssClassUiEventForPojo(domainObject);
    }

    private CssClassUiEvent<Object> newCssClassUiEventForPojo(final Object domainObject) {
        try {
            final CssClassUiEvent<Object> cssClassUiEvent = _Casts.uncheckedCast(cssClassUiEventClass.newInstance());
            cssClassUiEvent.setSource(domainObject);
            return cssClassUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new NonRecoverableException(ex);
        }
    }

}
