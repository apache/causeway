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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;

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
    static VirtualClock nowAt(@NonNull final Instant virtualNow) {
        // positive if the resulting clock is in the future
        val offsetMillis = ChronoUnit.MILLIS.between(Instant.now(), virtualNow);
        return new VirtualClock_withOffset(offsetMillis);
    }

    /**
     * Returns a ticking clock set to virtual time.
     */
    static VirtualClock nowAt(@NonNull final java.time.LocalDate virtualNow) {
        return nowAt(Instant.from(virtualNow.atStartOfDay().atZone(localTimeZone())));
    }

    static VirtualClock nowAt(@NonNull final java.time.LocalDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow.atZone(localTimeZone())));
    }

    static VirtualClock nowAt(@NonNull final java.time.OffsetDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow));
    }

    static VirtualClock nowAt(@NonNull final java.time.ZonedDateTime virtualNow) {
        return nowAt(Instant.from(virtualNow));
    }

    static VirtualClock nowAt(@NonNull final java.util.Date virtualNow) {
        return nowAt(virtualNow.toInstant());
    }

    /**
     * @deprecated convert use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock nowAt(@NonNull final org.joda.time.LocalDate virtualNow) {
        return nowAt(virtualNow.toDate());
    }

    /**
     * @deprecated convert use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock nowAt(@NonNull final org.joda.time.LocalDateTime virtualNow) {
        return nowAt(virtualNow.toDate());
    }

    /**
     * @deprecated convert use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock nowAt(@NonNull final org.joda.time.DateTime virtualNow) {
        return nowAt(virtualNow.toDate());
    }


    /**
     * Always returns the time {@link Instant} as given by {@code frozenAt}
     */
    static VirtualClock frozenAt(@NonNull final Instant frozenAt) {
        return new VirtualClock_frozen(frozenAt);
    }

    static VirtualClock frozenAt(@NonNull final java.time.LocalDate frozenAt) {
        return frozenAt(Instant.from(frozenAt.atStartOfDay(localTimeZone())));
    }

    static VirtualClock frozenAt(@NonNull final java.time.LocalDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt.atZone(localTimeZone())));
    }

    static VirtualClock frozenAt(@NonNull final java.time.OffsetDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt));
    }

    static VirtualClock frozenAt(@NonNull final java.time.ZonedDateTime frozenAt) {
        return frozenAt(Instant.from(frozenAt));
    }

    static VirtualClock frozenAt(@NonNull final java.util.Date frozenAt) {
        return frozenAt(frozenAt.toInstant());
    }

    /**
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock frozenAt(@NonNull final org.joda.time.LocalDate frozenAt) {
        return frozenAt(frozenAt.toDate());
    }

    /**
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock frozenAt(@NonNull final org.joda.time.LocalDateTime frozenAt) {
        return frozenAt(frozenAt.toDate());
    }

    /**
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    static VirtualClock frozenAt(@NonNull final org.joda.time.DateTime frozenAt) {
        return frozenAt(frozenAt.toDate());
    }


    /**
     * Always returns the time {@link Instant} 2003/8/17 21:30:25 (UTC)
     */
    static VirtualClock frozenTestClock() {
        val frozenAt = Instant.from(
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


    /**
     * Returns the time as a Joda {@link org.joda.time.DateTime}, using the specified {@link ZoneId} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaDateTime()
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.DateTime nowAsJodaDateTime(final @NonNull ZoneId zoneId) {
        return new org.joda.time.DateTime(nowAsEpochMilli(), DateTimeZone.forID(zoneId.getId()));
    }

    /**
     * Returns the time as a Joda {@link org.joda.time.DateTime}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaDateTime(ZoneId)
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.DateTime nowAsJodaDateTime() {
        return nowAsJodaDateTime(localTimeZone());
    }

    /**
     * Returns the time as a Joda {@link org.joda.time.LocalDateTime}, using the specified {@link ZoneId} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaDateTime()
     *//**
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.LocalDateTime nowAsJodaLocalDateTime(final @NonNull ZoneId zoneId) {
        return nowAsJodaDateTime(zoneId).toLocalDateTime();
    }

    /**
     * Returns the time as a Joda {@link org.joda.time.LocalDateTime}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaDateTime(ZoneId)
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.LocalDateTime nowAsJodaLocalDateTime() {
        return nowAsJodaDateTime().toLocalDateTime();
    }

    /**
     * Returns the time as a Joda {@link DateTime}, using the specified {@link ZoneId} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaDateTime()
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.LocalDate nowAsJodaLocalDate(final @NonNull ZoneId zoneId) {
        return new org.joda.time.LocalDate(nowAsEpochMilli(), DateTimeZone.forID(zoneId.getId()));
    }

    /**
     * Returns the time as a Joda {@link DateTime}, using the {@link ZoneId#systemDefault() system default} timezone.
     *
     * @apiNote - we recommend migrating to java.time.*, however this API is not (for the moment) deprecated.
     *
     * @see #nowAsJodaLocalDate(ZoneId)
     * @deprecated use java.time variant instead (Joda Time is deprecated)
     */
    @Deprecated // forRemoval=? ideally applib should no longer depend on joda.time, use converters instead
    default org.joda.time.LocalDate nowAsJodaLocalDate() {
        val zoneId = localTimeZone();
        return nowAsJodaLocalDate(zoneId);
    }

}
