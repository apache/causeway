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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.apache.isis.applib.services.iactn.Interaction;

import lombok.NonNull;

/**
 * Works in connection with {@link InteractionFactory}, such that it allows an {@link Interaction}
 * to run with its own simulated (or actual) time. 
 * <p>
 * Relates to {@link VirtualContext}
 * 
 * @since 2.0
 *
 */
@FunctionalInterface
public interface VirtualClock extends Serializable {
    
    // -- INTERFACE
    
    /**
     * Returns the (virtual) time as an {@link Instant}.
     * 
     * @apiNote This is a universal time difference, that does not depend on 
     * where you are (eg. your current timezone), just on when you are. 
     *
     * @see {@link Instant}
     */
    Instant now();
    
    // -- FACTORIES
    
    static VirtualClock system() {
        return Instant::now;
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
    default LocalDate getTimeAsLocalDate(final @NonNull ZoneId zoneId) {
        return getTimeAsLocalDateTime(zoneId).toLocalDate();
    }

    /**
     * Returns the (virtual) time as {@link LocalDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     */
    default LocalDateTime getTimeAsLocalDateTime(final @NonNull ZoneId zoneId) {
        return LocalDateTime.ofInstant(now(), zoneId);
    }

    /**
     * Returns the (virtual) time as {@link OffsetDateTime}, using the {@link ZoneId} timezone.
     * @param zoneId - the time-zone, which may be an offset, not null
     */
    default OffsetDateTime getTimeAsOffsetDateTime(final @NonNull ZoneId zoneId) {
        return OffsetDateTime.ofInstant(now(), zoneId);
    }

    default Timestamp getTimeAsJavaSqlTimestamp() {
        return new java.sql.Timestamp(getEpochMillis());
    }

    /**
     * Returns the time as a Joda {@link DateTime},
     * using the {@link ZoneId#systemDefault() system default} timezone.
     * @deprecated please migrate to java.time.* TODO provide a compatibility layer?
     */
    @Deprecated
    default DateTime getTimeAsJodaDateTime() {
        final ZoneId zoneId = ZoneId.systemDefault();
        return new DateTime(getEpochMillis(), DateTimeZone.forID(zoneId.getId()));
    }
}
