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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ExecutionContextTest {

    public static class GetParameter extends ExecutionContextTest {

        @Test
        public void happyCase() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bar\nbop=baz", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));

        }

        @Test
        public void givenNull() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext((String)null, null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void givenEmpty() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("", null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void malformed() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("abcde", null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void partiallyMalformed() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bar\nabcde\nbop=baz", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));
        }

        @Test
        public void trim() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext(" foo=bar\nbop=baz \n bip = bap ", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(3));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));
            assertThat(map.get("bip"), is("bap"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));
            assertThat(executionContext.getParameter("bip"), is("bap"));
        }

    }
    public static class SetParameterIfNotPresent extends ExecutionContextTest {

        @Test
        public void whenNotPresent() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext((String)null, null);
            executionContext.setParameterIfNotPresent("foo", "bar");

            assertThat(executionContext.getParameter("foo"), is("bar"));
        }

        @Test
        public void whenPresent() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bop\n", null);
            executionContext.setParameterIfNotPresent("foo", "bar");

            assertThat(executionContext.getParameter("foo"), is("bop"));
        }

    }


}