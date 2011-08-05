package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_isArray {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void isArray_forList() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.isArray(), is(true));
    }

    @Test
    public void isArray_forMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        assertThat(jsonRepresentation.isArray(), is(false));
    }

}
