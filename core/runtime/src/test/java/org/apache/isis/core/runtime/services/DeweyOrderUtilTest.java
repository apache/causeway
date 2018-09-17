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
import com.google.common.collect.Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

public class DeweyOrderUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void emptySet() throws Exception {
        assertThatSorting(
                ofS(),
                ofL());
    }

    @Test
    public void singleElement() throws Exception {
        assertThatSorting(
                ofS(el("1", "aaa")),
                ofL(el("1", "aaa")));
    }

    @Test
    public void inOrder() throws Exception {
        assertThatSorting(
                ofS(el("1", "aaa"), el("2", "bbb")),
                ofL(el("1", "aaa"), el("2", "bbb")));
    }

    @Test
    public void notInOrder() throws Exception {
        assertThatSorting(
                ofS(el("2", "bbb"), el("1", "aaa")),
                ofL(el("1", "aaa"), el("2", "bbb")));
    }

    @Test
    public void notInOrderDepth2() throws Exception {
        assertThatSorting(
                ofS(el("1.2", "bbb"), el("1.1", "aaa")),
                ofL(el("1.1", "aaa"), el("1.2", "bbb")));
    }

    @Test
    public void differentDepths() throws Exception {
        assertThatSorting(
                ofS(el("2", "aaa"), el("1.3", "aaa"), el("1.2", "aaa"), el("1.2.2", "ccc"), el("1.2.1", "bbb"), el("1.1", "aaa")),
                ofL(el("1.1", "aaa"), el("1.2.1", "bbb"), el("1.2.2", "ccc"), el("1.2", "aaa"), el("1.3", "aaa"), el("2", "aaa")));
    }

    @Test
    public void mismatchedDepth3() throws Exception {
        assertThatSorting(
                ofS(el("1.2.2", "ccc"), el("1.2.1", "bbb"), el("1.1", "aaa")),
                ofL(el("1.1", "aaa"), el("1.2.1", "bbb"), el("1.2.2", "ccc")));
    }

    private void assertThatSorting(Set<Map.Entry<String, Object>> input, List<Map.Entry<String, Object>> expected) {
        final List<Map.Entry<String, Object>> list = DeweyOrderUtil.deweySorted(input);
        Assert.assertThat(list, is(expected));
    }


    private static Set<Map.Entry<String, Object>> ofS(String[]... str) {
        final Set<Map.Entry<String,Object>> seq = _Sets.newLinkedHashSet();
        for (String[] strings : str) {
            if(strings.length != 2) {
                throw new IllegalArgumentException("array must have 2 elements");
            }
            seq.add(new MapEntry<String, Object>(strings[0], (Object) strings[1]));
        }
        return seq;
    }

    private static List<Map.Entry<String, Object>> ofL(String[]... str) {
        return Lists.newArrayList(ofS(str));
    }


    private static String[] el(String a, String b) {
        return new String[]{a, b};
    }

}