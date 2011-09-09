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
package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_arraySize_elementAt_setElementAt {

    private JsonRepresentation jsonRepresentation;
    private JsonRepresentation arrayRepr;
    private JsonRepresentation objectRepr;
    
    @Before
    public void setUp() throws Exception {
        arrayRepr = JsonRepresentation.newArray();
        objectRepr = JsonRepresentation.newMap();
    }

    @Test
    public void arraySize_forEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        assertThat(jsonRepresentation.arraySize(), is(0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void elementAt_outOfBounds() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        jsonRepresentation.arrayElementAt(0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setElementAt_outOfBounds() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        jsonRepresentation.arraySetElementAt(0, objectRepr);
    }

    @Test
    public void arraySize_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.arraySize(), is(2));
    }

    @Test
    public void elementAt_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.arrayElementAt(0), is(not(nullValue())));
    }

    @Test
    public void setElementAt_happyCaseWhenSetElementToObject() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        jsonRepresentation.arraySetElementAt(0, objectRepr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setElementAt_forAttemptingToSetElementToArray() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        jsonRepresentation.arraySetElementAt(0, arrayRepr);
    }
    
    @Test(expected=IllegalStateException.class)
    public void arraySize_forMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.arraySize();
    }

    @Test(expected=IllegalStateException.class)
    public void elementAt_forMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.arrayElementAt(0);
    }

    @Test(expected=IllegalStateException.class)
    public void arraySize_forValue() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("anInt");
        valueRepresentation.arraySize();
    }

    @Test(expected=IllegalStateException.class)
    public void elementAt_forValue() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("anInt");
        valueRepresentation.arrayElementAt(0);
    }

}
