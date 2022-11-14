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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.apache.causeway.viewer.restfulobjects.applib.JsonFixture.readJson;

class JsonRepresentationTest_streamArrayElements {

    private JsonRepresentation jsonRepresentation;

    @Test
    public void forJsonRepresentation() throws IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        final Iterator<JsonRepresentation> arrayIterator =
                jsonRepresentation.streamArrayElements(JsonRepresentation.class)
                .iterator();
        assertThat(arrayIterator.hasNext(), is(true));
        assertThat(arrayIterator.next().getString("a"), is("a1"));
        assertThat(arrayIterator.hasNext(), is(true));
        assertThat(arrayIterator.next().getString("b"), is("b1"));
        assertThat(arrayIterator.hasNext(), is(false));
    }

    @Test
    public void forString() throws IOException {
        jsonRepresentation = new JsonRepresentation(readJson("listOfStrings.json"));
        final Iterator<String> arrayIterator =
                jsonRepresentation.streamArrayElements(String.class)
                .iterator();
        assertThat(arrayIterator.hasNext(), is(true));
        assertThat(arrayIterator.next(), is("a"));
        assertThat(arrayIterator.hasNext(), is(true));
        assertThat(arrayIterator.next(), is("b"));
        assertThat(arrayIterator.hasNext(), is(true));
        assertThat(arrayIterator.next(), is("c"));
        assertThat(arrayIterator.hasNext(), is(false));
    }

}
