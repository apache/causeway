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
package org.apache.causeway.applib.clock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualClock_Test {

    private VirtualClock virtualClock;

    @BeforeEach
    void setup() {
        virtualClock = VirtualClock.frozenTestClock();
    }

    // -- FACTORIES (TICKING)

    final static long TOLERANCE_SECONDS = 2;

    @Test
    void nowAtLocalDate() {
        final java.time.LocalDate virtualNow = java.time.LocalDate.of(2014, 5, 18);
        assertTimeEquals(
                VirtualClock.nowAt(virtualNow),
                virtualNow.atStartOfDay().atZone(VirtualClock.localTimeZone()).toInstant(),
                TOLERANCE_SECONDS);
    }

    @Test
    void nowAtLocalDateTime() {
        final java.time.LocalDateTime virtualNow = java.time.LocalDateTime.of(2014, 5, 18, 7, 15);
        assertTimeEquals(
                VirtualClock.nowAt(virtualNow),
                virtualNow.atZone(VirtualClock.localTimeZone()).toInstant(),
                TOLERANCE_SECONDS);
    }

    @Test
    void nowAtOffsetDateTime() {
        final java.time.OffsetDateTime virtualNow =
                java.time.OffsetDateTime.of(2014, 5, 18, 7, 15, 0, 0, ZoneOffset.ofHours(-3));
        assertTimeEquals(
                VirtualClock.nowAt(virtualNow),
                virtualNow.toInstant(),
                TOLERANCE_SECONDS);
    }

    @Test
    void nowAtZonedDateTime() {
        final java.time.ZonedDateTime virtualNow =
                java.time.ZonedDateTime.of(2014, 5, 18, 7, 15, 0, 0, ZoneOffset.ofHours(-3));
        assertTimeEquals(
                VirtualClock.nowAt(virtualNow),
                virtualNow.toInstant(),
                TOLERANCE_SECONDS);
    }

    @Test
    void nowAtDate() {
        @SuppressWarnings("deprecation")
        final java.util.Date virtualNow = new java.util.Date(2014-1900, 5-1, 18);
        assertTimeEquals(
                VirtualClock.nowAt(virtualNow),
                Instant.ofEpochMilli(virtualNow.getTime()),
                TOLERANCE_SECONDS);
    }

    // -- FACTORIES (FROZEN)

    @Test
    void frozenAtLocalDate() {
        final java.time.LocalDate virtualNow = java.time.LocalDate.of(2014, 5, 18);
        assertTimeEquals(
                VirtualClock.frozenAt(virtualNow),
                virtualNow.atStartOfDay().atZone(VirtualClock.localTimeZone()).toInstant());
    }

    @Test
    void frozenAtLocalDateTime() {
        final java.time.LocalDateTime virtualNow = java.time.LocalDateTime.of(2014, 5, 18, 7, 15);
        assertTimeEquals(
                VirtualClock.frozenAt(virtualNow),
                virtualNow.atZone(VirtualClock.localTimeZone()).toInstant());
    }

    @Test
    void frozenAtOffsetDateTime() {
        final java.time.OffsetDateTime virtualNow =
                java.time.OffsetDateTime.of(2014, 5, 18, 7, 15, 0, 0, ZoneOffset.ofHours(-3));
        assertTimeEquals(
                VirtualClock.frozenAt(virtualNow),
                virtualNow.toInstant());
    }

    @Test
    void frozenAtZonedDateTime() {
        final java.time.ZonedDateTime virtualNow =
                java.time.ZonedDateTime.of(2014, 5, 18, 7, 15, 0, 0, ZoneOffset.ofHours(-3));
        assertTimeEquals(
                VirtualClock.frozenAt(virtualNow),
                virtualNow.toInstant());
    }

    @Test
    void frozenAtDate() {
        @SuppressWarnings("deprecation")
        final java.util.Date virtualNow = new java.util.Date(2014-1900, 5-1, 18);
        assertTimeEquals(
                VirtualClock.frozenAt(virtualNow),
                Instant.ofEpochMilli(virtualNow.getTime()));
    }


    // -- QUERIES

    @Test
    void nowAsInstant() {
        assertThat(virtualClock.nowAsInstant()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    void nowAsEpochMilli() {
        assertThat(virtualClock.nowAsEpochMilli()).isEqualTo(1058477425000L);
    }

    @Test
    void nowAsLocalDate() {
        assertThat(virtualClock.nowAsLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

    @Test
    void nowAsLocalDateTime() {
        assertThat(virtualClock.nowAsLocalDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25");
    }

    @Test
    void nowAsOffsetDateTime() {
        assertThat(virtualClock.nowAsOffsetDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaUtilDate() {
        assertThat(virtualClock.nowAsJavaUtilDate().toString()).isEqualTo("Thu Jul 17 22:30:25 BST 2003");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaSqlTimestamp() {
        assertThat(virtualClock.nowAsJavaSqlTimestamp().toString()).isEqualTo("2003-07-17 22:30:25.0");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsXmlGregorianCalendar() {
        assertThat(virtualClock.nowAsXmlGregorianCalendar().toString()).isEqualTo("2003-07-17T22:30:25.000+01:00");
    }

    @Test
    void nowAsJodaDateTime() {
        assertThat(virtualClock.nowAsJodaDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25.000Z");
    }

    @Test
    void nowAsJodaLocalDate() {
        assertThat(virtualClock.nowAsJodaLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

    // -- HELPER

    static void assertTimeEquals(final VirtualClock virtualClock, final Instant expectedInstant) {
        assertEquals(virtualClock.nowAsInstant(), expectedInstant);
    }

    private void assertTimeEquals(final VirtualClock virtualClock,
            final Instant expectedInstant, final long toleranceSeconds) {
        final long deltaSeconds = Math.abs(
                virtualClock.nowAsInstant().getEpochSecond()
                - expectedInstant.getEpochSecond());
        assertTrue(deltaSeconds <= toleranceSeconds);
    }



}