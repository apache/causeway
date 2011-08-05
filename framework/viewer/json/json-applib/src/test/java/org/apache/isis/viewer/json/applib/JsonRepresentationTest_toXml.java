package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class JsonRepresentationTest_toXml {


    @Test
    public void xpath_forLink() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation jsonRepresentation = 
                new JsonRepresentation(readJson("map.json"));
        
        String xml = jsonRepresentation.toXml();
        
        assertThat(xml, is(not(nullValue())));
    }
    
}
