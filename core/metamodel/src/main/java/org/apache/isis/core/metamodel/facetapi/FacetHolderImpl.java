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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * For base subclasses or, more likely, to help write tests.
 */
public class FacetHolderImpl implements FacetHolder {

    private final Map<Class<? extends Facet>, Facet> facetsByClass = new HashMap<Class<? extends Facet>, Facet>();

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return getFacet(facetType) != null;
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
    public void addFacet(final MultiTypedFacet facet) {
        final Class<? extends Facet>[] facetTypes = facet.facetTypes();
        for (final Class<? extends Facet> facetType : facetTypes) {
            addFacet(facetType, facet.getFacet(facetType));
        }
    }

    private void addFacet(final Class<? extends Facet> facetType, final Facet facet) {
        final Facet existingFacet = getFacet(facetType);
        if (existingFacet == null || existingFacet.isNoop()) {
            facetsByClass.put(facetType, facet);
            return;
        }
        if (!facet.alwaysReplace()) {
            return;
        }
        if (facet.isDerived() && !existingFacet.isDerived()) {
            return;
        }
        facet.setUnderlyingFacet(existingFacet);
        facetsByClass.put(facetType, facet);
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
    @SuppressWarnings("unchecked")
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return (T) facetsByClass.get(facetType);
    }

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return FacetUtil.getFacetTypes(facetsByClass);
    }

    @Override
    public List<Facet> getFacets(final Predicate<Facet> predicate) {
        return FacetUtil.getFacets(facetsByClass, predicate);
    }

}
