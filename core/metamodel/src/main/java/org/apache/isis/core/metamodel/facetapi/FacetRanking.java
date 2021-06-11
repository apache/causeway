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

import java.util.Objects;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Multimaps;

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

    /**
     * @return whether the top rank changed,
     * that is whether the winning facet should be reconsidered as a consequence of this call
     */
    public boolean add(final @NonNull Facet facet) {
        val facetType = facet.facetType();
        _Assert.assertEquals(this.facetType, facetType);

        val currentTopOrdinal = facetsByPrecedence.isEmpty()
                ? -1
                : facetsByPrecedence.asNavigableMapElseFail().lastKey().ordinal();

        // guard against invalidly mocked facets
        Objects.requireNonNull(facet.getPrecedence(),
                ()->String.format("facet %s declares no precedence", facet.getClass()));

        val changesTopRank = facet.getPrecedence().ordinal() >= currentTopOrdinal;

        // as an optimization (heap), don't store to lower ranks, as these have no effect any way when picking a winning facet
        if(changesTopRank) {
            facetsByPrecedence.putElement(facet.getPrecedence(), facet);
        }

        return changesTopRank;
    }

    /**
     * Returns a defensive copy of the top rank.
     * @param <F>
     * @param facetType - for convenience, so the caller does not need to cast the result
     */
    public <F extends Facet> Can<F> getTopRank(final @NonNull Class<? extends Facet> facetType) {
        _Assert.assertEquals(this.facetType, facetType);
        val topRankedFacets = facetsByPrecedence.asNavigableMapElseFail().lastEntry();
        return topRankedFacets!=null
                ? Can.<F>ofCollection(_Casts.uncheckedCast(topRankedFacets.getValue()))
                : Can.empty();
    }


//    /**
//     * @param <F>
//     * @param facetType - for convenience, so the caller does not need to cast the result
//     */
//    public <F extends Facet> Optional<F> getWinningFacet(final @NonNull Class<? extends Facet> facetType) {
//        _Assert.assertEquals(this.facetType, facetType);
//        val topRankedFacets = facetsByPrecedence.asNavigableMapElseFail().lastEntry();
//
//        return topRankedFacets!=null
//                ? Optional.ofNullable(
//                        _Casts.uncheckedCast(
//                                winningFacetAmongSamePrecedence(topRankedFacets.getValue())))
//                : Optional.empty();
//
//    }
//
//    // -- HELPER
//
//    private Optional<Facet> winningFacetAmongSamePrecedence(final @Nullable List<Facet> facets) {
//        return _NullSafe.isEmpty(facets)
//                ? Optional.empty()
//                : Optional.of(facets.get(0)); // TODO resolve conflicting semantics if any, when there are multiple facets
//    }




}
