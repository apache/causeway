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
package org.apache.causeway.commons.internal.base;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides time related functions.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public final class _Temporals {

    public final ZoneId UTC = ZoneId.of("UTC");

    /**
     * The default date/time format (seconds resolution): {@code 'yyyy-MM-dd HH:mm:ss'}.
     * As used for auditing, session-logging, etc.
     */
    public final DateTimeFormatter DEFAULT_LOCAL_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * The default date/time format (milliseconds resolution): {@code 'yyyy-MM-dd HH:mm:ss.SSS'}.
     * As used eg. for Xray.
     */
    public final DateTimeFormatter DEFAULT_LOCAL_DATETIME_FORMATTER_WITH_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Returns duration between {@code startedAt} and {@code completedAt} in <i>seconds</i>,
     * to 3 decimal places.
     * @implNote if {@code completedAt} is before {@code startedAt},
     * a negative value is returned.
     */
    public Optional<BigDecimal> secondsBetweenAsDecimal(
            final @Nullable Timestamp startedAt,
            final @Nullable Timestamp completedAt) {
        return startedAt!=null
                && completedAt!=null
                        ? Optional.of(millisToSeconds(completedAt.getTime() - startedAt.getTime()))
                        : Optional.empty();
    }

    // -- ZONE TRANSLATION

    /**
     * Translates given {@link Temporal} into provided {@link ZoneId} preserving the time instant.
     * If timeZone is absent, acts as a identity operator.
     * @param temporal - the {@link Temporal} to translate (required)
     * @param zoneId - target {@link ZoneId} (required)
     */
    public ZonedDateTime translateToTimeZone(final @NonNull ZonedDateTime temporal, final @NonNull ZoneId zoneId) {
        return temporal.withZoneSameInstant(zoneId);
    }

    /**
     * Translates given {@link Temporal} into provided {@link ZoneId} preserving the time instant.
     * If timeZone is absent, acts as a identity operator.
     * @param temporal - the {@link Temporal} to translate (required)
     * @param zoneId - target {@link ZoneId} (required)
     */
    public ZonedDateTime translateToTimeZone(final @NonNull OffsetDateTime temporal, final @NonNull ZoneId zoneId) {
        return ZonedDateTime.ofInstant(temporal.toInstant(), zoneId);
    }

    /**
     * Translates given {@link Temporal} into provided {@link ZoneId} preserving the time instant.
     * If timeZone is absent, acts as a identity operator.
     * @param temporal - the {@link Temporal} to translate (required)
     * @param offset - target {@link ZoneOffset} (required)
     */
    public OffsetTime translateToTimeOffset(final @NonNull OffsetTime temporal, final @NonNull ZoneOffset offset) {
        return temporal.withOffsetSameInstant(offset);
    }

    // -- TIME ZONE RENDERING

    public final DateTimeFormatter ISO_OFFSET_ONLY_FORMAT = new DateTimeFormatterBuilder()
            .appendOffsetId()
            .toFormatter(Locale.US); // arbitrarily picking a locale, just in case; (should have no effect)

    public final DateTimeFormatter DEFAULT_ZONEID_ONLY_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("VV")
            .toFormatter(Locale.US); // arbitrarily picking a locale, just in case; (should have no effect)

    public String formatZoneId(final ZoneId zoneId, final DateTimeFormatter zoneOnlyFormatter) {
        return zoneOnlyFormatter.format(ZonedDateTime.ofInstant(Instant.EPOCH, zoneId));
    }

    public String formatZoneId(final ZoneId zoneId) {
        return formatZoneId(zoneId, DEFAULT_ZONEID_ONLY_FORMAT);
    }

    // -- TEMPORAL TO STRING CONVERTERS

    private static final String FRACTIONAL_SECONDS_READ_PATTERN =
            "[.SSSSSSSSS][.SSSSSS][.SSS][.S]";

    private static final DateTimeFormatter OFFSETTIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS XXX");

    private static final DateTimeFormatter OFFSETTIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("HH:mm:ss" + FRACTIONAL_SECONDS_READ_PATTERN + "[ XXX]");

    private static final DateTimeFormatter OFFSETDATETIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS XXX");

    private static final DateTimeFormatter OFFSETDATETIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" + FRACTIONAL_SECONDS_READ_PATTERN + "[ XXX]");

    private static final DateTimeFormatter ZONEDDATETIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS VV");

    private static final DateTimeFormatter ZONEDDATETIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" + FRACTIONAL_SECONDS_READ_PATTERN + "[ VV]");


    /**
     * Returns a {@link String} representing given temporal suitable for the data-store.
     */
    @Nullable
    public String enstringOffsetTime(final @Nullable OffsetTime temporal) {
        return temporal != null
                ? temporal.format(OFFSETTIME_DATASTORE_FORMATTER)
                : null;
    }

    /**
     * Recovers a temporal from given {@link String}.
     */
    @Nullable
    public OffsetTime destringAsOffsetTime(final @Nullable String datastoreValue) {
        return _Strings.isNotEmpty(datastoreValue)
                ? hasZoneOrOffsetInfoWhenAssumingTimeOnly(datastoreValue)
                        ? OffsetTime.parse(datastoreValue, OFFSETTIME_DATASTORE_PARSER)
                        : OffsetTime.of(
                                LocalTime.parse(datastoreValue,
                                        OFFSETTIME_DATASTORE_PARSER), ZoneOffset.UTC)
                : null;
    }

    /**
     * Returns a {@link String} representing given temporal suitable for the data-store.
     */
    @Nullable
    public String enstringOffsetDateTime(final @Nullable OffsetDateTime temporal) {
        return temporal != null
                ? temporal.format(OFFSETDATETIME_DATASTORE_FORMATTER)
                : null;
    }

    /**
     * Recovers a temporal from given {@link String}.
     */
    @Nullable
    public OffsetDateTime destringAsOffsetDateTime(final @Nullable String datastoreValue) {
        return _Strings.isNotEmpty(datastoreValue)
                ? hasZoneOrOffsetInfoWhenAssumingDateAndTime(datastoreValue)
                        ? OffsetDateTime.parse(datastoreValue, OFFSETDATETIME_DATASTORE_PARSER)
                        : OffsetDateTime.of(
                                LocalDateTime.parse(datastoreValue,
                                        OFFSETDATETIME_DATASTORE_PARSER), ZoneOffset.UTC)
                : null;
    }

    /**
     * Returns a {@link String} representing given temporal suitable for the data-store.
     */
    @Nullable
    public String enstringZonedDateTime(final @Nullable ZonedDateTime temporal) {
        return temporal != null
                ? temporal.format(ZONEDDATETIME_DATASTORE_FORMATTER)
                : null;
    }

    /**
     * Recovers a temporal from given {@link String}.
     */
    @Nullable
    public ZonedDateTime destringAsZonedDateTime(final @Nullable String datastoreValue) {
        return _Strings.isNotEmpty(datastoreValue)
                ? hasZoneOrOffsetInfoWhenAssumingDateAndTime(datastoreValue)
                        ? ZonedDateTime.parse(datastoreValue, ZONEDDATETIME_DATASTORE_PARSER)
                        : ZonedDateTime.of(
                                LocalDateTime.parse(datastoreValue,
                                        ZONEDDATETIME_DATASTORE_PARSER), UTC)
                : null;
    }

    // -- ISO UTILS

    public static OffsetDateTime parseIsoDateTime(final String isoDateTime) {
        return DateTimeFormatter.ISO_DATE_TIME.parse(isoDateTime, OffsetDateTime::from);
    }

    // -- TEMPORAL SAMPLERS

    public Can<LocalDateTime> sampleLocalDateTime() {
        return Can.of(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(2).plusSeconds(15));
    }

    public Can<LocalDate> sampleLocalDate() {
        return Can.of(
                LocalDate.now(),
                LocalDate.now().plusDays(2));
    }

    public Can<LocalTime> sampleLocalTime() {
        return Can.of(
                LocalTime.now(),
                LocalTime.now().plusSeconds(15));
    }

    public Can<ZonedDateTime> sampleZonedDateTime() {
        // don't depend on current TimeZone.getDefault(),
        // instead use an arbitrary mix of fixed time-zone offsets Z, +02:00 and -02:00
        val localNow = LocalDateTime.now();
        return Can.of(
                ZonedDateTime.of(localNow, ZoneId.of("Europe/Paris")),
                ZonedDateTime.of(localNow, ZoneOffset.UTC),
                ZonedDateTime.of(localNow, ZoneId.of("UTC")),
                ZonedDateTime.of(localNow, ZoneOffset.ofHours(2)),
                ZonedDateTime.of(localNow, ZoneOffset.ofHours(-2)).plusDays(2).plusSeconds(15));
    }

    public Can<OffsetTime> sampleOffsetTime() {
        // don't depend on current TimeZone.getDefault(),
        // instead use an arbitrary mix of fixed time-zone offsets Z, +02:00 and -02:00
        val localNow = LocalTime.now();
        return Can.of(
                OffsetTime.of(localNow, ZoneOffset.UTC),
                OffsetTime.of(localNow, ZoneOffset.ofHours(2)),
                OffsetTime.of(localNow, ZoneOffset.ofHours(-2)).plusSeconds(15));
    }

    public Can<OffsetDateTime> sampleOffsetDateTime() {
        // don't depend on current TimeZone.getDefault(),
        // instead use an arbitrary mix of fixed time-zone offsets Z, +02:00 and -02:00
        val localNow = LocalDateTime.now();
        return Can.of(
                OffsetDateTime.of(localNow, ZoneOffset.UTC),
                OffsetDateTime.of(localNow, ZoneOffset.ofHours(2)),
                OffsetDateTime.of(localNow, ZoneOffset.ofHours(-2)).plusDays(2).plusSeconds(15));
    }

    // -- HELPER

    private BigDecimal millisToSeconds(final long millis) {
        return new BigDecimal(millis)
                .movePointLeft(3)
                .setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Whether the number of delimiting white-spaces hints at presence of zone/offset info.
     */
    private boolean hasZoneOrOffsetInfoWhenAssumingTimeOnly(final @NonNull String datastoreValue) {
        return delimitedChunksCount(datastoreValue)>1;
    }

    /**
     * Whether the number of delimiting white-spaces hints at presence of zone/offset info.
     */
    private boolean hasZoneOrOffsetInfoWhenAssumingDateAndTime(final @NonNull String datastoreValue) {
        return delimitedChunksCount(datastoreValue)>2;
    }

    private final Pattern DELIMITING_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private long delimitedChunksCount(final @NonNull String datastoreValue) {
        return _Strings.splitThenStream(datastoreValue, DELIMITING_WHITESPACE_PATTERN)
                .filter(_Strings::isNotEmpty)
                .count();
    }

}
