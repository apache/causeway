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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.Markup;

import lombok.val;

class JaxbAdaptersTest {

    private static class SampleValues {
        
        final String string = "Hello World!?{[()]}§$%&=´`*+~#',;.:-_|@€µ<>^°\"";
        final byte[] bytes = new byte[256];
        {
            for(int i=0; i<=255; ++i) {
                bytes[i]=(byte)i;
            }
        }
        
        final Markup markup = new Markup(string);
        
        final Blob blob = new Blob("sample", "text/plain", string.getBytes(StandardCharsets.UTF_8));
        final Clob clob = new Clob("sample", "text/plain", string);
                
        // java.time
        final LocalTime localTime = LocalTime.of(9, 54, 1);
        final OffsetTime offsetTime = OffsetTime.of(9, 54, 1, 123_000_000, ZoneOffset.ofTotalSeconds(-120));
        final LocalDate localDate = LocalDate.of(2015, 5, 23);
        final LocalDateTime localDateTime = LocalDateTime.of(2015, 5, 23, 9, 54, 1);
        final OffsetDateTime offsetDateTime = OffsetDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);

    }
    
    SampleValues sampleValues = new SampleValues();
    
    <T> void assertRoundtrip(XmlAdapter<String, T> xmlAdapter, T sampleValue) throws Exception {
        
        val xml = xmlAdapter.marshal(sampleValue);
        val recovered = xmlAdapter.unmarshal(xml);
        
        if(sampleValue instanceof byte[]) {
            Assertions.assertArrayEquals((byte[])sampleValue, (byte[])recovered);
        } else {
            Assertions.assertEquals(sampleValue, recovered);    
        }
        
        
        val nullXml = xmlAdapter.marshal(null);
        val nullRecovered = xmlAdapter.unmarshal(nullXml);
        
        Assertions.assertEquals(null, nullRecovered);
        
    }
    
    @Test
    void adapterRoundtrips_shouldBeNullsafeAndConsistent() throws Exception {
        
        assertRoundtrip(new JaxbAdapters.BytesAdapter(), sampleValues.bytes);
        assertRoundtrip(new JaxbAdapters.MarkupAdapter(), sampleValues.markup);
        
        assertRoundtrip(new JaxbAdapters.BlobAdapter(), sampleValues.blob);
        assertRoundtrip(new JaxbAdapters.ClobAdapter(), sampleValues.clob);
        
        assertRoundtrip(new JaxbAdapters.LocalTimeAdapter(), sampleValues.localTime);
        assertRoundtrip(new JaxbAdapters.OffsetTimeAdapter(), sampleValues.offsetTime);
        assertRoundtrip(new JaxbAdapters.LocalDateAdapter(), sampleValues.localDate);
        assertRoundtrip(new JaxbAdapters.LocalDateTimeAdapter(), sampleValues.localDateTime);
        assertRoundtrip(new JaxbAdapters.OffsetDateTimeAdapter(), sampleValues.offsetDateTime);
        assertRoundtrip(new JaxbAdapters.ZonedDateTimeAdapter(), sampleValues.zonedDateTime);
    }

}
