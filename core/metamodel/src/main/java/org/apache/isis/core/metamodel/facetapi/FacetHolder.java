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
package org.apache.isis.core.metamodel.facetapi;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Anything in the metamodel (which also includes peers in the reflector) that
 * can be extended.
 */
public interface FacetHolder extends HasMetaModelContext {

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

    // -- WRAPPER

    /**
     * Adds a transparent facade onto given delegate,
     * only overriding the {@link FacetHolder#getFeatureIdentifier()}.
     * <p>
     * Used by mixed in members.
     */
    public static FacetHolder wrapped(
            final @NonNull MetaModelContext mmc,
            final @NonNull Identifier featureIdentifier,
            final @NonNull FacetHolder delegate) {
        return new WrappedFacetHolder(mmc, featureIdentifier, delegate);
    }

    /**
     * @see FacetHolder#wrapped(MetaModelContext, Identifier, FacetHolder)
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class WrappedFacetHolder implements FacetHolder {

        @Getter(onMethod_ = {@Override})
        private final @NonNull MetaModelContext metaModelContext;
        private final @NonNull Identifier featureIdentifierOverride;
        private final @NonNull FacetHolder delegate;

        @Override public Identifier getFeatureIdentifier() {
            return featureIdentifierOverride; }
        @Override public int getFacetCount() {
            return delegate.getFacetCount(); }
        @Override public <T extends Facet> T getFacet(final Class<T> facetType) {
            return delegate.getFacet(facetType); }
        @Override public boolean containsFacet(final Class<? extends Facet> facetType) {
            return delegate.containsFacet(facetType); }
        @Override public Stream<Facet> streamFacets() {
            return delegate.streamFacets(); }
        @Override public Stream<FacetRanking> streamFacetRankings() {
            return delegate.streamFacetRankings(); }
        @Override public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
            return delegate.getFacetRanking(facetType); }
        @Override public void addFacet(final @NonNull Facet facet) {
            delegate.addFacet(facet);
        }
    }

}
