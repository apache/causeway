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

package org.apache.isis.applib.value;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.clock.Clock;

/**
 * Value object representing a time value.
 * 
 * <p>
 * TODO: other methods to implement:
 * <ul>
 * <li>comparison methods</li>
 * <li>sameHourAs() hour ==hour sameMinuteAs() minutes = minutes sameTimeAs(hour, min) hour == hour & minutes == minutes
 * </li>
 * <li>withinNextTimePeriod(int hours, int minutes); withinTimePeriod(Date d, int hours, int minutes)</li>
 * <li>withinPreviousTimePeriod(int hours, int minutes); d.hour >= this.hour >= d.hour + hours & d.minutes >=
 * this.minutes >= d.minutes + minutes</li>
 * </ul>
 */
@Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.value.time.TimeValueSemanticsProvider")
public class Time extends Magnitude<Time> {

    private static final long serialVersionUID = 1L;
    public static final int MINUTE = 60;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;
    

    private final DateTime time;

    /**
     * Create a Time object set to the current time.
     */
    public Time() {
        Calendar calendar = Clock.getTimeAsCalendar();
        final int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        final int minuteOfHour = calendar.get(Calendar.MINUTE);
        time = newDateTime(hourOfDay, minuteOfHour);
    }

    private DateTime newDateTime(int hourOfDay, int minuteOfHour) {
        return new DateTime(1971, 1, 1, hourOfDay, minuteOfHour, 0, 0, DateTimeZone.UTC);
    }

    /**
     * Create a Time object for storing a time with the time set to the specified hours and minutes.
     */
    public Time(final int hour, final int minute) {
        time = time(hour, minute);
    }

    private DateTime time(final int hour, final int minute) {
        checkTime(hour, minute, 0);
        return newDateTime(hour, minute);
    }

    /**
     * Create a Time object for storing a time with the time set to the specified time of the Java Date object.
     */
    public Time(final java.sql.Date date) {

        this.time = new DateTime(date.getTime(), DateTimeZone.UTC);
    }

    /**
     * 
     * @param date
     *            must have Date portion equal to Epoch
     * @param calendar
     */

    public Time(final java.util.Date date, final DateTimeZone dateTimeZone) {
        DateTime DateTime = new DateTime(date.getTime(), dateTimeZone);
        this.time = DateTime.secondOfMinute().setCopy(0);
    }


    /**
     * Create a Time object for storing a time with the time set to the specified time of the Joda Time DateTime object.
     */
    public Time(final DateTime dateTime) {
        this.time = newDateTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
    }

    /**
     * Create a new Time object from the millisSinceEpoch, using UTC.
     */
    public Time(long millisSinceEpoch) {
        this.time = new DateTime(millisSinceEpoch, DateTimeZone.UTC);
    }


    /**
     * Add the specified hours and minutes to this time value, returned as a new Time object.
     */
    public Time add(final int hours, final int minutes) {
        Period period = new Period(hours, minutes, 0, 0);
        return new Time(time.plus(period));
    }

    private void checkTime(final int hour, final int minute, final int second) {
        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException("Hour must be in the range 0 - 23 inclusive");
        }

        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException("Minute must be in the range 0 - 59 inclusive");
        }

        if ((second < 0) || (second > 59)) {
            throw new IllegalArgumentException("Second must be in the range 0 - 59 inclusive");
        }
    }

    /*
     * public java.util.Date dateValue() { return (date == null) ? null : date; }
     */

    public int getHour() {
        return time.getHourOfDay();
    }

    public int getMinute() {
        return time.getMinuteOfHour();
    }

    /**
     * returns true if the time of this object has the same value as the specified time
     */
    @Override
    public boolean isEqualTo(final Time time) {
        return (time == null) ? false : (this.equals(time));
    }

    /**
     * returns true if the time of this object is earlier than the specified time
     */
    @Override
    public boolean isLessThan(final Time time) {
        return (time != null) && this.time.isBefore((time).time);
    }

    /**
     * The number of seconds since midnight.
     */
    @Deprecated
    public long longValue() {
        return time.getMillisOfDay() / 1000;
    }

    /**
     * The number of seconds since midnight.
     */
    public long secondsSinceMidnight() {
        return time.getMillisOfDay() / 1000;
    }

    public long milliSecondsSinceMidnight() {
        return time.getMillisOfDay();
    }

    public String titleString() {
        return (time == null) ? "" : DateTimeFormat.shortTime().print(time);
        // final DateFormat timeInstance = DateFormat.getTimeInstance(DateFormat.SHORT);
        // return (date == null) ? "" : timeInstance.format(date);
    }

    @Override
    public String toString() {
        return getHour() + ":" + getMinute();

    }

    public boolean sameHourAs(final Time time) {
        return getHour() == time.getHour();
    }

    public boolean sameMinuteAs(final Time time) {
        return getMinute() == time.getMinute();
    }

    public Time onTheHour() {
        return new Time(getHour(), 0);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Time other = (Time) obj;
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        return true;
    }

    public java.util.Date asJavaDate() {
        return time.toDate();
    }

    public java.sql.Time asJavaTime() {
        java.sql.Time time1 = java.sql.Time.valueOf(toString() + ":00");
        // TODO: confirm that this is in UTC
        return time1;
    }


}
