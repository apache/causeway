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
package org.apache.isis.applib.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReasonsTest {

    @Test
    public void testNoReasonReturnsNull() throws Exception {
        assertThat(Reasons.coalesce(), is(nullValue()));
    }

    @Test
    public void testSingleNullReturnsNull() throws Exception {
        assertThat(Reasons.coalesce((String) null), is(nullValue()));
    }

    @Test
    public void testSingleNonNullReturnsSame() throws Exception {
        assertThat(Reasons.coalesce("yada"), is("yada"));
    }

    @Test
    public void testNullThenNonNullReturnsLatter() throws Exception {
        assertThat(Reasons.coalesce(null, "yada"), is("yada"));
    }

    @Test
    public void testNotNullThenNonNullReturnsBothConcatenated() throws Exception {
        assertThat(Reasons.coalesce("foobar", "yada"), is("foobar; yada"));
    }

    @Test
    public void testNotNullThenNullBothFormer() throws Exception {
        assertThat(Reasons.coalesce("foobar", null), is("foobar"));
    }

    @Test
    public void testNullsAreSkippedThenNonNull() throws Exception {
        assertThat(Reasons.coalesce("foobar", null, "yada"), is("foobar; yada"));
    }

}
