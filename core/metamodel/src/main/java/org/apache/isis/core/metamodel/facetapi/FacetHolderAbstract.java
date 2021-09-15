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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * For base subclasses or, more likely, to help write tests.
 */
@AllArgsConstructor
@RequiredArgsConstructor
//@Log4j2
public abstract class FacetHolderAbstract
implements FacetHolder {

    // -- FACTORY

    public static FacetHolderAbstract simple(final MetaModelContext mmc, final Identifier featureIdentifier) {
        final FacetHolderAbstract facetHolder = new FacetHolderAbstract(mmc) {};
        facetHolder.featureIdentifier = featureIdentifier;
        return facetHolder;
    }

    // -- FIELDS

    @Getter(onMethod_ = {@Override}) private final @NonNull MetaModelContext metaModelContext;

    // not private nor final, as featureIdentifier might depend on lazily provided LogicalTypeFacet
    @Getter(onMethod_ = {@Override}) protected Identifier featureIdentifier;

    private final Map<Class<? extends Facet>, FacetRanking> rankingByType = _Maps.newHashMap();
    private final Object $lock = new Object();

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        synchronized($lock) {
            return snapshot.get().containsKey(facetType);
        }
    }

    @Override
    public void addFacet(final @NonNull Facet facet) {
        synchronized($lock) {

            val ranking = rankingByType.computeIfAbsent(facet.facetType(), FacetRanking::new);
            val needsInvalidate = ranking.add(facet);
            if(needsInvalidate) {
                snapshot.clear(); //invalidate
            }
        }
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        synchronized($lock) {
            return uncheckedCast(snapshot.get().get(facetType));
        }
    }

    @Override
    public Stream<Facet> streamFacets() {
        synchronized($lock) {
            // consumers should play nice and don't take too long (as we have a lock)
            return snapshot.get().values().stream();
        }
    }

    @Override
    public int getFacetCount() {
        synchronized($lock) {
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

    // -- JUNIT SUPPORT

    /**
     *  Meant for simple JUnit tests, that don't use the FacetHolder's identifier.
     */
    public static FacetHolderAbstract forTesting(final MetaModelContext mmc) {
        return simple(mmc, Identifier.classIdentifier(LogicalType.fqcn(Object.class)));
    }

    // -- HELPER

    private final _Lazy<Map<Class<? extends Facet>, Facet>> snapshot = _Lazy.threadSafe(this::snapshot);

    // collect all facet information provided with the top-level facets (contributed facets and aliases)
    private Map<Class<? extends Facet>, Facet> snapshot() {
        val snapshot = _Maps.<Class<? extends Facet>, Facet>newHashMap();
        rankingByType.values()
        .stream()
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
