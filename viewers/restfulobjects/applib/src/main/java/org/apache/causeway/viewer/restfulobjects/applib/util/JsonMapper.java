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
package org.apache.causeway.viewer.restfulobjects.applib.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

/**
 * @since 1.x {@index}
 */
public record JsonMapper(
    ObjectMapper objectMapper,
    PrettyPrinting prettyPrinting) {

    public enum PrettyPrinting {
        ENABLE,
        DISABLE
    }

    /**
     * Returns a {@link org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapper.PrettyPrinting#ENABLE pretty-printing enabled} JSON mapper.
     */
    public static final JsonMapper instance() {
        return instance(PrettyPrinting.ENABLE);
    }

    public static final JsonMapper instance(final PrettyPrinting prettyPrinting) {
        return instanceByConfig.computeIfAbsent(prettyPrinting, JsonMapper::new);
    }

    public JsonRepresentation read(final String json) throws JsonParseException, JsonMappingException, IOException {
        return read(json, JsonRepresentation.class);
    }

    public <T> T read(final String json, final Class<T> requiredType) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, requiredType);
    }

    public String write(final Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.writeValueAsString(object);
    }

    // -- HELPER

    // non canonical constructor
    private JsonMapper(final PrettyPrinting prettyPrinting) {
        this(createObjectMapper(prettyPrinting), prettyPrinting);
    }

    private static final class JsonRepresentationDeserializer extends JsonDeserializer<JsonRepresentation> {
        @Override
        public JsonRepresentation deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            return new JsonRepresentation(jp.getCodec().readTree(jp));
        }
    }

    private static final class JsonRepresentationSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException {
            jgen.writeTree(((JsonRepresentation) value).asJsonNode());
        }
    }

    private static ObjectMapper createObjectMapper(final PrettyPrinting prettyPrinting) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule jsonModule = new SimpleModule("json", new Version(1, 0, 0, null, "org.apache", "causeway"));
        jsonModule.addDeserializer(JsonRepresentation.class, new JsonRepresentationDeserializer());
        jsonModule.addSerializer(JsonRepresentation.class, new JsonRepresentationSerializer());
        objectMapper.registerModule(jsonModule);

        if (prettyPrinting == PrettyPrinting.ENABLE) {
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static Map<PrettyPrinting, JsonMapper> instanceByConfig = new ConcurrentHashMap<>();

}
