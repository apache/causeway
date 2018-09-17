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

package org.apache.isis.core.runtime.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.isis.commons.internal.collections._Sets;

class DeweyOrderUtil  {

    public static final <V> Function<Parsed, Map.Entry<String, V>> toMapEntry() {
        return new Function<Parsed, Map.Entry<String, V>>() {
            @Override
            public Map.Entry<String, V> apply(Parsed input) {
                return input.toMapEntry();
            }
        };
    }

    private DeweyOrderUtil() {}

    static <V> List<Map.Entry<String, V>> deweySorted(Map<String, V> map) {
        Set<Map.Entry<String, V>> set = _Sets.newLinkedHashSet();
        for (Map.Entry<String, V> entry : map.entrySet()) {
            set.add(entry);
        }
        return deweySorted(set);
    }

    static <V> List<Map.Entry<String, V>> deweySorted(Set<Map.Entry<String, V>> keys) {
        if(keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        final Iterable<Parsed<V>> parsedIter = Iterables.transform(keys,
                new Function<Map.Entry<String,V>, Parsed<V>>() {
            @Override
            public Parsed<V> apply(Map.Entry<String,V> input) {
                return new Parsed(input);
            }
        });

        final SortedSet<Parsed<V>> parseds = _Sets.newTreeSet(parsedIter);
        final Iterable<Map.Entry<String, V>> transform = Iterables.transform(parseds, DeweyOrderUtil.<V>toMapEntry());
        return Lists.newArrayList(transform);
    }

}


class Parsed<V> implements Comparable<Parsed<V>> {
    private static final Function<String, Integer> PARSE = new Function<String, Integer>() {
        @Override
        public Integer apply(String input) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }
    };
    private final List<Integer> parts;
    private final String key;
    private final V value;
    Parsed(Map.Entry<String, V> entry) {
        key = entry.getKey();
        value = entry.getValue();
        final Iterable<String> iter = Splitter.on(".").split(entry.getKey());
        parts = Lists.newArrayList(Iterators.transform(iter.iterator(),PARSE));
    }

    @Override
    public int compareTo(Parsed<V> other) {
        for (int i = 0; i < parts.size(); i++) {
            Integer p = parts.get(i);
            if (other.parts.size() < parts.size()) {
                // run out of parts for other, put it before us
                return -1;
            }
            final Integer q = other.parts.get(i);
            final int comparison = p.compareTo(q);
            if(comparison != 0) {
                return +comparison;
            }
        }
        if(other.parts.size() > parts.size()) {
            // run out of parts on our side, still more on others; put us before it
            return +1;
        }
        return 0;
    }

    public Map.Entry<String, V> toMapEntry() {
        return new MapEntry<String, V>(key, value){};
    }
}

class MapEntry<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    MapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean equals(Object object) {
        if (object instanceof Map.Entry) {
            Map.Entry<?, ?> that = (Map.Entry<?, ?>) object;
            return Objects.equal(this.getKey(), that.getKey())
                    && Objects.equal(this.getValue(), that.getValue());
        }
        return false;
    }

    @Override public int hashCode() {
        K k = getKey();
        V v = getValue();
        return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
    }

    /**
     * Returns a string representation of the form {@code {key}={value}}.
     */
    @Override public String toString() {
        return getKey() + "=" + getValue();
    }
}
