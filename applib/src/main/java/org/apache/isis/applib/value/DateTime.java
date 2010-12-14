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
import java.util.Date;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.clock.Clock;

/**
 * Value object representing a date and time value.
 */
@Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.value.DateTimeValueSemanticsProvider")
public class DateTime extends Magnitude<DateTime> {

    private static final long serialVersionUID = 1L;
    private final Date date;

    /**
     * Create a Time object for storing a timeStamp set to the current time.
     */
    public DateTime() {
        final Calendar cal = Calendar.getInstance();
        final java.util.Date d = new java.util.Date(Clock.getTime());
        cal.setTime(d);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();
    }

    public DateTime(final Date date) {
        this.date = date;
    }

    public DateTime(int year, int month, int day) {
        this(year, month, day, 0, 0);
    }

    /**
     * Create a Date object set to the specified day, month and year.
     */
    public DateTime(final int year, final int month, final int day, final int hour, final int minute) {
        this(year, month, day, hour, minute, 0);
    }

    public DateTime(final int year, final int month, final int day, final int hour, final int minute, final int second) {
        checkTime(year, month, day, hour, minute);
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();
    }

    private void checkTime(final int year, final int month, final int day, final int hour, final int minute) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException("Month must be in the range 1 - 12 inclusive " + month);
        }

        final Calendar cal = Calendar.getInstance();

        cal.set(year, month - 1, 0);

        final int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);

        if ((day < 1) || (day > lastDayOfMonth)) {
            throw new IllegalArgumentException("Day must be in the range 1 - " + lastDayOfMonth + " inclusive " + day);
        }

        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException("Hour must be in the range 0 - 23 inclusive " + hour);
        }

        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException("Minute must be in the range 0 - 59 inclusive " + minute);
        }
    }

    /**
     * Add the specified days, years and months to this date value.
     */
    public DateTime add(final int years, final int months, final int days, final int hours, final int minutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.YEAR, years);
        return createDateTime(cal.getTime());
    }

    public Calendar calendarValue() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    protected DateTime createDateTime(final Date date) {
        return new DateTime(date);
    }

    public java.util.Date dateValue() {
        return new Date(date.getTime());
    }

    public int getDay() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    public int getMonth() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    /**
     * returns true if the time stamp of this object has the same value as the specified time
     */
    @Override
    public boolean isEqualTo(final DateTime timeStamp) {
        return this.date.equals((timeStamp).date);
    }

    /**
     * returns true if the timeStamp of this object is earlier than the specified timeStamp
     */
    @Override
    public boolean isLessThan(final DateTime timeStamp) {
        return date.before((timeStamp).date);
    }

    public long longValue() {
        return date.getTime();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateTime other = (DateTime) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getYear() + "-" + getMonth() + "-" + getDay() + " " + getHour() + ":" + getMinute();
    }
}
