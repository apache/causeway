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
package org.apache.causeway.valuetypes.jodatime.applib.value;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.lang.Nullable;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JodaTimeConverters {

    // -- INSTANT

    @Nullable public Instant toJoda(final @Nullable java.time.Instant input) {
        return input!=null
                ? new Instant(input.toEpochMilli())
                : null;
    }

    @Nullable public java.time.Instant fromJoda(final @Nullable Instant input) {
        return input!=null
                ? java.time.Instant.ofEpochMilli(input.getMillis())
                : null;
    }

    // -- LOCAL TIME

    @Nullable public LocalTime toJoda(final @Nullable java.time.LocalTime input) {
        return input!=null
                ? new LocalTime(
                        input.getHour(), input.getMinute(), input.getSecond(),
                        nanosToMillis(input.getNano()))
                : null;
    }

    @Nullable public java.time.LocalTime fromJoda(final @Nullable LocalTime input) {
        return input!=null
                ? java.time.LocalTime.of(
                        input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                        millisToNanos(input.getMillisOfSecond()))
                : null;
    }

    // -- LOCAL DATE

    @Nullable public LocalDate toJoda(final @Nullable java.time.LocalDate input) {
        return input!=null
                ? new LocalDate(
                        input.getYear(), input.getMonthValue(), input.getDayOfMonth())
                : null;
    }

    @Nullable public java.time.LocalDate fromJoda(final @Nullable LocalDate input) {
        return input!=null
                ? java.time.LocalDate.of(
                        input.getYear(), input.getMonthOfYear(), input.getDayOfMonth())
                : null;
    }

    // -- LOCAL DATE TIME

    @Nullable public LocalDateTime toJoda(final @Nullable java.time.LocalDateTime input) {
        return input!=null
                ? new LocalDateTime(
                        input.getYear(), input.getMonthValue(), input.getDayOfMonth(),
                        input.getHour(), input.getMinute(), input.getSecond(),
                        nanosToMillis(input.getNano()))
                : null;
    }

    @Nullable public java.time.LocalDateTime fromJoda(final @Nullable LocalDateTime input) {
        return input!=null
                ? java.time.LocalDateTime.of(
                        input.getYear(), input.getMonthOfYear(), input.getDayOfMonth(),
                        input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                        millisToNanos(input.getMillisOfSecond()))
                : null;
    }

    // -- DATE TIME WITH TIME ZONE DATA

    @Nullable public DateTime toJoda(final @Nullable java.time.ZonedDateTime input) {
        return input!=null
                ? new DateTime(
                        input.getYear(), input.getMonthValue(), input.getDayOfMonth(),
                        input.getHour(), input.getMinute(), input.getSecond(),
                        nanosToMillis(input.getNano()),
                        toJoda(input.getZone()))
                : null ;
    }

    @Nullable public java.time.ZonedDateTime fromJoda(final @Nullable DateTime input) {
        return input!=null
                ? java.time.ZonedDateTime.of(
                        input.getYear(), input.getMonthOfYear(), input.getDayOfMonth(),
                        input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                        millisToNanos(input.getMillisOfSecond()),
                        fromJoda(input.getZone()))
                : null;
    }

    // -- TIME ZONE

    @Nullable public DateTimeZone toJoda(final @Nullable java.time.ZoneId input) {
        return input!=null
                ? DateTimeZone.forTimeZone(java.util.TimeZone.getTimeZone(input))
                : null;
    }

    @Nullable public java.time.ZoneId fromJoda(final @Nullable DateTimeZone input) {
        return input!=null
                ? input.toTimeZone().toZoneId()
                : null;
    }

    // -- HELPER

    // private, as we don't, check any overflows or negative values
    private int nanosToMillis(final int nanos) {
        return nanos/1000_000;
    }

    // private, as we don't, check any overflows or negative values
    private int millisToNanos(final int millis) {
        return 1000_000 * millis;
    }

}
