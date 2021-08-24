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
package org.apache.isis.core.metamodel.facets;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;

class CollectionFacetUtilsTest {

    List<Object> iterable;

    @BeforeEach
    void setUp() throws Exception {
        iterable = Arrays.<Object>asList("a", "b", "c");
    }

    @Test
    void whenLinkedList() throws Exception {

        List<Class<?>> collectionTypes = _Lists.<Class<?>>of(
                LinkedList.class,
                ArrayList.class,
                CopyOnWriteArrayList.class,
                AbstractList.class,
                LinkedHashSet.class,
                HashSet.class,
                TreeSet.class,
                CopyOnWriteArraySet.class,
                AbstractSet.class,
                List.class,
                SortedSet.class,
                Set.class,
                Collection.class
                );
        for (Class<?> collectionType : collectionTypes) {
            Object o = CollectionFacet.AutofitUtils.collect(iterable.stream(), collectionType);
            assertThat(o, is(not(nullValue())));
            assertThat(collectionType.isAssignableFrom(o.getClass()), is(true));

            @SuppressWarnings("rawtypes")
            Collection copy = (Collection)o;
            assertThat(copy.size(), is(3));
        }

    }

    @Test
    void whenArray() throws Exception {
        Object o = CollectionFacet.AutofitUtils.collect(iterable.stream(), String[].class);
        assertThat(o instanceof String[], is(true));

        String[] copy = (String[])o;
        assertThat(copy.length, is(3));
    }

    @Test
    void whenNotSupported() throws Exception {
        Object o = CollectionFacet.AutofitUtils.collect(iterable.stream(), Map.class);
        assertThat(o, is(nullValue()));
    }

    @Test
    void whenRequiredTypeIsNull() throws Exception {

        Assertions.assertThrows(NullPointerException.class, ()->
            CollectionFacet.AutofitUtils.collect(iterable.stream(), null));
    }

    @Test
    void whenIterableIsNull() throws Exception {

        Assertions.assertThrows(NullPointerException.class, ()->
            CollectionFacet.AutofitUtils.collect(null, List.class));
    }


}