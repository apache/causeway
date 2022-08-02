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
package org.apache.isis.commons.internal.base;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.lang.Nullable;

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

    /**
     * The default date/time format (seconds resolution): {@code 'yyyy-MM-dd HH:mm:ss'}.
     * As used for auditing, session-logging, etc.
     */
    public static final DateTimeFormatter DEFAULT_LOCAL_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * The default date/time format (milliseconds resolution): {@code 'yyyy-MM-dd HH:mm:ss.SSS'}.
     * As used eg. for Xray.
     */
    public static final DateTimeFormatter DEFAULT_LOCAL_DATETIME_FORMATTER_WITH_MILLIS =
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

    // -- TEMPORAL TO STRING CONVERTERS

    private static final DateTimeFormatter OFFSETTIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS XXX");

    private static final DateTimeFormatter OFFSETTIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("HH:mm:ss[.SSSSSSSSS][ XXX]");

    private static final DateTimeFormatter OFFSETDATETIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS XXX");

    private static final DateTimeFormatter OFFSETDATETIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSSSSS][ XXX]");

    private static final DateTimeFormatter ZONEDDATETIME_DATASTORE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS VV");

    private static final DateTimeFormatter ZONEDDATETIME_DATASTORE_PARSER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSSSSS][ VV]");


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
                ? OffsetTime.parse(datastoreValue, OFFSETTIME_DATASTORE_PARSER)
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
                ? OffsetDateTime.parse(datastoreValue, OFFSETDATETIME_DATASTORE_PARSER)
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
                ? ZonedDateTime.parse(datastoreValue, ZONEDDATETIME_DATASTORE_PARSER)
                : null;
    }


    // -- HELPER

    private BigDecimal millisToSeconds(final long millis) {
        return new BigDecimal(millis)
                .movePointLeft(3)
                .setScale(3, RoundingMode.HALF_EVEN);
    }

}
