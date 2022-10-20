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
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

public interface HasFacetHolder extends FacetHolder {

    // -- INTERFACE

    FacetHolder getFacetHolder();

    // -- SHORTCUTS

    @Override
    default MetaModelContext getMetaModelContext() {
        return getFacetHolder().getMetaModelContext();
    }

    @Override
    default Identifier getFeatureIdentifier() {
        return getFacetHolder().getFeatureIdentifier();
    }

    @Override
    default int getFacetCount() {
        return getFacetHolder().getFacetCount();
    }

    @Override
    default <T extends Facet> T getFacet(final Class<T> cls) {
        return getFacetHolder().getFacet(cls);
    }

    @Override
    default boolean containsFacet(final Class<? extends Facet> facetType) {
        return getFacetHolder().containsFacet(facetType);
    }

    @Override
    default Stream<Facet> streamFacets() {
        return getFacetHolder().streamFacets();
    }

    @Override
    default void addFacet(final Facet facet) {
        getFacetHolder().addFacet(facet);
    }

    @Override
    default Stream<FacetRanking> streamFacetRankings() {
        return getFacetHolder().streamFacetRankings();
    }

    @Override
    default Optional<FacetRanking> getFacetRanking(final Class<? extends Facet> facetType) {
        return getFacetHolder().getFacetRanking(facetType);
    }


}
