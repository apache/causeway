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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.ValueType;

import static org.apache.isis.commons.internal.collections._Maps.entry;

import lombok.val;

/**
 * @since 1.x {@index}
 */
public final class CommonDtoUtils {

    // -- asValueType
    private static final Map<Class<?>, ValueType> valueTypeByClass =
            _Maps.unmodifiableEntries(
                    entry(Void.class, ValueType.VOID),
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

                    entry(Blob.class, ValueType.BLOB),
                    entry(Clob.class, ValueType.CLOB)
                    );

//    private static ValueTypeAndSemantics<?> asValueType(
//            final @NonNull Class<?> type,
//            final @NonNull SchemaValueMarshaller valueMarshaller) {
//
//        if(Iterable.class.isAssignableFrom(type)
//                        || type.isArray()) {
//            return ValueTypeAndSemantics.of(ValueType.COLLECTION, null);
//        }
////        if (type.isEnum()) {
////            return ValueType.ENUM;
////        }
////        // not strictly required: an optimization to infer ValueType, when directly mapped
////        final ValueType valueType = valueTypeByClass.get(type);
////        if (valueType != null) {
////            return valueType;
////        }
//        return ValueTypeAndSemantics.resolve(valueMarshaller, type);
//    }


    // -- MAP-DTO SUPPORT

    static String getMapValue(final MapDto mapDto, final String key) {
        if(mapDto == null) {
            return null;
        }
        final Optional<MapDto.Entry> entryIfAny = entryIfAnyFor(mapDto, key);
        return entryIfAny.map(MapDto.Entry::getValue).orElse(null);
    }

    static void putMapKeyValue(final MapDto mapDto, final String key, final String value) {
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
