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
package org.apache.isis.schema.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.schema.cmd.v1.MapDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.BlobDto;
import org.apache.isis.schema.common.v1.ClobDto;
import org.apache.isis.schema.common.v1.CollectionDto;
import org.apache.isis.schema.common.v1.EnumDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalTimeXMLGregorianCalendarAdapter;

public final class CommonDtoUtils {

    //region > PARAM_DTO_TO_NAME, PARAM_DTO_TO_TYPE

    public static final Function<ParamDto, String> PARAM_DTO_TO_NAME = new Function<ParamDto, String>() {
        @Override public String apply(final ParamDto paramDto) {
            return paramDto.getName();
        }
    };
    public static final Function<ParamDto, ValueType> PARAM_DTO_TO_TYPE = new Function<ParamDto, ValueType>() {
        @Override public ValueType apply(final ParamDto paramDto) {
            return paramDto.getType();
        }
    };
    //endregion

    //region > asValueType
    private final static ImmutableMap<Class<?>, ValueType> valueTypeByClass =
            new ImmutableMap.Builder<Class<?>, ValueType>()
                    .put(String.class, ValueType.STRING)
                    .put(byte.class, ValueType.BYTE)
                    .put(Byte.class, ValueType.BYTE)
                    .put(short.class, ValueType.SHORT)
                    .put(Short.class, ValueType.SHORT)
                    .put(int.class, ValueType.INT)
                    .put(Integer.class, ValueType.INT)
                    .put(long.class, ValueType.LONG)
                    .put(Long.class, ValueType.LONG)
                    .put(char.class, ValueType.CHAR)
                    .put(Character.class, ValueType.CHAR)
                    .put(boolean.class, ValueType.BOOLEAN)
                    .put(Boolean.class, ValueType.BOOLEAN)
                    .put(float.class, ValueType.FLOAT)
                    .put(Float.class, ValueType.FLOAT)
                    .put(double.class, ValueType.DOUBLE)
                    .put(Double.class, ValueType.DOUBLE)
                    .put(BigInteger.class, ValueType.BIG_INTEGER)
                    .put(BigDecimal.class, ValueType.BIG_DECIMAL)
                    .put(DateTime.class, ValueType.JODA_DATE_TIME)
                    .put(LocalDateTime.class, ValueType.JODA_LOCAL_DATE_TIME)
                    .put(LocalDate.class, ValueType.JODA_LOCAL_DATE)
                    .put(LocalTime.class, ValueType.JODA_LOCAL_TIME)
                    .put(java.sql.Timestamp.class, ValueType.JAVA_SQL_TIMESTAMP)
                    .put(Blob.class, ValueType.BLOB)
                    .put(Clob.class, ValueType.CLOB)
                    .build();

    public static Collection<Class<?>> VALUE_TYPES = valueTypeByClass.keySet();

    public static ValueType asValueType(final Class<?> type) {
        final ValueType valueType = valueTypeByClass.get(type);
        if (valueType != null) {
            return valueType;
        }
        if (type.isEnum()) {
            return ValueType.ENUM;
        }
        // assume reference otherwise
        return ValueType.REFERENCE;
    }
    //endregion

