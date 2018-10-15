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
package org.apache.isis.applib.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides JAXB XmlAdapters for Java built-in temporal types. 
 * Others types might be added, if convenient. 
 * 
 * @since 2.0.0-M2
 */
public final class JaxbAdapters {

    public static final class DateAdapter extends XmlAdapter<String, java.util.Date>{

        public java.util.Date unmarshal(String v) throws Exception {
            return new java.util.Date(Long.parseLong(v));
        }

        public String marshal(java.util.Date v) throws Exception {
            return Long.toString(v.getTime());
        }

    }
    
    public static final class SqlDateAdapter extends XmlAdapter<String, java.sql.Date>{

        public java.sql.Date unmarshal(String v) throws Exception {
            return java.sql.Date.valueOf(v);
        }

        public String marshal(java.sql.Date v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class SqlTimestampAdapter extends XmlAdapter<String, java.sql.Timestamp>{

        public java.sql.Timestamp unmarshal(String v) throws Exception {
            return new java.sql.Timestamp(Long.parseLong(v));
        }

        public String marshal(java.sql.Timestamp v) throws Exception {
            return Long.toString(v.getTime());
        }

    }
    
    public static final class LocalDateAdapter extends XmlAdapter<String, LocalDate>{

        public LocalDate unmarshal(String v) throws Exception {
            return LocalDate.parse(v);
        }

        public String marshal(LocalDate v) throws Exception {
            return v.toString();
        }

    }

    public static final class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime>{

        public LocalDateTime unmarshal(String v) throws Exception {
            return LocalDateTime.parse(v);
        }

        public String marshal(LocalDateTime v) throws Exception {
            return v.toString();
        }

    }

    public static final class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime>{

        public OffsetDateTime unmarshal(String v) throws Exception {
            return OffsetDateTime.parse(v);
        }

        public String marshal(OffsetDateTime v) throws Exception {
            return v.toString();
        }

    }
    
}
