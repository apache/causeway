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
package org.apache.isis.applib.jaxb;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JavaTimeXMLGregorianCalendarMarshalling {

    public static LocalDate toLocalDate(final XMLGregorianCalendar cal) {
        return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
    }

    public static LocalTime toLocalTime(final XMLGregorianCalendar cal) {
        return LocalTime.of(cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000);
    }

    public static LocalDateTime toLocalDateTime(final XMLGregorianCalendar cal) {
        return LocalDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000);
    }

    public static OffsetDateTime toOffsetDateTime(final XMLGregorianCalendar cal) {
        return OffsetDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static OffsetTime toOffsetTime(final XMLGregorianCalendar cal) {
        return OffsetTime.of(
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static ZonedDateTime toZonedDateTime(final XMLGregorianCalendar cal) {
        return ZonedDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalDate localDate) {
        return localDate!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
                        localDate.getYear(),
                        localDate.getMonthValue(),
                        localDate.getDayOfMonth(),
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalTime localTime) {
        return localTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        localTime.getHour(),
                        localTime.getMinute(),
                        localTime.getSecond(),
                        localTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalDateTime localDateTime) {
        return localDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        localDateTime.getYear(),
                        localDateTime.getMonthValue(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHour(),
                        localDateTime.getMinute(),
                        localDateTime.getSecond(),
                        localDateTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final OffsetTime offsetTime) {
        return offsetTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        offsetTime.getHour(),
                        offsetTime.getMinute(),
                        offsetTime.getSecond(),
                        offsetTime.getNano()/1000_000, // millis
                        offsetTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final OffsetDateTime offsetDateTime) {
        return offsetDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        offsetDateTime.getYear(),
                        offsetDateTime.getMonthValue(),
                        offsetDateTime.getDayOfMonth(),
                        offsetDateTime.getHour(),
                        offsetDateTime.getMinute(),
                        offsetDateTime.getSecond(),
                        offsetDateTime.getNano()/1000_000, // millis
                        offsetDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final ZonedDateTime zonedDateTime) {
        return zonedDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        zonedDateTime.getYear(),
                        zonedDateTime.getMonthValue(),
                        zonedDateTime.getDayOfMonth(),
                        zonedDateTime.getHour(),
                        zonedDateTime.getMinute(),
                        zonedDateTime.getSecond(),
                        zonedDateTime.getNano()/1000_000, // millis
                        zonedDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

}
