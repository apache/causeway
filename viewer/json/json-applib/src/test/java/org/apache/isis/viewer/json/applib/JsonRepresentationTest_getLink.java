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
import org.junit.Test;

public class JsonRepresentationTest_getLink {

    private Link link;
    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        link = new Link();
        link.setHref("http://foo/bar");
        link.setMethod(Method.GET);
        
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }


    @Test
    public void forLink_whenSimpleKey() throws JsonParseException, JsonMappingException, IOException {
        link.setRel("someRel");
        assertThat(jsonRepresentation.getLink("aLink"), is(link));
    }

    @Test
    public void forLink_whenMultipartKey() throws JsonParseException, JsonMappingException, IOException {
        link.setRel("someSubRel");
        assertThat(jsonRepresentation.getLink("aSubMap.aLink"), is(link));
    }
    

    @Test
    public void forNonExistent() throws JsonParseException, JsonMappingException, IOException {
        assertThat(jsonRepresentation.getLink("doesNotExist"), is(nullValue()));
    }

    @Test
    public void forValue() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getLink("anInt");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'anInt' (a value) does not represent a link"));
        }
    }

    @Test
    public void forMap() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getLink("aSubMap");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' (a map) does not fully represent a link"));
        }
    }

    @Test
    public void forList() throws JsonParseException, JsonMappingException, IOException {
        try {
            jsonRepresentation.getLink("aSubList");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubList' (an array) does not represent a link"));
        }
    }

}
