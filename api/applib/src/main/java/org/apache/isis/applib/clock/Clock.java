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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import lombok.val;

/**
 * Provides a mechanism to get (and possible to set) the current time.
 *
 * <p>
 * The clock is used primarily by the temporal value classes, and is accessed by
 * the NOF as a singleton. The actual implementation used can be configured at
 * startup, but once specified the clock instance cannot be changed.
 *
 * <p>
 * Unless another {@link Clock} implementation has been installed, the first
 * call to {@link #getInstance()} will instantiate an implementation that just
 * uses the system's own clock. Alternate implementations can be created via
 * suitable subclasses, but this must be done <b><i>before</i></b> the first
 * call to {@link #getInstance()}. 
 */
public abstract class Clock {
    
    protected static Clock instance;

    /**
     * Returns the (singleton) instance of {@link Clock}.
     *
     * <p>
     * Unless it has been otherwise created, will lazily instantiate an
     * implementation that just delegate to the computer's own system clock (as
     * per {@link System#currentTimeMillis()}.
     *
     * @return
     */
    public static final Clock getInstance() {
        if (!isInitialized()) {
            instance = new SystemClock();
        }
        return instance;
    }

    /**
     * Whether has been initialized or not.
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * The time as the number of milliseconds since the epoch start. (UTC)
     *
     * @see Date#getTime()
     */
    public static long getEpochMillis() {
        return getInstance().now().toEpochMilli();
    }

    public static LocalDate getTimeAsLocalDate() {
        return getTimeAsLocalDateTime().toLocalDate();
    }

    public static LocalDateTime getTimeAsLocalDateTime() {
        val zoneId = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(getInstance().now(), zoneId);
    }

    /**
     * Returns the time as {@link OffsetDateTime},
     * using the {@link ZoneId#systemDefault() system default} timezone.
     */
    public static OffsetDateTime getTimeAsOffsetDateTime() {
        val zoneId = ZoneId.systemDefault();
        return OffsetDateTime.ofInstant(getInstance().now(), zoneId);
    }


    public static Timestamp getTimeAsJavaSqlTimestamp() {
        return new java.sql.Timestamp(getEpochMillis());
    }

    /**
     * Returns the time as a Joda {@link DateTime},
     * using the {@link ZoneId#systemDefault() system default} timezone.
     */
    public static DateTime getTimeAsJodaDateTime() {
        final ZoneId zoneId = ZoneId.systemDefault();
        return new DateTime(getInstance().now().toEpochMilli(), DateTimeZone.forID(zoneId.getId()));
    }

    /**
     * Allows subclasses to remove their implementation.
     *
     * @return whether a clock was removed.
     */
    protected static boolean remove() {
        if (instance == null) {
            return false;
        }
        instance = null;
        return true;
    }

    protected Clock() {
        instance = this;
    }


    /**
     * The current time represented by an instant, either measured or simulated.
     */
    protected abstract Instant now();


}

final class SystemClock extends Clock {

    SystemClock() {}
    
    @Override
    protected Instant now() {
        return Instant.now();
    }

}
