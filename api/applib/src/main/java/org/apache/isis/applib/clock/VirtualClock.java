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
package org.apache.isis.applib.clock;

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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;

import lombok.NonNull;
import lombok.val;

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
    Instant now();

    // -- FACTORIES

    /**
     * Returns a the system's default ticking clock.
     */
    static VirtualClock system() {
        return new VirtualClock_system();
    }

    /**
     * Returns a ticking clock set to virtual time {@link Instant} {@code virtualNow}
     */
    static VirtualClock nowAt(@NonNull Instant virtualNow) {
        // positive if the resulting clock is in the future
        val offsetMillis = ChronoUnit.MILLIS.between(Instant.now(), virtualNow);
        return new VirtualClock_withOffset(offsetMillis);
    }

    /**
     * Always returns the time {@link Instant} as given by {@code frozenAt}
     */
    static VirtualClock frozenAt(@NonNull Instant frozenAt) {
        return new VirtualClock_frozen(frozenAt);
    }

    /**
     * Always returns the time {@link Instant} 2003/8/17 21:30:25 (UTC)
     */
    static VirtualClock frozenTestClock() {
        val frozenAt = Instant.from(
                ZonedDateTime.of(2003, 7, 17, 21, 30, 25, 0, ZoneId.from(ZoneOffset.UTC)));
        return frozenAt(frozenAt);
    }

    // -- UTILITY

    /**
     * Returns the (virtual) time as the number of milliseconds since the epoch start.
     *
     * @apiNote This is a universal time difference, that does not depend on
     * where you are (eg. your current timezone), just on when you are.
     *
     * @see {@link Instant}
     */
    default long getEpochMillis() {
        return now().toEpochMilli();
    }

    /**
     * Returns the (virtual) time as {@link LocalDate}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     */
    default LocalDate localDate(final @NonNull ZoneId zoneId) {
        return localDateTime(zoneId).toLocalDate();
    }

    /**
     * Returns the (virtual) time as {@link LocalDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     */
    default LocalDateTime localDateTime(final @NonNull ZoneId zoneId) {
        return LocalDateTime.ofInstant(now(), zoneId);
    }

    /**
     * Returns the (virtual) time as {@link OffsetDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     */
    default OffsetDateTime offsetDateTime(final @NonNull ZoneId zoneId) {
        return OffsetDateTime.ofInstant(now(), zoneId);
    }

    default java.util.Date javaUtilDate() {
        return new java.util.Date(getEpochMillis());
    }

    default java.sql.Timestamp javaSqlTimestamp() {
        return new java.sql.Timestamp(getEpochMillis());
    }

    default XMLGregorianCalendar xmlGregorianCalendar() {
        return JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(javaSqlTimestamp());
    }

    // -- DEPRECATIONS

    /**
     * Returns the time as a Joda {@link org.joda.time.DateTime},
     * using the {@link ZoneId#systemDefault() system default} timezone.
     * @deprecated please migrate to java.time.*
     */
    @Deprecated
    default org.joda.time.DateTime asJodaDateTime(final @NonNull ZoneId zoneId) {
        return new org.joda.time.DateTime(getEpochMillis(), DateTimeZone.forID(zoneId.getId()));
    }

    /**
     * Returns the time as a Joda {@link DateTime},
     * using the {@link ZoneId#systemDefault() system default} timezone.
     * @deprecated please migrate to java.time.*
     */
    @Deprecated
    default org.joda.time.LocalDate asJodaLocalDate(final @NonNull ZoneId zoneId) {
        return new org.joda.time.LocalDate(getEpochMillis(), DateTimeZone.forID(zoneId.getId()));
    }




}
