/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.restfulobjects.applib.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

public final class JsonMapper {

    public static String getEntityAsStringFrom(Response response) {

        final Object result = response.getEntity();

        if(result == null)
            return null;

        if(result instanceof String) {
            return (String) result;
        }

        // TODO [andi-huber] just a wild guess
        return response.readEntity(String.class);

        // legacy code ...
        // final ClientResponse<?> clientResponse = (ClientResponse<?>) response;
        // return clientResponse.getEntity(String.class);
    }

    public enum PrettyPrinting {
        ENABLE,
        DISABLE
    }

    private static final class JsonRepresentationDeserializer extends JsonDeserializer<JsonRepresentation> {
        @Override
        public JsonRepresentation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            return new JsonRepresentation(node);
        }
    }

    private static final class JsonRepresentationSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
            final JsonRepresentation jsonRepresentation = (JsonRepresentation) value;
            final JsonNode jsonNode = jsonRepresentation.asJsonNode();
            jgen.writeTree(jsonNode);
        }
    }

    private static ObjectMapper createObjectMapper(PrettyPrinting prettyPrinting) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule jsonModule = new SimpleModule("json", new Version(1, 0, 0, null, "org.apache", "isis"));
        jsonModule.addDeserializer(JsonRepresentation.class, new JsonRepresentationDeserializer());
        jsonModule.addSerializer(JsonRepresentation.class, new JsonRepresentationSerializer());
        objectMapper.registerModule(jsonModule);

        if (prettyPrinting == PrettyPrinting.ENABLE) {
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static Map<PrettyPrinting, JsonMapper> instanceByConfig = new ConcurrentHashMap();

    /**
     * Returns a {@link org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper.PrettyPrinting#ENABLE pretty-printing enabled} JSON mapper.
     */
    public static final JsonMapper instance() {
        return instance(PrettyPrinting.ENABLE);
    }

    public static final JsonMapper instance(final PrettyPrinting prettyPrinting) {
        final JsonMapper jsonMapper = instanceByConfig.get(prettyPrinting);
        if (jsonMapper != null) {
            return jsonMapper;
        }
        // there could be a race-condition here, but it doesn't matter; last one wins.
        final JsonMapper mapper = new JsonMapper(prettyPrinting);
        instanceByConfig.put(prettyPrinting, mapper);

        return mapper;
    }

    private final ObjectMapper objectMapper;

    private JsonMapper(PrettyPrinting prettyPrinting) {
        objectMapper = createObjectMapper(prettyPrinting);
    }

    public JsonRepresentation read(final String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, JsonRepresentation.class);
    }

    public <T> T read(final String json, final Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, requiredType);
    }

    public <T> T read(final Response response, final Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        final String entity = getEntityAsStringFrom(response);
        if (entity == null) {
            return null;
        }
        return read(entity, requiredType);
    }

    public String write(final Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.writeValueAsString(object);
    }

}
