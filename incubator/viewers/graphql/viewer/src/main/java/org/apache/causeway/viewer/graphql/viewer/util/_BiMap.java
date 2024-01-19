package org.apache.causeway.viewer.graphql.viewer.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class _BiMap<K, V> {
    private final Map<K, V> forwardMap = new LinkedHashMap<>();
    private final Map<V, K> inverseMap = new LinkedHashMap<>();

    public void put(K key, V value) {
        forwardMap.put(key, value);
        inverseMap.put(value, key);
    }

    public V get(K key) {
        return forwardMap.get(key);
    }

    public _BiMap<V, K> inverse() {
        _BiMap<V, K> inverseBiMap = new _BiMap<>();
        inverseBiMap.forwardMap.putAll(inverseMap);
        inverseBiMap.inverseMap.putAll(forwardMap);
        return inverseBiMap;
    }

    public boolean isEmpty() {
        return forwardMap.isEmpty();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return forwardMap.entrySet();
    }

    public Set<K> keySet() {
        return forwardMap.keySet();
    }

    public Collection<V> values() {
        return forwardMap.values();
    }

    public Map<K, V> getForwardMapAsImmutable() {
        return Collections.unmodifiableMap(forwardMap);
    }
}