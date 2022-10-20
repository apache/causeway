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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Json {

    public String toJson(final Object object) {
        ObjectMapper mapper = new ObjectMapper(new MappingJsonFactory());

        val jodaModule = new SimpleModule("jodaTime");
        jodaModule.addSerializer(new DateTimeSerializer());
        jodaModule.addSerializer(new LocalTimeSerializer());
        mapper.registerModule(jodaModule);

        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding object: " + object + " into JSON string", e);
        }

    }

    @Deprecated
    public static class DateTimeSerializer extends JsonSerializer<DateTime> {
        @Override
        public void serialize(final DateTime value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeString(ISODateTimeFormat.dateTime().print(value));
        }

        @Override
        public Class<DateTime> handledType() {
            return DateTime.class;
        }

    }

    @Deprecated
    public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(final LocalTime value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException,
            JsonProcessingException {
            jgen.writeString(value.toString("h:mmaa"));
        }

        @Override
        public Class<LocalTime> handledType() {
            return LocalTime.class;
        }

    }

}
