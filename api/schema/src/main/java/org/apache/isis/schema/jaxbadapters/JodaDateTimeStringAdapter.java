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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Note: not actually registered as a JAXB adapter.
 */
public final class JodaDateTimeStringAdapter {
    private JodaDateTimeStringAdapter() {
    }

    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    public static DateTime parse(final String dateTimeStr) {
        return !Strings.isNullOrEmpty(dateTimeStr) ? formatter.parseDateTime(dateTimeStr) : null;
    }

    public static String print(final DateTime date) {
        if (date == null) {
            return null;
        }
        return formatter.print(date);
    }

    public static class ForJaxb extends XmlAdapter<String, DateTime> {

        @Override
        public DateTime unmarshal(final String dateTimeStr) throws Exception {
            return JodaDateTimeStringAdapter.parse(dateTimeStr);
        }

        @Override
        public String marshal(final DateTime dateTime) throws Exception {
            return JodaDateTimeStringAdapter.print(dateTime);
        }
    }
}
