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
package org.apache.isis.testing.unittestsupport.applib.core.comparable;

import java.util.List;

import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.commons.internal.collections._Lists;

/**
 * @since 2.0 {@index}
 */
public class ComparableContractTester<T extends Comparable<T>> {

    private final List<List<T>> orderedTuples;

    /**
     * Provide an array of tuples; each tuple should consist of 4 elements, whereby
     * item0  < item1 = item2 < item3
     *
     * Typically item0 should be null valued (if supported by the impl).
     */
    public ComparableContractTester(List<List<T>> orderedTuples) {
        this.orderedTuples = orderedTuples;
    }

    public void test() {

        for(List<T> orderedTuple: orderedTuples) {

            T item1 = orderedTuple.get(0);
            T item2 = orderedTuple.get(1);
            T item3 = orderedTuple.get(2);
            T item4 = orderedTuple.get(3);

            assertThat(desc(item1, "<", item2), item1.compareTo(item2), is(Matchers.lessThan(0)));
            assertThat(desc(item2, ">", item1), item2.compareTo(item1), is(Matchers.greaterThan(0)));

            assertThat(desc(item2, "==", item3), item2.compareTo(item3), is(0));
            assertThat(desc(item3, "==", item2), item3.compareTo(item2), is(0));

            assertThat(desc(item3, "<", item4), item3.compareTo(item4), is(Matchers.lessThan(0)));
            assertThat(desc(item4, ">", item3), item4.compareTo(item3), is(Matchers.greaterThan(0)));
        }
    }

    protected static String desc(Object item1, final String op, Object item2) {
        return nullSafe(item1) + op + nullSafe(item2);
    }

    private static String nullSafe(Object item) {
        return item != null? item.toString(): "null";
    }

    /**
     * Syntax sugar to remove boilerplate from subclasses.
     */
    @SafeVarargs
    public static <E> List<E> listOf(E... elements) {
        return _Lists.of(elements);
    }


}
