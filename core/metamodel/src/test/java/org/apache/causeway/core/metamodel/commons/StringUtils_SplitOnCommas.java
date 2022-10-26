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
package org.apache.causeway.core.metamodel.commons;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class StringUtils_SplitOnCommas {

    @Test
    void length() {
        final List<String> list = StringExtensions.splitOnCommas("foo,bar");
        assertThat(list.size(), CoreMatchers.is(2));
    }

    @Test
    void elements() {
        final List<String> list = StringExtensions.splitOnCommas("foo,bar");
        assertThat(list.get(0), CoreMatchers.is("foo"));
        assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    void whenHasWhiteSpaceAfterComma() {
        final List<String> list = StringExtensions.splitOnCommas("foo, bar");
        assertThat(list.get(0), CoreMatchers.is("foo"));
        assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    void whenHasLeadingWhiteSpace() {
        final List<String> list = StringExtensions.splitOnCommas(" foo, bar");
        assertThat(list.get(0), CoreMatchers.is("foo"));
        assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    void whenNull() {
        final List<String> list = StringExtensions.splitOnCommas(null);
        assertThat(list, CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    void whenEmpty() {
        final List<String> list = StringExtensions.splitOnCommas("");
        assertThat(list.size(), CoreMatchers.is(0));
    }

    @Test
    void whenOnlyWhiteSpace() {
        final List<String> list = StringExtensions.splitOnCommas(" ");
        assertThat(list.size(), CoreMatchers.is(0));
    }

    @Test
    void whenOnlyWhiteSpaceTabs() {
        final List<String> list = StringExtensions.splitOnCommas("\t");
        assertThat(list.size(), CoreMatchers.is(0));
    }

}
