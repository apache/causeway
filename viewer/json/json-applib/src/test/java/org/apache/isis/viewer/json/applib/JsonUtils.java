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

public class JsonUtils {
    
    private JsonUtils(){}

    public static JsonNode readJson(String resourceName) throws JsonParseException, JsonMappingException, IOException {
        return JsonMapper.instance().read(Resources.toString(Resources.getResource(JsonUtils.class, resourceName), Charsets.UTF_8), JsonNode.class);
    }

}
