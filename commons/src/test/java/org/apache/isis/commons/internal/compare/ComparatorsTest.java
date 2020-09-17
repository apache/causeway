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

package org.apache.isis.commons.internal.compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.compare._Comparators;

public class ComparatorsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void nullArgumentLeftAndRigth() throws Exception {
        Assert.assertThat(
                _Comparators.deweyOrderCompare(null, null), 
                is(0));
    }

    @Test
    public void nullArgumentLeft() throws Exception {
        Assert.assertThat(
                _Comparators.deweyOrderCompare(null, "any"), 
                is(1));
    }

    @Test
    public void nullArgumentRight() throws Exception {
        Assert.assertThat(
                _Comparators.deweyOrderCompare("any", null), 
                is(-1));
    }

    @Test
    public void inOrderMixed() throws Exception {
        assertThatSorting(
                ofS("1", "a"),
                ofL("1", "a")
                );
    }

    @Test
    public void notInOrderMixed() throws Exception {
        assertThatSorting(
                ofS("b", "1"),
                ofL("1", "b")
                );
    }


    @Test
    public void emptySet() throws Exception {
        assertThatSorting(
                ofS(),
                ofL());
    }

    @Test
    public void singleElement() throws Exception {
        assertThatSorting(
                ofS("1"),
                ofL("1")
                );
    }

    @Test
    public void inOrder() throws Exception {
        assertThatSorting(
                ofS("1", "2"),
                ofL("1", "2")
                );
    }

    @Test
    public void notInOrder() throws Exception {
        assertThatSorting(
                ofS("2", "1"),
                ofL("1", "2")
                );
    }

    @Test
    public void notInOrderDepth2() throws Exception {
        assertThatSorting(
                ofS("1.2", "1.1"),
                ofL("1.1", "1.2")
                );
    }

    @Test
    public void differentDepths() throws Exception {
        assertThatSorting(
                ofS("2", "1.3", "1.2", "1.2.2", "1.2.1", "1.1"),
                ofL("1.1", "1.2", "1.2.1", "1.2.2", "1.3", "2")
                );
    }

    @Test
    public void mismatchedDepth3() throws Exception {
        assertThatSorting(
                ofS("1.2.2", "1.2.1", "1.1"),
                ofL("1.1", "1.2.1", "1.2.2")
                );
    }

    @Test
    public void X() throws Exception {
        assertThatSorting(
                ofS("45.1", "10.10"),
                ofL("10.10", "45.1")
                );
    }

    private static Collection<String> ofS(String... str) {
        return Arrays.asList(str);
    }

    private static List<String> ofL(String... str) {
        return _Lists.newArrayList(ofS(str));
    }

    private static void assertThatSorting(Collection<String> input, List<String> expected) {
        final SortedSet<String> treeSet = new TreeSet<String>(_Comparators.deweyOrderComparator);
        treeSet.addAll(input);
        final List<String> strings = _Lists.newArrayList(treeSet); 
        Assert.assertThat(strings, is(expected));
    }
}
