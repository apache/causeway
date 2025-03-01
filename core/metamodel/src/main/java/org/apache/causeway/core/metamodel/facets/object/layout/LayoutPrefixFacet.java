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
package org.apache.causeway.core.metamodel.facets.object.layout;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.ui.LayoutUiEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEventUtils;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;

/**
 * Provides the null-able layout prefix for an object.
 * The prefix - if present - is used to lookup concrete layout variants.
 * <p>
 * Typically corresponds to a method named <tt>layout</tt>.
 *
 * @see TitleFacet
 * @see IconFacet
 */
public record LayoutPrefixFacet(
    @NonNull String origin,
    @NonNull PrefixProvider prefixProvider,
    @NonNull FacetHolder facetHolder,
    Facet.@NonNull Precedence precedence
) implements Facet, HasImperativeAspect {

    public interface PrefixProvider {
        String layoutPrefix(LayoutPrefixFacet layoutFacet, @Nullable ManagedObject managedObject);
        default void visitAttributes(final BiConsumer<String, Object> visitor) {}
    }

    // -- LAYOUT PROVIDERS

    private record PrefixProviderForImperativeAspect(
        ImperativeAspect imperativeAspect) implements PrefixProvider {
        @Override public String layoutPrefix(final LayoutPrefixFacet layoutFacet, @Nullable final ManagedObject managedObject) {
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) return null;
            try {
                return (String) imperativeAspect.invokeSingleMethod(managedObject);
            } catch (final RuntimeException ex) {
                return null;
            }
        }
        @Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
            imperativeAspect.visitAttributes(visitor);
        }
    }

    private record PrefixProviderForUiEvent(
        Class<? extends LayoutUiEvent<Object>> layoutUiEventClass,
        MetamodelEventService metamodelEventService) implements LayoutPrefixFacet.PrefixProvider {
        @Override public String layoutPrefix(final LayoutPrefixFacet layoutFacet, final ManagedObject managedObject) {
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) return null;

            final LayoutUiEvent<Object> layoutUiEvent = newLayoutUiEvent(managedObject);

            metamodelEventService.fireLayoutUiEvent(layoutUiEvent);

            final String layout = layoutUiEvent.getLayout();
            if(layout != null) return layout;

            // ie no subscribers out there, then fallback to the underlying ...
            return layoutFacet.getSharedFacetRanking()
                .flatMap(facetRanking->facetRanking.getWinnerNonEvent(LayoutPrefixFacet.class))
                .map(underlyingLayoutFacet->underlyingLayoutFacet.layoutPrefix(managedObject))
                .orElse(null);
        }
        @Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
            visitor.accept("layoutUiEventClass", layoutUiEventClass);
        }
        private LayoutUiEvent<Object> newLayoutUiEvent(final ManagedObject owningAdapter) {
            return EventObjectBase.getInstanceWithSourceSupplier(layoutUiEventClass, owningAdapter::getPojo).orElseThrow();
        }
    }

    // -- FACTORIES

    public static LayoutPrefixFacet fallback(final FacetHolder facetHolder) {
        return new LayoutPrefixFacet("Fallback", (lf, mo)->null, facetHolder, Precedence.FALLBACK);
    }

    public static Optional<LayoutPrefixFacet> forLayoutMethod(
        final @Nullable ResolvedMethod methodIfAny,
        final FacetHolder holder) {

        return Optional.ofNullable(methodIfAny)
            .map(method->ImperativeAspect.singleRegularMethod(method, Intent.UI_HINT))
            .map(PrefixProviderForImperativeAspect::new)
            .map(layoutProvider->
                new LayoutPrefixFacet("LayoutMethod", layoutProvider, holder, Precedence.DEFAULT));
    }

    public static Optional<LayoutPrefixFacet> forDomainObjectLayoutAnnotationUsingLayoutUiEvent(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final MetamodelEventService metamodelEventService,
            final FacetHolder facetHolder) {

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::layoutUiEvent)
                .filter(layoutUiEvent -> MmEventUtils.eventTypeIsPostable(
                        layoutUiEvent,
                        LayoutUiEvent.Noop.class,
                        LayoutUiEvent.Default.class,
                        facetHolder.getConfiguration().getApplib().getAnnotation()
                            .getDomainObjectLayout().getLayoutUiEvent().isPostForDefault()))
                .map(layoutUiEvent -> new PrefixProviderForUiEvent(_Casts.uncheckedCast(layoutUiEvent), metamodelEventService))
                .map(layoutProvider -> new LayoutPrefixFacet("DomainObjectLayoutAnnotationWithLayoutUiEvent",
                    layoutProvider,
                        facetHolder, Precedence.EVENT));
    }

    // -- METHODS

    @Override public Class<? extends Facet> facetType() { return getClass(); }
    @Override public Precedence getPrecedence() { return precedence(); }
    @Override public FacetHolder getFacetHolder() { return facetHolder(); }

    public String layoutPrefix(final ManagedObject managedObject) {
        return prefixProvider().layoutPrefix(this, managedObject);
    }

    @Override
    public ImperativeAspect getImperativeAspect() {
        return prefixProvider() instanceof PrefixProviderForImperativeAspect aspectHolder
            ? aspectHolder.imperativeAspect()
            : null;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("origin", origin());
        visitor.accept("precedence", getPrecedence().name());
        prefixProvider().visitAttributes(visitor);
    }

}
