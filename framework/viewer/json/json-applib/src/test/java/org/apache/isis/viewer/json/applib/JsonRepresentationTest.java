package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonRepresentationTest {

    private static JsonNode readJson(String resourceName) throws JsonParseException, JsonMappingException, IOException {
        return JsonMapper.instance().read(Resources.toString(Resources.getResource(JsonRepresentationTest.class, resourceName), Charsets.UTF_8), JsonNode.class);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void size_forEmptyList() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("emptyList.json"));
        assertThat(jsonRepresentation.size(), is(0));
    }

    @Test
    public void size_forNonEmptyList() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("list.json"));
        assertThat(jsonRepresentation.size(), is(2));
    }

    @Test(expected=IllegalStateException.class)
    public void size_forMap() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("emptyMap.json"));
        jsonRepresentation.size();
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

    @Test
    public void getLink_forLink() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = new JsonRepresentation(readJson("map.json"));
        Link link = new Link();
        link.setHref("http://foo/bar");
        link.setMethod(Method.GET);
        link.setRel("someRel");
        assertThat(jsonRepresentation.getLink("aLink"), is(link));
    }

    
    
}
