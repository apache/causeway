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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Maps.AliasMap;
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
public class FacetHolderImpl implements FacetHolder, MetaModelContextAware {

    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    private final Map<Class<? extends Facet>, Facet> facetsByType = _Maps.newHashMap();
    private final Object $lock = new Object();

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        synchronized($lock) {
            return snapshot.get().containsKey(facetType);
        }
    }

    @Override
    public void addFacet(Facet facet) {
        synchronized($lock) {
            val changed = addFacetOrKeepExistingBasedOnPrecedence(facetsByType, facet);
            if(changed) {
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
            return snapshot.get().values().stream(); // consumers should play nice and don't take too long
        }
    }

    @Override
    public int getFacetCount() {
        synchronized($lock) {
            return snapshot.get().size();
        }
    }

    @Override
    public void addOrReplaceFacet(Facet facet) {
        synchronized($lock) {
            val facetType = facet.facetType();
            val existingFacet = getFacet(facetType);
            if (existingFacet != null) {
                remove(existingFacet);
                val underlyingFacet = existingFacet.getUnderlyingFacet();
                facet.setUnderlyingFacet(underlyingFacet);
            }

            addFacet(facet);
        }
    }

    // -- HELPER

    private final _Lazy<Map<Class<? extends Facet>, Facet>> snapshot = _Lazy.threadSafe(this::snapshot);

    // collect all facet information provided with the top-level facets (contributed facets and aliases)
    private Map<Class<? extends Facet>, Facet> snapshot() {
        val snapshot = _Maps.<Class<? extends Facet>, Facet>newAliasMap(HashMap::new);
        facetsByType.values().forEach(topLevelFacet->{

            snapshot.remap(
                    topLevelFacet.facetType(),
                    Can.ofNullable(topLevelFacet.facetAliasType()),
                    topLevelFacet);

            // honor contributed facets via recursive lookup
            collectChildren(snapshot, topLevelFacet);

        });
        return snapshot;
    }

    private void collectChildren(AliasMap<Class<? extends Facet>, Facet> target, Facet parentFacet) {
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

        val existingFacet = facetsByType.get(newFacet.facetType());
        if(existingFacet==null) {
            facetsByType.put(newFacet.facetType(), newFacet);
            return true;
        }

        val preferredFacet = getPreferredOf(existingFacet, newFacet);
        if(newFacet==preferredFacet) {
            facetsByType.put(preferredFacet.facetType(), preferredFacet);
            return true;
        }
        return false;
    }

    private void remove(Facet topLevelFacet) {
        snapshot.clear(); //invalidate
        facetsByType.remove(topLevelFacet.facetType());
    }

    // on equal precedence returns b
    private Facet getPreferredOf(final @NonNull Facet a, final @NonNull Facet b) {

        // guard against args being the same object
        if(a==b) {
            return a;
        }

        if(a.getPrecedence() == b.getPrecedence()) {
            log.warn("Facets {} and {} have same precedence. Undecidable, which to use. "
                    + "Arbitrarily chosing the latter.",
                    a.getClass().getName(),
                    b.getClass().getName());
            return b;
        }

        return a.getPrecedence().ordinal() < b.getPrecedence().ordinal()
                ? b
                : a;
    }

}
