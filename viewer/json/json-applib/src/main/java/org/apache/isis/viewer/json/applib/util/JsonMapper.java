package org.apache.isis.viewer.json.applib.util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.viewer.json.applib.homepage.HomePage;
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

    public <T> T read(String json, Class<T> expectedType) throws JsonParseException, JsonMappingException, IOException {
        return (T) objectMapper.readValue(json, expectedType);        
    }

    public String write(Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.writeValueAsString(object);
    }
    

    /**
     * @deprecated
     * @return
     */
    @Deprecated
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}