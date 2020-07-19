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

import java.sql.Date;
import java.sql.Timestamp;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

/**
 * Provides JAXB XmlAdapters for java sql temporal types.
 *
 * <p>
 * Example:<pre>
 * &#64;XmlElement &#64;XmlJavaTypeAdapter(JavaSqlJaxbAdapters.DateAdapter.class)
 * &#64;Getter &#64;Setter private java.sql.Date date;
 * </pre>
 *
 * @since 2.0
 */
@UtilityClass
public final class JavaSqlJaxbAdapters {

    public static final class DateToStringAdapter extends XmlAdapter<String, Date> {

        @Override
        public Date unmarshal(String v) throws Exception {
            return v!=null ? Date.valueOf(v) : null;
        }

        @Override
        public String marshal(Date v) {
            return v!=null ? v.toString() : null;
        }
    }

    public static final class TimestampToStringAdapter extends XmlAdapter<String, java.sql.Timestamp> {

        @Override
        public java.sql.Timestamp unmarshal(String v) {
            return v!=null ? new java.sql.Timestamp(Long.parseLong(v)) : null;
        }

        @Override
        public String marshal(java.sql.Timestamp v) {
            return v!=null ? Long.toString(v.getTime()) : null;
        }
    }

    public static class TimestampToXMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, Timestamp> {

        @Override
        public Timestamp unmarshal(final XMLGregorianCalendar timestampStr) {
            return JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(timestampStr);
        }

        @Override
        public XMLGregorianCalendar marshal(final Timestamp timestamp) {
            return JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp);
        }

    }
}
