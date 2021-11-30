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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JodaTimeConverters {

    // -- INSTANT

    public Instant toJoda(final java.time.Instant input) {
        return new Instant(input.toEpochMilli());
    }

    public java.time.Instant fromJoda(final Instant input) {
        return java.time.Instant.ofEpochMilli(input.getMillis());
    }

    // -- LOCAL TIME

    public LocalTime toJoda(final java.time.LocalTime input) {
        return new LocalTime(
                input.getHour(), input.getMinute(), input.getSecond(),
                nanosToMillis(input.getNano()));
    }

    public java.time.LocalTime fromJoda(final LocalTime input) {
        return java.time.LocalTime.of(
                input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                millisToNanos(input.getMillisOfSecond()));
    }

    // -- LOCAL DATE

    public LocalDate toJoda(final java.time.LocalDate input) {
        return new LocalDate(
                input.getYear(), input.getMonthValue(), input.getDayOfMonth());
    }

    public java.time.LocalDate fromJoda(final LocalDate input) {
        return java.time.LocalDate.of(
                input.getYear(), input.getMonthOfYear(), input.getDayOfMonth());
    }

    // -- LOCAL DATE TIME

    public LocalDateTime toJoda(final java.time.LocalDateTime input) {
        return new LocalDateTime(
                input.getYear(), input.getMonthValue(), input.getDayOfMonth(),
                input.getHour(), input.getMinute(), input.getSecond(),
                nanosToMillis(input.getNano()));
    }

    public java.time.LocalDateTime fromJoda(final LocalDateTime input) {
        return java.time.LocalDateTime.of(
                input.getYear(), input.getMonthOfYear(), input.getDayOfMonth(),
                input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                millisToNanos(input.getMillisOfSecond()));
    }

    // -- DATE TIME WITH TIME ZONE DATA

    public DateTime toJoda(final java.time.ZonedDateTime input) {
        return new DateTime(
                input.getYear(), input.getMonthValue(), input.getDayOfMonth(),
                input.getHour(), input.getMinute(), input.getSecond(),
                nanosToMillis(input.getNano()),
                toJoda(input.getZone()));
    }

    public java.time.ZonedDateTime fromJoda(final DateTime input) {
        return java.time.ZonedDateTime.of(
                input.getYear(), input.getMonthOfYear(), input.getDayOfMonth(),
                input.getHourOfDay(), input.getMinuteOfHour(), input.getSecondOfMinute(),
                millisToNanos(input.getMillisOfSecond()),
                fromJoda(input.getZone()));
    }

    // -- TIME ZONE

    public DateTimeZone toJoda(final java.time.ZoneId input) {
        return DateTimeZone.forTimeZone(java.util.TimeZone.getTimeZone(input));
    }

    public java.time.ZoneId fromJoda(final DateTimeZone input) {
        return input.toTimeZone().toZoneId();
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
