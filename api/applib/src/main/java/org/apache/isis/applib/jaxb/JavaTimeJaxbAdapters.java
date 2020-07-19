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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Function;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

/**
 * Provides JAXB XmlAdapters for Java time temporal types.
 * <p>
 * 
 * Example:<pre>
 * &#64;XmlElement &#64;XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateAdapter.class)
 * &#64;Getter &#64;Setter private java.time.LocalDate localDate;
 * </pre>
 * 
 *  
 * @since 2.0
 */
@UtilityClass
public final class JavaTimeJaxbAdapters {

    private static boolean isNullOrEmpty(String x) {
        return x == null || x.isEmpty();
    }

    public static final class LocalTimeToStringAdapter extends XmlAdapter<String, LocalTime> {

        @Override
        public LocalTime unmarshal(String v) {
            return isNullOrEmpty(v) ? null : LocalTime.parse(v);
        }

        @Override
        public String marshal(LocalTime v) {
            return v!=null ? v.toString() : null;
        }

    }

    public static final class LocalDateToStringAdapter extends XmlAdapter<String, LocalDate> {

        @Override
        public LocalDate unmarshal(String v) {
            return v!=null ? LocalDate.parse(v) : null;
        }

        @Override
        public String marshal(LocalDate v) {
            return v!=null ? v.toString() : null;
        }
    }

    public static final class LocalDateTimeToStringAdapter extends XmlAdapter<String, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(String v) {
            return v!=null ? LocalDateTime.parse(v) : null;
        }

        @Override
        public String marshal(LocalDateTime v) {
            return v!=null ? v.toString() : null;
        }
    }

    public static final class OffsetTimeAdapter extends XmlAdapter<String, OffsetTime> {

        @Override
        public OffsetTime unmarshal(String v) {
            return v!=null ? OffsetTime.parse(v) : null;
        }

        @Override
        public String marshal(OffsetTime v) {
            return v!=null ? v.toString() : null;
        }
    }

    public static final class OffsetDateTimeToStringAdapter extends XmlAdapter<String, OffsetDateTime> {

        @Override
        public OffsetDateTime unmarshal(String v) {
            return v!=null ? OffsetDateTime.parse(v) : null;
        }

        @Override
        public String marshal(OffsetDateTime v) {
            return v!=null ? v.toString() : null;
        }

    }

    public static final class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

        @Override
        public ZonedDateTime unmarshal(String v) {
            return v!=null ? ZonedDateTime.parse(v) : null;
        }

        @Override
        public String marshal(ZonedDateTime v) {
            return v!=null ? v.toString() : null;
        }

    }

    public static final class DurationToStringAdapter extends XmlAdapter<String, Duration> {

        @Override
        public Duration unmarshal(String v) {
            return v!=null
                    ? Duration.parse(v)
                    : null;
        }

        @Override
        public String marshal(Duration v) {
            return v!=null
                    ? v.toString()
                    : null;
        }

    }

    public static final class PeriodToStringAdapter extends XmlAdapter<String, Period> {

        @Override
        public Period unmarshal(String v) {
            return v!=null
                    ? Period.parse(v)
                    : null;
        }

        @Override
        public String marshal(Period v) {
            return v!=null
                    ? v.toString()
                    : null;
        }

    }

}
