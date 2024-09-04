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
package org.apache.causeway.commons.collections;

import java.io.IOException;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.testing._SerializationTester;

import lombok.Value;
import lombok.val;

class CanTest {

    @Test
    void tester_selftest() throws ClassNotFoundException, IOException {
        _SerializationTester.selftest();
    }

    @Test
    void emptyCans_shouldBeEqual() {
        assertEquals(Can.empty(), Can.<String>of());
    }

    @Test
    void emptyCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.empty());
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of());
    }

    @Test
    void singletonCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi"));
    }

    @Test
    void multiCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi", "there"));
    }

    // -- REVERTING

    @Test
    void multiCan_correctly_reverts() {
        assertEquals(Can.<String>of("c", "b", "a"), Can.<String>of("a", "b", "c").reverse());
    }

    @Test
    void multiCan_startsWith() {
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "c")));
        assertFalse(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "c", "x")));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b")));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>empty()));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(null));
        assertFalse(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "x")));
    }

    @Test
    void multiCan_endsWith() {
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("a", "b", "c")));
        assertFalse(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("x", "a", "b", "c")));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("b", "c")));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>empty()));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(null));
        assertFalse(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("x", "b", "a")));
    }

    // -- FILTERING

    @Test
    void emptyCanFilter_isIdentity() {
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(null));
    }

    @Test
    void singletonCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(x->true));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(null));
    }

    @Test
    void canFilter_whenNotAccept_isEmpty() {
        assertEquals(Can.<String>empty(), Can.<String>of("hi").filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>of("hi", "there").filter(x->false));
    }

    @Test
    void multiCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(x->true));
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(null));
    }

    @Test
    void multiCanFilter_whenAcceptOne_isDifferentCan() {
        assertEquals(Can.<String>of("there"), Can.<String>of("hi", "there").filter("there"::equals));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi", "there").filter("hi"::equals));
        assertEquals(Can.<String>of("hello"), Can.<String>of("hi", "hello", "there").filter("hello"::equals));
    }

    @Test
    void multiCanFilter_whenAcceptTwo_isDifferentCan() {
        assertEquals(Can.<String>of("hi", "hello"), Can.<String>of("hi", "hello", "there").filter(x->x.startsWith("h")));
    }

    // -- STREAM IDIOMS

    @Test
    void partialSums_reversed() {
        assertEquals(Can.<String>of("a", "b"), Can.<String>empty().add("a").add("b"));
        assertEquals(Can.<String>of("a", "b"), Can.<String>empty().add(0, "b").add(0, "a"));

        final Can<String> all = Can.<String>of("a", "b", "c");

        val iterator = all.reverseIterator();

        val partialSums = Stream.iterate(
                Can.<String>empty(),
                parts->parts.add(0, iterator.next()))
        .limit(4)
        .collect(Can.toCan());

        assertEquals(Can.of(
                    Can.<String>empty(),
                    Can.<String>of("c"),
                    Can.<String>of("b", "c"),
                    Can.<String>of("a", "b", "c")
                ),
                partialSums);

    }

    // -- TO SET CONVERSION

    @Test
    void multiCan_toSet_should_find_duplicates() {
        val expectedSet = _Sets.of("a", "b", "c");
        val duplicates = _Sets.<String>newHashSet();

        assertSetEquals(expectedSet, Can.<String>of("a", "c", "b", "a").toSet());
        assertSetEquals(expectedSet, Can.<String>of("a", "c", "b", "a").toSet(duplicates::add));
        assertSetEquals(_Sets.of("a"), duplicates);
    }

    // -- PICKING

    @Test
    void can_pickByIndex() {

        assertEquals(
                Can.empty(),
                Can.empty().pickByIndex(0, 1, 0));

        assertEquals(
                Can.empty(),
                Can.<String>of("a", "b", "c").pickByIndex(-2, 5));

        assertEquals(
                Can.of("a", "a", "a"),
                Can.<String>of("a").pickByIndex(0, 0, -2, 5, 0));

        assertEquals(
                Can.of("a", "b", "a"),
                Can.<String>of("a", "b", "c").pickByIndex(0, 1, -2, 0, 5));
    }

    // -- UNIQUE

    @Test
    void uniqueByEquals() {

        assertEquals(
                Can.empty(),
                Can.empty().distinct());

        assertEquals(
                Can.of("a"),
                Can.of("a").distinct());

        assertEquals(
                Can.of("a"),
                Can.of("a", "a", "a").distinct());

        assertEquals(
                Can.of("a", "b", "c"),
                Can.of("a", "b", "c").distinct());

        assertEquals(
                Can.of("a", "b"),
                Can.of("a", "b", "a").distinct());
    }

    @Test
    void uniqueByEqualityRelation() {

        final BiPredicate<String, String> firstCharEquility =
                (left, right) -> left.charAt(0) == right.charAt(0);

        assertEquals(
                Can.empty(),
                Can.<String>empty().distinct(firstCharEquility));

        assertEquals(
                Can.of("a"),
                Can.of("a").distinct(firstCharEquility));

        assertEquals(
                Can.of("aDog"),
                Can.of("aDog", "aCat", "aMonkey").distinct(firstCharEquility));

        assertEquals(
                Can.of("aDog", "bCat", "cMonkey"),
                Can.of("aDog", "bCat", "cMonkey").distinct(firstCharEquility));

        assertEquals(
                Can.of("aDog", "bCat"),
                Can.of("aDog", "bCat", "aMonkey").distinct(firstCharEquility));

    }

    @Test
    void partitioningInner_whenLastIsSmaller() {
        final Can<Integer> origin = Can.of(1, 2, 3, 4, 5, 6, 7, 8);
        final Can<Can<Integer>> subCans = origin.partitionInnerBound(3);
        assertEquals(3, subCans.size());
        assertEquals(Can.of(1, 2, 3),
                subCans.getElseFail(0));
        assertEquals(Can.of(4, 5, 6),
                subCans.getElseFail(1));
        assertEquals(Can.of(7, 8),
                subCans.getElseFail(2));
    }

    @Test
    void partitioningInner_whenLastIsFull() {
        final Can<Integer> origin = Can.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Can<Can<Integer>> subCans = origin.partitionInnerBound(3);
        assertEquals(3, subCans.size());
        assertEquals(Can.of(1, 2, 3),
                subCans.getElseFail(0));
        assertEquals(Can.of(4, 5, 6),
                subCans.getElseFail(1));
        assertEquals(Can.of(7, 8, 9),
                subCans.getElseFail(2));
    }

    @Test
    void partitioningOuter_whenEvenlySized() {
        final Can<Integer> origin = Can.of(1, 2, 3, 4, 5, 6);
        final Can<Can<Integer>> subCans = origin.partitionOuterBound(2);
        assertEquals(2, subCans.size());
        assertEquals(Can.of(1, 2, 3),
                subCans.getElseFail(0));
        assertEquals(Can.of(4, 5, 6),
                subCans.getElseFail(1));
    }

    @Test
    void partitioningOuter_whenUnevenlySized() {
        final Can<Integer> origin = Can.of(1, 2, 3, 4, 5);
        final Can<Can<Integer>> subCans = origin.partitionOuterBound(2);
        assertEquals(2, subCans.size());
        assertEquals(Can.of(1, 2, 3),
                subCans.getElseFail(0));
        assertEquals(Can.of(4, 5),
                subCans.getElseFail(1));
    }

    @Test
    void partitioningOuter_whenUnderRepresented() {
        final Can<Integer> origin = Can.of(1, 2, 3);
        final Can<Can<Integer>> subCans = origin.partitionOuterBound(5);
        assertEquals(3, subCans.size());
        assertEquals(Can.of(1),
                subCans.getElseFail(0));
        assertEquals(Can.of(2),
                subCans.getElseFail(1));
        assertEquals(Can.of(3),
                subCans.getElseFail(2));
    }

    // -- SUB-CAN 1 ARG

    @Test
    void subCan_singleArg_emptyCan() {
        final Can<Integer> origin = Can.empty();
        assertEquals(origin, origin.subCan(-1));
        assertEquals(origin, origin.subCan(0));
        assertEquals(origin, origin.subCan(1));
    }

    @Test
    void subCan_singleArg_singleCan() {
        final Can<Integer> origin = Can.of(9);
        assertEquals(origin, origin.subCan(-1));
        assertEquals(origin, origin.subCan(0));
        assertEquals(Can.empty(), origin.subCan(1));
        assertEquals(Can.empty(), origin.subCan(2));
    }

    @Test
    void subCan_singleArg_multiCan() {
        final Can<Integer> origin = Can.of(1, 2, 3);
        assertEquals(origin, origin.subCan(-1));
        assertEquals(origin, origin.subCan(0));
        assertEquals(Can.of(2, 3), origin.subCan(1));
        assertEquals(Can.of(3), origin.subCan(2));
        assertEquals(Can.empty(), origin.subCan(3));
        assertEquals(Can.empty(), origin.subCan(4));
    }

    // -- SUB-CAN 2 ARGS

    @Test
    void subCan_biArg_emptyCan() {
        final Can<Integer> origin = Can.empty();
        // negative end index semantics
        assertEquals(origin, origin.subCan(-1, -1));
        assertEquals(origin, origin.subCan(0, -1));
        assertEquals(origin, origin.subCan(1, -1));
        // ignore upper index overflow - same expectations as single arg call
        assertEquals(origin, origin.subCan(-1, 1));
        assertEquals(origin, origin.subCan(0, 1));
        assertEquals(origin, origin.subCan(1, 1));
    }

    @Test
    void subCan_biArg_singleCan() {
        final Can<Integer> origin = Can.of(9);
        assertEquals(Can.empty(), origin.subCan(-1, 0));
        assertEquals(Can.empty(), origin.subCan(0, 0));
        assertEquals(Can.empty(), origin.subCan(1, 0));
        // negative end index semantics
        assertEquals(Can.empty(), origin.subCan(-1, -1));
        assertEquals(Can.empty(), origin.subCan(0, -1));
        assertEquals(Can.empty(), origin.subCan(1, -1));
        // ignore upper index overflow - same expectations as single arg call
        assertEquals(origin, origin.subCan(-1, 2));
        assertEquals(origin, origin.subCan(0, 2));
        assertEquals(Can.empty(), origin.subCan(1, 2));
        assertEquals(Can.empty(), origin.subCan(2, 2));
    }

    @Test
    void subCan_biArg_multiCan() {
        final Can<Integer> origin = Can.of(1, 2, 3);
        assertEquals(Can.of(1, 2), origin.subCan(-1, 2));
        assertEquals(Can.of(1, 2), origin.subCan(0, 2));
        assertEquals(Can.of(2), origin.subCan(1, 2));
        assertEquals(Can.empty(), origin.subCan(2, 2));
        assertEquals(Can.empty(), origin.subCan(3, 2));
        // negative end index semantics
        assertEquals(Can.of(1, 2), origin.subCan(-1, -1));
        assertEquals(Can.of(1, 2), origin.subCan(0, -1));
        assertEquals(Can.of(2), origin.subCan(1, -1));
        assertEquals(Can.empty(), origin.subCan(2, -1));
        assertEquals(Can.empty(), origin.subCan(3, -1));
        // ignore upper index overflow - same expectations as single arg call
        assertEquals(origin, origin.subCan(-1, 4));
        assertEquals(origin, origin.subCan(0, 4));
        assertEquals(Can.of(2, 3), origin.subCan(1, 4));
        assertEquals(Can.of(3), origin.subCan(2, 4));
        assertEquals(Can.empty(), origin.subCan(3, 4));
        assertEquals(Can.empty(), origin.subCan(4, 4));
    }

    // -- TO MAP

    @Value
    static class Customer {
        final String name;
    }

    @Test
    void toMap_emptyCan() {
        final Can<Customer> origin = Can.empty();
        var map = origin.toMap(Customer::getName);
        assertNotNull(map);
        assertEquals(0, map.size());
        // verify immutability
        assertThrows(Exception.class, ()->{
           map.put("John", new Customer("John"));
        });
    }
    @Test
    void toMap_singleCan() {
        final Can<Customer> origin = Can.of(new Customer("Jeff"));
        var map = origin.toMap(Customer::getName);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(new Customer("Jeff"), map.get("Jeff"));
        // verify immutability
        assertThrows(Exception.class, ()->{
           map.put("John", new Customer("John"));
        });
    }
    @Test
    void toMap_multiCan() {
        final Can<Customer> origin = Can.of(new Customer("Jeff"), new Customer("Jane"));
        var map = origin.toMap(Customer::getName);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(new Customer("Jeff"), map.get("Jeff"));
        assertEquals(new Customer("Jane"), map.get("Jane"));
        // verify immutability
        assertThrows(Exception.class, ()->{
           map.put("John", new Customer("John"));
        });
    }

    // -- HEPER

    private static <T> void assertSetEquals(final Set<T> a, final Set<T> b) {
        assertTrue(_Sets.minus(a, b).isEmpty());
        assertTrue(_Sets.minus(b, a).isEmpty());
    }

}
