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

package org.apache.isis.extensions.fixtures.legacy;

import java.util.Calendar;
import java.util.TimeZone;

import javax.ejb.Singleton;

import org.apache.isis.applib.clock.Clock;

/**
 * This clock, for use by fixtures, can be set to specific time.
 *
 * <p>
 * If not set it will provide the time provided by the system clock.
 *
 * <p>
 * Note that - by design - it does not provide any mechanism to advance the time
 * (eg automatic ticking of the clock). That is, the time returned is always
 * explicitly under the control of the programmer (it can be moved forward or
 * back as required).
 */
public class FixtureClock extends Clock {
    private static final TimeZone UTC_TIME_ZONE;

    static {
        TimeZone tempTimeZone = TimeZone.getTimeZone("Etc/UTC");
        if (tempTimeZone == null) {
            tempTimeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = tempTimeZone;
    }

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
    public synchronized static FixtureClock initialize() {
        if (!isInitialized() || !(getInstance() instanceof FixtureClock)) {
            // installs the FixtureClock as the Clock singleton via the Clock's
            // constructor
            // if was initialized, then will replace.
            // (if non-replaceable, then superclass will throw exception for us.
            new FixtureClock();
        }
        return (FixtureClock) getInstance();
    }

    /**
     * Makes {@link Clock#remove()} visible.
     */
    public static boolean remove() {
        return Clock.remove();
    }

    // //////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////

    // if non-null, then indicates that the time has been explicitly set.
    // Otherwise returns the system time.
    private Calendar calendar = null;

    private FixtureClock() {
    }

    // //////////////////////////////////////////////////
    // hook
    // //////////////////////////////////////////////////

    /**
     * Access via {@link Clock#getTime()}.
     *
     * <p>
     * Will just return the system time until {@link #setDate(int, int, int)} or
     * {@link #setTime(int, int)} (or one of the overloads) has been called.
     */
    @Override
    protected long time() {
        if (calendar == null) {
            return System.currentTimeMillis();
        }
        return calendar.getTime().getTime();
    }

    // //////////////////////////////////////////////////
    // setting/adjusting time
    // //////////////////////////////////////////////////

    /**
     * Sets the clock to epoch, that is midnight, 1 Jan 1970 UTC.
     *
     * <p>
     * This is typically called before either {@link #setDate(int, int, int)}
     * (so that time is set to midnight) and/or {@link #setTime(int, int)} (so
     * that date is set to a well known value).
     */
    public void clear() {
        setupCalendarIfRequired();
        calendar.clear();
    }

    /**
     * Sets the hours and minutes as specified, and sets the seconds and
     * milliseconds to zero, but the date portion is left unchanged.
     *
     * @see #setDate(int, int, int)
     * @see #addTime(int, int)
     */
    public void setTime(final int hour, final int min) {
        setupCalendarIfRequired();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Sets the date, but the time portion is left unchanged.
     *
     * @see #setTime(int, int)
     * @see #addDate(int, int, int)
     */
    public void setDate(final int year, final int month, final int day) {
        setupCalendarIfRequired();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
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
        setupCalendarIfRequired();
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
        setupCalendarIfRequired();
        calendar.add(Calendar.YEAR, years);
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DAY_OF_MONTH, days);
    }

    private void setupCalendarIfRequired() {
        if (calendar != null) {
            return;
        }
        calendar = Calendar.getInstance();
        calendar.setTimeZone(UTC_TIME_ZONE);
    }

    // //////////////////////////////////////////////////
    // reset
    // //////////////////////////////////////////////////

    /**
     * Go back to just returning the system's time.
     */
    public void reset() {
        calendar = null;
    }

    // //////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////

    @Override
    public String toString() {
        return (calendar == null ? "System" : "Explicitly set") + ": " + Clock.getTimeAsDateTime().toString();
    }

}
