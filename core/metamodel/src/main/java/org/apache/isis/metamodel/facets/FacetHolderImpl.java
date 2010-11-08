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


package org.apache.isis.metamodel.facets;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.filters.Filter;


/**
 * For base subclasses or, more likely, to help write tests.
 */
public class FacetHolderImpl implements FacetHolder {

    private final Map<Class<? extends Facet>, Facet> facetsByClass = new HashMap<Class<? extends Facet>, Facet>();

    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return getFacet(facetType) != null;
    }

    public void addFacet(final Facet facet) {
        addFacet(facet.facetType(), facet);
    }

    public void addFacet(final MultiTypedFacet facet) {
        final Class<? extends Facet>[] facetTypes = facet.facetTypes();
        for (int i = 0; i < facetTypes.length; i++) {
            addFacet(facetTypes[i], facet.getFacet(facetTypes[i]));
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

    public void removeFacet(final Facet facet) {
        FacetUtil.removeFacet(facetsByClass, facet);
    }

    public void removeFacet(final Class<? extends Facet> facetType) {
        FacetUtil.removeFacet(facetsByClass, facetType);
    }

    @SuppressWarnings("unchecked")
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return (T) facetsByClass.get(facetType);
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return FacetUtil.getFacetTypes(facetsByClass);
    }

    public Facet[] getFacets(final Filter<Facet> filter) {
        return FacetUtil.getFacets(facetsByClass, filter);
    }


}
