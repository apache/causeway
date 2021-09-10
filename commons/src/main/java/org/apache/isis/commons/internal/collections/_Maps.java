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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

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

    /**
     * A Map that supports value lookup by key and <em>alias</em> keys.
     * <p>
     * example use-case: store values by class type then allow lookup by interface type
     * @param <K>
     * @param <V>
     */
    public static interface AliasMap<K, V> extends Map<K, V> {

        /**
         *
         * @param key
         * @param aliases
         * @param value
         * @throws IllegalArgumentException if there is an alias-key collision
         */
        public V put(K key, Can<K> aliases, V value);

        /**
         * Like {@link #put(Object, Can, Object)}, but on alias-key collision re-maps existing alias-keys
         * @param key
         * @param aliases
         * @param value
         */
        public V remap(K key, Can<K> aliases, V value);

    }

    // -- UNMODIFIABLE MAP

    public static <K, V> Map<K, V> unmodifiable(final K k1, final V v1) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(final K k1, final V v1, final K k2, final V v2) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        mapPreservingOrder.put(k3, v3);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map<K, V> unmodifiable(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {
        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();
        mapPreservingOrder.put(k1, v1);
        mapPreservingOrder.put(k2, v2);
        mapPreservingOrder.put(k3, v3);
        mapPreservingOrder.put(k4, v4);
        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> unmodifiableEntries(final @NonNull Map.Entry<? extends K,? extends V>... entries) {
        if(entries.length==0) {
            return Collections.emptyMap();
        }

        final LinkedHashMap<K, V> mapPreservingOrder = newLinkedHashMap();

        Stream.of(entries)
        .forEach(entry->mapPreservingOrder.put(entry.getKey(), entry.getValue()));

        return Collections.unmodifiableMap(mapPreservingOrder);
    }

    public static <K, V> Map.Entry<K, V> entry(final K k, final V v){
        return new AbstractMap.SimpleEntry<K, V>(k, v);
    }

    // -- TO STRING

    public static String toString(
            final @Nullable Map<?, ?> map,
            final @NonNull  CharSequence delimiter) {

        return map==null
            ? ""
            : map.entrySet()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
        }

    // -- MODIFICATIONS

    /**
     * For a given {@code map} either adds or removes the specified {@code key} based on whether
     * the map contains the {@code key}.
     * If this is an add operation, then given {@code value} is associated with the {@code key}.
     * @param <K>
     * @param <V>
     * @param map
     * @param key
     * @param value
     * @return whether given map contains the {@code key} after the operation
     */
    public static <K, V> boolean toggleElement(
            final @NonNull Map<K, V> map,
            final @NonNull K key,
            final @NonNull V value) {

        val newValue = map.compute(key, (k, v) -> (v==null) ? value : null);
        return newValue!=null;
    }

    // -- TRANSFORMATIONS

    public static <K, V0, V1> Map<K, V1> mapValues(
            final @Nullable Map<K, V0> input,
            final @NonNull Supplier<Map<K, V1>> mapFactory,
            final @NonNull Function<V0, V1> valueMapper) {

        val resultMap = mapFactory.get();

        if(input==null
                || input.isEmpty()) {
            return resultMap;
        }

        input.forEach((k, v)->resultMap.put(k, valueMapper.apply(v)));
        return resultMap;
    }


    public static <K, V> Map<K, V> filterKeys(
            final @Nullable Map<K, V> input,
            final @NonNull Predicate<K> keyFilter,
            final @NonNull Supplier<Map<K, V>> factory) {

        final Map<K, V> result = factory.get();

        if(input==null) {
            return result;
        }

        input.forEach((k, v)->{
            if(keyFilter.test(k)) {
                result.put(k, v);
            }
        });

        return result;
    }

    public static <K, V> ListMultimap<V, K> invertToListMultimap(final Map<K, V> input) {
        final ListMultimap<V, K> result = _Multimaps.newListMultimap();
        if(input==null) {
            return result;
        }
        input.forEach((k, v)->result.putElement(v, k));
        return result;
    }

    // -- FACTORIES ...

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

    public static <K, V> TreeMap<K, V> newTreeMap(final Comparator<? super K> comparator) {
        return new TreeMap<K, V>(comparator);
    }

    // -- ALIAS MAP

    @Value(staticConstructor = "of")
    private static final class KeyPair<K> {
        K key;
        Can<K> aliasKeys;
    }

    public static <K, V> AliasMap<K, V> newAliasMap(
            final @NonNull Supplier<Map<K, V>> mapFactory){

        return new AliasMap<K, V>() {

            final Map<K, V> delegate = mapFactory.get();

            @Override public int size() { return delegate.size(); }
            @Override public boolean isEmpty() { return delegate.isEmpty(); }
            @Override public boolean containsValue(final Object value) { return delegate.containsValue(value); }
            @Override public Set<K> keySet() { return delegate.keySet(); }
            @Override public Collection<V> values() { return delegate.values();   }
            @Override public Set<Entry<K, V>> entrySet() { return delegate.entrySet(); }

            @Override
            public V put(final K key, final V value) {
                return this.put(key, Can.empty(), value);
            }

            @Override
            public void putAll(final Map<? extends K, ? extends V> other) {
                if(!_NullSafe.isEmpty(other)) {
                    other.forEach((k, v)->this.put(k, v));
                }
            }

            @Override
            public V put(final K key, final Can<K> aliases, final V value) {
                putAliasKeys(key, aliases, /*re-map*/ false);
                return delegate.put(key, value);
            }

            @Override
            public V remap(final K key, final Can<K> aliases, final V value) {
                putAliasKeys(key, aliases, /*re-map*/ true);
                return delegate.put(key, value);
            }

            @Override
            public boolean containsKey(final Object keyOrAliasKey) {
                return delegate.containsKey(keyOrAliasKey) ||
                    containsAliasKey(keyOrAliasKey);
            }


            @Override
            public V get(final Object keyOrAliasKey) {
                val v = delegate.get(keyOrAliasKey);
                if(v!=null) {
                    return v;
                }
                return getByAliasKey(keyOrAliasKey);
            }

            @Override
            public V remove(final Object key) {
                removeAliasKeysOf(key);
                return delegate.remove(key);
            }

            @Override
            public void clear() {
                delegate.clear();
                clearAliasKeys();
            }

            // -- HELPER

            private final Map<K, KeyPair<K>> pairByAliasKey = _Maps.newHashMap();

            private void putAliasKeys(final K key, final Can<K> aliasKeys, final boolean remap) {
                if(aliasKeys.isNotEmpty()) {
                    val keyPair = KeyPair.of(key, aliasKeys);
                    for(val aliasKey : aliasKeys) {

                        val existingKeyPair = pairByAliasKey.put(aliasKey, keyPair);
                        if(existingKeyPair!=null && !remap) {

                            throw _Exceptions.illegalArgument(
                                    "alias key collision on alias %s: existing-key=%s, new-key=%s",
                                    aliasKey, existingKeyPair.key, keyPair.key);
                        }
                    }
                }
            }

            private V getByAliasKey(final Object aliasKey) {
                val keyPair = pairByAliasKey.get(aliasKey);
                if(keyPair!=null) {
                    return delegate.get(keyPair.getKey());
                }
                return null;
            }

            private boolean containsAliasKey(final Object aliasKey) {
                return pairByAliasKey.containsKey(aliasKey);
            }

            private void removeAliasKeysOf(final Object key) {
                //XXX this implementation is slow for large alias maps, since we traverse the entire map
                pairByAliasKey.entrySet()
                .removeIf(entry->{
                    val keyPair = entry.getValue();
                    return keyPair.getKey().equals(key);
                });

            }

            private void clearAliasKeys() {
                pairByAliasKey.clear();
            }


        };
    }

    // --

}
