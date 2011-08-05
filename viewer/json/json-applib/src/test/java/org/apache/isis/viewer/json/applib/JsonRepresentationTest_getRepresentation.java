package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class JsonRepresentationTest_getRepresentation {

    @Test
    public void getRepresentation_forMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        JsonRepresentation subRepresentation = jsonRepresentation.getRepresentation("aLink");
        assertThat(subRepresentation.getString("rel"), is("someRel"));
    }
    
}
