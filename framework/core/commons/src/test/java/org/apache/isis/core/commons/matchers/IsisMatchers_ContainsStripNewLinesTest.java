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

package org.apache.isis.core.commons.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class IsisMatchers_ContainsStripNewLinesTest {

    private Matcher<String> fooMatcher;

    @Before
    public void setUp() {
        fooMatcher = IsisMatchers.containsStripNewLines("foo");
    }

    @Test
    public void shouldMatchExactString() {
        assertThat(fooMatcher.matches("foo"), is(true));
    }

    @Test
    public void shouldMatchIfContainsStringNoNewLines() {
        assertThat(fooMatcher.matches("abcfoodef"), is(true));
    }

    @Test
    public void shouldMatchIfContainsStringHasNewLinesBefore() {
        assertThat(fooMatcher.matches("a\nb\rc\r\ndfoodef"), is(true));
    }

    @Test
    public void shouldMatchIfContainsStringHasNewLinesAfter() {
        assertThat(fooMatcher.matches("abrdfood\ne\rfan\rg"), is(true));
    }

    @Test
    public void shouldMatchIfContainsStringHasNewLinesWithin() {
        assertThat(fooMatcher.matches("abcf\ro\nodef"), is(true));
    }

    @Test
    public void shouldNotMatchIfDoesNotContainsString() {
        assertThat(fooMatcher.matches("fob"), is(false));
    }

}
