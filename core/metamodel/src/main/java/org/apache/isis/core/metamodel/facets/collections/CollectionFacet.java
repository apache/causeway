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

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Attached to {@link ObjectSpecification}s that represent a collection.
 *
 * @implNote Factories of (implementations of this) facet should ensure that a
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
    Stream<ManagedObject> stream(ManagedObject collectionAdapter);

    default Optional<ManagedObject> firstElement(final ManagedObject collectionAdapter) {
        return stream(collectionAdapter).findFirst();
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
    Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory,
            ObjectSpecification collectionSpec,
            Stream<ManagedObject> elements,
            int elementCount);

    /**
     * Convenience method that returns the {@link TypeOfFacet} on this facet's
     * {@link #getFacetHolder() holder}.
     */
    TypeOfFacet getTypeOfFacet();

    // -- UTILS

    public static Optional<CollectionFacet> lookup(@Nullable final ManagedObject container) {
        if(container==null) {
            return Optional.empty();
        }
        val collectionSpec = container.getSpecification();
        return Optional.ofNullable(collectionSpec.getFacet(CollectionFacet.class));
    }

    public static int elementCount(@Nullable final ManagedObject container) {
        if(container instanceof PackedManagedObject) {
            return ((PackedManagedObject)container).unpack().size();
        }
        return lookup(container)
                .map(collectionFacet->collectionFacet.size(container))
                .orElse(0);
    }

    public static Stream<ManagedObject> streamAdapters(@Nullable final ManagedObject container) {
        if(container instanceof PackedManagedObject) {
            return ((PackedManagedObject)container).unpack().stream();
        }
        return lookup(container)
                .map(collectionFacet->collectionFacet.stream(container))
                .orElse(Stream.empty());
    }

    public static Object[] toArrayOfPojos(@Nullable final ManagedObject container) {
        val elementAdapters = streamAdapters(container)
                .collect(Collectors.toList());
        return UnwrapUtil.multipleAsArray(elementAdapters);
    }

    @UtilityClass
    public static class AutofitUtils {

        /**
         * Copies the iterable into the specified type.
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public static Object collect(
                final @NonNull Stream<?> stream,
                final @NonNull Class<?> requiredType) {

            Stream rawStream = stream;

            val factoryIfAny = factoriesByType.get(requiredType);
            if(factoryIfAny!=null) {
                Supplier rawFactory = factoryIfAny;
                Collector rawCollector = Collectors.toCollection(rawFactory);
                return rawStream.collect(rawCollector);
            }

            // array
            if (requiredType.isArray()) {
                Class<?> elementType = requiredType.getComponentType();
                return rawStream.collect(_Arrays.toArray(elementType));
            }

            // not recognized
            return null;

        }


        // -- HELPER

        private static final Map<Class<?>, Supplier<Collection<?>>> factoriesByType = _With.hashMap(
                map-> {
                    // specific list implementations
                    map.put(CopyOnWriteArrayList.class, _Lists::newConcurrentList);
                    map.put(LinkedList.class, _Lists::newLinkedList);
                    map.put(ArrayList.class, _Lists::newArrayList);
                    map.put(AbstractList.class, _Lists::newArrayList);

                    // specific set implementations
                    map.put(CopyOnWriteArraySet.class, _Sets::newCopyOnWriteArraySet);
                    map.put(LinkedHashSet.class, _Sets::newLinkedHashSet);
                    map.put(HashSet.class, _Sets::newHashSet);
                    map.put(TreeSet.class, _Sets::newTreeSet);
                    map.put(AbstractSet.class, _Sets::newLinkedHashSet);

                    // interfaces
                    map.put(List.class, _Lists::newArrayList);
                    map.put(SortedSet.class, _Sets::newTreeSet);
                    map.put(Set.class, _Sets::newLinkedHashSet);
                    map.put(Collection.class, _Lists::newArrayList);
                });

    }


}
