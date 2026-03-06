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
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.core.metamodel.facetapi.QualifiedFacet.Key;

/**
 * Multiple {@link FacetRank}(s) are collected into a single {@link FacetRanking}.
 *
 * @apiNote thread-safe
 */
record FacetRank<F extends Facet>(
        Class<F> facetType,
        Facet.Precedence precedence,
        _Multimaps.ListMultimap<QualifiedFacet.Key, F> facetsByQualifier) {

    FacetRank(
            final Class<F> facetType,
            final Facet.Precedence precedence) {
        this(facetType, precedence, _Multimaps.newConcurrentListMultimap());
    }

    QualifiedFacet.Key key(final @Nullable String qualifier) {
        return new QualifiedFacet.Key(facetType, qualifier);
    }

    /**
     * If given {@link Facet} is non-null, it is appended the proper lane (one of):
     * <ul>
     * <li><code>[qualifier==null]: Facet is <b>not</b> an instance of {@link QualifiedFacet}</code></li>
     * <li><code>[qualifier==""]: Facet is an instance of {@link QualifiedFacet} with an <b>empty qualifier</b></code></li>
     * <li><code>[!qualifier.isEmpty()]: Facet is an instance of {@link QualifiedFacet} with a <b>populated qualifier</b></code></li>
     * </ul>
     */
    FacetRank<F> add(final @Nullable F facet) {
        if(facet==null)
            return this; // no-op

        _Assert.assertEquals(this.precedence(), facet.precedence());
        _Assert.assertEquals(this.facetType(), facet.facetType());

        facetsByQualifier.putElement(QualifiedFacet.Key.forFacet(facet), facet);
        return this;
    }

    /**
     * Removes all matching facets from the underlying collections.
     */
    void purgeIf(
            final QualifiedFacet.@NonNull Key qualifierKey,
            final @NonNull Predicate<? super F> facetFilter) {
        lookup(qualifierKey)
            .ifPresent(list->list.removeIf(facetFilter));
    }

    /**
     * Returns in sequence:
     * <ul>
     * <li>all matching {@link QualifiedFacet}(s)</li>
     * <li>non-matching {@link QualifiedFacet}(s) are ignored</li>
     * <li>all Facets which are <b>not</b> an instance of {@link QualifiedFacet}</li>
     * </ul>
     */
    Can<F> facetsMatching(final Key key) {
        return lookupQualified(key)
            .filter(list->!list.isEmpty())
            .or(()->lookupUnqualified(key)
                    .filter(list->!list.isEmpty()))
            .map(Can::<F>ofCollection)
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
    Optional<F> findBest(final QualifiedFacet.Key key) {
        return lookupQualified(key)
            .flatMap(_Lists::lastElement)
            .or(()->lookupUnqualified(key)
                    .flatMap(_Lists::lastElement));
    }

    /**
     * Whether this rank contains at least one matching {@link QualifiedFacet}.
     */
    boolean hasAny(final QualifiedFacet.Key key) {
        return hasElements(lookupQualified(key))
            || hasElements(lookupUnqualified(key));
    }

    // -- HELPER

    private Optional<List<F>> lookup(final QualifiedFacet.Key key) {
        return Optional.ofNullable(facetsByQualifier.get(key));
    }
    private Optional<List<F>> lookupQualified(final QualifiedFacet.Key key) {
        return Optional.ofNullable(facetsByQualifier.get(key.toQualified()));
    }
    private Optional<List<F>> lookupUnqualified(final QualifiedFacet.Key key) {
        return Optional.ofNullable(facetsByQualifier.get(key.toUnqualified()));
    }
    private static <T> boolean hasElements(final Optional<List<T>> listOpt) {
        return listOpt
            .map(list->!list.isEmpty())
            .orElse(false);
    }

}
