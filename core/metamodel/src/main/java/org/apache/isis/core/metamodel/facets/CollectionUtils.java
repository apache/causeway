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

package org.apache.isis.core.metamodel.facets;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import org.apache.isis.commons.internal.collections._Sets;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static Object[] getCollectionAsObjectArray(final Object option, final ObjectSpecification spec, final ObjectAdapterProvider adapterProvider) {
        final ObjectAdapter collection = adapterProvider.adapterFor(option);
        final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(collection);
        final Object[] optionArray = new Object[facet.size(collection)];
        int j = 0;
        for (final ObjectAdapter nextElement : facet.iterable(collection)) {
            optionArray[j++] = nextElement != null? nextElement.getObject(): null;
        }
        return optionArray;
    }

    private final static Map<Class<?>, Function<Iterable<Object>, Object>> factoriesByType = _With.hashMap(
            map-> {
                // specific list implementations
                map.put(CopyOnWriteArrayList.class, _Lists::newCopyOnWriteArrayList);
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

    /**
     * Copies the iterable into the specified type.
     */
    public static Object copyOf(final Iterable<Object> iterable, final Class<?> requiredType) {

        if(iterable == null) {
            throw new IllegalArgumentException("Iterable must be provided");
        }
        if(requiredType == null) {
            throw new IllegalArgumentException("RequiredType must be provided");
        }

        final Function<Iterable<Object>, Object> factory = factoriesByType.get(requiredType);
        if(factory!=null) {
            return factory.apply(iterable);
        }

        // array
        if (requiredType.isArray()) {
            Class<?> componentType = requiredType.getComponentType();

            @SuppressWarnings("rawtypes") Iterable rawIterable = iterable;
            return _Arrays.toArray(_Casts.uncheckedCast(rawIterable), componentType);
        }

        // not recognized
        return null;

    }

}
