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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.events.ui.TitleUiEvent;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent
extends TitleFacetAbstract {

    public static Optional<TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final IsisConfiguration configuration,
            final FacetHolder facetHolder) {

        val isPostForDefault = configuration
                .getApplib()
                .getAnnotation()
                .getDomainObjectLayout()
                .getTitleUiEvent()
                .isPostForDefault();

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::titleUiEvent)
                .filter(titleUiEvent -> EventUtil.eventTypeIsPostable(
                        titleUiEvent,
                        TitleUiEvent.Noop.class,
                        TitleUiEvent.Default.class,
                        isPostForDefault))
                .map(titleUiEventClass -> {
                    return new TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent(
                            titleUiEventClass,
                            translationContextFor(facetHolder),
                            metamodelEventService,
                            facetHolder);
                });
    }

    private final Class<? extends TitleUiEvent<?>> titleUiEventClass;
    private final TranslationService translationService;
    private final TranslationContext translationContext;
    private final MetamodelEventService metamodelEventService;

    public TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent(
            final Class<? extends TitleUiEvent<?>> titleUiEventClass,
                    final TranslationContext translationContext,
                    final MetamodelEventService metamodelEventService,
                    final FacetHolder holder) {
        super(holder, Precedence.EVENT);
        this.titleUiEventClass = titleUiEventClass;
        this.translationService = super.getTranslationService();
        this.translationContext = translationContext;
        this.metamodelEventService = metamodelEventService;
    }

    @Override
    public String title(final TitleRenderRequest titleRenderRequest) {

        final ManagedObject owningAdapter = titleRenderRequest.getObject();

        if(owningAdapter == null) {
            return null;
        }

        final TitleUiEvent<Object> titleUiEvent = newTitleUiEvent(owningAdapter);

        metamodelEventService.fireTitleUiEvent(titleUiEvent);

        if(titleUiEvent.getTitle() == null
                && titleUiEvent.getTranslatableTitle() == null) {
            // ie no subscribers out there...

            final TitleFacet underlyingTitleFacet = getSharedFacetRanking()
            .flatMap(facetRanking->facetRanking.getWinnerNonEvent(TitleFacet.class))
            .orElse(null);

            if(underlyingTitleFacet!=null) {
                return underlyingTitleFacet.title(titleRenderRequest);
            }
        }

        final TranslatableString translatedTitle = titleUiEvent.getTranslatableTitle();
        if(translatedTitle != null) {
            return translatedTitle.translate(translationService, translationContext);
        }
        return titleUiEvent.getTitle();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("titleUiEventClass", titleUiEventClass);
    }

    // -- HELPER

    private static TranslationContext translationContextFor(final FacetHolder facetHolder) {
        if(facetHolder instanceof ObjectSpecification) {
            val facetHolderAsSpec = (ObjectSpecification) facetHolder;
            return TranslationContext.forTranslationContextHolder(facetHolderAsSpec.getFeatureIdentifier());
        }
        return null;
    }

    private TitleUiEvent<Object> newTitleUiEvent(final ManagedObject owningAdapter) {
        final Object domainObject = owningAdapter.getPojo();
        return newTitleUiEvent(domainObject);
    }

    private TitleUiEvent<Object> newTitleUiEvent(final Object domainObject) {
        try {
            final TitleUiEvent<Object> titleUiEvent = _Casts.uncheckedCast(titleUiEventClass.newInstance());
            titleUiEvent.initSource(domainObject);
            return titleUiEvent;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new UnrecoverableException(ex);
        }
    }

}
