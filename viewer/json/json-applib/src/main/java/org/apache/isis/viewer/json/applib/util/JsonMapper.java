package org.apache.isis.viewer.json.applib.util;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.codehaus.jackson.map.deser.JsonNodeDeserializer;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.JavaType;
import org.jboss.resteasy.client.ClientResponse;


public final class JsonMapper {

    /**
     * Provides polymorphic deserialization to any subtype of {@link JsonRepresentation}.
     */
    @SuppressWarnings("deprecation")
    private static final class JsonRepresentationDeserializerFactory extends BeanDeserializerFactory {
        @Override
        public JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig config, DeserializerProvider p, JavaType type, BeanProperty property) throws JsonMappingException {
            Class<?> rawClass = type.getRawClass();
            if (JsonRepresentation.class.isAssignableFrom(rawClass)) {
                try {
                    // ensure has a constructor taking a JsonNode
                    Constructor<?> rawClassConstructor = rawClass.getConstructor(JsonNode.class);
                    return new JsonRepresentationDeserializer(rawClassConstructor);
                } catch (SecurityException e) {
                    // fall through
                } catch (NoSuchMethodException e) {
                    // fall through
                }
            } 
            return super.createBeanDeserializer(config, p, type, property);
        }

        private static final class JsonRepresentationDeserializer extends JsonDeserializer<Object> {
            private JsonDeserializer<? extends JsonNode> jsonNodeDeser = 
                JsonNodeDeserializer.getDeserializer(JsonNode.class);
            
            private final Constructor<?> rawClassConstructor;
            public JsonRepresentationDeserializer(Constructor<?> rawClassConstructor) {
                this.rawClassConstructor = rawClassConstructor;
            }

            @Override
            public JsonRepresentation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                JsonNode jsonNode = jsonNodeDeser.deserialize(jp, ctxt);
                try {
                    return (JsonRepresentation) rawClassConstructor.newInstance(jsonNode);
                } catch (Exception e) {
                    throw new IllegalStateException(e); 
                }
            }
        }
    }


    private static class JsonRepresentationSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            JsonRepresentation jsonRepresentation = (JsonRepresentation) value;
            JsonNode jsonNode = jsonRepresentation.asJsonNode();
            jgen.writeTree(jsonNode);
        }
    }

    private static ObjectMapper createObjectMapper() {
        // it's a shame that the serialization and deserialization mechanism used aren't symmetric... but it works.
        DeserializerProvider deserializerProvider = new StdDeserializerProvider(new JsonRepresentationDeserializerFactory());
        ObjectMapper objectMapper = new ObjectMapper(null, null, deserializerProvider);
        SimpleModule jsonModule = new SimpleModule("json", new Version(1,0,0,null));
        jsonModule.addSerializer(JsonRepresentation.class, new JsonRepresentationSerializer());
        objectMapper.registerModule(jsonModule);
        
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }


    private static JsonMapper instance = new JsonMapper();

    // threadsafe
    public final static JsonMapper instance() {
        return instance;
    }
    
    private final ObjectMapper objectMapper;

    private JsonMapper() {
        objectMapper = createObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readAsMap(String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, LinkedHashMap.class);
    }

    public List<?> readAsList(String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, ArrayList.class);
    }

    public JsonRepresentation read(String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, JsonRepresentation.class);
    }

    public <T> T read(String json, Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        return (T) objectMapper.readValue(json, requiredType);        
    }

    public <T> T read(Response response, Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        final ClientResponse<?> clientResponse = (ClientResponse<?>)response; // a shame, but needed if calling resources directly
        Object entityObj = clientResponse.getEntity(String.class);
        if(entityObj == null) {
            return null;
        }
        if(!(entityObj instanceof String)) {
            throw new IllegalArgumentException("response entity must be a String (was " + entityObj.getClass().getName() + ")");
        }
        String entity = (String) entityObj;

        return read(entity, requiredType);
    }

    public String write(Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.writeValueAsString(object);
    }

}