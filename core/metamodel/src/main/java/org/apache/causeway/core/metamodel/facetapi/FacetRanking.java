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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Acts as a facade to {@link TypedFacetRanking}, encapsulating type casting complexity.
 *
 * @implNote thread-safe
 * @see TypedFacetRanking
 *
 * @since 2.0, revised to a facade in 4.0
 */
@AllArgsConstructor @Getter @Accessors(fluent = true)
public final class FacetRanking {
	
    private final Class<? extends Facet> facetType;
    private final TypedFacetRanking<?> typedFacetRanking;

    public FacetRanking(
            final Class<? extends Facet> facetType) {
        this(facetType, new TypedFacetRanking<>(facetType));
    }

    public <F extends Facet> void add(final F facet) {
        delegate(facet).add(facet);
    }

    public <F extends Facet> void addAll(final FacetRanking facetRanking) {
        @SuppressWarnings("unchecked")
        var a = delegate((Class<F>)facetType);
        @SuppressWarnings("unchecked")
        var b = facetRanking.delegate((Class<F>)facetType);
        a.addAll(b);
    }

    public <F extends Facet> Optional<F> getWinner(final Class<F> facetType) {
        return delegate(facetType).getWinner();
    }

    public <F extends Facet> Optional<F> getWinnerNonEvent(final Class<F> facetType) {
        return delegate(facetType).getWinnerNonEvent();
    }

    public <F extends Facet> Optional<F> getWinnerNonEventLowerOrEqualTo(
            final @NonNull Class<F> facetType,
            final @NonNull Precedence precedenceUpper) {
        return delegate(facetType).getWinnerNonEventLowerOrEqualTo(precedenceUpper);
    }

    /**
     * Optionally returns the top ranking event facet, based on whether there was one added.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getEventFacet(final @NonNull Class<F> facetType) {
        return delegate(facetType).getEventFacet();
    }

    /**
     * Returns a defensive copy of the top rank.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Can<F> getTopRank(final @NonNull Class<F> facetType) {
        return delegate(facetType).getTopRank();
    }

    /**
     * Returns a defensive copy of the selected rank of given precedence constraint.
     * @param facetType - for convenience, so the caller does not need to cast the result
     * @param precedenceUpper - upper bound
     */
    public <F extends Facet> Can<F> getRankLowerOrEqualTo(
            final @NonNull Class<F> facetType,
            final @NonNull Precedence precedenceUpper) {
        return delegate(facetType).getRankLowerOrEqualTo(precedenceUpper);
    }

    /**
     * Returns highest found precedence within ranks that conforms to given precedence constraint.
     * @param precedenceUpper - upper bound
     */
    public Optional<Precedence> getHighestPrecedenceLowerOrEqualTo(final @NonNull Precedence precedenceUpper) {
        return typedFacetRanking.getHighestPrecedenceLowerOrEqualTo(precedenceUpper);
    }

    public Optional<Facet.Precedence> getTopPrecedence() {
        return typedFacetRanking.getTopPrecedence();
    }

    /**
     * Within given constraints (qualifier and filter),
     * removes any {@link Facet} of {@code facetType} from facetHolder.
     *
     * <p>Motivated by layout reloading, that is,
     * reloading of domain-object layouts and menu-bar layouts.
     *
     * @param facetType - to ensure the filter is properly generic-type-constraint
     */
    public <F extends Facet> void purgeIf(
            final @NonNull Class<F> facetType,
            final @NonNull QualifiedFacet.Key qualifierKey,
            final @NonNull Predicate<? super F> facetFilter,
            final @NonNull Predicate<Facet.Precedence> precedenceFilter) {
        delegate(facetType).purgeIf(facetFilter, qualifierKey, precedenceFilter);
    }

    // -- LAYOUT SWITCHING

    public static void setQualifier(final LayoutKey layoutKey) {
        TypedFacetRanking.setQualifier(layoutKey);
    }
    public static void removeQualifier() {
        TypedFacetRanking.removeQualifier();
    }

    // -- VALIDATION SUPPORT

    public <F extends Facet> void visitTopRankPairsSemanticDiffering(
            final @NonNull Class<F> facetType,
            final @NonNull BiConsumer<F, F> visitor) {
        visitTopRankPairs(facetType, (a, b)->{
            if(!a.semanticEquals(b)) {
                visitor.accept(a, b);
            }
        });
    }

    public <F extends Facet> void visitTopRankPairs(
            final @NonNull Class<F> facetType,
            final @NonNull BiConsumer<F, F> visitor) {
        var topRankingFacets = getTopRank(facetType);

        if(topRankingFacets.isCardinalityMultiple()) {
            var firstOfTopRanking = topRankingFacets.getFirstElseFail();
            topRankingFacets
                .stream()
                .skip(1)
                .forEach(next->visitor.accept(firstOfTopRanking, next));
        }
    }

    public int totalFacetCount() {
        return typedFacetRanking.totalFacetCount();
    }

    // -- CASTING

    @SuppressWarnings("unchecked")
    private <F extends Facet> TypedFacetRanking<F> delegate(final Class<F> facetType) {
        return (TypedFacetRanking<F>) typedFacetRanking;
    }
    @SuppressWarnings("unchecked")
    private <F extends Facet> TypedFacetRanking<F> delegate(final F facet) {
        return (TypedFacetRanking<F>) typedFacetRanking;
    }

}
