package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JsonRepresentationTest_getString {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getString_forMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        assertThat(jsonRepresentation.getString("aString"), is("aStringValue"));
    }

    @Test
    public void getString_forMap_nonExistent() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        assertThat(jsonRepresentation.getString("doesNotExist"), is(nullValue()));
    }

    @Test
    public void getString_forMap_whenValueButNotAString() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        try {
            jsonRepresentation.getString("anInt");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'anInt' (123) is not a string"));
        }
    }

    @Test
    public void getString_forMap_whenSubNodeIsMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        try {
            jsonRepresentation.getString("aSubMap");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' (a map) is not a string"));
        }
    }

    @Test
    public void getString_forMap_whenSubNodeIsList() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        try {
            jsonRepresentation.getString("aSubList");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubList' (a list) is not a string"));
        }
    }

    @Ignore
    @Test
    public void getString_forSubMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        assertThat(jsonRepresentation.getString("aSubMap.aString"), is("aSubMapStringValue"));
    }
    
}
