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
package org.apache.isis.core.metamodel.facets;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotationsTest  {

    @Test
    public void when_null() throws Exception {
        final List<String> fieldNameCandidates = Annotations.fieldNameCandidatesFor(null);
        assertThat(fieldNameCandidates, is(not(nullValue())));
        assertThat(fieldNameCandidates.size(), is(0));
    }

    @Test
    public void when_starts_with_getter() throws Exception {
        final List<String> fieldNameCandidates = Annotations.fieldNameCandidatesFor("getFirstName");
        assertThat(fieldNameCandidates, is(not(nullValue())));
        assertThat(fieldNameCandidates.size(), is(2));
        assertThat(fieldNameCandidates.get(0), is("firstName"));
        assertThat(fieldNameCandidates.get(1), is("_firstName"));
    }

    @Test
    public void tolerant_to_not_exactly_following_javaBean_convention() throws Exception {
        final List<String> fieldNameCandidates = Annotations.fieldNameCandidatesFor("getfirstName");
        assertThat(fieldNameCandidates, is(not(nullValue())));
        assertThat(fieldNameCandidates.size(), is(2));
        assertThat(fieldNameCandidates.get(0), is("firstName"));
        assertThat(fieldNameCandidates.get(1), is("_firstName"));
    }

    @Test
    public void when_is_exactly_just_the_get() throws Exception {
        final List<String> fieldNameCandidates = Annotations.fieldNameCandidatesFor("get");
        assertThat(fieldNameCandidates, is(not(nullValue())));
        assertThat(fieldNameCandidates.size(), is(0));
    }

    @Test
    public void when_starts_with_something_else_getter() throws Exception {
        final List<String> fieldNameCandidates = Annotations.fieldNameCandidatesFor("XetFirstName");
        assertThat(fieldNameCandidates, is(not(nullValue())));
        assertThat(fieldNameCandidates.size(), is(0));
    }

}