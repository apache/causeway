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

package org.apache.isis.commons.internal.collections;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Map creation idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Maps {

    private _Maps(){}

    // -- UNMODIFIABLE MAP

    public static <K, V> Map<K, V> unmodifiable(K k1, V v1) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(K k1, V v1, K k2, V v2) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(K k1, V v1, K k2, V v2, K k3, V v3) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        mapPreservingOrder.put(k3, v3);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        mapPreservingOrder.put(k3, v3);
        mapPreservingOrder.put(k4, v4);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> unmodifiableEntries(Map.Entry<? extends K,? extends V>... entries) {
        requires(entries, "entries"); // don't accept null elements
        if(entries.length==0) {
            return Collections.emptyMap();
        }

        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();

        Stream.of(entries)
        .forEach(entry->mapPreservingOrder.put(entry.getKey(), entry.getValue()));

        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map.Entry<K, V> entry(K k, V v){
        return new AbstractMap.SimpleEntry<K, V>(k, v);
    }
    
    // -- TRANSFORMATIONS
    
    public static <K, V> Map<K, V> filterKeys(
            @Nullable Map<K, V> input,
            Predicate<K> keyFilter, 
            Supplier<Map<K, V>> factory) {
        
        requires(factory, "factory");
        final Map<K, V> result = factory.get();
        
        if(input==null) {
            return result;
        }
        
        requires(keyFilter, "keyFilter");
        
        input.forEach((k, v)->{
            if(keyFilter.test(k)) {
                result.put(k, v);
            }
        });
        
        return result;
    }
    
    public static <K, V> ListMultimap<V, K> invertToListMultimap(Map<K, V> input) {
        final ListMultimap<V, K> result = _Multimaps.newListMultimap();        
        if(input==null) {
            return result;
        }
        input.forEach((k, v)->result.putElement(v, k));        
        return result;
    }

    // -- HASH MAP

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    // -- LINKED HASH MAP

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    // -- CONCURRENT HASH MAP

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<K, V>();
    }

    // -- TREE MAP

    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<K, V>(comparator);
    }

    // --

}
