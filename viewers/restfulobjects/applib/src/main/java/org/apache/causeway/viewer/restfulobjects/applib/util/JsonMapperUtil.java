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

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * @since 4.x {@index}
 */
public record JsonMapperUtil(
    JsonMapper jsonMapper,
    PrettyPrinting prettyPrinting) {

    public enum PrettyPrinting {
        ENABLE,
        DISABLE
    }

    /**
     * Returns a {@link org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapperUtil.PrettyPrinting#ENABLE pretty-printing enabled} JSON mapper.
     */
    public static final JsonMapperUtil instance() {
        return instance(PrettyPrinting.ENABLE);
    }

    public static final JsonMapperUtil instance(final PrettyPrinting prettyPrinting) {
        return instanceByConfig.computeIfAbsent(prettyPrinting, JsonMapperUtil::new);
    }

    public JsonRepresentation read(final String json) throws IOException {
        return read(json, JsonRepresentation.class);
    }

    public <T> T read(final String json, final Class<T> requiredType) throws IOException {
        return jsonMapper.readValue(json, requiredType);
    }

    public String write(final Object object) throws IOException {
        return jsonMapper.writeValueAsString(object);
    }

    // -- HELPER

    // non canonical constructor
    private JsonMapperUtil(final PrettyPrinting prettyPrinting) {
        this(createObjectMapper(prettyPrinting), prettyPrinting);
    }

    private static final class JsonRepresentationDeserializer extends ValueDeserializer<JsonRepresentation> {
        @Override
        public JsonRepresentation deserialize(final JsonParser jp, final DeserializationContext ctxt) {
            return new JsonRepresentation(jp.objectReadContext().readTree(jp));
        }
    }

    private static final class JsonRepresentationSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(final Object value, final JsonGenerator jgen, final SerializationContext ctxt) {
            jgen.writeTree(((JsonRepresentation) value).asJsonNode());
        }
    }

    private static JsonMapper createObjectMapper(final PrettyPrinting prettyPrinting) {
		var builder = JsonMapper.builder();
        
        final SimpleModule jsonModule = new SimpleModule("json", new Version(1, 0, 0, null, "org.apache", "causeway"));
        jsonModule.addDeserializer(JsonRepresentation.class, new JsonRepresentationDeserializer());
        jsonModule.addSerializer(JsonRepresentation.class, new JsonRepresentationSerializer());
        builder.addModule(jsonModule);

        if (prettyPrinting == PrettyPrinting.ENABLE) {
        	builder.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return builder.build();
    }

    private static final Map<PrettyPrinting, JsonMapperUtil> instanceByConfig = new ConcurrentHashMap<>();

}
