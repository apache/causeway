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
package org.apache.isis.schema.jaxbadapters;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

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
    	return XmlCalendarFactory.create(dateTime);
    }

    public static class ForJaxb extends XmlAdapter<XMLGregorianCalendar, DateTime> {

        @Override
        public DateTime unmarshal(final XMLGregorianCalendar dateTimeStr) throws Exception {
            return JodaDateTimeXMLGregorianCalendarAdapter.parse(dateTimeStr);
        }

        @Override
        public XMLGregorianCalendar marshal(final DateTime DateTime) throws Exception {
            return JodaDateTimeXMLGregorianCalendarAdapter.print(DateTime);
        }
    }


}
