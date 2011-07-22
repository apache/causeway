package org.apache.isis.viewer.json.applib.util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;


public class JsonMapper {
    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonMapper() {
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readAsMap(String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, LinkedHashMap.class);
    }

    public List<?> readAsList(String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, ArrayList.class);
    }

    public <T> T read(String json, Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        return (T) objectMapper.readValue(json, requiredType);        
    }

    public String write(Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.writeValueAsString(object);
    }

    public <T> T read(Response response, Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        
        int status = response.getStatus();
        if(status >= 200 && status < 300) {
            throw new IllegalArgumentException("response status must be in 2xx range (was " + status + ")");
        }
        Object entityObj = response.getEntity();
        if(entityObj == null) {
            return null;
        }
        if(!(entityObj instanceof String)) {
            throw new IllegalArgumentException("response entity must be a String (was " + entityObj.getClass().getName() + ")");
        }
        String entity = (String) entityObj;

        return read(entity, requiredType);
    }

}