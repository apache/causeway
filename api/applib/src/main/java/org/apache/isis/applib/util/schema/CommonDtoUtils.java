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

import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.BlobDto;
import org.apache.isis.schema.common.v2.ClobDto;
import org.apache.isis.schema.common.v2.EnumDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class CommonDtoUtils {

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

        val dto = new ValueWithTypeDto();
        dto.setType(valueType);

        if(valueDto==null) {
            return dto; // null to empty
        }

        switch(valueType) {
        case BIG_DECIMAL:
            dto.setBigDecimal(valueDto.getBigDecimal());
            break;
        case BIG_INTEGER:
            dto.setBigInteger(valueDto.getBigInteger());
            break;
        case BLOB:
            dto.setBlob(valueDto.getBlob());
            break;
        case BOOLEAN:
            dto.setBoolean(valueDto.isBoolean());
            break;
        case BYTE:
            dto.setByte(valueDto.getByte());
            break;
        case CHAR:
            dto.setChar(valueDto.getChar());
            break;
        case CLOB:
            dto.setClob(valueDto.getClob());
            break;
        case COLLECTION:
            dto.setCollection(valueDto.getCollection());
            break;
        case COMPOSITE:
            dto.setComposite(valueDto.getComposite());
            break;
        case DOUBLE:
            dto.setDouble(valueDto.getDouble());
            break;
        case ENUM:
            dto.setEnum(valueDto.getEnum());
            break;
        case FLOAT:
            dto.setFloat(valueDto.getFloat());
            break;
        case INT:
            dto.setInt(valueDto.getInt());
            break;
        case LOCAL_DATE:
            dto.setLocalDate(valueDto.getLocalDate());
            break;
        case LOCAL_DATE_TIME:
            dto.setLocalDateTime(valueDto.getLocalDateTime());
            break;
        case LOCAL_TIME:
            dto.setLocalTime(valueDto.getLocalTime());
            break;
        case LONG:
            dto.setLong(valueDto.getLong());
            break;
        case OFFSET_DATE_TIME:
            dto.setOffsetDateTime(valueDto.getOffsetDateTime());
            break;
        case OFFSET_TIME:
            dto.setOffsetTime(valueDto.getOffsetTime());
            break;
        case REFERENCE:
            dto.setReference(valueDto.getReference());
            break;
        case SHORT:
            dto.setShort(valueDto.getShort());
            break;
        case STRING:
            dto.setString(valueDto.getString());
            break;
        case VOID:
            break;
        case ZONED_DATE_TIME:
            dto.setZonedDateTime(valueDto.getZonedDateTime());
            break;
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }
        return dto;
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


}
