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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.apache.isis.viewer.restfulobjects.applib.JsonFixture.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JsonRepresentationTest_getDouble {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }

    @Test
    public void happyCase() throws IOException {
        assertThat(jsonRepresentation.getDouble("aDouble"), is(12345678901234534.3242));
    }

    @Test
    public void forNonExistent() throws IOException {
        assertThat(jsonRepresentation.getDouble("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forValueButNotAnLong() throws IOException {
        try {
            jsonRepresentation.getDouble("aString");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aString' is not a double"));
        }
    }

    @Test
    public void forMap() throws IOException {
        try {
            jsonRepresentation.getDouble("aSubMap");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' is not a double"));
        }
    }

    @Test
    public void forList() throws IOException {
        try {
            jsonRepresentation.getDouble("aSubList");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubList' is not a double"));
        }
    }

    @Test
    public void forMultipartKey() throws IOException {
        assertThat(jsonRepresentation.getDouble("aSubMap.aDouble"), is(12345678901234534.4567));
    }

}
