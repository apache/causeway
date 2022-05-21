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

import org.apache.isis.applib.Identifier;

import lombok.NonNull;

/**
 * Supposed to provide a merged view of the local and the parent-layer,
 * where the parent is used as read-only fallback.
 *
 * @apiNote It is still not clear to me why we do need this at all,
 * why not just simply delegate/wrap?
 */
class FacetHolderLayered
extends FacetHolderAbstract {

    private final @NonNull FacetHolder parentLayer;

    public FacetHolderLayered(
            final @NonNull Identifier featureIdentifier,
            final @NonNull FacetHolder parentLayer) {
        super(parentLayer.getMetaModelContext(), featureIdentifier);
        this.parentLayer = parentLayer;

        // legacy implementation, it will miss any changes that happen to the parent-layer after copying
        copyFacetsTo(parentLayer, this);
    }

   /* XXX first attempt on an implementation, but does not handle facet precedence correctly
    @Override
    public int getFacetCount() {
        // cannot simply add up this and parent
        return (int)streamFacets().count();
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return Optional.ofNullable(super.getFacet(facetType))
                .orElse(parentLayer.getFacet(facetType));
    }

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return super.containsFacet(facetType)
                || parentLayer.containsFacet(facetType);
    }

    @Override
    public Stream<Facet> streamFacets() {
        val localFacetTypes = new HashSet<Class<? extends Facet>>();
        return Stream.concat(
                super.streamFacets()
                .peek(facet->localFacetTypes.add(facet.facetType()))
                ,
                parentLayer.streamFacets()
                .filter(facet->!localFacetTypes.contains(facet.facetType())));
    }

    @Override
    public Stream<FacetRanking> streamFacetRankings() {
        val localFacetTypes = new HashSet<Class<? extends Facet>>();
        return Stream.concat(
                super.streamFacetRankings()
                .peek(ranking->localFacetTypes.add(ranking.facetType()))
                ,
                parentLayer.streamFacetRankings()
                .filter(ranking->!localFacetTypes.contains(ranking.facetType())));
    }

    @Override
    public Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        return Optional.ofNullable(super.getFacetRanking(facetType))
                .orElse(parentLayer.getFacetRanking(facetType));
    }*/

    // -- HELPER

    private static void copyFacetsTo(final FacetHolder source, final FacetHolder target) {
        source.streamFacets()
        .forEach(target::addFacet);
    }

}
