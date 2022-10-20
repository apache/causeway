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
package org.apache.causeway.core.metamodel.facets.value.temporal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.core.metamodel.valuesemantics.temporal.TemporalAdjust;

class TemporalAdjustTest {


    final TemporalAdjust plus1Year = TemporalAdjust.of(1, 0, 0, 0, 0);
    final TemporalAdjust minus1Year = TemporalAdjust.of(-1, 0, 0, 0, 0);

    final TemporalAdjust plus1Month = TemporalAdjust.of(0, 1, 0, 0, 0);
    final TemporalAdjust minus1Month = TemporalAdjust.of(0, -1, 0, 0, 0);

    final TemporalAdjust plus1Day = TemporalAdjust.of(0, 0, 1, 0, 0);
    final TemporalAdjust minus1Day = TemporalAdjust.of(0, 0, -1, 0, 0);

    final TemporalAdjust plus1Hour = TemporalAdjust.of(0, 0, 0, 1, 0);
    final TemporalAdjust minus1Hour = TemporalAdjust.of(0, 0, 0, -1, 0);

    final TemporalAdjust plus1Minute = TemporalAdjust.of(0, 0, 0, 0, 1);
    final TemporalAdjust minus1Minute = TemporalAdjust.of(0, 0, 0, 0, -1);

    // java.time
    final LocalTime localTime = LocalTime.of(9, 54, 1);
    final OffsetTime offsetTime = OffsetTime.of(9, 54, 1, 123_000_000, ZoneOffset.ofTotalSeconds(-120));
    final LocalDate localDate = LocalDate.of(2015, 5, 23);
    final LocalDateTime localDateTime = LocalDateTime.of(2015, 5, 23, 9, 54, 1);
    final OffsetDateTime offsetDateTime = OffsetDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(2015, 5, 23, 9, 54, 1, 0, ZoneOffset.UTC);


    private void assertNegates(final TemporalAdjust a, final TemporalAdjust b) {

        assertEquals(a, a.sign(1)); // identity

        assertEquals(a, b.sign(-1));
        assertEquals(a.sign(-1), b);
    }


    private void assertAdjust(final long amount, final ChronoUnit unit, final TemporalAdjust adjust) {

        // time only
        if(unit.isTimeBased()) {
            assertEquals(localTime.plus(amount, unit), adjust.adjustLocalTime(localTime));
            assertEquals(offsetTime.plus(amount, unit), adjust.adjustOffsetTime(offsetTime));
        } else {
            assertThrows(IllegalArgumentException.class, ()->adjust.adjustLocalTime(localTime));
            assertThrows(IllegalArgumentException.class, ()->adjust.adjustOffsetTime(offsetTime));
        }

        // date only
        if(unit.isDateBased()) {
            assertEquals(localDate.plus(amount, unit), adjust.adjustLocalDate(localDate));
        } else {
            assertThrows(IllegalArgumentException.class, ()->adjust.adjustLocalDate(localDate));
        }

        // data and time
        assertEquals(localDateTime.plus(amount, unit), adjust.adjustLocalDateTime(localDateTime));
        assertEquals(offsetDateTime.plus(amount, unit), adjust.adjustOffsetDateTime(offsetDateTime));
        assertEquals(zonedDateTime.plus(amount, unit), adjust.adjustZonedDateTime(zonedDateTime));
    }


    @Test
    void identityAndSign_shouldBeConsistent() {

        assertNegates(plus1Year, minus1Year);
        assertNegates(plus1Month, minus1Month);
        assertNegates(plus1Day, minus1Day);
        assertNegates(plus1Hour, minus1Hour);
        assertNegates(plus1Minute, minus1Minute);

    }

    @Test
    void adjusting_shouldBeConsistent() {

        assertAdjust(1, ChronoUnit.YEARS, plus1Year);
        assertAdjust(1, ChronoUnit.MONTHS, plus1Month);
        assertAdjust(1, ChronoUnit.DAYS, plus1Day);

        assertAdjust(1, ChronoUnit.HOURS, plus1Hour);
        assertAdjust(1, ChronoUnit.MINUTES, plus1Minute);

    }


}
