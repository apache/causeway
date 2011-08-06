package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_isArray_isMap_isValue {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }
    
    @Test
    public void forMap() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.isArray(), is(false));
        assertThat(jsonRepresentation.isMap(), is(true));
        assertThat(jsonRepresentation.isValue(), is(false));
    }

    @Test
    public void forValue() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("aString");
        assertThat(valueRepresentation.isArray(), is(false));
        assertThat(valueRepresentation.isMap(), is(false));
        assertThat(valueRepresentation.isValue(), is(true));
    }

    @Test
    public void forList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.isArray(), is(true));
        assertThat(jsonRepresentation.isMap(), is(false));
        assertThat(jsonRepresentation.isValue(), is(false));
    }
    
}
