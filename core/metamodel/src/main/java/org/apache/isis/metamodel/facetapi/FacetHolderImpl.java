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

package org.apache.isis.metamodel.facetapi;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContextAware;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * For base subclasses or, more likely, to help write tests.
 */
public class FacetHolderImpl implements FacetHolder, MetaModelContextAware {

    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;
    
    private final Map<Class<? extends Facet>, Facet> facetsByClass = new ConcurrentHashMap<>();
    private final _Lazy<Set<Facet>> snapshot = _Lazy.threadSafe(this::snapshot);

    private Set<Facet> snapshot() {
        val snapshot = _Sets.<Facet>newHashSet();
        facetsByClass.values().forEach(snapshot::add);
        return snapshot;
    }
    
    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return facetsByClass.containsKey(facetType);
    }

    @Override
    public void addFacet(Facet facet) {
        addFacet(facet.facetType(), facet);
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> facetType) {
        return uncheckedCast(facetsByClass.get(facetType));
    }

    @Override
    public Stream<Facet> streamFacets() {
        synchronized(snapshot) {
            return snapshot.get().stream(); // consumers should play nice and don't take too long  
        }
    }

    @Override
    public int getFacetCount() {
        synchronized(snapshot) {
            return snapshot.get().size();    
        }
    }
    
    @Override
    public void addOrReplaceFacet(Facet facet) {
        
        Optional.ofNullable(getFacet(facet.facetType()))
        .filter(each -> facet.getClass() == each.getClass())
        .ifPresent(existingFacet -> {
            remove(existingFacet);
            val underlyingFacet = existingFacet.getUnderlyingFacet();
            facet.setUnderlyingFacet(underlyingFacet);
        } );
        
        addFacet(facet);
    }

    // -- HELPER

    private void addFacet(Class<? extends Facet> facetType, Facet facet) {
        val existingFacet = getFacet(facetType);
        if (existingFacet == null || existingFacet.isFallback()) {
            put(facetType, facet);
            return;
        }
        if (!facet.alwaysReplace()) {
            return; //eg. ValueSemanticsProviderAndFacetAbstract is alwaysReplace=false
        }
        if (facet.isDerived() && !existingFacet.isDerived()) {
            return;
        }
        facet.setUnderlyingFacet(existingFacet);
        put(facetType, facet);
    }

    private void put(Class<? extends Facet> facetType, Facet facet) {
        synchronized(snapshot) {
            snapshot.clear();
            facetsByClass.put(facetType, facet);
            facet.forEachAlias(aliasType->facetsByClass.put(aliasType, facet));
        }
    }
    
    private void remove(Facet facet) {
        synchronized(snapshot) {
            snapshot.clear();
            facetsByClass.remove(facet.facetType());
            // for all the registered aliases that point to the given facet, remove ...
            facet.forEachAlias(aliasType->{
                val aliasFor = facetsByClass.get(aliasType);
                if(facet == aliasFor) {
                    facetsByClass.remove(aliasType);
                }
            });
        }
    }
    

}
