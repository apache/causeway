package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_getLink {

    @Before
    public void setUp() throws Exception {
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
