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
import org.apache.causeway.applib.events.ui.CssClassUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.util.EventUtil;

public class CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent
extends CssClassFacetAbstract {

    public static Optional<CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
        .map(DomainObjectLayout::cssClassUiEvent)
        .filter(cssClassUiEventClass -> EventUtil.eventTypeIsPostable(
                cssClassUiEventClass,
                CssClassUiEvent.Noop.class,
                CssClassUiEvent.Default.class,
                facetHolder.getConfiguration().getApplib().getAnnotation()
                    .getDomainObjectLayout().getCssClassUiEvent().isPostForDefault()))
        .map(cssClassUiEventClass -> {
            return new CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent(
                    cssClassUiEventClass, metamodelEventService, facetHolder);
        });

    }

    private final Class<? extends CssClassUiEvent<Object>> cssClassUiEventClass;
    private final MetamodelEventService metamodelEventService;

    private CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent(
            final Class<? extends CssClassUiEvent<?>> cssClassUiEventClass,
            final MetamodelEventService metamodelEventService,
            final FacetHolder holder) {

        super(holder, Precedence.EVENT);
        this.cssClassUiEventClass = _Casts.uncheckedCast(cssClassUiEventClass);
        this.metamodelEventService = metamodelEventService;
    }

    @Override
    public String cssClass(final ManagedObject owningAdapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(owningAdapter)) {
            return null;
        }

        final CssClassUiEvent<Object> cssClassUiEvent = newCssClassUiEvent(owningAdapter);

        metamodelEventService.fireCssClassUiEvent(cssClassUiEvent);

        final String cssClass = cssClassUiEvent.getCssClass();

        if(cssClass == null) {
            // ie no subscribers out there...

            final CssClassFacet underlyingCssClassFacet = getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(CssClassFacet.class))
            .orElse(null);

            if(underlyingCssClassFacet!=null) {
                return underlyingCssClassFacet.cssClass(owningAdapter);
            }
        }

        return cssClass;
    }

    private CssClassUiEvent<Object> newCssClassUiEvent(final ManagedObject owningAdapter) {
        final Object domainObject = owningAdapter.getPojo();
        return newCssClassUiEventForPojo(domainObject);
    }

    private CssClassUiEvent<Object> newCssClassUiEventForPojo(final Object domainObject) {
        return EventObjectBase.getInstanceWithSource(cssClassUiEventClass, domainObject).orElseThrow();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("cssClassUiEventClass", cssClassUiEventClass);
    }
}
