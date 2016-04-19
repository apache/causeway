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

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.utils.jaxbadapters.JodaDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalTimeXMLGregorianCalendarAdapter;

public final class CommonDtoUtils {

    public static final Function<OidDto, String> OID_DTO_2_STR = new Function<OidDto, String>() {
        @Nullable @Override
        public String apply(final OidDto oidDto) {
            final Bookmark bookmark = Bookmark.from(oidDto);
            return bookmark.toString();
        }
    };

    public static final Function<ParamDto, String> PARAM_DTO_TO_NAME = new Function<ParamDto, String>() {
        @Override public String apply(final ParamDto paramDto) {
            return paramDto.getParameterName();
        }
    };
    public static final Function<ParamDto, ValueType> PARAM_DTO_TO_TYPE = new Function<ParamDto, ValueType>() {
        @Override public ValueType apply(final ParamDto paramDto) {
            return paramDto.getParameterType();
        }
    };

    public static boolean setValue(
            final ValueDto valueDto,
            final Class<?> type,
            final Object val) {
        if(type == String.class) {
            final String argValue = (String) val;
            valueDto.setString(argValue);
        } else
        if(type == byte.class || type == Byte.class) {
            final Byte argValue = (Byte) val;
            valueDto.setByte(argValue);
        } else
        if(type == short.class || type == Short.class) {
            final Short argValue = (Short) val;
            valueDto.setShort(argValue);
        }else
        if(type == int.class || type == Integer.class) {
            final Integer argValue = (Integer) val;
            valueDto.setInt(argValue);
        }else
        if(type == long.class || type == Long.class) {
            final Long argValue = (Long) val;
            valueDto.setLong(argValue);
        }else
        if(type == char.class || type == Character.class) {
            final Character argValue = (Character) val;
            valueDto.setChar("" + argValue);
        }else
        if(type == boolean.class || type == Boolean.class) {
            final Boolean argValue = (Boolean) val;
            valueDto.setBoolean(argValue);
        }else
        if(type == float.class || type == Float.class) {
            final Float argValue = (Float) val;
            valueDto.setFloat(argValue);
        }else
        if(type == double.class || type == Double.class) {
            final Double argValue = (Double) val;
            valueDto.setDouble(argValue);
        }else
        if(type == BigInteger.class) {
            final BigInteger argValue = (BigInteger) val;
            valueDto.setBigInteger(argValue);
        }else
        if(type == BigDecimal.class) {
            final BigDecimal argValue = (BigDecimal) val;
            valueDto.setBigDecimal(argValue);
        }else
        if(type == DateTime.class) {
            final DateTime argValue = (DateTime) val;
            valueDto.setDateTime(JodaDateTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalDateTime.class) {
            final LocalDateTime argValue = (LocalDateTime) val;
            valueDto.setLocalDateTime(JodaLocalDateTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalDate.class) {
            final LocalDate argValue = (LocalDate) val;
            valueDto.setLocalDate(JodaLocalDateXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalTime.class) {
            final LocalTime argValue = (LocalTime) val;
            valueDto.setLocalTime(JodaLocalTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        {
            // none of the supported value types
            return false;
        }
        return true;
    }

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
        case JODA_DATE_TIME:
            return (T) JodaDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getDateTime());
        case JODA_LOCAL_DATE:
            return (T) JodaLocalDateXMLGregorianCalendarAdapter.parse(valueDto.getLocalDate());
        case JODA_LOCAL_DATE_TIME:
            return (T) JodaLocalDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalDateTime());
        case JODA_LOCAL_TIME:
            return (T) JodaLocalTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalTime());
        case REFERENCE:
            return (T) valueDto.getReference();
        }
        throw new IllegalStateException("Value type was not recognised (possible bug)");
    }

    public static OidDto asOidDto(final Bookmark reference) {
        return reference != null ? reference.toOidDto() : null;
    }


}
