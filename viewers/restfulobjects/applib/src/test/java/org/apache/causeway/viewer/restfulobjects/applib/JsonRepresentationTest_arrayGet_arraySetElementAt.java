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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.apache.causeway.viewer.restfulobjects.applib.JsonFixture.readJson;

class JsonRepresentationTest_arrayGet_arraySetElementAt {

    private JsonRepresentation jsonRepresentation;
    private JsonRepresentation arrayRepr;
    private JsonRepresentation objectRepr;

    @BeforeEach
    public void setUp() throws Exception {
        arrayRepr = JsonRepresentation.newArray();
        objectRepr = JsonRepresentation.newMap();
    }

    @Test
    public void arrayGet_outOfBounds() throws IOException {
        assertThrows(IndexOutOfBoundsException.class, ()->{
            jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
            jsonRepresentation.arrayGet(0);
        });
    }

    @Test
    public void arraySetElementAt_outOfBounds() throws IOException {
        assertThrows(IndexOutOfBoundsException.class, ()->{
            jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
            jsonRepresentation.arraySetElementAt(0, objectRepr);
        });
    }

    @Test
    public void arrayGet_forNonEmptyList() throws IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.arrayGet(0), is(not(nullValue())));
    }

    @Test
    public void arraySetElementAt_happyCaseWhenSetElementToObject() throws IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        jsonRepresentation.arraySetElementAt(0, objectRepr);
    }

    @Test
    public void arraySetElementAt_forAttemptingToSetElementToArray() throws IOException {
        assertThrows(IllegalArgumentException.class, ()->{
            jsonRepresentation = new JsonRepresentation(readJson("list.json"));
            jsonRepresentation.arraySetElementAt(0, arrayRepr);
        });
    }

    @Test
    public void arrayGet_forMap() throws IOException {
        assertThrows(IllegalStateException.class, ()->{
            jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
            jsonRepresentation.arrayGet(0);
        });
    }

    @Test
    public void arrayGet_forValue() throws IOException {
        assertThrows(IllegalStateException.class, ()->{
            jsonRepresentation = new JsonRepresentation(readJson("map.json"));
            final JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("anInt");
            valueRepresentation.arrayGet(0);
        });
    }

}
