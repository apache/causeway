package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_size {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void size_forEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        assertThat(jsonRepresentation.size(), is(0));
    }

    @Test
    public void size_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.size(), is(2));
    }

    @Test(expected=IllegalStateException.class)
    public void size_forMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.size();
    }

}
