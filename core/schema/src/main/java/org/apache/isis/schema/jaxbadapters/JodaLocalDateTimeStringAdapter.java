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

import org.joda.time.LocalDateTime;

/**
 * Note: not actually registered as a JAXB adapter.
 */
public final class JodaLocalDateTimeStringAdapter {
    private JodaLocalDateTimeStringAdapter() {
    }

    public static LocalDateTime parse(final String localDateTimeStr) {
        if (Strings.isNullOrEmpty(localDateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(localDateTimeStr);
    }

    public static String print(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toString();
    }

    public static class ForJaxb extends XmlAdapter<String, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(final String localDateTimeStr) throws Exception {
            return JodaLocalDateTimeStringAdapter.parse(localDateTimeStr);
        }

        @Override
        public String marshal(final LocalDateTime localDateTime) throws Exception {
            return JodaLocalDateTimeStringAdapter.print(localDateTime);
        }
    }

}
