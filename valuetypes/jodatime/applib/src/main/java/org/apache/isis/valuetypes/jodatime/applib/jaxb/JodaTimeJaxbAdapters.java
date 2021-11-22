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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JodaTimeJaxbAdapters {

    public static class LocalDateToStringAdapter extends XmlAdapter<String, LocalDate> {

        @Override
        public LocalDate unmarshal(final String localDateStr) throws Exception {
            if (isNullOrEmpty(localDateStr)) {
                return null;
            }
            return LocalDate.parse(localDateStr);
        }

        @Override
        public String marshal(final LocalDate localDate) throws Exception {
            if (localDate == null) {
                return null;
            }
            return localDate.toString();
        }
    }

    public static class LocalDateToXMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, LocalDate> {

        @Override
        public LocalDate unmarshal(final XMLGregorianCalendar localDateXgc) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalDate(localDateXgc);
        }

        @Override
        public XMLGregorianCalendar marshal(final LocalDate LocalDate) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(LocalDate);
        }
    }

    public static class DateTimeToStringAdapter extends XmlAdapter<String, DateTime> {

        private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

        @Override
        public DateTime unmarshal(final String dateTimeStr) throws Exception {
            return !isNullOrEmpty(dateTimeStr) ? formatter.parseDateTime(dateTimeStr) : null;
        }

        @Override
        public String marshal(final DateTime dateTime) throws Exception {
            if (dateTime == null) {
                return null;
            }
            return formatter.print(dateTime);
        }
    }

    public static class DateTimeToXMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, DateTime> {

        @Override
        public DateTime unmarshal(final XMLGregorianCalendar dateTimeXgc) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toDateTime(dateTimeXgc);
        }

        @Override
        public XMLGregorianCalendar marshal(final DateTime DateTime) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(DateTime);
        }
    }

    public static class LocalDateTimeToStringAdapter extends XmlAdapter<String, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(final String localDateTimeStr) throws Exception {
            if (isNullOrEmpty(localDateTimeStr)) {
                return null;
            }
            return LocalDateTime.parse(localDateTimeStr);
        }

        @Override
        public String marshal(final LocalDateTime localDateTime) throws Exception {
            if (localDateTime == null) {
                return null;
            }
            return localDateTime.toString();
        }
    }

    public static class LocalDateTimeToXMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(final XMLGregorianCalendar localDateTimeXgc) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalDateTime(localDateTimeXgc);
        }

        @Override
        public XMLGregorianCalendar marshal(final LocalDateTime LocalDateTime) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(LocalDateTime);
        }
    }

    public static class LocalTimeToStringAdapter extends XmlAdapter<String, LocalTime> {

        @Override
        public LocalTime unmarshal(final String localTimeStr) throws Exception {
            if (isNullOrEmpty(localTimeStr)) {
                return null;
            }
            return LocalTime.parse(localTimeStr);
        }

        @Override
        public String marshal(final LocalTime localTime) throws Exception {
            if (localTime == null) {
                return null;
            }
            return localTime.toString();
        }
    }

    public static class LocalTimeToXMLGregorianCalendar extends XmlAdapter<XMLGregorianCalendar, LocalTime> {

        @Override
        public LocalTime unmarshal(final XMLGregorianCalendar localTimeXgc) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toLocalTime(localTimeXgc);
        }

        @Override
        public XMLGregorianCalendar marshal(final LocalTime LocalTime) throws Exception {
            return JodaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(LocalTime);
        }
    }


    private static boolean isNullOrEmpty(final String x) {
        return x == null || x.isEmpty();
    }


}

