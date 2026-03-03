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

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    private final NavigableMap<Facet.Precedence, FacetRank> ranksByPrecedence = new ConcurrentSkipListMap<>();

    private final @NonNull AtomicReference<Facet> eventFacetRef = new AtomicReference<>();
    private final @NonNull Map<QualifiedFacet.Key, Precedence> topPrecedenceRef = new ConcurrentHashMap<>();

    /**
     * @return whether the top rank changed,
     * that is whether the winning facet should be reconsidered as a consequence of this call
     */
    public boolean add(final @NonNull Facet facet) {
        var facetType = facet.facetType();
        _Assert.assertEquals(this.facetType, facetType);

        // guard against invalidly mocked facets
        var facetPrecedence = Objects.requireNonNull(facet.precedence(),
                ()->String.format("facet %s declares no precedence", facet.getClass()));
        var key = QualifiedFacet.Key.forFacet(facet);

        // handle top priority (EVENT) facets separately
        if(facetPrecedence.isEvent()) {
            eventFacetRef.getAndUpdate(previous->{
                if(previous!=null)
                    throw _Exceptions
                        .illegalState("cannot override an event facet %s with %s, "
                                + "must be unique per facet-holder and facet-type",
                                previous.getClass(),
                                facet.getClass());
                return facet;
            });

            topPrecedenceRef.put(key, facetPrecedence);
            return true; // changes apply
        }

        var currentTopOrdinal = getTopPrecedence()
                .map(Precedence::ordinal)
                .orElse(-1);

        if(facetPrecedence.ordinal() > currentTopOrdinal) {
            topPrecedenceRef.put(key, facetPrecedence);
        }

        var currentTopRankOrdinal = ranksByPrecedence.isEmpty() //FIXME needs context
                ? -1
                : ranksByPrecedence.lastKey().ordinal();

        var changesTopRank = facetPrecedence.ordinal() >= currentTopRankOrdinal;

        // As an optimization (heap), don't store to lower ranks,
        // as these are not considered when picking a winning facet.
        // However, there are use-cases, where access to all facets of a given type are required,
        // regardless of facet-precedence (eg. MemberNamedFacets).
        if(changesTopRank
                || facet.isPopulateAllFacetRanks()) {
            var rank = ranksByPrecedence
                    .computeIfAbsent(facetPrecedence, __->new FacetRank(facetType(), facetPrecedence));
            rank.add(facet);
        }

        return changesTopRank;
    }

    public void addAll(final @NonNull FacetRanking facetRanking) {
        facetRanking.ranksByPrecedence
            .forEach((k, rank)->rank.facetsByQualifier().streamElements().forEach(this::add));
    }

    /**
     * Optionally returns the winning facet, considering the event facet (if any) and the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getWinner(
            final @NonNull Class<F> facetType) {
        var eventFacet = getEventFacet(facetType);
        return eventFacet.isPresent()
            ? eventFacet
            : getWinnerNonEvent(facetType);
    }

    /**
     * Optionally returns the winning facet, considering the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Optional<F> getWinnerNonEvent(
            final @NonNull Class<F> facetType) {
        var topRank = topRankInternal();
        return topRank!=null
                ? (Optional<F>) topRank.findBest(key())
                : Optional.empty();
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
        var key = key();
        var selectedRank = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);
        return selectedRank
                .map(ranksByPrecedence::get)
                .flatMap(rank->rank.findBest(key))
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
        var key = key();
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(rank.hasBest(key))
                return (Can<F>) rank.facetsMatching(key);
        }
        return Can.empty();
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

        var key = key();
        var precedenceSelected = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);

        return precedenceSelected
            .map(ranksByPrecedence::get)
            .flatMap(rank->rank.findBest(key))


            .map(facetsOfSameRank->Can.<F>ofCollection(_Casts.uncheckedCast(facetsOfSameRank)))
            .orElseGet(Can::empty);
    }

    /**
     * Returns highest found precedence within ranks that conforms to given precedence constraint.
     * @param precedenceUpper - upper bound
     */
    public Optional<Precedence> getHighestPrecedenceLowerOrEqualTo(final @NonNull Precedence precedenceUpper) {
        var key = key();
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(rank.precedence().ordinal()>precedenceUpper.ordinal()) {
                continue; //next
            }
            if(rank.hasBest(key))
                return Optional.of(rank.precedence());
        }
        return Optional.empty();
    }

    public Optional<Facet.Precedence> getTopPrecedence() {
        var key = key();
        return Optional.ofNullable(topPrecedenceRef.get(key));
    }

    // -- DYNAMIC UPDATE SUPPORT

//    /**
//     * Removes any facet of {@code facetType} from facetHolder if it passes the given {@code filter}.
//     * @param facetType - to ensure the filter is properly generic-type-constraint
//     * @param filter
//     */
//    public <F extends Facet> void purgeIf(
//            final @NonNull Class<F> facetType,
//            final @NonNull Predicate<? super F> filter) {
//
//        // reassess the top precedence
//        final _Reduction<Facet.Precedence> top = _Reduction.of(null, (a, b)->a==null?b:a.ordinal()>b.ordinal()?a:b);
//        var markedForRemoval = _Lists.newArrayList(facetsByPrecedence.size());
//
//        facetsByPrecedence.forEach((precedence, facets)->{
//            facets.removeIf(_Casts.uncheckedCast(filter));
//            if(!facets.isEmpty()) {
//                top.accept(precedence);
//            } else {
//                markedForRemoval.add(precedence);
//            }
//        });
//
//        topPrecedenceRef.set(top.getResult().orElse(null));
//
//        // remove keys that associate empty lists, so finding highest used precedence by key is simple
//        markedForRemoval.forEach(facetsByPrecedence::remove);
//    }

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

    // -- HELPER

    QualifiedFacet.Key key() {
        return new QualifiedFacet.Key(facetType, Facets.qualifier(null));
    }

    @Nullable
    FacetRank topRankInternal() {
        var key = key();
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(rank.hasBest(key))
                return rank;
        }
        return null;
    }

    /**
     * Rules in order of strength:
     * <ul>
     * <li>all matching {@link QualifiedFacet}(s) take precedence</li>
     * <li>all non-matching {@link QualifiedFacet}(s) must be ignored</li>
     * <li>later take precedence over earlier</li>
     * </ul>
     */
    @Deprecated
    private static <F extends Facet> F findBestWithinRank(
            final @NonNull Class<F> facetType,
            final Can<? extends F> rank,
            final @Nullable String qualifier) {

        F bestNonQualified = null;
        for(var it = rank.reverseIterator(); it.hasNext(); ) {
            var facet = it.next();
            if(facet instanceof QualifiedFacet qFacet) {
                if(Objects.equals(qualifier, qFacet.qualifier()))
                    return facet;
                else {
                    continue;
                }
            }
            if(bestNonQualified==null) {
                bestNonQualified = facet;
            }
        }
        return bestNonQualified;
    }

}
