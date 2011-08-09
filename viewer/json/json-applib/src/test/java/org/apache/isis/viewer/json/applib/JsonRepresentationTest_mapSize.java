package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class JsonRepresentationTest_mapSize {

    private JsonRepresentation jsonRepresentation;

    @Test
    public void mapSize_forEmptyMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        assertThat(jsonRepresentation.mapSize(), is(0));
    }

    @Test
    public void mapSize_forNonEmptyMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        assertThat(jsonRepresentation.mapSize(), is(12));
    }

    @Test(expected=IllegalStateException.class)
    public void mapSize_forList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        jsonRepresentation.mapSize();
    }

    @Test(expected=IllegalStateException.class)
    public void mapSize_forValue() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        JsonRepresentation valueRepresentation = jsonRepresentation.getRepresentation("anInt");
        valueRepresentation.mapSize();
    }

}
