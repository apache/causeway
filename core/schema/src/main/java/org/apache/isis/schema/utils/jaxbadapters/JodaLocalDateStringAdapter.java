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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.LocalDate;

import org.apache.isis.commons.internal.base._Strings;

/**
 * Note: not actually registered as a JAXB adapter.
 */
public final class JodaLocalDateStringAdapter {
    private JodaLocalDateStringAdapter() {
    }

    public static LocalDate parse(final String date) {
        if (_Strings.isNullOrEmpty(date)) {
            return null;
        }
        return LocalDate.parse(date);
    }

    public static String print(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.toString();
    }


    public static class ForJaxb extends XmlAdapter<String, LocalDate> {

        @Override
        public LocalDate unmarshal(final String localDateStr) throws Exception {
            return JodaLocalDateStringAdapter.parse(localDateStr);
        }

        @Override
        public String marshal(final LocalDate localDate) throws Exception {
            return JodaLocalDateStringAdapter.print(localDate);
        }
    }


}
