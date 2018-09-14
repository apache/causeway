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

package org.apache.isis.core.metamodel.facets.collections;

import java.util.Collection;
import java.util.Iterator;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;

public abstract class CollectionFacetAbstract extends FacetAbstract implements CollectionFacet {

    public CollectionFacetAbstract(final FacetHolder holder) {
        super(CollectionFacet.class, holder, Derivation.NOT_DERIVED);
    }

    @Override
    public final boolean contains(final ObjectAdapter collectionAdapter, final ObjectAdapter candidateObjectAdapter) {
        final Collection<ObjectAdapter> collection = collection(collectionAdapter);
        return collection.contains(candidateObjectAdapter);
    }

    @Override
    public final Iterator<ObjectAdapter> iterator(final ObjectAdapter collectionAdapter) {
        final Collection<ObjectAdapter> collection = collection(collectionAdapter);
        return collection.iterator();
    }

    @Override
    public Iterable<ObjectAdapter> iterable(final ObjectAdapter collectionAdapter) {
        return new Iterable<ObjectAdapter>() {
            @Override
            public Iterator<ObjectAdapter> iterator() {
                return CollectionFacetAbstract.this.iterator(collectionAdapter);
            }
        };
    }

    @Override
    public final TypeOfFacet getTypeOfFacet() {
        return getFacetHolder().getFacet(TypeOfFacet.class);
    }

}
