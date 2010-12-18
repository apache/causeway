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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.MultiTypedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public class JavaObjectActionParamPeer implements FacetHolder, ObjectActionParamPeer {

    private final ObjectSpecification specification;
    private final FacetHolderImpl holder = new FacetHolderImpl();

    public JavaObjectActionParamPeer(final ObjectSpecification specification) {
        this.specification = specification;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return specification;
    }

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return holder.getFacetTypes();
    }

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return holder.containsFacet(facetType);
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        return holder.getFacet(cls);
    }

    @Override
    public Facet[] getFacets(Filter<Facet> filter) {
        return holder.getFacets(filter);
    }

    @Override
    public void addFacet(Facet facet) {
        holder.addFacet(facet);
    }

    @Override
    public void addFacet(MultiTypedFacet facet) {
        holder.addFacet(facet);
    }

    @Override
    public void removeFacet(Facet facet) {
        holder.removeFacet(facet);
    }

    @Override
    public void removeFacet(Class<? extends Facet> facetType) {
        holder.removeFacet(facetType);
    }

}
