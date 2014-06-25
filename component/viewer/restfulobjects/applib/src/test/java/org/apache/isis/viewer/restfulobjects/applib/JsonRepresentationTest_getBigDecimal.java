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

import static org.apache.isis.viewer.restfulobjects.applib.JsonFixture.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonRepresentationTest_getBigDecimal {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }

    @Test
    public void happyCase() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("aBigDecimal"), is(new BigDecimal("12345678901234567890.1234")));
    }

    @Test
    public void happyCaseConvertingAnotherDoubleToBigDecimal() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("anotherDouble"), is(new BigDecimal("1234567.89")));
    }

    @Test
    public void happyCaseConvertingAnIntToBigDecimal() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("anInt"), is(new BigDecimal("123")));
    }
    
    @Test
    public void happyCaseForFormatJustFits() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("aBigDecimal", "big-decimal(24,4)"), is(new BigDecimal("12345678901234567890.1234")));
    }

    @Test
    public void invalidFormat() throws JsonParseException, JsonMappingException, IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value '12345678901234567890.1234' larger than that allowed by format 'big-decimal(22,3)'");

        assertThat(jsonRepresentation.getBigDecimal("aBigDecimal", "big-decimal(22,3)"), is(new BigDecimal("12345678901234567890")));
    }

    @Test
    public void validFormattedFromPath() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("yetAnotherSubMap.aFormattedBigDecimal.value"), is(new BigDecimal("123.45")));
    }

    @Test
    public void invalidFormattedFromPath() throws JsonParseException, JsonMappingException, IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value '123.45' larger than that allowed by format 'big-decimal(4,2)'");

        jsonRepresentation.getBigDecimal("yetAnotherSubMap.anInvalidFormattedBigDecimal.value");
    }

    @Test
    public void invalidFormattedFromPathButOverridden() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("yetAnotherSubMap.anInvalidFormattedBigDecimal.value", "big-decimal(5,2)"), is(new BigDecimal("123.45")));
    }

    @Test
    public void forNonExistent() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forNonParseableString() throws JsonParseException, JsonMappingException, IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'aString' is not a bigdecimal");

        jsonRepresentation.getBigDecimal("aString");
    }

    @Test
    public void forMap() throws JsonParseException, JsonMappingException, IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'aSubMap' is not a bigdecimal");

        jsonRepresentation.getBigDecimal("aSubMap");
    }

    @Test
    public void forList() throws JsonParseException, JsonMappingException, IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'aSubList' is not a bigdecimal");

        jsonRepresentation.getBigDecimal("aSubList");
    }

    @Test
    public void forMultipartKey() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getBigDecimal("aSubMap.aBigDecimal"), is(new BigDecimal("12345678901234567890.1234")));
    }

}
