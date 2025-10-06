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
package org.apache.causeway.commons.internal.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.jspecify.annotations.NonNull;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides Map-of-List and Map-of-Set implementations
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public class _Multimaps {

    /**
     * Represents a Map of Lists.
     * @param <K>
     * @param <V>
     */
    public static interface ListMultimap<K, V> extends Map<K, List<V>> {
        /**
         * Adds {@code value} to the List stored under {@code key}.
         * (If no such List exists, a new List is created.)
         * @param key
         * @param value
         */
        public void putElement(K key, V value);

        /**
         * Returns the List to which the specified key is mapped,
         * or a new List if this map contains no mapping for the key.
         * <p> In the latter case the List is also put onto this map.
         * @param key
         */
        public List<V> getOrElseNew(K key);

        /**
         * Returns the List to which the specified key is mapped,
         * or an immutable empty List if this map contains no mapping for the key.
         * <p> In the latter case the List is not put onto this map.
         * @param key
         */
        default List<V> getOrElseEmpty(final K key) {
            return getOrDefault(key, Collections.emptyList());
        }

        default Stream<V> streamElements() {
            return values().stream()
                    .flatMap(Collection::stream);
        }

        default Stream<V> streamElements(final K key) {
            return getOrElseEmpty(key).stream();
        }

        /**
         * @return optionally the underlying map, based on whether it implements a {@link NavigableMap}
         */
        default Optional<NavigableMap<K, List<V>>> asNavigableMap() {
            return Optional.empty();
        }

        default NavigableMap<K, List<V>> asNavigableMapElseFail() {
            return asNavigableMap()
                    .orElseThrow(()->_Exceptions
                            .unrecoverable("underlying map is not an instance of NavigableMap"));
        }

        public ListMultimap<K, V> asUnmodifiable();

    }

    private record ListMultimapWrapper<K, V>(
        Map<K, List<V>> delegate,
        Supplier<List<V>> elementCollectionFactory) implements ListMultimap<K, V> {

            @Override public int size() { return delegate.size(); }
            @Override public boolean isEmpty() { return delegate.isEmpty(); }
            @Override public boolean containsKey(final Object key) { return delegate.containsKey(key); }
            @Override public boolean containsValue(final Object value) { return delegate.containsValue(value); }
            @Override public List<V> get(final Object key) { return delegate.get(key); }
            @Override public List<V> put(final K key, final List<V> value) { return delegate.put(key, value); }
            @Override public List<V> remove(final Object key) { return delegate.remove(key); }
            @Override public void putAll(final Map<? extends K, ? extends List<V>> m) { delegate.putAll(m); }
            @Override public void clear() { delegate.clear(); }
            @Override public Set<K> keySet() { return delegate.keySet(); }
            @Override public Collection<List<V>> values() { return delegate.values();   }
            @Override public Set<Entry<K, List<V>>> entrySet() { return delegate.entrySet(); }

            @Override
            public void putElement(final K key, final V value) {
                getOrElseNew(key).add(value);
            }

            @Override
            public List<V> getOrElseNew(final K key) {
                var collection = delegate.computeIfAbsent(key, __->elementCollectionFactory.get());
                return collection;
            }

            @Override
            public Optional<NavigableMap<K, List<V>>> asNavigableMap() {
                return delegate instanceof NavigableMap
                        ? Optional.of(_Casts.uncheckedCast(delegate))
                        : Optional.empty();
            }
            @Override
            public ListMultimap<K, V> asUnmodifiable() {
                return new ListMultimapWrapper<>(Collections.unmodifiableMap(delegate), elementCollectionFactory);
            }
    }

    /**
     * Represents a Map of Sets.
     * @param <K>
     * @param <V>
     */
    public static interface SetMultimap<K, V> extends Map<K, Set<V>> {
        /**
         * Adds {@code value} to the Set stored under {@code key}.
         * (If no such Set exists, a new Set is created.)
         * @param key
         * @param value
         */
        public void putElement(K key, V value);

        /**
         * Returns the Set to which the specified key is mapped,
         * or a new Set if this map contains no mapping for the key.
         * <p> In the latter case the Set is also put onto this map.
         * @param key
         */
        public Set<V> getOrElseNew(K key);

        /**
         * Returns the Set to which the specified key is mapped,
         * or an immutable empty Set if this map contains no mapping for the key.
         * <p> In the latter case the Set is not put onto this map.
         * @param key
         */
        default public Set<V> getOrElseEmpty(final K key) {
            return getOrDefault(key, Collections.emptySet());
        }

        default public Stream<V> streamElements() {
            return values().stream()
                    .flatMap(Collection::stream);
        }

        public SetMultimap<K, V> asUnmodifiable();
    }

    private record SetMultimapWrapper<K, V>(
        Map<K, Set<V>> delegate,
        Supplier<? extends Set<V>> elementCollectionFactory) implements SetMultimap<K, V> {

            @Override public int size() { return delegate.size(); }
            @Override public boolean isEmpty() { return delegate.isEmpty(); }
            @Override public boolean containsKey(final Object key) { return delegate.containsKey(key); }
            @Override public boolean containsValue(final Object value) { return delegate.containsValue(value); }
            @Override public Set<V> get(final Object key) { return delegate.get(key); }
            @Override public Set<V> put(final K key, final Set<V> value) { return delegate.put(key, value); }
            @Override public Set<V> remove(final Object key) { return delegate.remove(key); }
            @Override public void putAll(final Map<? extends K, ? extends Set<V>> m) {  delegate.putAll(m); }
            @Override public void clear() { delegate.clear(); }
            @Override public Set<K> keySet() { return delegate.keySet(); }
            @Override public Collection<Set<V>> values() { return delegate.values();    }
            @Override public Set<Entry<K, Set<V>>> entrySet() { return delegate.entrySet(); }

            @Override
            public void putElement(final K key, final V value) {
                getOrElseNew(key).add(value);
            }

            @Override
            public Set<V> getOrElseNew(final K key) {
                var collection = delegate.computeIfAbsent(key, __->elementCollectionFactory.get());
                return collection;
            }

            @Override
            public SetMultimap<K, V> asUnmodifiable() {
                return new SetMultimapWrapper<>(Collections.unmodifiableMap(delegate), elementCollectionFactory);
            }
    }

    /**
     * Represents a Map of Maps.
     * @param <K1>
     * @param <K2>
     * @param <V>
     */
    public static interface MapMultimap<K1, K2, V> extends Map<K1, Map<K2, V>> {

        /**
         * Puts {@code value} into the element-map stored under {@code key}.
         * (If no such Map exists, a new Map is created.)
         * @param key
         * @param subkey the key into the element-map where to store the {@code value}
         * @param value
         */
        public void putElement(K1 key, K2 subkey, V value);

        /**
         * Get the element-map by {@code key}, then within the element-map looks up {@code subkey}.
         * @param key
         * @param subkey
         * @return null if no such element exists
         */
        public V getElement(K1 key, K2 subkey);

        /**
         * Returns the Map to which the specified key is mapped,
         * or a new Map if this map contains no mapping for the key.
         * <p> In the latter case the Map is also put onto this map.
         * @param key
         */
        public Map<K2, V> getOrElseNew(K1 key);

        /**
         * Returns the Map to which the specified key is mapped,
         * or an immutable empty Map if this map contains no mapping for the key.
         * <p> In the latter case the Map not is put onto this map.
         * @param key
         */
        default public Map<K2, V> getOrElseEmpty(final K1 key) {
            return getOrDefault(key, Collections.emptyMap());
        }

        default public Stream<V> streamElements() {
            return values().stream()
                    .map(Map::values)
                    .flatMap(Collection::stream);
        }
    }

    public static <K, V> ListMultimap<K, V> newListMultimap(
            final @NonNull Supplier<Map<K, List<V>>> mapFactory,
            final @NonNull Supplier<List<V>> elementCollectionFactory){
        return new ListMultimapWrapper<>(mapFactory.get(), elementCollectionFactory);
    }


    public static <K, V, S extends Set<V>> SetMultimap<K, V> newSetMultimap(
        final @NonNull Supplier<? extends Map<K, S>> mapFactory,
        final @NonNull Supplier<S> elementCollectionFactory){
        final Map<K, Set<V>> delegate = _Casts.uncheckedCast(mapFactory.get());
        return new SetMultimapWrapper<K, V>(delegate, elementCollectionFactory);
    }

    public static <K1, K2, V> MapMultimap<K1, K2, V> newMapMultimap(
            final @NonNull Supplier<Map<K1, Map<K2, V>>> mapFactory,
            final @NonNull Supplier<Map<K2, V>> elementMapFactory){

        return new MapMultimap<K1, K2, V>() {

            final Map<K1, Map<K2, V>> delegate = mapFactory.get();

            @Override public int size() { return delegate.size(); }
            @Override public boolean isEmpty() { return delegate.isEmpty();	}
            @Override public boolean containsKey(final Object key) { return delegate.containsKey(key); }
            @Override public boolean containsValue(final Object value) { return delegate.containsValue(value); }
            @Override public Map<K2, V> get(final Object key) { return delegate.get(key); }
            @Override public Map<K2, V> put(final K1 key, final Map<K2, V> value) { return delegate.put(key, value); }
            @Override public Map<K2, V> remove(final Object key) { return delegate.remove(key); }
            @Override public void putAll(final Map<? extends K1, ? extends Map<K2, V>> m) {	delegate.putAll(m);	}
            @Override public void clear() {	delegate.clear(); }
            @Override public Set<K1> keySet() { return delegate.keySet(); }
            @Override public Collection<Map<K2, V>> values() { return delegate.values();	}
            @Override public Set<Entry<K1, Map<K2, V>>> entrySet() { return delegate.entrySet(); }

            @Override
            public void putElement(final K1 key, final K2 subkey, final V value) {
                getOrElseNew(key).put(subkey, value);
            }

            @Override
            public V getElement(final K1 key, final K2 subkey) {
                final Map<K2, V> elementMap = delegate.get(key);
                return elementMap!=null ? elementMap.get(subkey) : null;
            }

            @Override
            public Map<K2, V> getOrElseNew(final K1 key) {
                var elementMap = delegate.computeIfAbsent(key, __->elementMapFactory.get());
                return elementMap;
            }

        };
    }

    // -- SHORTCUTS

    /**
     * @return HashMap of Lists with given listFactory
     */
    public static <K, V> ListMultimap<K, V> newListMultimap(final Supplier<List<V>> listFactory){
        return newListMultimap(HashMap<K, List<V>>::new, listFactory);
    }

    /**
     *
     * @return HashMap of Sets with given setFactory
     */
    public static <K, V> SetMultimap<K, V> newSetMultimap(final Supplier<Set<V>> setFactory){
        return newSetMultimap(HashMap<K, Set<V>>::new, setFactory);
    }

    // -- CONVENIENT DEFAULTS

    /**
     * @return HashMap of ArrayLists
     */
    public static <K, V> ListMultimap<K, V> newListMultimap(){
        return newListMultimap(HashMap<K, List<V>>::new, ArrayList::new);
    }

    /**
     * @return TreeMap of ArrayLists
     */
    public static <K, V> ListMultimap<K, V> newListTreeMultimap(){
        return newListMultimap(TreeMap<K, List<V>>::new, ArrayList::new);
    }

    /**
     * @return ConcurrentHashMap of CopyOnWriteArrayList (fully concurrent)
     */
    public static <K, V> ListMultimap<K, V> newConcurrentListMultimap(){
        return newListMultimap(ConcurrentHashMap<K, List<V>>::new, CopyOnWriteArrayList::new);
    }

    /**
     * @return ConcurrentSkipListMap of CopyOnWriteArrayList (fully concurrent)
     */
    public static <K, V> ListMultimap<K, V> newSortedConcurrentListMultimap(){
        return newListMultimap(ConcurrentSkipListMap<K, List<V>>::new, CopyOnWriteArrayList::new);
    }

    /**
     * @return HashMap of HashSets
     */
    public static <K, V> SetMultimap<K, V> newSetMultimap(){
        return newSetMultimap(HashMap<K, Set<V>>::new, HashSet::new);
    }

    /**
     * @return ConcurrentHashMap of ConcurrentHashMap-key-sets (fully concurrent)
     */
    public static <K, V> SetMultimap<K, V> newConcurrentSetMultimap(){
        return newSetMultimap(ConcurrentHashMap<K, Set<V>>::new, _Sets::newConcurrentHashSet);
    }

    /**
     * @param keyComparator - if {@code null} uses natural ordering
     * @param elementComparator - if {@code null} uses natural ordering
     * @return TreeMap of TreeSets
     */
    public static <K, V> SetMultimap<K, V> newSortedSetMultimap(
            final @Nullable Comparator<K> keyComparator,
            final @Nullable Comparator<V> elementComparator){

        final Supplier<SortedMap<K, SortedSet<V>>> mapFactory = ()->new TreeMap<K, SortedSet<V>>(keyComparator);
        final Supplier<SortedSet<V>> elementSetFactory = ()->new TreeSet<V>(elementComparator);
        return newSetMultimap(mapFactory, elementSetFactory);
    }

    /**
     * @return HashMap of HashMaps
     */
    public static <K1, K2, V> MapMultimap<K1, K2, V> newMapMultimap(){
        return newMapMultimap(HashMap<K1, Map<K2, V>>::new, HashMap::new);
    }

    /**
     * @return ConcurrentHashMap of HashMaps (not fully concurrent)
     */
    public static <K1, K2, V> MapMultimap<K1, K2, V> newConcurrentMapMultimap(){
        return newMapMultimap(ConcurrentHashMap<K1, Map<K2, V>>::new, HashMap::new);
    }

}
