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
package org.apache.isis.viewer.restfulobjects.applib.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RestfulRequest_RequestParameterTest_valueOf_xrofollowlinks {

    private final RequestParameter<List<List<String>>> requestParameter = RestfulRequest.RequestParameter.FOLLOW_LINKS;

    private JsonRepresentation repr;

    @Before
    public void setUp() throws Exception {
        repr = JsonRepresentation.newMap();
    }

    @Test
    public void mapContainsList() {
        repr.mapPut("x-ro-follow-links", "a,b.c");
        final List<List<String>> valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }

    @Test
    public void mapHasNoKey() {
        repr.mapPut("something-else", "a,b.c");
        final List<List<String>> valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsEmpty() {
        final List<List<String>> valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsNull() {
        final List<List<String>> valueOf = requestParameter.valueOf(null);

        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapContainsCommaSeparatedList() {

        repr.mapPut("x-ro-follow-links", "a,b.c");
        final List<List<String>> valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }

    
    
}
