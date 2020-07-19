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
package org.apache.isis.applib.util.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.jaxb.JodaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.common.v2.BlobDto;
import org.apache.isis.schema.common.v2.ClobDto;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.EnumDto;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import static org.apache.isis.core.commons.internal.collections._Maps.entry;

public final class CommonDtoUtils {

    // -- PARAM_DTO_TO_NAME, PARAM_DTO_TO_TYPE

    public static final Function<ParamDto, String> PARAM_DTO_TO_NAME = ParamDto::getName;
    public static final Function<ParamDto, ValueType> PARAM_DTO_TO_TYPE = ParamDto::getType;

    // -- asValueType
    public static final Map<Class<?>, ValueType> valueTypeByClass =
            _Maps.unmodifiableEntries(
                    entry(String.class, ValueType.STRING),
                    entry(byte.class, ValueType.BYTE),
                    entry(Byte.class, ValueType.BYTE),
                    entry(short.class, ValueType.SHORT),
                    entry(Short.class, ValueType.SHORT),
                    entry(int.class, ValueType.INT),
                    entry(Integer.class, ValueType.INT),
                    entry(long.class, ValueType.LONG),
                    entry(Long.class, ValueType.LONG),
                    entry(char.class, ValueType.CHAR),
                    entry(Character.class, ValueType.CHAR),
                    entry(boolean.class, ValueType.BOOLEAN),
                    entry(Boolean.class, ValueType.BOOLEAN),
                    entry(float.class, ValueType.FLOAT),
                    entry(Float.class, ValueType.FLOAT),
                    entry(double.class, ValueType.DOUBLE),
                    entry(Double.class, ValueType.DOUBLE),
                    entry(BigInteger.class, ValueType.BIG_INTEGER),
                    entry(BigDecimal.class, ValueType.BIG_DECIMAL),

                    // java.time
                    entry(LocalDate.class, ValueType.LOCAL_DATE),
                    entry(LocalDateTime.class, ValueType.LOCAL_DATE_TIME),
                    entry(LocalTime.class, ValueType.LOCAL_TIME),
                    entry(OffsetDateTime.class, ValueType.OFFSET_DATE_TIME),
                    entry(OffsetTime.class, ValueType.OFFSET_TIME),
                    entry(ZonedDateTime.class, ValueType.ZONED_DATE_TIME),
                    
                    // joda.time
                    entry(org.joda.time.DateTime.class, ValueType.JODA_DATE_TIME),
                    entry(org.joda.time.LocalDateTime.class, ValueType.JODA_LOCAL_DATE_TIME),
                    entry(org.joda.time.LocalDate.class, ValueType.JODA_LOCAL_DATE),
                    entry(org.joda.time.LocalTime.class, ValueType.JODA_LOCAL_TIME),
                    
                    entry(java.sql.Timestamp.class, ValueType.JAVA_SQL_TIMESTAMP),
                    entry(Blob.class, ValueType.BLOB),
                    entry(Clob.class, ValueType.CLOB)
                    );

    private static final Set<Class<?>> VALUE_TYPES = valueTypeByClass.keySet();

    public static ValueType asValueType(final Class<?> type) {
        final ValueType valueType = valueTypeByClass.get(type);
        if (valueType != null) {
            return valueType;
        }
        if (type!=null && type.isEnum()) {
            return ValueType.ENUM;
        }
        // assume reference otherwise
        return ValueType.REFERENCE;
    }


    // -- newValueDto, setValueOn

    public static ValueDto newValueDto(
            final ValueType valueType,
            final Object val,
            final BookmarkService bookmarkService) {

        if(val == null) {
            return null;
        }

        final ValueDto valueDto = new ValueDto();
        return setValueOn(valueDto, valueType, val, bookmarkService);
    }

