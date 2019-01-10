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
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.TitleUiEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.EventUtil;

public class TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent extends TitleFacetAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(
            TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent.class);

    public static Facet create(
            final ViewModelLayout viewModelLayout,
            final ServicesInjector servicesInjector,
            final IsisConfiguration configuration, final FacetHolder facetHolder) {
        if(viewModelLayout == null) {
            return null;
        }
        final Class<? extends TitleUiEvent<?>> titleUiEventClass = viewModelLayout.titleUiEvent();

        if(!EventUtil.eventTypeIsPostable(
                titleUiEventClass,
                TitleUiEvent.Noop.class,
                TitleUiEvent.Default.class,
                "isis.reflector.facet.viewModelLayoutAnnotation.titleUiEvent.postForDefault",
                configuration)) {
            return null;
        }

        final TranslationService translationService = servicesInjector.lookupService(TranslationService.class);
        final ObjectSpecification facetHolderAsSpec = (ObjectSpecification) facetHolder; // bit naughty...
        final String translationContext = facetHolderAsSpec.getCorrespondingClass().getCanonicalName();
        final EventBusService eventBusService = servicesInjector.lookupService(EventBusService.class);

        return new TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent(
                titleUiEventClass, translationService, translationContext, eventBusService, facetHolder);
    }

    private final Class<? extends TitleUiEvent<?>> titleUiEventClass;
    private final TranslationService translationService;
    private final String translationContext;
    private final EventBusService eventBusService;

    public TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent(
            final Class<? extends TitleUiEvent<?>> titleUiEventClass,
            final TranslationService translationService,
            final String translationContext,
            final EventBusService eventBusService,
            final FacetHolder holder) {
        super(holder);
        this.titleUiEventClass = titleUiEventClass;
        this.translationService = translationService;
        this.translationContext = translationContext;
        this.eventBusService = eventBusService;
    }

    @Override
    public String title(final ObjectAdapter owningAdapter) {

        if(owningAdapter == null) {
            return null;
        }

        final TitleUiEvent<Object> titleUiEvent = newTitleUiEvent(owningAdapter);

        eventBusService.post(titleUiEvent);

        final TranslatableString translatedTitle = titleUiEvent.getTranslatableTitle();
        if(translatedTitle != null) {
            return translatedTitle.translate(translationService, translationContext);
        }
        final String title = titleUiEvent.getTitle();

        if(title == null) {
            // ie no subscribers out there...
            final Facet underlyingFacet = getUnderlyingFacet();
            if(underlyingFacet instanceof TitleFacet) {
                final TitleFacet underlyingTitleFacet = (TitleFacet) underlyingFacet;
                return underlyingTitleFacet.title(owningAdapter);
            }
        }
        return title;
    }

    private TitleUiEvent<Object> newTitleUiEvent(final ObjectAdapter owningAdapter) {
        final Object domainObject = owningAdapter.getObject();
        return newTitleUiEvent(domainObject);
    }

    private TitleUiEvent<Object> newTitleUiEvent(final Object domainObject) {
        try {
            final TitleUiEvent<Object> titleUiEvent = (TitleUiEvent<Object>) titleUiEventClass.newInstance();
            titleUiEvent.setSource(domainObject);
            return titleUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new NonRecoverableException(ex);
        }
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("titleUiEventClass", titleUiEventClass);
    }
}
