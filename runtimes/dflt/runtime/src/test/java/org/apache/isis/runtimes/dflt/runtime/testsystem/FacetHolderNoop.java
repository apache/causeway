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


package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;

import com.google.common.collect.Lists;


/**
 * Has no functionality but makes it easier to write tests that require an instance of an {@link Identifier}.
 */
public class FacetHolderNoop implements FacetHolder {

    @Override
    public void addFacet(final Facet facet) {}

    @Override
    public void addFacet(final MultiTypedFacet facet) {}

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return false;
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[0];
    }

    @Override
    public List<Facet> getFacets(final Filter<Facet> filter) {
        return Lists.newArrayList();
    }

    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public void removeFacet(final Facet facet) {}

    @Override
    public void removeFacet(final Class<? extends Facet> facetType) {}

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.facetapi.FacetHolder#containsDoOpFacet(java.lang.Class)
     */
    @Override
    public boolean containsDoOpFacet(Class<? extends Facet> facetType) {
        // TODO Auto-generated method stub
        return false;
    }

}

