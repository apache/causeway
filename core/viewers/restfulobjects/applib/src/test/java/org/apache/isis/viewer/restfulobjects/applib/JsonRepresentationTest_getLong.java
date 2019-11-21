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

import static org.apache.isis.viewer.restfulobjects.applib.JsonFixture.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JsonRepresentationTest_getLong {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }

    @Test
    public void happyCase() throws IOException {
        assertThat(jsonRepresentation.getLong("aLong"), is(1234567890123L));
    }

    @Test
    public void forNonExistent() throws IOException {
        assertThat(jsonRepresentation.getLong("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forValueButNotAnLong() throws IOException {
        try {
            jsonRepresentation.getLong("aString");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aString' is not a long"));
        }
    }

    @Test
    public void forMap() throws IOException {
        try {
            jsonRepresentation.getLong("aSubMap");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' is not a long"));
        }
    }

    @Test
    public void forList() throws IOException {
        try {
            jsonRepresentation.getLong("aSubList");
            fail();
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubList' is not a long"));
        }
    }

    @Test
    public void forMultipartKey() throws IOException {
        assertThat(jsonRepresentation.getLong("aSubMap.aLong"), is(1234567890456L));
    }

}
