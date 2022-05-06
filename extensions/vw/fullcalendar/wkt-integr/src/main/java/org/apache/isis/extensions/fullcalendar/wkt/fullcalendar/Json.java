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

package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.openjson.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;

class Json {
	private Json() {

	}

	private static class MyJsonFactory extends MappingJsonFactory {

		@Override
		public JsonGenerator createGenerator(Writer w) throws IOException {
			return super.createGenerator(w).useDefaultPrettyPrinter();
		}

		@Override
		public JsonGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
			return super.createGenerator(f, enc).useDefaultPrettyPrinter();
		}

		@Override
		public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
			return super.createGenerator(out, enc).useDefaultPrettyPrinter();
		}

	}

	public static String toJson(Object object) {

		ObjectMapper mapper = new ObjectMapper(new MyJsonFactory());
		SimpleModule module = new SimpleModule("fullcalendar");
		module.addSerializer(new DateTimeSerializer());
		module.addSerializer(new LocalTimeSerializer());
		mapper.registerModule(module);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		String json = null;
		try {
			json = mapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException("Error encoding object: " + object + " into JSON string", e);
		}
		return json;
	}

	public static class DateTimeSerializer extends JsonSerializer<DateTime> {
		@Override
		public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeString(ISODateTimeFormat.dateTime().print(value));
		}

		@Override
		public Class<DateTime> handledType() {
			return DateTime.class;
		}

	}

	public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
		@Override
		public void serialize(LocalTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
			jgen.writeString(value.toString("h:mmaa"));
		}

		@Override
		public Class<LocalTime> handledType() {
			return LocalTime.class;
		}

	}

	public static class Script implements Serializable {
		private String code;

		public Script(String value) {
			this.code = value;
		}

		public String getDeclaration() {
			return code;
		}

	}

}
