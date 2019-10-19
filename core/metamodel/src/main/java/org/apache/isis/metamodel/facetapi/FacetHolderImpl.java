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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContextAware;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.Getter;
import lombok.Setter;

/**
 * For base subclasses or, more likely, to help write tests.
 */
public class FacetHolderImpl implements FacetHolder, MetaModelContextAware {

    @Getter(onMethod = @__(@Override)) @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;
    
    private final Map<Class<? extends Facet>, Facet> facetsByClass = new ConcurrentHashMap<>();
    private final Set<Class<? extends Facet>> implementedFacetInterfaces = new HashSet<>();

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return facetsByClass.containsKey(facetType);
    }

    @Override
    public boolean containsFacetWithInterface(final Class<? extends Facet> facetType) {
        synchronized(implementedFacetInterfaces) {
            return implementedFacetInterfaces.contains(facetType);
        }
    }

    @Override
    public boolean containsDoOpFacet(final Class<? extends Facet> facetType) {
        final Facet facet = getFacet(facetType);
        return facet != null && !facet.isNoop();
    }

    @Override
    public boolean containsDoOpNotDerivedFacet(final Class<? extends Facet> facetType) {
        final Facet facet = getFacet(facetType);
        return facet != null && !facet.isNoop() && !facet.isDerived();
    }

    @Override
    public void addFacet(final Facet facet) {
        addFacet(facet.facetType(), facet);
    }

    @Override
    public void addFacet(final MultiTypedFacet mtFacet) {
        mtFacet.facetTypes()
        .forEach(facetType->addFacet(facetType, mtFacet.getFacet(facetType)));
    }

    @Override
    public void removeFacet(final Facet facet) {
        FacetUtil.removeFacet(facetsByClass, facet);
    }

    @Override
    public void removeFacet(final Class<? extends Facet> facetType) {
        FacetUtil.removeFacet(facetsByClass, facetType);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return uncheckedCast(facetsByClass.get(facetType));
    }

    @Override
    public Stream<Facet> streamFacets() {
        return facetsByClass.values()
                .stream()
                .distinct();
    }

    @Override
    public int getFacetCount() {
        return facetsByClass.size();
    }

    // -- HELPER

    private void addFacet(final Class<? extends Facet> facetType, final Facet facet) {
        final Facet existingFacet = getFacet(facetType);
        if (existingFacet == null || existingFacet.isNoop()) {
            put(facetType, facet);
            return;
        }
        if (!facet.alwaysReplace()) {
            return;
        }
        if (facet.isDerived() && !existingFacet.isDerived()) {
            return;
        }
        facet.setUnderlyingFacet(existingFacet);
        put(facetType, facet);
    }

    private void put(final Class<? extends Facet> facetType, final Facet facet) {
        facetsByClass.put(facetType, facet);

        synchronized(implementedFacetInterfaces) {
            _NullSafe.stream(facetType.getInterfaces())
            .filter(intfc->Facet.class.isAssignableFrom(intfc))
            .map(intfc-> _Casts.<Class<? extends Facet>>uncheckedCast(intfc))
            .forEach(implementedFacetInterfaces::add);
        }

    }

}
