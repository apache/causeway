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

import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Instances of {@link TypedFacetRanking} are shared among {@link Facet}(s)
 *  of same {@link Facet#facetType() facetType} in the context of a single
 * {@link FacetHolder} instance.
 *
 * <p>Records the history of adding {@link Facet}(s) of same
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
public final class TypedFacetRanking<F extends Facet> {

    @Getter @Accessors(fluent = true) private final @NonNull Class<F> facetType;

    private final NavigableMap<Facet.Precedence, FacetRank<F>> ranksByPrecedence = new ConcurrentSkipListMap<>();

    private final @NonNull AtomicReference<F> eventFacetRef = new AtomicReference<>();
    //private final @NonNull Map<QualifiedFacet.Key, Precedence> topPrecedenceRef = new ConcurrentHashMap<>();

    /**
     * @return whether the top rank changed,
     * that is whether the winning facet should be reconsidered as a consequence of this call
     */
    public void add(final @NonNull F facet) {
        var facetType = facet.facetType();
        _Assert.assertEquals(this.facetType, facetType);

        // guard against invalidly mocked facets
        var facetPrecedence = Objects.requireNonNull(facet.precedence(),
                ()->String.format("facet %s declares no precedence", facet.getClass()));

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

            //topPrecedenceRef.put(key, facetPrecedence);
            return;//true; // changes apply
        }

//        var currentTopRankOrdinal = ranksByPrecedence.isEmpty() //FIXME needs context
//                ? -1
//                : ranksByPrecedence.lastKey().ordinal();
//
//        var changesTopRank = facetPrecedence.ordinal() >= currentTopRankOrdinal;

        // As an optimization (heap), don't store to lower ranks,
        // as these are not considered when picking a winning facet.
        // However, there are use-cases, where access to all facets of a given type are required,
        // regardless of facet-precedence (eg. MemberNamedFacets).
//        if(changesTopRank
//                || facet.isPopulateAllFacetRanks()) {
            var rank = ranksByPrecedence
                    .computeIfAbsent(facetPrecedence, __->new FacetRank<>(facetType(), facetPrecedence));
            rank.add(facet);
//        }
//
//        return changesTopRank;
    }

    public void addAll(final @NonNull TypedFacetRanking<F> facetRanking) {
        facetRanking.ranksByPrecedence
            .forEach((k, rank)->rank.facetsByQualifier().streamElements().forEach(this::add));
    }

    /**
     * Optionally returns the winning facet, considering the event facet (if any) and the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public Optional<F> getWinner() {
        var eventFacet = getEventFacet();
        return eventFacet.isPresent()
            ? eventFacet
            : getWinnerNonEvent();
    }

    /**
     * Optionally returns the winning facet, considering the top rank,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public Optional<F> getWinnerNonEvent() {
        var topRank = topRankInternal();
        return topRank!=null
                ? topRank.findBest(key())
                : Optional.empty();
    }

    /**
     * Optionally returns the winning facet, considering only the rank with selected precedence constraints,
     * based on whether there was any added that has given facetType.
     * @param facetType - for convenience, so the caller does not need to cast the result
     * @param precedenceUpper - upper bound
     */
    public Optional<F> getWinnerNonEventLowerOrEqualTo(final @NonNull Precedence precedenceUpper) {
        var key = key();
        var selectedRank = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);
        return selectedRank
                .map(ranksByPrecedence::get)
                .flatMap(rank->rank.findBest(key));
    }

    /**
     * Optionally returns the top ranking event facet, based on whether there was one added.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public Optional<F> getEventFacet() {
        return Optional.ofNullable(eventFacetRef.get());
    }

    /**
     * Returns a defensive copy of the top rank.
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public Can<F> getTopRank() {
        var key = key();
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(rank.hasAny(key))
                return rank.facetsMatching(key);
        }
        return Can.empty();
    }

    /**
     * Returns a defensive copy of the selected rank of given precedence constraint.
     * @param facetType - for convenience, so the caller does not need to cast the result
     * @param precedenceUpper - upper bound
     */
    public Can<F> getRankLowerOrEqualTo(final @NonNull Precedence precedenceUpper) {
        var key = key();
        var precedenceSelected = getHighestPrecedenceLowerOrEqualTo(precedenceUpper);
        return precedenceSelected
            .map(ranksByPrecedence::get)
            .map(rank->rank.facetsMatching(key))
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
            if(rank.hasAny(key))
                return Optional.of(rank.precedence());
        }
        return Optional.empty();
    }

    public Optional<Facet.Precedence> getTopPrecedence() {
        return Optional.ofNullable(topRankInternal())
            .map(FacetRank::precedence);
    }

    // -- DYNAMIC UPDATE SUPPORT

    /**
     * Removes any facet of {@code facetType} from facetHolder if it passes the given {@code filter}.
     */
    public void purgeIf(final @NonNull Predicate<? super F> facetFilter, final Predicate<Facet.Precedence> precedenceFilter) {
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(!precedenceFilter.test(rank.precedence())) {
                continue; //next
            }
            rank.purgeIf(facetFilter);
        }
    }

    // -- HELPER

    QualifiedFacet.Key key() {
        return new QualifiedFacet.Key(facetType, Facets.qualifier(null));
    }

    @Nullable
    FacetRank<F> topRankInternal() {
        var key = key();
        for(var rank : ranksByPrecedence.descendingMap().values()) {
            if(rank.hasAny(key))
                return rank;
        }
        return null;
    }

}