    //region > newValueDto, setValueOn

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
            final Object val,
            final BookmarkService bookmarkService) {
        if(val instanceof Collection) {
            final Collection collection = (Collection) val;
            final CollectionDto collectionDto = asCollectionDto(collection, valueType, bookmarkService);
            valueDto.setCollection(collectionDto);
            return valueDto;
        }
        switch (valueType) {
        case STRING: {
            final String argValue = (String) val;
            valueDto.setString(argValue);
            return valueDto;
        }
        case BYTE: {
            final Byte argValue = (Byte) val;
            valueDto.setByte(argValue);
            return valueDto;
        }
        case SHORT: {
            final Short argValue = (Short) val;
            valueDto.setShort(argValue);
            return valueDto;
        }
        case INT: {
            final Integer argValue = (Integer) val;
            valueDto.setInt(argValue);
            return valueDto;
        }
        case LONG: {
            final Long argValue = (Long) val;
            valueDto.setLong(argValue);
            return valueDto;
        }
        case CHAR: {
            final Character argValue = (Character) val;
            valueDto.setChar("" + argValue);
            return valueDto;
        }
        case BOOLEAN: {
            final Boolean argValue = (Boolean) val;
            valueDto.setBoolean(argValue);
            return valueDto;
        }
        case FLOAT: {
            final Float argValue = (Float) val;
            valueDto.setFloat(argValue);
            return valueDto;
        }
        case DOUBLE: {
            final Double argValue = (Double) val;
            valueDto.setDouble(argValue);
            return valueDto;
        }
        case BIG_INTEGER: {
            final BigInteger argValue = (BigInteger) val;
            valueDto.setBigInteger(argValue);
            return valueDto;
        }
        case BIG_DECIMAL: {
            final BigDecimal argValue = (BigDecimal) val;
            valueDto.setBigDecimal(argValue);
            return valueDto;
        }
        case JODA_DATE_TIME: {
            final DateTime argValue = (DateTime) val;
            valueDto.setDateTime(JodaDateTimeXMLGregorianCalendarAdapter.print(argValue));
            return valueDto;
        }
        case JODA_LOCAL_DATE_TIME: {
            final LocalDateTime argValue = (LocalDateTime) val;
            valueDto.setLocalDateTime(JodaLocalDateTimeXMLGregorianCalendarAdapter.print(argValue));
            return valueDto;
        }
        case JODA_LOCAL_DATE: {
            final LocalDate argValue = (LocalDate) val;
            valueDto.setLocalDate(JodaLocalDateXMLGregorianCalendarAdapter.print(argValue));
            return valueDto;
        }
        case JODA_LOCAL_TIME: {
            final LocalTime argValue = (LocalTime) val;
            valueDto.setLocalTime(JodaLocalTimeXMLGregorianCalendarAdapter.print(argValue));
            return valueDto;
        }
        case JAVA_SQL_TIMESTAMP: {
            final java.sql.Timestamp argValue = (java.sql.Timestamp) val;
            valueDto.setTimestamp(JavaSqlTimestampXmlGregorianCalendarAdapter.print(argValue));
            return valueDto;
        }
        case ENUM: {
            final Enum argValue = (Enum) val;
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
            final Bookmark bookmark = val instanceof Bookmark
                    ? (Bookmark) val
                    : bookmarkService.bookmarkFor(val);

            if (bookmark != null) {
                OidDto argValue = bookmark.toOidDto();
                valueDto.setReference(argValue);
            }
            return valueDto;
        }
        case BLOB: {
            final Blob blob = (Blob) val;
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
            final Clob clob = (Clob) val;
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
            // should never happen; all cases are listed above
            throw new IllegalArgumentException(String.format(
                    "newValueDto(): do not recognize valueType %s (likely a framework error)",
                    valueType));
        }
    }

    private static CollectionDto asCollectionDto(
            final Iterable iterable,
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
    //endregion

    //region > getValue (from valueDto)

    public static <T> T getValue(
            final ValueDto valueDto,
            final ValueType valueType) {
        switch(valueType) {
        case STRING:
            return (T) valueDto.getString();
        case BYTE:
            return (T) valueDto.getByte();
        case SHORT:
            return (T) valueDto.getShort();
        case INT:
            return (T) valueDto.getInt();
        case LONG:
            return (T) valueDto.getLong();
        case FLOAT:
            return (T) valueDto.getFloat();
        case DOUBLE:
            return (T) valueDto.getDouble();
        case BOOLEAN:
            return (T) valueDto.isBoolean();
        case CHAR:
            final String aChar = valueDto.getChar();
            if(Strings.isNullOrEmpty(aChar)) { return null; }
            return (T) (Object)aChar.charAt(0);
        case BIG_DECIMAL:
            return (T) valueDto.getBigDecimal();
        case BIG_INTEGER:
            return (T) valueDto.getBigInteger();
        case JAVA_SQL_TIMESTAMP:
            return (T) JavaSqlTimestampXmlGregorianCalendarAdapter.parse(valueDto.getDateTime());
        case JODA_DATE_TIME:
            return (T) JodaDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getDateTime());
        case JODA_LOCAL_DATE:
            return (T) JodaLocalDateXMLGregorianCalendarAdapter.parse(valueDto.getLocalDate());
        case JODA_LOCAL_DATE_TIME:
            return (T) JodaLocalDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalDateTime());
        case JODA_LOCAL_TIME:
            return (T) JodaLocalTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalTime());
        case ENUM:
            final EnumDto enumDto = valueDto.getEnum();
            final String enumType = enumDto.getEnumType();
            final Class<? extends Enum> enumClass = loadClassElseThrow(enumType);
            return (T) Enum.valueOf(enumClass, enumDto.getEnumName());
        case REFERENCE:
            return (T) valueDto.getReference();
        case COLLECTION:
            return (T)valueDto.getCollection();
        case BLOB:
            final BlobDto blobDto = valueDto.getBlob();
            return (T)new Blob(blobDto.getName(), blobDto.getMimeType(), blobDto.getBytes());
        case CLOB:
            final ClobDto clobDto = valueDto.getClob();
            return (T)new Clob(clobDto.getName(), clobDto.getMimeType(), clobDto.getChars());
        case VOID:
            return null;
        default:
            // should never happen; all cases are listed above
            throw new IllegalArgumentException(String.format(
                    "getValueDto(...): do not recognize valueType %s (likely a framework error)",
                    valueType));
        }
    }

    private static <T> Class<T> loadClassElseThrow(final String enumType) {
        try {
            return (Class<T>) loadClass(enumType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if(ccl == null) {
            return loadClass(className, (ClassLoader)null);
        } else {
            try {
                return loadClass(className, ccl);
            } catch (ClassNotFoundException var3) {
                return loadClass(className, (ClassLoader)null);
            }
        }
    }

    private static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader == null?Class.forName(className):Class.forName(className, true, classLoader);
    }
    //endregion

    //region > newValueWithTypeDto


    public static ValueWithTypeDto newValueWithTypeDto(
            final Class<?> type,
            final Object val,
            final BookmarkService bookmarkService) {

        final ValueWithTypeDto valueWithTypeDto = new ValueWithTypeDto();

        final ValueType valueType = asValueType(type);
        setValueOn(valueWithTypeDto, valueType, val, bookmarkService);

        return valueWithTypeDto;
    }

    //endregion

    //region > getValue (from ValueWithTypeDto)

    public static <T> T getValue(final ValueWithTypeDto valueWithTypeDto) {
        if(valueWithTypeDto.isNull()) {
            return null;
        }
        final ValueType type = valueWithTypeDto.getType();
        return CommonDtoUtils.getValue(valueWithTypeDto, type);
    }


    //endregion


    //region > newParamDto

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
    //endregion

    //region > getValue (from ParamDto)

    public static <T> T getValue(final ParamDto paramDto) {
        if(paramDto.isNull()) {
            return null;
        }
        final ValueType parameterType = paramDto.getType();
        return CommonDtoUtils.getValue(paramDto, parameterType);
    }

    //endregion

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
        return FluentIterable.from(mapDto.getEntry())
                    .firstMatch(new Predicate<MapDto.Entry>() {
                        @Override
                        public boolean apply(final MapDto.Entry entry) {
                            return Objects.equal(entry.getKey(), key);
                        }
                    });
    }

}
