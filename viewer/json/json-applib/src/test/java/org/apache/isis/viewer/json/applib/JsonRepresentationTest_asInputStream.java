package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;

public class JsonRepresentationTest_asInputStream {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }
    
    @Test
    public void asInputStream() throws JsonParseException, JsonMappingException, IOException {
        InputStream array = jsonRepresentation.asInputStream();

        ByteArrayOutputStream to = new ByteArrayOutputStream();
        com.google.common.io.ByteStreams.copy(array, to);
        
        String jsonStr = to.toString(Charsets.UTF_8.name());
        
        assertThat(jsonStr, is(not(nullValue())));
    }

    
}
