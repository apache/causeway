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
package org.apache.isis.valuetypes.jodatime.applib.jaxb;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.jaxb.DataTypeFactory;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public class JodaTimeXMLGregorianCalendarMarshalling {

    public DateTime toDateTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final GregorianCalendar gc = xgc.toGregorianCalendar();
        final Date time = gc.getTime();
        final TimeZone timeZone = gc.getTimeZone();

        final DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        return new DateTime(time, dateTimeZone);
    }

    public LocalDate toLocalDate(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final int year = xgc.getYear();
        final int month = xgc.getMonth();
        final int day = xgc.getDay();

        return new LocalDate(year, month, day);
    }

    public LocalDateTime toLocalDateTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final int year = xgc.getYear();
        final int month = xgc.getMonth();
        final int day = xgc.getDay();
        final int hour = xgc.getHour();
        final int minute = xgc.getMinute();
        final int second = xgc.getSecond();
        final int millisecond = xgc.getMillisecond();

        return new LocalDateTime(year, month, day, hour, minute, second, millisecond);
    }

    public LocalTime toLocalTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) {
            return null;
        }

        final int hour = xgc.getHour();
        final int minute = xgc.getMinute();
        final int second = xgc.getSecond();
        final int millisecond = xgc.getMillisecond();

        return new LocalTime(hour, minute, second, millisecond);
    }

    public XMLGregorianCalendar toXMLGregorianCalendar(final DateTime dateTime) {
        return dateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        dateTime.toGregorianCalendar()))
                : null;
    }

    public XMLGregorianCalendar toXMLGregorianCalendar(final LocalDateTime localDateTime) {
        return localDateTime !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        localDateTime.getYear(),
                        localDateTime.getMonthOfYear(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHourOfDay(),
                        localDateTime.getMinuteOfHour(),
                        localDateTime.getSecondOfMinute(),
                        localDateTime.getMillisOfSecond(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

    public XMLGregorianCalendar toXMLGregorianCalendar(final LocalDate localDate) {
        return localDate !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
                        localDate.getYear(),
                        localDate.getMonthOfYear(),
                        localDate.getDayOfMonth(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

    public XMLGregorianCalendar toXMLGregorianCalendar(final LocalTime localTime) {
        return localTime !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        localTime.getHourOfDay(),
                        localTime.getMinuteOfHour(),
                        localTime.getSecondOfMinute(),
                        localTime.getMillisOfSecond(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

}
