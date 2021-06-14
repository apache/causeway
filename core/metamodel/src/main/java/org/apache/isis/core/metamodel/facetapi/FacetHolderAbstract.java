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
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * For base subclasses or, more likely, to help write tests.
 */
@Log4j2
public abstract class FacetHolderAbstract
implements FacetHolder, MetaModelContextAware {

    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    // not private, as identifier might depend on lazily provided spec-loader
    @Getter(onMethod_ = {@Override}) protected Identifier identifier;

    private final Map<Class<? extends Facet>, FacetRanking> rankingByType = _Maps.newHashMap();
    private final Object $lock = new Object();

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
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
    public <T extends Facet> T getFacet(Class<T> facetType) {
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
    public Optional<FacetRanking> getFacetRanking(Class<? extends Facet> facetType) {
        return Optional.ofNullable(rankingByType.get(facetType));
    }

    // -- JUNIT SUPPORT

    public static FacetHolderAbstract simple(Identifier identifier) {
        final FacetHolderAbstract facetHolder = new FacetHolderAbstract() {};
        facetHolder.identifier = identifier;
        return facetHolder;
    }

    /**
     *  Meant for simple JUnit tests, that don't use the FacetHolder's identifier.
     */
    public static FacetHolderAbstract forTesting() {
        return simple(Identifier.classIdentifier(LogicalType.fqcn(Object.class)));
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

            // honor contributed facets via recursive lookup
            collectChildren(snapshot, winningFacet);

        });
        return snapshot;
    }

    private void collectChildren(Map<Class<? extends Facet>, Facet> target, Facet parentFacet) {
        parentFacet.forEachContributedFacet(child->{
            val added = addFacetOrKeepExistingBasedOnPrecedence(target, child);
            if(added) {
                collectChildren(target, child);
            }
        });
    }

    private boolean addFacetOrKeepExistingBasedOnPrecedence(
            final @NonNull Map<Class<? extends Facet>, Facet> facetsByType,
            final @NonNull Facet newFacet) {

        val facetType = newFacet.facetType();

        val existingFacet = facetsByType.get(facetType);
        if(existingFacet==null) {
            facetsByType.put(facetType, newFacet);
            return true; // changes
        }

        val preferredFacet = preferredOf(existingFacet, newFacet);
        if(newFacet==preferredFacet) {
            facetsByType.put(facetType, preferredFacet);
            return true; // changes
        }
        return false; // no changes
    }

    @Deprecated // introduced so can resolve initial conflicts
    private static Set<String> uniquePrecedenceWarnings = _Sets.newConcurrentHashSet();

    // on equal precedence returns b
    private Facet preferredOf(final @NonNull Facet a, final @NonNull Facet b) {

        // guard against args being the same object
        if(a==b) {
            return a;
        }

        // if args are semantically equal, prefer a
        if(a.semanticEquals(b)) {
            return a;
        }

        if(a.getPrecedence() == b.getPrecedence()) {

            val msg = a.getClass()==b.getClass()
                    ? String.format("Facets of identical type %s have equal semantics (precedence %s). "
                            + "Undecidable, which to use. "
                            + "Arbitrarily chosing the latter.",
                            friendlyName(a.getClass()),
                            a.getPrecedence().name())
                    : String.format("Facets %s and %s have same precedence %s. "
                            + "Undecidable, which to use. "
                            + "Arbitrarily chosing the latter.",
                            friendlyName(a.getClass()),
                            friendlyName(b.getClass()),
                            a.getPrecedence().name());

            if(uniquePrecedenceWarnings.add(msg)) {
                log.warn(msg);
            }

            return b;
        }

        return a.getPrecedence().ordinal() < b.getPrecedence().ordinal()
                ? b
                : a;
    }

    private static String friendlyName(Class<?> cls) {
        return cls.getName().replace("org.apache.isis", "o.a.i");
    }

}
