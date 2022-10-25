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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.resources._Json;
import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.BlobDto;
import org.apache.causeway.schema.common.v2.ClobDto;
import org.apache.causeway.schema.common.v2.EnumDto;
import org.apache.causeway.schema.common.v2.NamedValueWithTypeDto;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class CommonDtoUtils {

    // -- VALUE FROM/TO JSON

    public static ValueWithTypeDto getFundamentalValueFromJson(
            final @NonNull ValueType valueType,
            final @Nullable String json) {
        val valueDto = new ValueWithTypeDto();
        valueDto.setType(valueType);

        if(_Strings.isNullOrEmpty(json)) {
            return valueDto;
        }

        switch(valueType) {
        case REFERENCE:
        case COMPOSITE:
        case COLLECTION:
            throw _Exceptions.unsupportedOperation("valueType %s is not fundamental", valueType);

        case STRING: {
            valueDto.setString(json);
            return valueDto;
        }
        case BYTE: {
            valueDto.setByte(Byte.valueOf(json));
            return valueDto;
        }
        case SHORT: {
            valueDto.setShort(Short.valueOf(json));
            return valueDto;
        }
        case INT: {
            valueDto.setInt(Integer.valueOf(json));
            return valueDto;
        }
        case LONG: {
            valueDto.setLong(Long.valueOf(json));
            return valueDto;
        }
        case CHAR: {
            valueDto.setChar(json);
            return valueDto;
        }
        case BOOLEAN: {
            valueDto.setBoolean(Boolean.valueOf(json));
            return valueDto;
        }
        case FLOAT: {
            valueDto.setFloat(Float.valueOf(json));
            return valueDto;
        }
        case DOUBLE: {
            valueDto.setDouble(Double.valueOf(json));
            return valueDto;
        }
        case BIG_INTEGER: {
            valueDto.setBigInteger(new BigInteger(json));
            return valueDto;
        }
        case BIG_DECIMAL: {
            valueDto.setBigDecimal(new BigDecimal(json));
            return valueDto;
        }
        case LOCAL_DATE: {
            final LocalDate argValue = LocalDate.parse(json);
            valueDto.setLocalDate(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_TIME: {
            final LocalTime argValue = LocalTime.parse(json);
            valueDto.setLocalTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_DATE_TIME: {
            final LocalDateTime argValue = LocalDateTime.parse(json);
            valueDto.setLocalDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_DATE_TIME: {
            final OffsetDateTime argValue = OffsetDateTime.parse(json);
            valueDto.setOffsetDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_TIME: {
            final OffsetTime argValue = OffsetTime.parse(json);
            valueDto.setOffsetTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ZONED_DATE_TIME: {
            final ZonedDateTime argValue = ZonedDateTime.parse(json);
            valueDto.setZonedDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ENUM: {
            final EnumDto enumDto = _Json.readJson(EnumDto.class, json).getValue().orElseThrow();
            valueDto.setEnum(enumDto);
            return valueDto;
        }
        case BLOB: {
            final BlobDto blobDto = _Json.readJson(BlobDto.class, json).getValue().orElseThrow();
            valueDto.setBlob(blobDto);
            return valueDto;
        }
        case CLOB: {
            final ClobDto clobDto = _Json.readJson(ClobDto.class, json).getValue().orElseThrow();
            valueDto.setClob(clobDto);
            return valueDto;
        }
        case VOID:
            return valueDto;
        }

        throw _Exceptions.unmatchedCase(valueType);

    }

    public String getFundamentalValueAsJson(
            final @Nullable ValueWithTypeDto valueDto) {
        if(valueDto==null) {
            return null;
        }
        return getFundamentalValueAsJson(valueDto.getType(), valueDto);
    }

    @SneakyThrows
    public String getFundamentalValueAsJson(
            final @NonNull ValueType valueType,
            final @Nullable ValueDto valueDto) {

        if(valueDto==null) {
            return null;
        }
        switch(valueType) {
        case REFERENCE:
        case COMPOSITE:
        case COLLECTION:
            throw _Exceptions.unsupportedOperation("valueType %s is not fundamental", valueType);

        case STRING:
            return valueDto.getString();
        case BYTE:
            return _NullSafe.toString(valueDto.getByte());
        case SHORT:
            return _NullSafe.toString(valueDto.getShort());
        case INT:
            return _NullSafe.toString(valueDto.getInt());
        case LONG:
            return _NullSafe.toString(valueDto.getLong());
        case FLOAT:
            return _NullSafe.toString(valueDto.getFloat());
        case DOUBLE:
            return _NullSafe.toString(valueDto.getDouble());
        case BOOLEAN:
            return _NullSafe.toString(valueDto.isBoolean());
        case CHAR:
            final String aChar = valueDto.getChar();
            if(_Strings.isNullOrEmpty(aChar)) { return null; }
            return ""+aChar.charAt(0);
        case BIG_DECIMAL:
            return _NullSafe.toString(valueDto.getBigDecimal());
        case BIG_INTEGER:
            return _NullSafe.toString(valueDto.getBigInteger());
        case LOCAL_DATE:
            return _NullSafe.toString(valueDto.getLocalDate());
        case LOCAL_TIME:
            return _NullSafe.toString(valueDto.getLocalTime());
        case LOCAL_DATE_TIME:
            return _NullSafe.toString(valueDto.getLocalDateTime());
        case OFFSET_DATE_TIME:
            return _NullSafe.toString(valueDto.getOffsetDateTime());
        case OFFSET_TIME:
            return _NullSafe.toString(valueDto.getOffsetTime());
        case ZONED_DATE_TIME:
            return _NullSafe.toString(valueDto.getZonedDateTime());
        case ENUM:
            return dtoToJson(valueDto.getEnum());
        case BLOB:
            return dtoToJson(valueDto.getBlob());
        case CLOB:
            return dtoToJson(valueDto.getClob());
        case VOID:
            return null;
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }
    }

    @Nullable
    public String getCompositeValueAsJson(final @Nullable TypedTupleDto composite) {
        return composite!=null
            ? _Json.toString(
                composite,
                _Json::jaxbAnnotationSupport,
                _Json::onlyIncludeNonNull)
            : null;
    }

    @SneakyThrows
    @Nullable
    public TypedTupleDto getCompositeValueFromJson(final @Nullable String json) {
        return _Strings.isNotEmpty(json)
                ? _Json.readJson(TypedTupleDto.class, json, _Json::jaxbAnnotationSupport)
                        .getValue().orElseThrow()
                : null;
    }

    private String dtoToJson(final @Nullable Object dto) {
        return _Json.toString(dto);
    }

    // -- VALUE RECORD

    public <D extends ValueDto> D recordFundamentalValue(
            final @NonNull ValueType valueType,
            final D valueDto,
            final Object pojo) {

        if(valueDto instanceof ValueWithTypeDto) {
            ((ValueWithTypeDto)valueDto).setType(valueType);
        }

        if(pojo==null) {
            // leave the DTO empty (representing a null value or reference)
            return valueDto;
        }

        switch (valueType) {
        case COLLECTION:
        case COMPOSITE:
        case REFERENCE:
            throw _Exceptions.unsupportedOperation("valueType %s is not fundamental", valueType);

        case STRING: {
            final String argValue = (String) pojo;
            valueDto.setString(argValue);
            return valueDto;
        }
        case BYTE: {
            final Byte argValue = (Byte) pojo;
            valueDto.setByte(argValue);
            return valueDto;
        }
        case SHORT: {
            final Short argValue = (Short) pojo;
            valueDto.setShort(argValue);
            return valueDto;
        }
        case INT: {
            final Integer argValue = (Integer) pojo;
            valueDto.setInt(argValue);
            return valueDto;
        }
        case LONG: {
            final Long argValue = (Long) pojo;
            valueDto.setLong(argValue);
            return valueDto;
        }
        case CHAR: {
            final Character argValue = (Character) pojo;
            valueDto.setChar("" + argValue);
            return valueDto;
        }
        case BOOLEAN: {
            final Boolean argValue = (Boolean) pojo;
            valueDto.setBoolean(argValue);
            return valueDto;
        }
        case FLOAT: {
            final Float argValue = (Float) pojo;
            valueDto.setFloat(argValue);
            return valueDto;
        }
        case DOUBLE: {
            final Double argValue = (Double) pojo;
            valueDto.setDouble(argValue);
            return valueDto;
        }
        case BIG_INTEGER: {
            final BigInteger argValue = (BigInteger) pojo;
            valueDto.setBigInteger(argValue);
            return valueDto;
        }
        case BIG_DECIMAL: {
            final BigDecimal argValue = (BigDecimal) pojo;
            valueDto.setBigDecimal(argValue);
            return valueDto;
        }
        case LOCAL_DATE: {
            final LocalDate argValue = (LocalDate) pojo;
            valueDto.setLocalDate(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_TIME: {
            final LocalTime argValue = (LocalTime) pojo;
            valueDto.setLocalTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_DATE_TIME: {
            final LocalDateTime argValue = (LocalDateTime) pojo;
            valueDto.setLocalDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_DATE_TIME: {
            final OffsetDateTime argValue = (OffsetDateTime) pojo;
            valueDto.setOffsetDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_TIME: {
            final OffsetTime argValue = (OffsetTime) pojo;
            valueDto.setOffsetTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ZONED_DATE_TIME: {
            final ZonedDateTime argValue = (ZonedDateTime) pojo;
            valueDto.setZonedDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ENUM: {
            final Enum<?> argValue = (Enum<?>) pojo;
            final EnumDto enumDto = new EnumDto();
            valueDto.setEnum(enumDto);
            enumDto.setEnumType(argValue.getClass().getName());
            enumDto.setEnumName(argValue.name());
            return valueDto;
        }
        case BLOB: {
            final Blob blob = (Blob) pojo;
            if(blob != null) {
                final BlobDto blobDto = new BlobDto();
                blobDto.setName(blob.getName());
                blobDto.setBytes(blob.getBytes());
                blobDto.setMimeType(blob.getMimeType().toString());
                valueDto.setBlob(blobDto);
            }
            return valueDto;
        }
        case CLOB: {
            final Clob clob = (Clob) pojo;
            if(clob != null) {
                final ClobDto clobDto = new ClobDto();
                clobDto.setName(clob.getName());
                clobDto.setChars(clob.getChars().toString());
                clobDto.setMimeType(clob.getMimeType().toString());
                valueDto.setClob(clobDto);
            }
            return valueDto;
        }
        case VOID: {
            return null;
        }
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }
    }

    // -- VALUE RECOVERY

    @SneakyThrows
    public Object getValueAsObject (
            final @Nullable ValueWithTypeDto valueDto) {

        if(valueDto==null) {
            return null;
        }

        return getValueAsObject(valueDto.getType(), valueDto);
    }

    @SneakyThrows
    public Object getValueAsObject (
            final @NonNull  ValueType valueType,
            final @Nullable ValueDto valueDto) {

        if(valueDto==null) {
            return null;
        }

        switch(valueType) {
        case STRING:
            return valueDto.getString();
        case BYTE:
            return valueDto.getByte();
        case SHORT:
            return valueDto.getShort();
        case INT:
            return valueDto.getInt();
        case LONG:
            return valueDto.getLong();
        case FLOAT:
            return valueDto.getFloat();
        case DOUBLE:
            return valueDto.getDouble();
        case BOOLEAN:
            return valueDto.isBoolean();
        case CHAR:
            final String aChar = valueDto.getChar();
            if(_Strings.isNullOrEmpty(aChar)) { return null; }
            return aChar.charAt(0);
        case BIG_DECIMAL:
            return valueDto.getBigDecimal();
        case BIG_INTEGER:
            return valueDto.getBigInteger();
        case LOCAL_DATE:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalDate(valueDto.getLocalDate());
        case LOCAL_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalTime(valueDto.getLocalTime());
        case LOCAL_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalDateTime(valueDto.getLocalDateTime());
        case OFFSET_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toOffsetDateTime(valueDto.getOffsetDateTime());
        case OFFSET_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toOffsetTime(valueDto.getOffsetTime());
        case ZONED_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toZonedDateTime(valueDto.getZonedDateTime());
        case ENUM:
            final EnumDto enumDto = valueDto.getEnum();
            final String enumType = enumDto.getEnumType();
            @SuppressWarnings("rawtypes")
            final Class<? extends Enum> enumClass =
                    _Casts.uncheckedCast(_Context.loadClassAndInitialize(enumType));
            return Enum.valueOf(_Casts.uncheckedCast(enumClass), enumDto.getEnumName());
        case REFERENCE:
            return valueDto.getReference();
        case BLOB:
            final BlobDto blobDto = valueDto.getBlob();
            return new Blob(blobDto.getName(), blobDto.getMimeType(), blobDto.getBytes());
        case CLOB:
            final ClobDto clobDto = valueDto.getClob();
            return new Clob(clobDto.getName(), clobDto.getMimeType(), clobDto.getChars());
        case VOID:
            return null;
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }

    }

    // -- LIFTING

    public ValueWithTypeDto toValueWithTypeDto(
            final @NonNull  ValueType valueType,
            final @Nullable ValueDto valueDto) {

        if(valueDto instanceof ValueWithTypeDto) {
            val downCast = (ValueWithTypeDto) valueDto;
            _Assert.assertEquals(valueType, downCast.getType());
            return downCast;
        }

        val liftedDto = new ValueWithTypeDto();
        liftedDto.setType(valueType);

        if(valueDto==null) {
            return liftedDto; // null to empty
        }

        _copy(valueType, valueDto, liftedDto);

        return liftedDto;
    }

    // -- MAP-DTO SUPPORT

    public String getMapValue(final MapDto mapDto, final String key) {
        if(mapDto == null) {
            return null;
        }
        final Optional<MapDto.Entry> entryIfAny = entryIfAnyFor(mapDto, key);
        return entryIfAny.map(MapDto.Entry::getValue).orElse(null);
    }

    public void putMapKeyValue(final MapDto mapDto, final String key, final String value) {
        if(mapDto == null) {
            return;
        }
        final Optional<MapDto.Entry> entryIfAny = entryIfAnyFor(mapDto, key);
        if(entryIfAny.isPresent()) {
            entryIfAny.get().setValue(value);
        } else {
            val entry = new MapDto.Entry();
            entry.setKey(key);
            entry.setValue(value);
            mapDto.getEntry().add(entry);
        }
    }

    private static Optional<MapDto.Entry> entryIfAnyFor(final MapDto mapDto, final String key) {
        return mapDto.getEntry().stream()
                .filter(entry->Objects.equals(entry.getKey(), key))
                .findFirst();
    }

    // -- VALUE FACTORY

    public static <T> ValueWithTypeDto fundamentalType(final ValueType vType, final T value) {
        return recordFundamentalValue(vType, new ValueWithTypeDto(), value);
    }

    public static <T> ValueDecomposition fundamentalTypeAsDecomposition(final ValueType vType, final T value) {
        return ValueDecomposition.ofFundamental(fundamentalType(vType, value));
    }

    // -- TYPED TUPLE BUILDER

    @RequiredArgsConstructor
    public static class TypedTupleBuilder<T> {

        private final T value;
        private final TypedTupleDto dto = new TypedTupleDto();

        public TypedTupleBuilder<T> addFundamentalType(
                final ValueType vType, final String fieldName, final Function<T, Object> getter) {
            val elementDto = new NamedValueWithTypeDto();
            _Assert.assertTrue(_Strings.isNotEmpty(fieldName));
            elementDto.setName(fieldName);
            dto.getElements().add(
                    recordFundamentalValue(vType, elementDto, getter.apply(value)));
            return this;
        }

        public TypedTupleDto build() {
            dto.setType(value.getClass().getName());
            dto.setCardinality(dto.getElements().size());
            return dto;
        }

        public ValueDecomposition buildAsDecomposition() {
            return ValueDecomposition.ofComposite(build());
        }

    }

    public static <T> TypedTupleBuilder<T> typedTupleBuilder(final T value) {
        return new TypedTupleBuilder<T>(value);
    }

    // -- TYPED TUPLE AS MAP

    public static Map<String, Object> typedTupleAsMap(final TypedTupleDto dto) {

        val map = new LinkedHashMap<String, Object>(dto.getCardinality()); // preserve order

        dto.getElements()
            .forEach(elementDto->
                map.put(elementDto.getName(), getValueAsObject(elementDto)));

        return map;
    }

    // -- PARAM DTO FACTORIES

    public static ParamDto paramDto(final @NonNull String paramName) {
        val paramDto = new ParamDto();
        if(paramName.isBlank()) {
            throw _Exceptions.illegalArgument("paramName must not be blank '%s'", paramName);
        }
        paramDto.setName(paramName);
        return paramDto;
    }

    // -- COPY UTILITIES

    public static void copy(
            final @NonNull ValueWithTypeDto src,
            final @NonNull ValueWithTypeDto dst) {

        val valueType = src.getType();
        dst.setType(valueType);

        _copy(valueType, src, dst);
    }

    private static void _copy(
            final @NonNull ValueType valueType,
            final @NonNull ValueDto src,
            final @NonNull ValueDto dst) {

        switch(valueType) {
        case BIG_DECIMAL:
            dst.setBigDecimal(src.getBigDecimal());
            break;
        case BIG_INTEGER:
            dst.setBigInteger(src.getBigInteger());
            break;
        case BLOB:
            dst.setBlob(src.getBlob());
            break;
        case BOOLEAN:
            dst.setBoolean(src.isBoolean());
            break;
        case BYTE:
            dst.setByte(src.getByte());
            break;
        case CHAR:
            dst.setChar(src.getChar());
            break;
        case CLOB:
            dst.setClob(src.getClob());
            break;
        case COLLECTION:
            dst.setCollection(src.getCollection());
            break;
        case COMPOSITE:
            dst.setComposite(src.getComposite());
            break;
        case DOUBLE:
            dst.setDouble(src.getDouble());
            break;
        case ENUM:
            dst.setEnum(src.getEnum());
            break;
        case FLOAT:
            dst.setFloat(src.getFloat());
            break;
        case INT:
            dst.setInt(src.getInt());
            break;
        case LOCAL_DATE:
            dst.setLocalDate(src.getLocalDate());
            break;
        case LOCAL_DATE_TIME:
            dst.setLocalDateTime(src.getLocalDateTime());
            break;
        case LOCAL_TIME:
            dst.setLocalTime(src.getLocalTime());
            break;
        case LONG:
            dst.setLong(src.getLong());
            break;
        case OFFSET_DATE_TIME:
            dst.setOffsetDateTime(src.getOffsetDateTime());
            break;
        case OFFSET_TIME:
            dst.setOffsetTime(src.getOffsetTime());
            break;
        case REFERENCE:
            dst.setReference(src.getReference());
            break;
        case SHORT:
            dst.setShort(src.getShort());
            break;
        case STRING:
            dst.setString(src.getString());
            break;
        case VOID:
            break;
        case ZONED_DATE_TIME:
            dst.setZonedDateTime(src.getZonedDateTime());
            break;
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }

    }

}
