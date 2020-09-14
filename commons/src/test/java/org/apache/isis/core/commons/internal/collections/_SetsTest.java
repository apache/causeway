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
package org.apache.isis.core.commons.internal.collections;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;

/**
 * @since Feb 23, 2020
 *
 */
class _SetsTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }
    
    void assertUnmodifiable(Set<Integer> set) {
        if(set.size()>0) {
            assertThrows(Exception.class, ()->set.clear());
            val iterator = set.iterator();
            assertThrows(Exception.class, ()->{
                iterator.next();
                iterator.remove();
            });
            assertThrows(Exception.class, ()->set.remove(1));
        }
        assertThrows(Exception.class, ()->set.add(2));
    }
    
    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#singleton(java.lang.Object)}.
     */
    @Test
    void testSingleton() {
        val set = _Sets.<Integer>singleton(1);
        assertUnmodifiable(set);
        assertEquals(1, set.size());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#singletonOrElseEmpty(java.lang.Object)}.
     */
    @Test
    void testSingletonOrElseEmpty() {
        val set = _Sets.<Integer>singletonOrElseEmpty(1);
        assertUnmodifiable(set);
        assertEquals(1, set.size());
        val emptySet = _Sets.<Integer>singletonOrElseEmpty(null);
        assertUnmodifiable(emptySet);
        assertEquals(0, emptySet.size());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#of(T[])}.
     */
    @Test
    void testOf() {
        assertArrayEquals(new Integer[] {}, _Arrays.toArray(_Sets.of(), Integer.class)); 
        assertArrayEquals(new Integer[] {0, 1, -1, 3}, _Arrays.toArray(_Sets.of(0, 1, -1, 3), Integer.class));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#unmodifiable(java.lang.Iterable)}.
     */
    @Test
    void testUnmodifiable() {
        val set = _Sets.unmodifiable(Samples.iterable(3, 1, 2));
        assertUnmodifiable(set);
        Samples.assertListEquals(_Lists.of(3, 1, 2), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newTreeSet()}.
     */
    @Test
    void testNewTreeSet() {
        val set = _Sets.newTreeSet();
        assertEquals(TreeSet.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newTreeSet(java.util.Comparator)}.
     */
    @Test
    void testNewTreeSetComparatorOfT() {
        val set = _Sets.newTreeSet(Integer::compare);
        assertEquals(TreeSet.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newTreeSet(java.lang.Iterable)}.
     */
    @Test
    void testNewTreeSetIterableOfT() {
        val set = _Sets.newTreeSet(Samples.iterable(3, 1, 2));
        assertEquals(TreeSet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newTreeSet(java.lang.Iterable, java.util.Comparator)}.
     */
    @Test
    void testNewTreeSetIterableOfTComparatorOfT() {
        val set = _Sets.newTreeSet(Samples.iterable(3, 1, 2), (a, b)->Integer.compare(b, a));
        assertEquals(TreeSet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(3, 2, 1), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newHashSet()}.
     */
    @Test
    void testNewHashSet() {
        val set = _Sets.newHashSet();
        assertEquals(HashSet.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newHashSet(java.util.Collection)}.
     */
    @Test
    void testNewHashSetCollectionOfT() {
        assertEquals(HashSet.class, _Sets.newHashSet(null).getClass());
        val set = _Sets.newHashSet(_Lists.of(3, 1, 2, 3, 3));
        assertEquals(HashSet.class, set.getClass());
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newHashSet(java.lang.Iterable)}.
     */
    @Test
    void testNewHashSetIterableOfT() {
        val set = _Sets.newHashSet(Samples.iterable(3, 1, 2, 3, 3));
        assertEquals(HashSet.class, set.getClass());
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newLinkedHashSet()}.
     */
    @Test
    void testNewLinkedHashSet() {
        val set = _Sets.newLinkedHashSet();
        assertEquals(LinkedHashSet.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newLinkedHashSet(java.util.Collection)}.
     */
    @Test
    void testNewLinkedHashSetCollectionOfT() {
        assertEquals(LinkedHashSet.class, _Sets.newLinkedHashSet(null).getClass());
        val set = _Sets.newLinkedHashSet(_Lists.of(3, 1, 2, 3, 3));
        assertEquals(LinkedHashSet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(3, 1, 2), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newLinkedHashSet(java.lang.Iterable)}.
     */
    @Test
    void testNewLinkedHashSetIterableOfT() {
        val set = _Sets.newLinkedHashSet(Samples.iterable(3, 1, 2, 3, 3));
        assertEquals(LinkedHashSet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(3, 1, 2), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newConcurrentHashSet()}.
     */
    @Test
    void testNewConcurrentHashSet() {
        val set = _Sets.newConcurrentHashSet();
        assertEquals(ConcurrentHashMap.KeySetView.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newConcurrentHashSet(java.util.Collection)}.
     */
    @Test
    void testNewConcurrentHashSetCollectionOfT() {
        val set = _Sets.newConcurrentHashSet(Samples.iterable(3, 1, 2, 3, 3));
        assertEquals(ConcurrentHashMap.KeySetView.class, set.getClass());
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newConcurrentHashSet(java.lang.Iterable)}.
     */
    @Test
    void testNewConcurrentHashSetIterableOfT() {
        val set = _Sets.newConcurrentHashSet(_Lists.of(3, 1, 2, 3, 3));
        assertEquals(ConcurrentHashMap.KeySetView.class, set.getClass());
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newCopyOnWriteArraySet()}.
     */
    @Test
    void testNewCopyOnWriteArraySet() {
        val set = _Sets.newCopyOnWriteArraySet();
        assertEquals(CopyOnWriteArraySet.class, set.getClass());
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newCopyOnWriteArraySet(java.util.Collection)}.
     */
    @Test
    void testNewCopyOnWriteArraySetCollectionOfT() {
        assertEquals(CopyOnWriteArraySet.class, _Sets.newCopyOnWriteArraySet(null).getClass());
        val set = _Sets.newCopyOnWriteArraySet(_Lists.of(3, 1, 2, 3, 3));
        assertEquals(CopyOnWriteArraySet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(3, 1, 2), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#newCopyOnWriteArraySet(java.lang.Iterable)}.
     */
    @Test
    void testNewCopyOnWriteArraySetIterableOfT() {
        val set = _Sets.newCopyOnWriteArraySet(Samples.iterable(3, 1, 2, 3, 3));
        assertEquals(CopyOnWriteArraySet.class, set.getClass());
        Samples.assertListEquals(_Lists.of(3, 1, 2), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#intersect(java.util.Set, java.util.Set)}.
     */
    @Test
    void testIntersect() {
        Samples.assertSetEquals(_Sets.of(), _Sets.intersect(null, null));
        Samples.assertSetEquals(_Sets.of(), _Sets.intersect(null, _Sets.of()));
        Samples.assertSetEquals(_Sets.of(), _Sets.intersect(_Sets.of(), null));
        Samples.assertSetEquals(_Sets.of(), _Sets.intersect(_Sets.of(), _Sets.of()));
        Samples.assertSetEquals(_Sets.of(), _Sets.intersect(_Sets.of(0, 1), _Sets.of(2, 3)));
        Samples.assertSetEquals(_Sets.of(1, 2), _Sets.intersect(_Sets.of(0, 1, 2), _Sets.of(1, 2, 3)));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#intersectSorted(java.util.SortedSet, java.util.SortedSet)}.
     */
    @Test
    void testIntersectSorted() {
        Samples.assertListEquals(_Lists.of(), _Sets.intersectSorted(null, null));
        Samples.assertListEquals(_Lists.of(), _Sets.intersectSorted(null, _Sets.newTreeSet(_Sets.of())));
        Samples.assertListEquals(_Lists.of(), _Sets.intersectSorted(_Sets.newTreeSet(), null));
        Samples.assertListEquals(_Lists.of(), _Sets.intersectSorted(_Sets.newTreeSet(), _Sets.newTreeSet()));
        Samples.assertListEquals(_Lists.of(), _Sets.intersectSorted(_Sets.newTreeSet(_Sets.of(1, 0)), _Sets.newTreeSet(_Sets.of(3, 2))));
        Samples.assertListEquals(_Lists.of(1, 2), _Sets.intersectSorted(_Sets.newTreeSet(_Sets.of(2, 0, 1)), _Sets.newTreeSet((_Sets.of(3, 1, 2)))));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#minus(java.util.Set, java.util.Set)}.
     */
    @Test
    void testMinusSetOfTSetOfT() {
        Samples.assertSetEquals(_Sets.of(), _Sets.minus(null, null));
        Samples.assertSetEquals(_Sets.of(), _Sets.minus(null, _Sets.of()));
        Samples.assertSetEquals(_Sets.of(), _Sets.minus(_Sets.of(), null));
        Samples.assertSetEquals(_Sets.of(), _Sets.minus(_Sets.of(), _Sets.of()));
        Samples.assertSetEquals(_Sets.of(0, 1), _Sets.minus(_Sets.of(0, 1), _Sets.of(2, 3)));
        Samples.assertSetEquals(_Sets.of(0), _Sets.minus(_Sets.of(0, 1, 2), _Sets.of(1, 2, 3)));

    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#minusSorted(java.util.SortedSet, java.util.SortedSet)}.
     */
    @Test
    void testMinusSortedSortedSetOfTSortedSetOfT() {
        Samples.assertListEquals(_Lists.of(), _Sets.minusSorted(null, null));
        Samples.assertListEquals(_Lists.of(), _Sets.minusSorted(null, _Sets.newTreeSet(_Sets.of())));
        Samples.assertListEquals(_Lists.of(), _Sets.minusSorted(_Sets.newTreeSet(), null));
        Samples.assertListEquals(_Lists.of(), _Sets.minusSorted(_Sets.newTreeSet(), _Sets.newTreeSet()));
        Samples.assertListEquals(_Lists.of(0, 1), _Sets.minusSorted(_Sets.newTreeSet(_Sets.of(1, 0)), _Sets.newTreeSet(_Sets.of(3, 2))));
        Samples.assertListEquals(_Lists.of(0), _Sets.minusSorted(_Sets.newTreeSet(_Sets.of(2, 0, 1)), _Sets.newTreeSet((_Sets.of(3, 1, 2)))));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#minus(java.util.Set, java.util.Set, java.util.function.Supplier)}.
     */
    @Test
    void testMinusSetOfTSetOfTSupplierOfSetOfT() {
        val set = _Sets.<Integer>minus(null, null, TreeSet::new);
        assertUnmodifiable(set);
        Samples.assertSetEquals(_Sets.of(), _Sets.minus(_Sets.newTreeSet(_Sets.of(1)), null, TreeSet::new));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#minusSorted(java.util.SortedSet, java.util.SortedSet, java.util.function.Supplier)}.
     */
    @Test
    void testMinusSortedSortedSetOfTSortedSetOfTSupplierOfSortedSetOfT() {
        val set = _Sets.<Integer>minusSorted(null, null, TreeSet::new);
        assertUnmodifiable(set);
        Samples.assertListEquals(_Lists.of(1), _Sets.minusSorted(_Sets.newTreeSet(_Sets.of(1)), null, TreeSet::new));
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#toUnmodifiable(java.util.function.Supplier)}.
     */
    @Test
    void testToUnmodifiableSupplierOfSetOfT() {
        val set = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiable(HashSet::new));
        assertUnmodifiable(set);
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
        
        val sortedSet = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiable(TreeSet::new));
        assertUnmodifiable(sortedSet);
        Samples.assertListEquals(_Lists.of(1, 2, 3), sortedSet);
        
        val orderedSet = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiable(LinkedHashSet::new));
        assertUnmodifiable(orderedSet);
        Samples.assertListEquals(_Lists.of(3, 1, 2), orderedSet);
        
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#toUnmodifiable()}.
     */
    @Test
    void testToUnmodifiable() {
        val set = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiable(HashSet::new));
        assertUnmodifiable(set);
        Samples.assertSetEquals(_Sets.of(1, 2, 3), set);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#toUnmodifiablePreservingOrder()}.
     */
    @Test
    void testToUnmodifiablePreservingOrder() {
        val orderedSet = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiablePreservingOrder());
        assertUnmodifiable(orderedSet);
        Samples.assertListEquals(_Lists.of(3, 1, 2), orderedSet);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#toUnmodifiableSorted(java.util.function.Supplier)}.
     */
    @Test
    void testToUnmodifiableSortedSupplierOfSortedSetOfT() {
        val sortedSet = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiableSorted(TreeSet::new));
        assertUnmodifiable(sortedSet);
        Samples.assertListEquals(_Lists.of(1, 2, 3), sortedSet);
    }

    /**
     * Test method for {@link org.apache.isis.core.commons.internal.collections._Sets#toUnmodifiableSorted()}.
     */
    @Test
    void testToUnmodifiableSorted() {
        val sortedSet = Stream.of(3,1,2)
        .collect(_Sets.toUnmodifiableSorted());
        assertUnmodifiable(sortedSet);
        Samples.assertListEquals(_Lists.of(1, 2, 3), sortedSet);
    }

}
