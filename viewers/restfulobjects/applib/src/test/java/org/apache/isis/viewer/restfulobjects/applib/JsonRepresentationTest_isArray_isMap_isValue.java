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
package org.apache.isis.viewer.restfulobjects.applib;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.apache.isis.viewer.restfulobjects.applib.JsonFixture.readJson;

public class JsonRepresentationTest_isArray_isMap_isValue {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }

    @Test
    public void forMap() throws IOException {
        assertThat(jsonRepresentation.isArray(), is(false));
        assertThat(jsonRepresentation.isMap(), is(true));
        assertThat(jsonRepresentation.isValue(), is(false));
    }

    @Test
    public void forValue() throws IOException {
        final JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("aString");
        assertThat(valueRepresentation.isArray(), is(false));
        assertThat(valueRepresentation.isMap(), is(false));
        assertThat(valueRepresentation.isValue(), is(true));
    }

    @Test
    public void forList() throws IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.isArray(), is(true));
        assertThat(jsonRepresentation.isMap(), is(false));
        assertThat(jsonRepresentation.isValue(), is(false));
    }

}
