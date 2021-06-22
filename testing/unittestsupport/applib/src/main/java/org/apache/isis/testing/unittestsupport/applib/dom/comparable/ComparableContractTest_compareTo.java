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
package org.apache.isis.testing.unittestsupport.applib.dom.comparable;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * <p>
 *     Used by core and domain apps.
 * </p>
 *
 * @param <T>
 *
 * @since 2.0 {@index}
 */
public abstract class ComparableContractTest_compareTo<T extends Comparable<T>> {

    /**
     * Return an array of tuples; each tuple should consist of 4 elements, whereby
     * item0  < item1 = item2 < item3
     *
     * Typically item0 should be null valued (if supported by the impl).
     */
    protected abstract List<List<T>> orderedTuples();

    @Test
    public void compareAllOrderedTuples() {

        new ComparableContractTester<T>(orderedTuples()).test();
    }

    /**
     * Syntax sugar to remove boilerplate from subclasses.
     */
    @SafeVarargs
    protected static <E> List<E> listOf(E... elements) {
        return Arrays.asList(elements);
    }

}
