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
package org.apache.isis.valuetypes.jodatime.applib.value;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class JodaTimeConvertersTest {

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideAllZoneIds")
    void timeZone_Roundtrip(final String zoneIdName, final ZoneId zoneId) {

        final DateTimeZone jodaValue;

        try {
            jodaValue = JodaTimeConverters.toJoda(zoneId);
        } catch (Exception e) {
            fail(String.format("conversion failed for %s", zoneIdName), e);
            return;
        }

        final ZoneId recoveredValue = JodaTimeConverters.fromJoda(jodaValue);
        assertEquals(zoneId.getRules(), recoveredValue.getRules(), ()->String.format("rountrip failed for %s", zoneIdName));
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideAllZoneIds")
    void zonedDateFormat_roundtrip(final String zoneIdName, final ZoneId zoneId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss x");
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2021, 11, 24, 11, 13, 41, 0, zoneId);

        org.joda.time.DateTime jodaValue = JodaTimeConverters.toJoda(zonedDateTime);
        ZonedDateTime recoveredValue = JodaTimeConverters.fromJoda(jodaValue);

        assertEquals(zonedDateTime.format(formatter), recoveredValue.format(formatter));
    }

    @Test
    void zonedDateFormat_zeroOffset() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss x");
        ZonedDateTime zonedDateTime = formatter.parse("2021-11-24 11:13:41 +00", ZonedDateTime::from);

        org.joda.time.DateTime jodaValue = JodaTimeConverters.toJoda(zonedDateTime);
        ZonedDateTime recoveredValue = JodaTimeConverters.fromJoda(jodaValue);

        assertEquals(zonedDateTime.format(formatter), recoveredValue.format(formatter));
    }

    // -- TEST PARAMS

    static Stream<Arguments> provideAllZoneIds() {
        return ZoneId.getAvailableZoneIds().stream()
                .filter(zoneIdName->!zoneIdName.startsWith("SystemV/"))
                .filter(zoneIdName->!zoneIdName.equals("Pacific/Enderbury"))
                .map(ZoneId::of)
                .map(zoneId->Arguments.of(
                        zoneId.getId(), zoneId));
    }



}
