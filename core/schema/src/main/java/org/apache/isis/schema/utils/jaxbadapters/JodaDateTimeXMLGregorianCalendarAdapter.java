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
package org.apache.isis.schema.utils.jaxbadapters;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Note: not actually registered as a JAXB adapter.
 */
public final class JodaDateTimeXMLGregorianCalendarAdapter {
    private JodaDateTimeXMLGregorianCalendarAdapter() {
    }


    public static DateTime parse(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final GregorianCalendar gc = xgc.toGregorianCalendar();
        final Date time = gc.getTime();
        final TimeZone timeZone = gc.getTimeZone();

        final DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        return new DateTime(time, dateTimeZone);
    }

    public static XMLGregorianCalendar print(final DateTime dateTime) {
        if(dateTime == null) {
            return null;
        }

        final long millis = dateTime.getMillis();
        final DateTimeZone dateTimeZone = dateTime.getZone();

        final TimeZone timeZone = dateTimeZone.toTimeZone();
        final GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.setTimeInMillis(millis);

        return new XMLGregorianCalendarImpl(calendar);
    }

}