    public static <T extends ValueWithTypeDto> T setValueOn(
            final T valueWithTypeDto,
            final ValueType valueType,
            final Object val,
            final BookmarkService bookmarkService) {
        valueWithTypeDto.setType(valueType);

        setValueOn((ValueDto)valueWithTypeDto, valueType, val, bookmarkService);
        valueWithTypeDto.setNull(val == null);

        if(val instanceof Collection) {
            // TODO: this is probably irrelevant
            valueWithTypeDto.setType(ValueType.COLLECTION);
        }
        return valueWithTypeDto;
    }

    public static <T extends ValueDto> T setValueOn(
            final T valueDto,
            final ValueType valueType,
            final Object pojo,
            final BookmarkService bookmarkService) {
        if(pojo instanceof Collection) {
            final Collection<?> collection = (Collection<?>) pojo;
            final CollectionDto collectionDto = asCollectionDto(collection, valueType, bookmarkService);
            valueDto.setCollection(collectionDto);
            return valueDto;
        }
        switch (valueType) {
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
            valueDto.setLocalDate(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case LOCAL_TIME: {
            final LocalTime argValue = (LocalTime) pojo;
            valueDto.setLocalTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case LOCAL_DATE_TIME: {
            final LocalDateTime argValue = (LocalDateTime) pojo;
            valueDto.setLocalDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case OFFSET_DATE_TIME: {
            final OffsetDateTime argValue = (OffsetDateTime) pojo;
            valueDto.setOffsetDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case OFFSET_TIME: {
            final OffsetTime argValue = (OffsetTime) pojo;
            valueDto.setOffsetTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case ZONED_DATE_TIME: {
            final ZonedDateTime argValue = (ZonedDateTime) pojo;
            valueDto.setZonedDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar2(argValue));
            return valueDto;
        }
        case JODA_DATE_TIME: {
            final org.joda.time.DateTime argValue = (org.joda.time.DateTime) pojo;
            valueDto.setOffsetDateTime(JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case JODA_LOCAL_DATE_TIME: {
            final org.joda.time.LocalDateTime argValue = (org.joda.time.LocalDateTime) pojo;
            valueDto.setLocalDateTime(JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case JODA_LOCAL_DATE: {
            final org.joda.time.LocalDate argValue = (org.joda.time.LocalDate) pojo;
            valueDto.setLocalDate(JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case JODA_LOCAL_TIME: {
            final org.joda.time.LocalTime argValue = (org.joda.time.LocalTime) pojo;
            valueDto.setLocalTime(JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case JAVA_SQL_TIMESTAMP: {
            final java.sql.Timestamp argValue = (java.sql.Timestamp) pojo;
            valueDto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ENUM: {
            final Enum<?> argValue = (Enum<?>) pojo;
            if(argValue == null) {
                return null;
            }
            final EnumDto enumDto = new EnumDto();
            valueDto.setEnum(enumDto);
            enumDto.setEnumType(argValue.getClass().getName());
            enumDto.setEnumName(argValue.name());
            return valueDto;
        }
        case REFERENCE: {
            final Bookmark bookmark = pojo instanceof Bookmark
                    ? (Bookmark) pojo
                    : bookmarkService!=null 
                            ? bookmarkService.bookmarkFor(pojo) 
                            : null;

            if (bookmark != null) {
                OidDto argValue = bookmark.toOidDto();
                valueDto.setReference(argValue);
            }
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

    private static CollectionDto asCollectionDto(
            final Iterable<?> iterable,
            final ValueType valueType,
            final BookmarkService bookmarkService) {
        final CollectionDto collectionDto = new CollectionDto();
        collectionDto.setType(valueType);
        for (Object o : iterable) {
            final ValueDto valueDto = new ValueDto();
            setValueOn(valueDto, valueType, o, bookmarkService);
            collectionDto.getValue().add(valueDto);
        }
        return collectionDto;
    }


    // -- getValue (from valueDto)

    public static <T> T getValue(
            final ValueDto valueDto,
            final ValueType valueType) {
        return _Casts.uncheckedCast(getValueAsObject(valueDto, valueType));
    }

    private static Object getValueAsObject(
            final ValueDto valueDto,
            final ValueType valueType) {
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
         // JAVA TIME    
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
        case JAVA_SQL_TIMESTAMP:
            return JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(valueDto.getTimestamp());
        case JODA_DATE_TIME:
            return JodaTimeXMLGregorianCalendarMarshalling.toDateTime(valueDto.getOffsetDateTime());
        case JODA_LOCAL_DATE:
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalDate(valueDto.getLocalDate());
        case JODA_LOCAL_DATE_TIME:
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalDateTime(valueDto.getLocalDateTime());
        case JODA_LOCAL_TIME:
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalTime(valueDto.getLocalTime());
        case ENUM:
            final EnumDto enumDto = valueDto.getEnum();
            final String enumType = enumDto.getEnumType();
            @SuppressWarnings("rawtypes")
            final Class<? extends Enum> enumClass = loadClassElseThrow(enumType);
            return Enum.valueOf(_Casts.uncheckedCast(enumClass), enumDto.getEnumName());
        case REFERENCE:
            return valueDto.getReference();
        case COLLECTION:
            return valueDto.getCollection();
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

    private static <T> Class<T> loadClassElseThrow(final String className) {
        try {
            return _Casts.uncheckedCast(_Context.loadClassAndInitialize(className));
        } catch (ClassNotFoundException e) {

            // [ahuber] fallback to pre 2.0.0 behavior, not sure if needed
            try {
                return _Casts.uncheckedCast(Class.forName(className));
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e);
            }
        }
    }



    // -- newValueWithTypeDto


    public static ValueWithTypeDto newValueWithTypeDto(
            final Class<?> type,
            final Object val,
            final BookmarkService bookmarkService) {

        final ValueWithTypeDto valueWithTypeDto = new ValueWithTypeDto();

        final ValueType valueType = asValueType(type);
        setValueOn(valueWithTypeDto, valueType, val, bookmarkService);

        return valueWithTypeDto;
    }



    // -- getValue (from ValueWithTypeDto)

    public static <T> T getValue(final ValueWithTypeDto valueWithTypeDto) {
        if(valueWithTypeDto.isNull()) {
            return null;
        }
        final ValueType type = valueWithTypeDto.getType();
        return CommonDtoUtils.getValue(valueWithTypeDto, type);
    }





    // -- newParamDto

    public static ParamDto newParamDto(
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {

        final ParamDto paramDto = new ParamDto();

        paramDto.setName(parameterName);

        ValueType valueType = CommonDtoUtils.asValueType(parameterType);
        // this hack preserves previous behaviour before we were able to serialize blobs and clobs into XML
        // however, we also don't want this new behaviour for parameter arguments
        // (else these large objects could end up being persisted).
        if(valueType == ValueType.BLOB) valueType = ValueType.REFERENCE;
        if(valueType == ValueType.CLOB) valueType = ValueType.REFERENCE;

        paramDto.setType(valueType);

        CommonDtoUtils.setValueOn(paramDto, valueType, arg, bookmarkService);

        return paramDto;
    }


    // -- getValue (from ParamDto)

    public static <T> T getValue(final ParamDto paramDto) {
        if(paramDto.isNull()) {
            return null;
        }
        final ValueType parameterType = paramDto.getType();
        return CommonDtoUtils.getValue(paramDto, parameterType);
    }



    public static String getMapValue(final MapDto mapDto, final String key) {
        if(mapDto == null) {
            return null;
        }
        final Optional<MapDto.Entry> entryIfAny = entryIfAnyFor(mapDto, key);
        return entryIfAny.isPresent() ? entryIfAny.get().getValue() : null;
    }

    public static void putMapKeyValue(final MapDto mapDto, final String key, final String value) {
        if(mapDto == null) {
            return;
        }
        final Optional<MapDto.Entry> entryIfAny = entryIfAnyFor(mapDto, key);
        if(entryIfAny.isPresent()) {
            entryIfAny.get().setValue(value);
        } else {
            final MapDto.Entry entry = new MapDto.Entry();
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


    public static boolean isValueType(Class<?> type) {
        return VALUE_TYPES.contains(type);
    }

}
