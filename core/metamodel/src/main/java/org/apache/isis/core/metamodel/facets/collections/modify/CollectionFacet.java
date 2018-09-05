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

package org.apache.isis.core.metamodel.facets.collections.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Attached to {@link ObjectSpecification}s that represent a collection.
 *
 * <p>
 * Factories of (implementations of this) facet should ensure that a
 * {@link TypeOfFacet} is also attached to the same facet holder. The
 * {@link #getTypeOfFacet()} is a convenience for this.
 */
public interface CollectionFacet extends Facet {

    int size(ObjectAdapter collection);

    Iterable<ObjectAdapter> iterable(ObjectAdapter collectionAdapter);

    Iterator<ObjectAdapter> iterator(ObjectAdapter collectionAdapter);

    /**
     * Returns an unmodifiable {@link Collection} of {@link ObjectAdapter}s.
     */
    Collection<ObjectAdapter> collection(ObjectAdapter collectionAdapter);

    ObjectAdapter firstElement(ObjectAdapter collectionAdapter);

    boolean contains(ObjectAdapter collectionAdapter, ObjectAdapter element);

    /**
     * Set the contents of this collection.
     * @return a possibly new instance
     */
    ObjectAdapter init(ObjectAdapter collectionAdapter, ObjectAdapter[] elements);

    /**
     * Convenience method that returns the {@link TypeOfFacet} on this facet's
     * {@link #getFacetHolder() holder}.
     */
    TypeOfFacet getTypeOfFacet();


    public static class Utils {

        public static CollectionFacet getCollectionFacetFromSpec(final ObjectAdapter objectRepresentingCollection) {
            final ObjectSpecification collectionSpec = objectRepresentingCollection.getSpecification();
            return collectionSpec.getFacet(CollectionFacet.class);
        }

        public static int size(final ObjectAdapter collection) {
            final CollectionFacet facet = getCollectionFacetFromSpec(collection);
            return facet.size(collection);
        }

        public static List<ObjectAdapter> convertToAdapterList(final ObjectAdapter collection) {
            final CollectionFacet facet = getCollectionFacetFromSpec(collection);
            final List<ObjectAdapter> adapters = new ArrayList<ObjectAdapter>();
            for (final ObjectAdapter adapter : facet.iterable(collection)) {
                adapters.add(adapter);
            }
            return adapters;
        }

    }
}
