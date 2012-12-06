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
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class CollectionFacetUtils {
    private CollectionFacetUtils() {
    }

    public static CollectionFacet getCollectionFacetFromSpec(final ObjectAdapter objectRepresentingCollection) {
        final ObjectSpecification collectionSpec = objectRepresentingCollection.getSpecification();
        return collectionSpec.getFacet(CollectionFacet.class);
    }

    public static int size(final ObjectAdapter collection) {
        final CollectionFacet facet = getCollectionFacetFromSpec(collection);
        return facet.size(collection);
    }

    public static ObjectAdapter firstElement(final ObjectAdapter collection) {
        final CollectionFacet facet = getCollectionFacetFromSpec(collection);
        return facet.firstElement(collection);
    }

    /**
     * @deprecated - use instead {@link #convertToList(ObjectAdapter)}.
     */
    @Deprecated
    public static Object[] convertToArray(final ObjectAdapter collection) {
        return convertToList(collection).toArray();
    }

    public static List<Object> convertToList(final ObjectAdapter collection) {
        final CollectionFacet facet = getCollectionFacetFromSpec(collection);
        final List<Object> objects = new ArrayList<Object>();
        for (final ObjectAdapter adapter : facet.iterable(collection)) {
            objects.add(adapter.getObject());
        }
        return objects;
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
