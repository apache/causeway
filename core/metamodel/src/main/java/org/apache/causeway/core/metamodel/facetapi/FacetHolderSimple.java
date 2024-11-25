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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import static org.apache.causeway.commons.internal.base._Casts.uncheckedCast;

import lombok.NonNull;

/**
 * Provides a (simple) list of {@link Facet}s.
 */
record FacetHolderSimple(
    MetaModelContext metaModelContext,
    Identifier featureIdentifier,
    Map<Class<? extends Facet>, FacetRanking> rankingByType,
    _Lazy<Map<Class<? extends Facet>, Facet>> snapshot
    )
implements FacetHolder {

    public FacetHolderSimple(
            final @NonNull MetaModelContext metaModelContext,
            final Identifier featureIdentifier) {
        this(metaModelContext, featureIdentifier, new HashMap<>());
    }

    private FacetHolderSimple(
        final @NonNull MetaModelContext metaModelContext,
        final Identifier featureIdentifier,
        final Map<Class<? extends Facet>, FacetRanking> rankingByType) {
        this(metaModelContext, featureIdentifier, rankingByType, _Lazy.threadSafe(()->makeSnapshot(rankingByType.values())));
    }

    // -- FIELDS

    @Override
    public MetaModelContext getMetaModelContext() {
        return metaModelContext;
    }

    @Override
    public Identifier getFeatureIdentifier() {
        return featureIdentifier;
    }

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        synchronized(rankingByType) {
            return snapshot.get().containsKey(facetType);
        }
    }

    @Override
    public void addFacet(final @NonNull Facet facet) {
        synchronized(rankingByType) {
            var ranking = rankingByType.computeIfAbsent(facet.facetType(), FacetRanking::new);
            var needsInvalidate = ranking.add(facet);
            if(needsInvalidate) {
                snapshot.clear(); //invalidate
            }
        }
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        synchronized(rankingByType) {
            return uncheckedCast(snapshot.get().get(facetType));
        }
    }

    @Override
    public Stream<Facet> streamFacets() {
        synchronized(rankingByType) {
            // consumers should play nice and don't take too long (as we have a lock)
            return snapshot.get().values().stream();
        }
    }

    @Override
    public int getFacetCount() {
        synchronized(rankingByType) {
            return snapshot.get().size();
        }
    }

    // -- VALIDATION SUPPORT

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        return rankingByType.values().stream();
    }

    @Override
    public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        return Optional.ofNullable(rankingByType.get(facetType));
    }

    // -- HELPER

    // collect all facet information provided with the top-level facets (contributed facets and aliases)
    private static Map<Class<? extends Facet>, Facet> makeSnapshot(final Collection<FacetRanking> rankings) {
        var snapshot =  new HashMap<Class<? extends Facet>, Facet>();
        rankings.stream()
        .map(facetRanking->facetRanking.getWinner(facetRanking.facetType()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(winningFacet->{

            snapshot.put(
                    winningFacet.facetType(),
                    winningFacet);

        });
        return snapshot;
    }

}
