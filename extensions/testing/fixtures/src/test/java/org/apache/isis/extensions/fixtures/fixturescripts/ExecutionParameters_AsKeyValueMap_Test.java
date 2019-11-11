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
package org.apache.isis.extensions.fixtures.fixturescripts;

import java.util.Map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExecutionParameters_AsKeyValueMap_Test extends ExecutionContextTest {

    @Test
    public void happyCase() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("foo=bar\nbop=baz");
        assertThat(map.size(), is(2));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
    }

    @Test
    public void givenNull() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap(null);
        assertThat(map.size(), is(0));
    }

    @Test
    public void givenEmpty() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("");
        assertThat(map.size(), is(0));
    }

    @Test
    public void trim() throws Exception {

        final Map<String, String> map = ExecutionParameters.asKeyValueMap(" foo=bar\nbop=baz \n bip = bap ");
        assertThat(map.size(), is(3));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
        assertThat(map.get("bip"), is("bap"));
    }

    @Test
    public void malformed() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("abcde");
        assertThat(map.size(), is(0));
    }

    @Test
    public void partiallyMalformed() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("foo=bar\nabcde\nbop=baz");
        assertThat(map.size(), is(2));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
    }
}
