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

package org.apache.isis.metamodel.facets.collections.modify;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * Attached to {@link ObjectSpecification}s that represent a collection.
 *
 * <p>
 * Factories of (implementations of this) facet should ensure that a
 * {@link TypeOfFacet} is also attached to the same facet holder. The
 * {@link #getTypeOfFacet()} is a convenience for this.
 */
public interface CollectionFacet extends Facet {

    int size(ManagedObject collectionAdapter);

    /**
     * @param collectionAdapter
     * @return Stream of specified {@code collectionAdapter}'s elements 
     * (typically the elements of a collection or array)
     * @since 2.0
     */
    <T extends ManagedObject> Stream<T> stream(T collectionAdapter);

    default <T extends ManagedObject> T firstElement(T collectionAdapter) {
        return stream(collectionAdapter).findFirst().orElse(null);
    }

    /**
     * Set the contents of the collection (POJO) as provided by the optional supplier.
     * <p>
     * 
     * @param emptyCollectionPojoFactory empty collection or array factory
     * @param collectionSpec
     * @param elements
     * @param elementCount
     * @return a possibly new instance
     * @since 2.0
     */
    <T extends ManagedObject> Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory, 
            ObjectSpecification collectionSpec, 
            Stream<T> elements, 
            int elementCount);

    /**
     * Convenience method that returns the {@link TypeOfFacet} on this facet's
     * {@link #getFacetHolder() holder}.
     */
    TypeOfFacet getTypeOfFacet();

    public static class Utils {

        public static CollectionFacet getCollectionFacetFromSpec(ManagedObject objectRepresentingCollection) {
            val collectionSpec = objectRepresentingCollection.getSpecification();
            return collectionSpec.getFacet(CollectionFacet.class);
        }

        public static int size(ManagedObject collection) {
            val collectionFacet = getCollectionFacetFromSpec(collection);
            return collectionFacet.size(collection);
        }

        public static <T extends ManagedObject> Stream<T> streamAdapters(T collectionAdapter) {
            val collectionFacet = getCollectionFacetFromSpec(collectionAdapter);
            return Utils.<T>downCast(collectionFacet.stream(collectionAdapter));
        }

        public static <T extends ManagedObject> List<T> toAdapterList(T collectionAdapter) {
            return streamAdapters(collectionAdapter)
                    .collect(Collectors.toList());
        }

        private static <T extends ManagedObject> Stream<T> downCast(Stream<ManagedObject> stream) {
            final Function<ManagedObject, T> uncheckedCast = _Casts::uncheckedCast;
            return stream.map(uncheckedCast);
        }

    }

}
