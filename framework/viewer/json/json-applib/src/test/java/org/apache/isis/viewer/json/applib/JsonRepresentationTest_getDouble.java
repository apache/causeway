package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_getDouble {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }
    
    @Test
    public void happyCase() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getDouble("aDouble"), is(12345678901234534.3242));
    }

    @Test
    public void forNonExistent() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getDouble("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forValueButNotAnLong() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getDouble("aString");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aString' (\"aStringValue\") is not a double"));
        }
    }

    @Test
    public void forMap() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getDouble("aSubMap");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' (a map) is not a double"));
        }
    }

    @Test
    public void forList() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getDouble("aSubList");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubList' (a list) is not a double"));
        }
    }

    @Test
    public void forMultipartKey() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getDouble("aSubMap.aDouble"), is(12345678901234534.4567));
    }
    
}
