package org.apache.causeway.viewer.graphql.viewer.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class _BiMap_Test {

    @Test
    public void testPutAndGet() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        assertThat(biMap.get("one")).isEqualTo(1);
        assertThat(biMap.get("two")).isEqualTo(2);
        assertThat(biMap.get("nonexistent")).isNull();
    }

    @Test
    public void testInverse() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        _BiMap<Integer, String> inverseBiMap = biMap.inverse();

        assertThat(inverseBiMap.get(1)).isEqualTo("one");
        assertThat(inverseBiMap.get(2)).isEqualTo("two");
        assertThat(inverseBiMap.get(3)).isNull();
    }

    @Test
    public void testIsEmpty() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        assertThat(biMap.isEmpty()).isTrue();

        biMap.put("one", 1);
        assertThat(biMap.isEmpty()).isFalse();
    }

    @Test
    public void testEntrySet() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        Set<Map.Entry<String, Integer>> entrySet = biMap.entrySet();

        assertThat(entrySet).containsExactlyInAnyOrder(
                Map.entry("one", 1),
                Map.entry("two", 2)
        );
    }

    @Test
    public void testKeySet() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        Set<String> keySet = biMap.keySet();

        assertThat(keySet).containsExactlyInAnyOrder("one", "two");
    }

    @Test
    public void testValues() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        Collection<Integer> values = biMap.values();

        assertThat(values).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void testGetForwardMapAsImmutable() {
        _BiMap<String, Integer> biMap = new _BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);

        Map<String, Integer> immutableForwardMap = biMap.getForwardMapAsImmutable();

        assertThat(immutableForwardMap).containsExactly(
                Map.entry("one", 1),
                Map.entry("two", 2)
        );

        // Ensure the immutable map cannot be modified
        assertThatThrownBy(() -> immutableForwardMap.put("three", 3))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
