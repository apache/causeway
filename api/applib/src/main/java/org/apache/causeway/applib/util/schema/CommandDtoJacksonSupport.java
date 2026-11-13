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
package org.apache.causeway.applib.util.schema;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.JsonUtils.JacksonCustomizer;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.ValueDto;

import lombok.experimental.UtilityClass;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.jsontype.NamedType;

@UtilityClass
class CommandDtoJacksonSupport {

    JsonUtils.JacksonCustomizer yamlWriteCustomizer() {
    	return ((JacksonCustomizer) JsonUtils::jaxbAnnotationSupport)
            .andThen((JacksonCustomizer) CommandDtoJacksonSupport::memberDtoSupport)
            .andThen((JacksonCustomizer) CommandDtoJacksonSupport::valueDtoSupport)
            .andThen((JacksonCustomizer) JsonUtils::onlyIncludeNonNull)
            ::accept;
    }
    JsonUtils.JacksonCustomizer yamlReadCustomizer() {
    	return ((JacksonCustomizer) JsonUtils::jaxbAnnotationSupport)
            .andThen((JacksonCustomizer) CommandDtoJacksonSupport::memberDtoSupport)
            .andThen((JacksonCustomizer) CommandDtoJacksonSupport::valueDtoSupport)
            ::accept;
    }
    
    // -- HELPER

    // Mix-in to add type metadata to MemberDto
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    private abstract class AbstractDtoMixIn {}

    private void memberDtoSupport(final MapperBuilder<?, ?> mb) {
        // add mix-in so MemberDto carries @JsonTypeInfo without modifying source
        mb.addMixIn(MemberDto.class, AbstractDtoMixIn.class);
        // register concrete sub-types with logical names
        mb.registerSubtypes(new NamedType(ActionDto.class, "ACT"));
        mb.registerSubtypes(new NamedType(PropertyDto.class, "PROP"));
    }
	
    // Mix-in to ignore unknown properties for ValueDto
    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract class AbstractValueDtoMixIn {
        @JsonSerialize(using = LocalDateXmlGregorianCalendarSerializer.class)
        abstract XMLGregorianCalendar getLocalDate();

        @JsonDeserialize(using = LocalDateXmlGregorianCalendarDeserializer.class)
        abstract void setLocalDate(XMLGregorianCalendar localDate);

        @JsonSerialize(using = LocalDateTimeXmlGregorianCalendarSerializer.class)
        abstract XMLGregorianCalendar getLocalDateTime();

        @JsonDeserialize(using = LocalDateTimeXmlGregorianCalendarDeserializer.class)
        abstract void setLocalDateTime(XMLGregorianCalendar localDateTime);

        @JsonSerialize(using = LocalTimeXmlGregorianCalendarSerializer.class)
        abstract XMLGregorianCalendar getLocalTime();

        @JsonDeserialize(using = LocalTimeXmlGregorianCalendarDeserializer.class)
        abstract void setLocalTime(XMLGregorianCalendar localTime);
    }

    private void valueDtoSupport(final MapperBuilder<?, ?> mb) {
        mb.addMixIn(ValueDto.class, AbstractValueDtoMixIn.class);
    }

    private static final DatatypeFactory DATATYPE_FACTORY = datatypeFactory();

    private static DatatypeFactory datatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize DatatypeFactory", ex);
        }
    }

    private static final class LocalDateXmlGregorianCalendarSerializer
            extends ValueSerializer<XMLGregorianCalendar> {
		@Override
		public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializationContext ctxt)
				throws JacksonException {
			if (value == null) {
                gen.writeNull();
                return;
            }
            final XMLGregorianCalendar dateOnly = DATATYPE_FACTORY.newXMLGregorianCalendarDate(
                    value.getYear(),
                    value.getMonth(),
                    value.getDay(),
                    DatatypeConstants.FIELD_UNDEFINED);
            gen.writeString(dateOnly.toXMLFormat());
		}
    }

    private static final class LocalDateXmlGregorianCalendarDeserializer
            extends ValueDeserializer<XMLGregorianCalendar> {
		@Override
		public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			final String text = p.getValueAsString();
            if (_Strings.isNullOrEmpty(text)) {
                return null;
            }
            final XMLGregorianCalendar parsed = DATATYPE_FACTORY.newXMLGregorianCalendar(text);
            return DATATYPE_FACTORY.newXMLGregorianCalendarDate(
                    parsed.getYear(),
                    parsed.getMonth(),
                    parsed.getDay(),
                    DatatypeConstants.FIELD_UNDEFINED);
		}
    }

    private static final class LocalDateTimeXmlGregorianCalendarSerializer
            extends ValueSerializer<XMLGregorianCalendar> {

		@Override
		public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializationContext ctxt)
				throws JacksonException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			final XMLGregorianCalendar localDateTime = DATATYPE_FACTORY.newXMLGregorianCalendar(
					value.getYear(),
					value.getMonth(),
					value.getDay(),
					value.getHour(),
					value.getMinute(),
					value.getSecond(),
					millisecondsOf(value),
					DatatypeConstants.FIELD_UNDEFINED);
			gen.writeString(localDateTime.toXMLFormat());
		}
    }

    private static final class LocalDateTimeXmlGregorianCalendarDeserializer
            extends ValueDeserializer<XMLGregorianCalendar> {

		@Override
		public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			final String text = p.getValueAsString();
			if (_Strings.isNullOrEmpty(text)) {
				return null;
			}
			final XMLGregorianCalendar parsed = DATATYPE_FACTORY.newXMLGregorianCalendar(text);
			return DATATYPE_FACTORY.newXMLGregorianCalendar(
					parsed.getYear(),
					parsed.getMonth(),
					parsed.getDay(),
					parsed.getHour(),
					parsed.getMinute(),
					parsed.getSecond(),
					millisecondsOf(parsed),
					DatatypeConstants.FIELD_UNDEFINED);
		}
    }

    private static final class LocalTimeXmlGregorianCalendarSerializer
            extends ValueSerializer<XMLGregorianCalendar> {

		@Override
		public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializationContext ctxt)
				throws JacksonException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			final XMLGregorianCalendar localTime = DATATYPE_FACTORY.newXMLGregorianCalendarTime(
					value.getHour(),
					value.getMinute(),
					value.getSecond(),
					millisecondsOf(value),
					DatatypeConstants.FIELD_UNDEFINED);
			gen.writeString(localTime.toXMLFormat());
		}
    }

    private static final class LocalTimeXmlGregorianCalendarDeserializer
            extends ValueDeserializer<XMLGregorianCalendar> {
		@Override
		public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			final String text = p.getValueAsString();
			if (_Strings.isNullOrEmpty(text)) {
				return null;
			}
			final XMLGregorianCalendar parsed = DATATYPE_FACTORY.newXMLGregorianCalendar(text);
			return DATATYPE_FACTORY.newXMLGregorianCalendarTime(
					parsed.getHour(),
					parsed.getMinute(),
					parsed.getSecond(),
					millisecondsOf(parsed),
					DatatypeConstants.FIELD_UNDEFINED);
		}
    }

    private static int millisecondsOf(final XMLGregorianCalendar value) {
        final int millis = value.getMillisecond();
        return millis == DatatypeConstants.FIELD_UNDEFINED
                ? DatatypeConstants.FIELD_UNDEFINED
                : millis;
    }
}
