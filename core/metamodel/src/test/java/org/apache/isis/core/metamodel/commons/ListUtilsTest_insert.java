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
package org.apache.isis.core.metamodel.commons;

import java.util.List;

import org.junit.Test;

import org.apache.isis.commons.internal.collections._Lists;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ListUtilsTest_insert {

    @Test
    public void insert_whenInsertionPointAtBeginning() throws Exception {
        final List<Integer> list = _Lists.newArrayList(_Lists.of(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.insert(list, 0, Integer.valueOf(10));

        assertThat(list.size(), is(4));
        assertThat(list.get(0), is(Integer.valueOf(10)));
        assertThat(list.get(1), is(Integer.valueOf(0)));
        assertThat(list.get(2), is(Integer.MAX_VALUE));
        assertThat(list.get(3), is(Integer.MIN_VALUE));
    }

    @Test
    public void insert_whenInsertionPointInMiddle() throws Exception {
        final List<Integer> list = _Lists.newArrayList(_Lists.of(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.insert(list, 1, Integer.valueOf(10));

        assertThat(list.size(), is(4));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.valueOf(10)));
        assertThat(list.get(2), is(Integer.MAX_VALUE));
        assertThat(list.get(3), is(Integer.MIN_VALUE));
    }

    @Test
    public void insert_whenInsertionPointAtEnd() throws Exception {
        final List<Integer> list = _Lists.newArrayList(_Lists.of(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.insert(list, 3, Integer.valueOf(10));

        assertThat(list.size(), is(4));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));
        assertThat(list.get(3), is(Integer.valueOf(10)));
    }

    @Test
    public void insert_whenInsertionPointBeyondEnd() throws Exception {
        final List<Integer> list = _Lists.newArrayList(_Lists.of(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.insert(list, 4, Integer.valueOf(10));

        assertThat(list.size(), is(5));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));
        assertThat(list.get(3), is(nullValue()));
        assertThat(list.get(4), is(Integer.valueOf(10)));
    }



}
