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
package org.apache.isis.metamodel.commons;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ListUtilsTest_mutableCopy {

    @Test
    public void mutableCopyOfList_whenNotNull() throws Exception {
        List<Integer> input = Arrays.asList(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE);

        final List<Integer> list = ListExtensions.mutableCopy(input);

        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));

        // is mutable
        list.add(Integer.valueOf(-1));
    }

    @Test
    public void mutableCopyOfList_whenNull() throws Exception {
        List<Integer> input = null;

        final List<Integer> list = ListExtensions.mutableCopy(input);

        assertThat(list, is(not(nullValue())));
        assertThat(list.size(), is(0));

        // is mutable
        list.add(Integer.valueOf(-1));
    }

    @Test
    public void mutableCopyOfArray_whenNotNull() throws Exception {
        Integer[] input = {Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE};

        final List<Integer> list = ListExtensions.mutableCopy(input);

        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));

        // is mutable
        list.add(Integer.valueOf(-1));
    }

    @Test
    public void mutableCopyOfArray_whenNull() throws Exception {
        Integer[] input = null;

        final List<Integer> list = ListExtensions.mutableCopy(input);

        assertThat(list, is(not(nullValue())));
        assertThat(list.size(), is(0));

        // is mutable
        list.add(Integer.valueOf(-1));
    }

}
