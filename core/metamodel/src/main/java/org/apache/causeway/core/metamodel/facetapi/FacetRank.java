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

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet.Key;

/**
 * Multiple {@link FacetRank}(s) are collected into a single {@link FacetRanking}.
 *
 * @apiNote not thread-safe
 */
record FacetRank(
        Class<? extends Facet> facetType,
        Facet.Precedence precedence,
        _Multimaps.ListMultimap<QualifiedFacet.Key, Facet> facetsByQualifier) {

    FacetRank(
            final Class<? extends Facet> facetType,
            final Facet.Precedence precedence) {
        this(facetType, precedence, _Multimaps.newListMultimap());
        //this(facetType, precedence, _Multimaps.newListMultimap(ConcurrentSkipListMap::new, CopyOnWriteArrayList::new));
    }

    QualifiedFacet.Key key(final @Nullable String qualifier) {
        return new QualifiedFacet.Key(facetType, qualifier);
    }

    FacetRank add(final @Nullable Facet facet) {
        if(facet==null)
            return this; // no-op

        _Assert.assertEquals(this.precedence(), facet.precedence());
        _Assert.assertEquals(this.facetType(), facet.facetType());

        facetsByQualifier.putElement(QualifiedFacet.Key.forFacet(facet), facet);
        return this;
    }

    /**
     * Whether this rank contains at least one matching {@link QualifiedFacet}.
     */
    boolean matches(final String qualifier) {
        return facetsByQualifier.containsKey(key(qualifier));
    }

    Can<Facet> facetsMatching(final Key key) {
        return lookupQualified(key)
            .filter(list->!list.isEmpty())
            .or(()->lookupUnqualified(key)
                    .filter(list->!list.isEmpty()))
            .map(Can::ofCollection)
            .orElseGet(Can::empty);
    }

    /**
     * Rules in order of strength:
     * <ul>
     * <li>all matching {@link QualifiedFacet}(s) take precedence</li>
     * <li>all non-matching {@link QualifiedFacet}(s) must be ignored</li>
     * <li>later take precedence over earlier</li>
     * </ul>
     */
    Optional<Facet> findBest(final QualifiedFacet.Key key) {
        return lookupQualified(key)
            .flatMap(_Lists::lastElement)
            .or(()->lookupUnqualified(key)
                    .flatMap(_Lists::lastElement));
    }

    boolean hasBest(final QualifiedFacet.Key key) {
        return isNotEmpty(lookupQualified(key))
            || isNotEmpty(lookupUnqualified(key));
    }

    // -- HELPER

    private Optional<List<Facet>> lookupQualified(final QualifiedFacet.Key key) {
        return Optional.ofNullable(facetsByQualifier.get(key.toQualified()));
    }
    private Optional<List<Facet>> lookupUnqualified(final QualifiedFacet.Key key) {
        return Optional.ofNullable(facetsByQualifier.get(key.toUnqualified()));
    }
    private static <T> boolean isNotEmpty(final Optional<List<T>> listOpt) {
        return listOpt
            .map(list->!list.isEmpty())
            .orElse(false);
    }

}
