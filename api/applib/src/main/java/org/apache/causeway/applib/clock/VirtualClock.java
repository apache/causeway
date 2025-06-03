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

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;

/**
 * Works in connection with {@link InteractionService},
 * such that it allows an {@link Interaction}
 * to run with its own simulated (or actual) time.
 *
 * @see InteractionContext
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface VirtualClock extends Serializable {

    // -- INTERFACE

    /**
     * Returns the (virtual) time as an {@link Instant}.
     *
     * @apiNote This is a universal time difference, that does not depend on
     * where you are (eg. your current timezone), just on when you are.
     */
    Instant nowAsInstant();

    // -- FACTORIES

    /**
     * Returns the system's default ticking clock.
     */
    static VirtualClock system() {
        return new VirtualClock_system();
    }

    /**
     * Returns a ticking clock set to virtual time {@link Instant} {@code virtualNow}
     */
    static VirtualClock nowAt(final @NonNull Instant virtualNow) {
        // positive if the resulting clock is in the future
        var offsetMillis = ChronoUnit.MILLIS.between(Instant.now(), virtualNow);
        return new VirtualClock_withOffset(offsetMillis);
    }

    /**
     * Returns a ticking clock set to virtual time.
     */
    static VirtualClock nowAt(final java.time.@NonNull LocalDate virtualNow) {
        return nowAt(Instant.from(virtualNow.atStartOfDay().atZone(localTimeZone())));
    }

    static VirtualClock nowAt(final java.time.@NonNull LocalDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow.atZone(localTimeZone())));
    }

    static VirtualClock nowAt(final java.time.@NonNull OffsetDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow));
    }

    static VirtualClock nowAt(final java.time.@NonNull ZonedDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow));
    }

    static VirtualClock nowAt(final java.util.@NonNull Date virtualNow) {
        return nowAt(virtualNow.toInstant());
    }

    /**
     * Always returns the time {@link Instant} as given by {@code frozenAt}
     */
    static VirtualClock frozenAt(final @NonNull Instant frozenAt) {
        return new VirtualClock_frozen(frozenAt);
    }

    static VirtualClock frozenAt(final java.time.@NonNull LocalDate frozenAt) {
        return frozenAt(Instant.from(frozenAt.atStartOfDay(localTimeZone())));
    }

    static VirtualClock frozenAt(final java.time.@NonNull LocalDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt.atZone(localTimeZone())));
    }

    static VirtualClock frozenAt(final java.time.@NonNull OffsetDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt));
    }

    static VirtualClock frozenAt(final java.time.@NonNull ZonedDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt));
    }

    static VirtualClock frozenAt(final java.util.@NonNull Date frozenAt) {
        return frozenAt(frozenAt.toInstant());
    }

    /**
     * Always returns the time {@link Instant} 2003/8/17 21:30:25 (UTC)
     */
    static VirtualClock frozenTestClock() {
        var frozenAt = Instant.from(
                ZonedDateTime.of(2003, 7, 17, 21, 30, 25, 0, ZoneId.from(ZoneOffset.UTC)));
        return frozenAt(frozenAt);
    }

    // -- TIME ZONE

    /**
     * Used to interpret local time.
     * <p>
     * Returns {@link ZoneId#systemDefault()} .
     */
    static ZoneId localTimeZone() {
        return ZoneId.systemDefault();
    }

    // -- UTILITY

    /**
     * Returns the (virtual) time as the number of milliseconds since the epoch start.
     *
     * @apiNote This is a universal time difference, that does not depend on
     * where you are (eg your current timezone), just on when you are.
     *
     * @see #nowAsInstant()
     */
    default long nowAsEpochMilli() {
        return nowAsInstant().toEpochMilli();
    }

    /**
     * Returns the (virtual) time as {@link LocalDate}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     *
     * @see #nowAsInstant()
     * @see #nowAsLocalDate()
     */
    default LocalDate nowAsLocalDate(final @NonNull ZoneId zoneId) {
        return nowAsLocalDateTime(zoneId).toLocalDate();
    }

    /**
     * Returns the (virtual) time as {@link LocalDate}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @see #nowAsInstant()
     * @see #nowAsLocalDate(ZoneId)
     */
    default LocalDate nowAsLocalDate() {
        return nowAsLocalDate(localTimeZone());
    }

    /**
     * Returns the (virtual) time as {@link LocalDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     *
     * @see #nowAsInstant()
     * @see #nowAsLocalDateTime()
     */
    default LocalDateTime nowAsLocalDateTime(final @NonNull ZoneId zoneId) {
        return LocalDateTime.ofInstant(nowAsInstant(), zoneId);
    }

    /**
     * Returns the (virtual) time as {@link LocalDateTime}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @see #nowAsLocalDateTime(ZoneId)
     */
    default LocalDateTime nowAsLocalDateTime() {
        return nowAsLocalDateTime(localTimeZone());
    }

    /**
     * Returns the (virtual) time as {@link OffsetDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     *
     * @see #nowAsOffsetDateTime()
     */
    default OffsetDateTime nowAsOffsetDateTime(final @NonNull ZoneId zoneId) {
        return OffsetDateTime.ofInstant(nowAsInstant(), zoneId);
    }

    /**
     * Returns the (virtual) time as {@link OffsetDateTime}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @see #nowAsOffsetDateTime(ZoneId)
     */
    default OffsetDateTime nowAsOffsetDateTime() {
        return nowAsOffsetDateTime(localTimeZone());
    }

    /**
     * Returns the (virtual)time as {@link java.util.Date}.
     */
    default java.util.Date nowAsJavaUtilDate() {
        return new java.util.Date(nowAsEpochMilli());
    }

    /**
     * Returns the (virtual) time as {@link java.sql.Timestamp}.
     */
    default java.sql.Timestamp nowAsJavaSqlTimestamp() {
        return new java.sql.Timestamp(nowAsEpochMilli());
    }

    /**
     * Returns the (virtual) time as {@link XMLGregorianCalendar}.
     */
    default XMLGregorianCalendar nowAsXmlGregorianCalendar() {
        return JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(nowAsJavaSqlTimestamp());
    }

}
