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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a (simple) list of {@link Facet}s.
 */
record FacetHolderSimple(
    MetaModelContext metaModelContext,
    Identifier featureIdentifier,
    Map<Class<? extends Facet>, FacetRanking> rankingByType)
implements FacetHolderInternal {

    public FacetHolderSimple(
            final @NonNull MetaModelContext metaModelContext,
            final Identifier featureIdentifier) {
        this(metaModelContext, featureIdentifier, new ConcurrentHashMap<>());
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
        return lookupFacet(facetType).isPresent();
    }

    @Override
    public void addFacet(final @Nullable Facet facet) {
    	if(facet==null)
    		return;
        rankingByType.computeIfAbsent(facet.facetType(), FacetRanking::new)
            .add(facet);
    }

    @Override
    public void removeFacet(@Nullable final Facet facet) {
    	if(facet==null)
    		return;
    	lookupFacetRanking(facet.facetType())
    		.ifPresent(ranking->{
    			ranking.remove(facet);
    		});
    }

    @Override
    public <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType) {
        return lookupFacetRanking(facetType)
            .flatMap(facetRanking->facetRanking.getWinner(facetType));
    }

    @Override
    public Stream<Facet> streamFacets() {
        return streamFacetRankings()
            .flatMap(facetRanking->facetRanking.getWinner(facetRanking.facetType())
                    .stream());
    }

    @Override
    public int getFacetCount() {
        return Math.toIntExact(streamFacets().count());
    }

    // -- VALIDATION SUPPORT

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        return rankingByType.values().stream();
    }

    @Override
    public Optional<FacetRanking> lookupFacetRanking(final Class<? extends Facet> facetType) {
        return Optional.ofNullable(rankingByType.get(facetType));
    }

}
