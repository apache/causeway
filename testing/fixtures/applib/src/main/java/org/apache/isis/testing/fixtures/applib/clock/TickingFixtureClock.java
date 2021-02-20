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
package org.apache.isis.testing.fixtures.applib.services.clock;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public class TickingFixtureClock extends Clock {
    private static final TimeZone UTC_TIME_ZONE;

    static {
        TimeZone tempTimeZone = TimeZone.getTimeZone("Etc/UTC");
        if (tempTimeZone == null) {
            tempTimeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = tempTimeZone;
    }


    static Clock existingInstance;

    /**
     * Configures the system to use a FixtureClock rather than the in-built
     * system clock. Can be called multiple times.
     *
     * <p>
     * Must call before any other call to {@link Clock#getInstance()}.
     *
     * @throws IllegalStateException
     *             if Clock singleton already initialized with some other
     *             implementation.
     */
    public synchronized static TickingFixtureClock replaceExisting() {
        final Clock instance = getInstance();
        if (instance instanceof TickingFixtureClock) {
            return (TickingFixtureClock) instance;
        }

        final long time = Clock.getEpochMillis();
        existingInstance = Clock.instance;

        // installs as the singleton
        Clock.remove();

        return new TickingFixtureClock(time);
    }

    /**
     * Makes {@link Clock#remove()} visible.
     */
    public static boolean reinstateExisting() {
        Clock.instance = existingInstance;
        return true;
    }



    private final Calendar calendar = Calendar.getInstance();
    private long t0 = 0L;

    private TickingFixtureClock(final long time) {
        calendar.setTimeZone(UTC_TIME_ZONE);
        calendar.setTimeInMillis(time);

        t0 = System.currentTimeMillis();
    }

    private long getOffset() {
        return System.currentTimeMillis() - t0;
    }


    /**
     * Access via {@link Clock#getTime()}.
     *
     * <p>
     * Will just return the system time until {@link #setDate(int, int, int)} or
     * {@link #setTime(int, int)} (or one of the overloads) has been called.
     */
    @Override
    protected Instant now() {
        return Instant.ofEpochMilli(calendar.getTime().getTime() + getOffset());
    }

    // //////////////////////////////////////////////////
    // setting/adjusting time
    // //////////////////////////////////////////////////

    /**
     * Sets the hours and minutes as specified, and sets the seconds and
     * milliseconds to zero, but the date portion is left unchanged.
     *
     * @see #setDate(int, int, int)
     * @see #addTime(int, int)
     */
    public void setTime(final int hour, final int min) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        t0 = System.currentTimeMillis();
    }

    public void setTime(final Timestamp timestamp) {
        setTime(timestamp.getTime());
    }

    public void setTime(final long millis) {
        calendar.setTimeInMillis(millis);
        t0 = System.currentTimeMillis();
    }

    /**
     * Sets the date, but the time portion is left unchanged.
     *
     * @see #setTime(int, int)
     * @see #addDate(int, int, int)
     */
    public void setDate(final int year, final int month, final int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        t0 = System.currentTimeMillis();
    }

    /**
     * Adjusts the time by the specified number of hours and minutes.
     *
     * <p>
     * Typically called after {@link #setTime(int, int)}, to move the clock
     * forward or perhaps back.
     *
     * @see #addDate(int, int, int)
     */
    public void addTime(final int hours, final int minutes) {
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        calendar.add(Calendar.MINUTE, minutes);
    }

    /**
     * Adjusts the time by the specified number of years, months or days.
     *
     * <p>
     * Typically called after {@link #setDate(int, int, int)}, to move the clock
     * forward or perhaps back.
     *
     * @see #addTime(int, int)
     */
    public void addDate(final int years, final int months, final int days) {
        calendar.add(Calendar.YEAR, years);
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DAY_OF_MONTH, days);
    }



    @Override
    public String toString() {
        return Clock.getTimeAsOffsetDateTime().toString();
    }

}
