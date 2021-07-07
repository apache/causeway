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

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * Instances of {@link FacetRanking} are shared among {@link Facet}(s)
 *  of same {@link Facet#facetType() facetType} in the context of a single
 * {@link FacetHolder} instance.
 * <p>
 * Records the history of adding {@link Facet}(s) of same
 * {@link Facet#facetType() facetType} to a single instance of
 * {@link FacetHolder}, which serves following purposes:
 * <ul>
 * <li>metamodel validation can look for conflicting semantics</li>
 * <li>find the winning facet, that is the one with highest precedence</li>
 * </ul>
 *
 * @implNote thread-safe
 * @since 2.0
 */
@RequiredArgsConstructor
public final class FacetRanking {

    @Getter @Accessors(fluent = true) private final @NonNull Class<? extends Facet> facetType;

    private final @NonNull _Multimaps.ListMultimap<Facet.Precedence, Facet> facetsByPrecedence
            = _Multimaps.newSortedConcurrentListMultimap();

    private final @NonNull AtomicReference<Facet> eventFacetRef = new AtomicReference<>();
    private final @NonNull AtomicReference<Precedence> topPrecedenceRef = new AtomicReference<>();

    /**
     * @return whether the top rank changed,
     * that is whether the winning facet should be reconsidered as a consequence of this call
     */
    public boolean add(final @NonNull Facet facet) {
        val facetType = facet.facetType();
        _Assert.assertEquals(this.facetType, facetType);

        // guard against invalidly mocked facets
        val facetPreference = Objects.requireNonNull(facet.getPrecedence(),
                ()->String.format("facet %s declares no precedence", facet.getClass()));

        // handle top priority (EVENT) facets separately
        if(facetPreference.isEvent()) {
            eventFacetRef.getAndUpdate(previous->{
                if(previous!=null) {
                    throw _Exceptions
                        .illegalState("cannot override an event facet %s with %s, "
                                + "must be unique per facet-holder and facet-type",
                                previous.getClass(),
                                facet.getClass());
                }
                return facet;
            });
            topPrecedenceRef.set(facetPreference);
            return true; // changes apply
        }

        val currentTopOrdinal = getTopPrecedence()
                .map(Precedence::ordinal)
                .orElse(-1);

        if(facetPreference.ordinal() > currentTopOrdinal) {
            topPrecedenceRef.set(facetPreference);
        }

        val currentTopRankOrdinal = facetsByPrecedence.isEmpty()
                ? -1
                : facetsByPrecedence.asNavigableMapElseFail().lastKey().ordinal();

        val changesTopRank = facetPreference.ordinal() >= currentTopRankOrdinal;

        // As an optimization (heap), don't store to lower ranks,
        // as these are not considered when picking a winning facet.
        // However, there are use-cases, where access to all facets of a given type are required,
        // regardless of facet-precedence (eg. MemberNamedFacets).
        if(changesTopRank
            || facet.isPopulateAllFacetRanks()) {
            facetsByPrecedence.putElement(facetPreference, facet);
        }

        return changesTopRank;
    }

    /**
     * Optionally returns the winning facet, considering the event facet (if any) and the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getWinner(final @NonNull Class<F> facetType) {
        val eventFacet = getEventFacet(facetType);
        if(eventFacet.isPresent()) {
            return eventFacet;
        }
        return getWinnerNonEvent(facetType);
    }

    /**
     * Optionally returns the winning facet, considering the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getWinnerNonEvent(final @NonNull Class<F> facetType) {
        val topRank = getTopRank(facetType);
        // TODO find winner if there are more than one
        // (also report conflicting semantics) - only historically the last one wins
        return topRank.getLast();
    }

    /**
     * Optionally returns the winning facet, considering only the rank with selected precedence constraints,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     * @param precedenceUpper - upper bound
     */
    public <F extends Facet> Optional<F> getWinnerNonEventLowerOrEqualTo(
            final @NonNull Class<F> facetType,
            final @NonNull Precedence precedenceUpper) {
        val selectedRank = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);
        return selectedRank
                .map(facetsByPrecedence::get)
                .map(_Lists::lastElementIfAny) // only historically the last one wins
                .map(_Casts::uncheckedCast);
    }


    /**
     * Optionally returns the top ranking event facet, based on whether there was one added.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getEventFacet(final @NonNull Class<F> facetType) {
        return Optional.ofNullable(_Casts.uncheckedCast(eventFacetRef.get()));
    }

    /**
     * Returns a defensive copy of the top rank.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Can<F> getTopRank(final @NonNull Class<F> facetType) {
        _Assert.assertEquals(this.facetType, facetType);
        val topRankedFacets = facetsByPrecedence.asNavigableMapElseFail().lastEntry();
        return topRankedFacets!=null
                ? Can.<F>ofCollection(_Casts.uncheckedCast(topRankedFacets.getValue()))
                : Can.empty();
    }

    /**
     * Returns a defensive copy of the selected rank of given precedence constraint.
     * @param facetType - for convenience, so the caller does not need to cast the result
     * @param precedenceUpper - upper bound
     */
    public <F extends Facet> Can<F> getRankLowerOrEqualTo(
            final @NonNull Class<F> facetType,
            final @NonNull Precedence precedenceUpper) {
        _Assert.assertEquals(this.facetType, facetType);

        val precedenceSelected = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);

        return precedenceSelected
        .map(facetsByPrecedence::get)
        .map(facetsOfSameRank->Can.<F>ofCollection(_Casts.uncheckedCast(facetsOfSameRank)))
        .orElseGet(Can::empty);
    }

    /**
     * Returns highest found precedence within ranks that conforms to given precedence constraint.
     * @param precedenceUpper - upper bound
     */
    public Optional<Precedence> getHighestPrecedenceLowerOrEqualTo(final @NonNull Precedence precedenceUpper) {
        return facetsByPrecedence
        .keySet()
        .stream()
        .filter(precedence->precedence.ordinal()<=precedenceUpper.ordinal())
        .max(Comparator.comparing(Precedence::ordinal));
    }

    public Optional<Facet.Precedence> getTopPrecedence() {
        return Optional.ofNullable(topPrecedenceRef.get());
    }

    // -- DYNAMIC UPDATE SUPPORT

    /**
     * Removes any facet of {@code facetType} from facetHolder if it passes the given {@code filter}.
     * @param facetType - to ensure the filter is properly generic-type-constraint
     * @param filter
     */
    public <F extends Facet> void purgeIf(
            final @NonNull Class<F> facetType,
            final @NonNull Predicate<? super F> filter) {

        // reassess the top precedence
        final _Reduction<Facet.Precedence> top = _Reduction.of(null, (a, b)->a==null?b:a.ordinal()>b.ordinal()?a:b);
        val markedForRemoval = _Lists.newArrayList(facetsByPrecedence.size());

        facetsByPrecedence.forEach((precedence, facets)->{
            facets.removeIf(_Casts.uncheckedCast(filter));
            if(!facets.isEmpty()) {
                top.accept(precedence);
            } else {
                markedForRemoval.add(precedence);
            }
        });

        topPrecedenceRef.set(top.getResult().orElse(null));

        // remove keys that associate empty lists, so finding highest used precedence by key is simple
        markedForRemoval.forEach(facetsByPrecedence::remove);
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

        val topRankingFacets = getTopRank(facetType);

        if(topRankingFacets.isCardinalityMultiple()) {

            val firstOfTopRanking = topRankingFacets.getFirstOrFail();

            topRankingFacets
            .stream()
            .skip(1)
            .forEach(next->visitor.accept(firstOfTopRanking, next));
        }

    }


}
