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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.i18n.HasTranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.NonNull;
import lombok.val;

/**
 * Anything in the metamodel (which also includes peers in the reflector) that
 * can be extended.
 */
public interface FacetHolder
extends HasMetaModelContext, HasTranslationContext {

    // -- FACTORIES

    public static FacetHolderAbstract simple(
            final MetaModelContext mmc,
            final Identifier featureIdentifier) {
        return new FacetHolderSimple(mmc, featureIdentifier);
    }

    public static FacetHolder layered(
            final Identifier featureIdentifier,
            final FacetHolder parentLayer) {
        return new FacetHolderLayered(featureIdentifier, parentLayer);
    }

    // -- JUNIT SUPPORT

    /**
     *  Meant for simple JUnit tests, that don't use the FacetHolder's identifier.
     */
    public static FacetHolderAbstract forTesting(final MetaModelContext mmc) {
        return simple(mmc, Identifier.classIdentifier(LogicalType.fqcn(Object.class)));
    }

    // --

    /**
     * Identifier of the feature this holder represents or is associated with.
     */
    Identifier getFeatureIdentifier();

    int getFacetCount();

    /**
     * Get the facet of the specified type (as per the type it reports from
     * {@link Facet#facetType()}).
     */
    <T extends Facet> T getFacet(Class<T> facetType);

    // -- FACET LOOKUP

    default <T extends Facet> Optional<T> lookupFacet(
            final @NonNull Class<T> facetType) {
        return Optional.ofNullable(getFacet(facetType));
    }

    default <T extends Facet> Optional<T> lookupFacet(
            final @NonNull Class<T> facetType,
            final @NonNull Predicate<T> filter) {
        return lookupFacet(facetType).map(facet->filter.test(facet) ? facet : null);
    }

    default <T extends Facet> Optional<T> lookupNonFallbackFacet(
            final @NonNull Class<T> facetType) {
        return lookupFacet(facetType, facet->!facet.getPrecedence().isFallback());
    }

    // -- CONTAINS

    /**
     * Whether there is a facet registered of the specified type.
     */
    boolean containsFacet(Class<? extends Facet> facetType);

    /**
     * Whether there is a facet registered of the specified type that is not a
     * {@link Facet.Precedence#isFallback() fallback} .
     * <p>
     * Convenience; saves having to {@link #getFacet(Class)} and then check if
     * <tt>null</tt> and not a fallback.
     */
    default boolean containsNonFallbackFacet(final Class<? extends Facet> facetType) {
        val facet = getFacet(facetType);
        return facet != null
                && !facet.getPrecedence().isFallback();
    }

    /**
     * As {@link #containsNonFallbackFacet(Class)}, with additional requirement, that the
     * facet is <i>explicit</i>, not {@link Facet.Precedence#isInferred() inferred}.
     */
    default boolean containsExplicitNonFallbackFacet(final Class<? extends Facet> facetType) {
        val facet = getFacet(facetType);
        return facet != null
                && !facet.getPrecedence().isFallback()
                && !facet.getPrecedence().isInferred();
    }

    Stream<Facet> streamFacets();

    default <F extends Facet> Stream<F> streamFacets(final Class<F> requiredType) {
        return streamFacets()
                .filter(facet->requiredType.isAssignableFrom(facet.getClass()))
                .map(requiredType::cast);
    }

    /**
     * Adds the facet, extracting its {@link Facet#facetType() type} as the key.
     *
     * <p>
     * Any previously added facet of the same type will be overwritten,
     * when given {@link Facet} has equal or higher precedence.
     * Otherwise is ignored.
     */
    void addFacet(@NonNull Facet facet);

    // -- VALIDATION SUPPORT

    Stream<FacetRanking> streamFacetRankings();
    Optional<FacetRanking> getFacetRanking(Class<? extends Facet> facetType);

    // -- TRANSLATION CONTEXT

    @Override
    default TranslationContext getTranslationContext() {
        return getFeatureIdentifier().getTranslationContext();
    }

}
