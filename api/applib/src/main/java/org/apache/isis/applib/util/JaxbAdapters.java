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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Base64;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._Strings;

/**
 * Provides JAXB XmlAdapters for Java built-in temporal types. 
 * Others types might be added, if convenient. 
 * <p>
 * 
 * Example:<pre>
 * &#64;XmlElement &#64;XmlJavaTypeAdapter(JaxbAdapters.LocalDateAdapter.class)
 * &#64;Getter &#64;Setter private LocalDate javaLocalDate;
 * </pre>
 * 
 *  
 * @since 2.0
 */
public final class JaxbAdapters {

    // -- MARKUP

    public static final class MarkupAdapter extends XmlAdapter<String, Markup> {

        private final static Base64.Encoder encoder = Base64.getEncoder(); 
        private final static Base64.Decoder decoder = Base64.getDecoder();

        @Override
        public Markup unmarshal(String v) throws Exception {
            if(v==null) {
                return null;
            }
            final String html = _Strings.ofBytes(decoder.decode(v), StandardCharsets.UTF_8);
            return new Markup(html);
        }

        @Override
        public String marshal(Markup v) throws Exception {
            if(v==null) {
                return null;
            }
            final String html = v.asString();
            return encoder.encodeToString(_Strings.toBytes(html, StandardCharsets.UTF_8));
        }

    }

    // -- BLOB

    public static final class BlobAdapter extends XmlAdapter<String, Blob> {
        // copy pasted code from BlobValueSemanticsProvider

        @Override
        public Blob unmarshal(String data) throws Exception {
            if(data==null) {
                return null;
            }
            final int colonIdx = data.indexOf(':');
            final String name  = data.substring(0, colonIdx);
            final int colon2Idx  = data.indexOf(":", colonIdx+1);
            final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
            final String payload = data.substring(colon2Idx+1);
            final byte[] bytes = _Bytes.decodeBase64(Base64.getDecoder(), payload.getBytes(StandardCharsets.UTF_8));
            try {
                return new Blob(name, new MimeType(mimeTypeBase), bytes);
            } catch (MimeTypeParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String marshal(Blob blob) throws Exception {
            if(blob==null) {
                return null;
            }
            return blob.getName() + ":" + 
            blob.getMimeType().getBaseType() + ":" + 
            _Strings.ofBytes(_Bytes.encodeToBase64(Base64.getEncoder(), blob.getBytes()), 
                    StandardCharsets.UTF_8);
        }

    }


    // -- TEMPORAL VALUE TYPES

    public static final class DateAdapter extends XmlAdapter<String, java.util.Date> {

        @Override
        public java.util.Date unmarshal(String v) throws Exception {
            return new java.util.Date(Long.parseLong(v));
        }

        @Override
        public String marshal(java.util.Date v) throws Exception {
            return Long.toString(v.getTime());
        }

    }

    public static final class SqlDateAdapter extends XmlAdapter<String, java.sql.Date> {

        @Override
        public java.sql.Date unmarshal(String v) throws Exception {
            return java.sql.Date.valueOf(v);
        }

        @Override
        public String marshal(java.sql.Date v) throws Exception {
            return v.toString();
        }

    }

    public static final class SqlTimestampAdapter extends XmlAdapter<String, java.sql.Timestamp> {

        @Override
        public java.sql.Timestamp unmarshal(String v) throws Exception {
            return new java.sql.Timestamp(Long.parseLong(v));
        }

        @Override
        public String marshal(java.sql.Timestamp v) throws Exception {
            return Long.toString(v.getTime());
        }

    }

    public static final class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

        @Override
        public LocalTime unmarshal(String v) throws Exception {
            return LocalTime.parse(v);
        }

        @Override
        public String marshal(LocalTime v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

        @Override
        public LocalDate unmarshal(String v) throws Exception {
            return LocalDate.parse(v);
        }

        @Override
        public String marshal(LocalDate v) throws Exception {
            return v.toString();
        }

    }

    public static final class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

        @Override
        public LocalDateTime unmarshal(String v) throws Exception {
            return LocalDateTime.parse(v);
        }

        @Override
        public String marshal(LocalDateTime v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class OffsetTimeAdapter extends XmlAdapter<String, OffsetTime> {

        @Override
        public OffsetTime unmarshal(String v) throws Exception {
            return OffsetTime.parse(v);
        }

        @Override
        public String marshal(OffsetTime v) throws Exception {
            return v.toString();
        }

    }

    public static final class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

        @Override
        public OffsetDateTime unmarshal(String v) throws Exception {
            return OffsetDateTime.parse(v);
        }

        @Override
        public String marshal(OffsetDateTime v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

        @Override
        public ZonedDateTime unmarshal(String v) throws Exception {
            return ZonedDateTime.parse(v);
        }

        @Override
        public String marshal(ZonedDateTime v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class DurationAdapter extends XmlAdapter<String, Duration> {

        @Override
        public Duration unmarshal(String v) throws Exception {
            return Duration.parse(v);
        }

        @Override
        public String marshal(Duration v) throws Exception {
            return v.toString();
        }

    }
    
    public static final class PeriodAdapter extends XmlAdapter<String, Period> {

        @Override
        public Period unmarshal(String v) throws Exception {
            return Period.parse(v);
        }

        @Override
        public String marshal(Period v) throws Exception {
            return v.toString();
        }

    }
    
    

}
