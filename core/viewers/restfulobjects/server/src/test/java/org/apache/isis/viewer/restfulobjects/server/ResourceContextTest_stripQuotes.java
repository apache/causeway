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
package org.apache.isis.viewer.restfulobjects.server;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceContextTest_stripQuotes {

    @Test
    public void whenQuotes() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123\"");
        assertThat(x, is("123"));
    }

    @Test
    public void whenNoQuotes() throws Exception {
        final String x = ResourceContext.stripQuotes("123");
        assertThat(x, is("123"));
    }

    @Test
    public void whenFirstQuote() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123");
        assertThat(x, is("\"123"));
    }

    @Test
    public void whenEndQuote() throws Exception {
        final String x = ResourceContext.stripQuotes("123\"");
        assertThat(x, is("123\""));
    }

    @Test
    public void whenCharsAfter() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123\" ");
        assertThat(x, is("\"123\" "));
    }

    @Test
    public void whenCharsBefore() throws Exception {
        final String x = ResourceContext.stripQuotes(" \"123\"");
        assertThat(x, is(" \"123\""));
    }

    @Test
    public void whenEmpty() throws Exception {
        final String x = ResourceContext.stripQuotes("");
        assertThat(x, is(""));
    }

    @Test
    public void whenNull() throws Exception {
        final String x = ResourceContext.stripQuotes(null);
        assertThat(x, is(nullValue()));
    }

}