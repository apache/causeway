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
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.EventUtil;

public class IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent extends IconFacetAbstract {

    //private static final Logger LOG = LoggerFactory.getLogger(IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent.class);

    public static Facet create(
            final List<DomainObjectLayout> domainObjectLayouts,
            final ServicesInjector servicesInjector,
            final IsisConfiguration configuration,
            final FacetHolder facetHolder) {

        return domainObjectLayouts.stream()
                .map(DomainObjectLayout::iconUiEvent)
                .filter(iconUiEvent -> EventUtil.eventTypeIsPostable(
                        iconUiEvent,
                        IconUiEvent.Noop.class,
                        IconUiEvent.Default.class,
                        "isis.reflector.facet.domainObjectLayoutAnnotation.iconUiEvent.postForDefault",
                        configuration))
                .findFirst()
                .map(iconUiEvent -> {

                    final EventBusService eventBusService = servicesInjector.lookupServiceElseFail(EventBusService.class);

                    return new IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
                            iconUiEvent, eventBusService, facetHolder);
                })
                .orElse(null);
    }

    private final Class<? extends IconUiEvent<?>> iconUiEventClass;
    private final EventBusService eventBusService;

    public IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent(
            final Class<? extends IconUiEvent<?>> iconUiEventClass,
                    final EventBusService eventBusService,
                    final FacetHolder holder) {
        super(holder);
        this.iconUiEventClass = iconUiEventClass;
        this.eventBusService = eventBusService;
    }

    @Override
    public String iconName(final ManagedObject owningAdapter) {

        final IconUiEvent<Object> iconUiEvent = newIconUiEvent(owningAdapter);

        eventBusService.post(iconUiEvent);

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
            iconUiEvent.setSource(domainObject);
            return iconUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new NonRecoverableException(ex);
        }
    }

}
