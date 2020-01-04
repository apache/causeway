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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static org.apache.isis.viewer.restfulobjects.applib.JsonFixture.readJson;

public class JsonRepresentationTest_getNull_isNull {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }

    @Test
    public void forNullValue() throws IOException {
        final JsonRepresentation nullValue = jsonRepresentation.getNull("aNull");
        assertThat(nullValue, is(not(nullValue())));
        assertThat(nullValue.isNull(), is(true));

        final Boolean isNull = jsonRepresentation.isNull("aNull");
        assertThat(isNull, is(true));
    }

    @Test
    public void isNull_forArray() throws IOException {
        final Boolean isNull = jsonRepresentation.isNull("anEmptyArray");
        assertThat(isNull, is(false));
    }

    @Test
    public void getNull_forArray() {
        try {
            jsonRepresentation.getNull("anEmptyArray");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'anEmptyArray' is not the null value"));
        }
    }

    @Test
    public void isNull_forNonExistent() {
        final Boolean isNull = jsonRepresentation.isNull("nonExistent");
        assertThat(isNull, is(nullValue())); // ie don't know whether is null
    }

    @Test
    public void getNull_forNonExistent() {
        assertThat(jsonRepresentation.getArray("doesNotExist"), is(nullValue()));
    }

    @Test
    public void isNull_forMap() throws IOException {
        final Boolean isNull = jsonRepresentation.isNull("aSubMap");
        assertThat(isNull, is(false));
    }

    @Test
    public void forMap() {
        try {
            jsonRepresentation.getNull("aSubMap");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' is not the null value"));
        }
    }

}
