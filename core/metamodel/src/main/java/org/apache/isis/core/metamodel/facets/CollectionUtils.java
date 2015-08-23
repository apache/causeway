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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static Object[] getCollectionAsObjectArray(final Object option, final ObjectSpecification spec, final AdapterManager adapterMap) {
        final ObjectAdapter collection = adapterMap.adapterFor(option);
        final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(collection);
        final Object[] optionArray = new Object[facet.size(collection)];
        int j = 0;
        for (final ObjectAdapter nextElement : facet.iterable(collection)) {
            optionArray[j++] = nextElement != null? nextElement.getObject(): null;
        }
        return optionArray;
    }

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

        // specific list implementations
        if (CopyOnWriteArrayList.class == requiredType) {
            return Lists.newCopyOnWriteArrayList(iterable);
        }
        if (LinkedList.class == requiredType) {
            return Lists.newLinkedList(iterable);
        }
        if (ArrayList.class == requiredType) {
            return Lists.newArrayList(iterable);
        }

        if (AbstractList.class == requiredType) {
            return Lists.newArrayList(iterable);
        }

        // specific set implementations
        if (CopyOnWriteArraySet.class == requiredType) {
            return Sets.newCopyOnWriteArraySet(iterable);
        }
        if (LinkedHashSet.class == requiredType) {
            return Sets.newLinkedHashSet(iterable);
        }
        if (HashSet.class == requiredType) {
            return Sets.newHashSet(iterable);
        }
        if (TreeSet.class == requiredType) {
            Iterable rawIterable = iterable;
            return Sets.newTreeSet(rawIterable);
        }

        if (AbstractSet.class == requiredType) {
            return Sets.newLinkedHashSet(iterable);
        }


        // interfaces
        if (List.class == requiredType) {
            return Lists.newArrayList(iterable);
        }
        if (SortedSet.class == requiredType) {
            Iterable rawIterable = iterable;
            return Sets.newTreeSet(rawIterable);
        }
        if (Set.class == requiredType) {
            return Sets.newLinkedHashSet(iterable);
        }
        if (Collection.class == requiredType) {
            return Lists.newArrayList(iterable);
        }

        // array
        if (requiredType.isArray()) {
            Class<?> componentType = requiredType.getComponentType();
            Iterable rawIterable = iterable;
            return Iterables.toArray(rawIterable, componentType);
        }

        // not recognized
        return null;
    }
}
