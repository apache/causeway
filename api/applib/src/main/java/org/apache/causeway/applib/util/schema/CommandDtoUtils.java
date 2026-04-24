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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.JsonUtils.JacksonCustomizer;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.PeriodDto;
import org.apache.causeway.schema.common.v2.ValueDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class CommandDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<CommandDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(CommandDto.class));

    public DtoMapper<CommandDto> dtoMapper() {
        return dtoMapper.get();
    }

    public OidsDto targetsFor(final CommandDto dto) {
        OidsDto targets = dto.getTargets();
        if(targets == null) {
            targets = new OidsDto();
            dto.setTargets(targets);
        }
        return targets;
    }

    public ParamsDto parametersFor(final ActionDto actionDto) {
        ParamsDto parameters = actionDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            actionDto.setParameters(parameters);
        }
        return parameters;
    }

    public PeriodDto timingsFor(final CommandDto commandDto) {
        PeriodDto timings = commandDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            commandDto.setTimings(timings);
        }
        return timings;
    }

    public String getUserData(final CommandDto dto, final String key) {
        if(dto == null || key == null) {
            return null;
        }
        return CommonDtoUtils.getMapValue(dto.getUserData(), key);
    }

    public void setUserData(
            final CommandDto dto, final String key, final String value) {
        if(dto == null || key == null || _Strings.isNullOrEmpty(value)) {
            return;
        }
        final MapDto userData = userDataFor(dto);
        CommonDtoUtils.putMapKeyValue(userData, key, value);
    }

    public void setUserData(
            final CommandDto dto, final String key, final Bookmark bookmark) {
        if(dto == null || key == null || bookmark == null) {
            return;
        }
        setUserData(dto, key, bookmark.toString());
    }

    public void clearUserData(
            final CommandDto dto, final String key) {
        if(dto == null || key == null) {
            return;
        }
        userDataFor(dto).getEntry().removeIf(x -> x.getKey().equals(key));
    }

    private MapDto userDataFor(final CommandDto commandDto) {
        MapDto userData = commandDto.getUserData();
        if(userData == null) {
            userData = new MapDto();
            commandDto.setUserData(userData);
        }
        return userData;
    }

    // -- YAML SUPPORT

    public String toYaml(final Iterable<CommandDto> commandDtos) {
    	final JsonUtils.JacksonCustomizer customizer = new JacksonCustomizer() {
			@Override
			public ObjectMapper apply(ObjectMapper mapper) {
				JsonUtils.jaxbAnnotationSupport(mapper);
				CommandDtoUtils.memberDtoSupport(mapper);
				CommandDtoUtils.valueDtoSupport(mapper);
				JsonUtils.onlyIncludeNonNull(mapper);
				return mapper;
			}
		};
        return YamlUtils.toStringUtf8(
            _NullSafe.stream(commandDtos)
                .collect(Collectors.toList()),
            customizer);
    }

    public List<CommandDto> fromYaml(final DataSource commandDtosYaml) {
	    final JsonUtils.JacksonCustomizer customizer = yamlCommandDtoCustomizer();

        final Try<List<CommandDto>> asList = YamlUtils.tryReadAsList(CommandDto.class, commandDtosYaml, customizer);
        if (asList.isSuccess()) {
            return asList.getValue().orElseGet(Collections::emptyList);
        }

        final Try<List<CommandDto>> asMultiDocument = tryReadAsMultiDocument(CommandDto.class, commandDtosYaml, customizer);
        asMultiDocument.getFailure().ifPresent(multiDocFailure ->
            asList.getFailure().ifPresent(multiDocFailure::addSuppressed));

        return asMultiDocument
                .ifFailureFail()
                .getValue()
                .orElseGet(Collections::emptyList);
    }

    private JsonUtils.JacksonCustomizer yamlCommandDtoCustomizer() {
        return mapper -> {
            JsonUtils.jaxbAnnotationSupport(mapper);
            CommandDtoUtils.memberDtoSupport(mapper);
            CommandDtoUtils.valueDtoSupport(mapper);
            return mapper;
        };
    }

    private <T> Try<List<T>> tryReadAsMultiDocument(
            final Class<T> elementType,
            final DataSource source,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll(is -> Try.call(() -> {
            final ObjectMapper mapper = createYamlReader(customizers);
            final var documentReader = mapper.readerFor(elementType).readValues(is);
            final List<T> elements = new ArrayList<>();
            while (documentReader.hasNextValue()) {
                final T next = documentReader.nextValue();
                if (next != null) {
                    elements.add(next);
                }
            }
            return elements;
        }));
    }

    private ObjectMapper createYamlReader(final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper = JsonUtils.jdk8Support(mapper);
        mapper = JsonUtils.readingJavaTimeSupport(mapper);
        mapper = JsonUtils.readingCanSupport(mapper);
        for (JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

    // Mix-in to add type metadata to MemberDto
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    private abstract class AbstractDtoMixIn {}

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

    private void valueDtoSupport(final ObjectMapper mb) {
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
            extends JsonSerializer<XMLGregorianCalendar> {

        @Override
        public void serialize(
                final XMLGregorianCalendar value,
                final JsonGenerator gen,
                final SerializerProvider serializers) throws IOException {
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
            extends JsonDeserializer<XMLGregorianCalendar> {

        @Override
        public XMLGregorianCalendar deserialize(
                final JsonParser p,
                final DeserializationContext ctxt) throws IOException {
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
            extends JsonSerializer<XMLGregorianCalendar> {

        @Override
        public void serialize(
                final XMLGregorianCalendar value,
                final JsonGenerator gen,
                final SerializerProvider serializers) throws IOException {
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
            extends JsonDeserializer<XMLGregorianCalendar> {

        @Override
        public XMLGregorianCalendar deserialize(
                final JsonParser p,
                final DeserializationContext ctxt) throws IOException {
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
            extends JsonSerializer<XMLGregorianCalendar> {

        @Override
        public void serialize(
                final XMLGregorianCalendar value,
                final JsonGenerator gen,
                final SerializerProvider serializers) throws IOException {
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
            extends JsonDeserializer<XMLGregorianCalendar> {

        @Override
        public XMLGregorianCalendar deserialize(
                final JsonParser p,
                final DeserializationContext ctxt) throws IOException {
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

    private void memberDtoSupport(final ObjectMapper mb) {
        // add mix-in so MemberDto carries @JsonTypeInfo without modifying source
        mb.addMixIn(MemberDto.class, AbstractDtoMixIn.class);
        // register concrete sub-types with logical names
        mb.registerSubtypes(new NamedType(ActionDto.class, "ACT"));
        mb.registerSubtypes(new NamedType(PropertyDto.class, "PROP"));
    }

}
