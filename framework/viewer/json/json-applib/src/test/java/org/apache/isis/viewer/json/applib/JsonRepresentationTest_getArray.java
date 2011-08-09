package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_getArray {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }
    
    @Test
    public void nonEmptyArray() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation array = jsonRepresentation.getArray("aStringArray");
        assertThat(array, is(not(nullValue())));
        assertThat(array.arraySize(), is(3));
    }

    @Test
    public void emptyArray() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation array = jsonRepresentation.getArray("anEmptyArray");
        assertThat(array, is(not(nullValue())));
        assertThat(array.arraySize(), is(0));
    }

    @Test
    public void forNonExistent() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getArray("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forValue() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getArray("aString");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aString' (a value) is not an array"));
        }
    }

    @Test
    public void forMap() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getArray("aSubMap");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' (a map) is not an array"));
        }
    }
    
}
