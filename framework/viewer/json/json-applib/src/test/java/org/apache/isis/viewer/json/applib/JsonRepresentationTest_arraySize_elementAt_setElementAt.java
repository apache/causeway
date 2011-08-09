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
        objectRepr = JsonRepresentation.newObject();
    }

    @Test
    public void arraySize_forEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        assertThat(jsonRepresentation.arraySize(), is(0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void elementAt_outOfBounds() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        jsonRepresentation.elementAt(0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setElementAt_outOfBounds() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        jsonRepresentation.setElementAt(0, objectRepr);
    }

    @Test
    public void arraySize_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.arraySize(), is(2));
    }

    @Test
    public void elementAt_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.elementAt(0), is(not(nullValue())));
    }

    @Test
    public void setElementAt_happyCaseWhenSetElementToObject() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        jsonRepresentation.setElementAt(0, objectRepr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setElementAt_forAttemptingToSetElementToArray() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        jsonRepresentation.setElementAt(0, arrayRepr);
    }
    
    @Test(expected=IllegalStateException.class)
    public void arraySize_forMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.arraySize();
    }

    @Test(expected=IllegalStateException.class)
    public void elementAt_forMap() throws JsonParseException, JsonMappingException, IOException {
        jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.elementAt(0);
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
        valueRepresentation.elementAt(0);
    }

}
