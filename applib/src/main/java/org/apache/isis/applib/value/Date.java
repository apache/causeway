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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.clock.Clock;

/**
 * Value object representing a date (not time) value.
 * 
 * <p>
 * TODO: other methods to implement comparison methods:
 * <ul>
 * <li>sameDateAs() day == day & month == month & year == year</li>
 * <li>withinNextDatePeriod(int days, int months, int years)</li>
 * <li>withinDatePeriod(int days, int months, int years)</li>
 * <li>withinPreviousDatePeriod(int days, int months, int years)</li>
 * </ul>
 */
@Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.value.DateValueSemanticsProvider")
public class Date extends Magnitude<Date> {
    private static final long serialVersionUID = 1L;
    private static final TimeZone UTC_TIME_ZONE;
    private final java.util.Date date;

    static {
        // for dotnet compatibility -
        TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC");
        if (timeZone == null) {
            // for dotnet compatibility - "Etc/UTC fails in dotnet
            timeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = timeZone;
    }

    /**
     * Create a Date object for today's date.
     */
    public Date() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        final java.util.Date d = new java.util.Date(Clock.getTime());
        cal.setTime(d);
        clearTime(cal);
        date = cal.getTime();
    }

    /**
     * Create a Date object set to the specified day, month and year.
     */
    public Date(final int year, final int month, final int day) {
        checkDate(year, month, day);
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        clearTime(cal);
        cal.set(year, month - 1, day);
        date = cal.getTime();
    }

    /**
     * Create a Date object based on the specified Java date object. The time portion of the Java date is disposed of.
     */
    public Date(final java.util.Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);

        // TODO when input date is BST then then date value ends up as the previous day
        cal.setTime(date);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();
    }

    protected Date createDate(final java.util.Date time) {
        return new Date(time);
    }

    /**
     * Add the specified days, years and months to this date value and return a new date object containing the result.
     */
    public Date add(final int years, final int months, final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.YEAR, years);
        return createDate(cal.getTime());
    }

    private void checkDate(final int year, final int month, final int day) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException("Month must be in the range 1 - 12 inclusive");
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        cal.set(year, month - 1, 0);
        final int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);
        if ((day < 1) || (day > lastDayOfMonth)) {
            throw new IllegalArgumentException("Day must be in the range 1 - " + lastDayOfMonth + " inclusive: " + day);
        }
    }

    /**
     * clear all aspects of the time that are not used
     */
    private void clearTime(final Calendar cal) {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Return this date value as a Java Date object.
     * 
     * @see java.util.Date
     */
    public java.util.Date dateValue() {
        return new java.util.Date(date.getTime());
    }

    private int getEndDayOfMonthOneDotOne(final Calendar originalCalendar) {
        final Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeZone(originalCalendar.getTimeZone());
        newCalendar.setTime(originalCalendar.getTime());

        final int firstPossibleDay = originalCalendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
        final int lastPossibleDay = originalCalendar.getMaximum(Calendar.DAY_OF_MONTH);
        int lastValidDay = firstPossibleDay;

        for (int day = firstPossibleDay + 1; day < lastPossibleDay; day++) {
            newCalendar.set(Calendar.DAY_OF_MONTH, day);
            if (newCalendar.get(Calendar.MONTH) != originalCalendar.get(Calendar.MONTH)) {
                return lastValidDay;
            }
            lastValidDay = day;
        }
        return lastPossibleDay;
    }

    private int getEndDayOfMonth(final Calendar originalCalendar) {

        final Class<?> cls = originalCalendar.getClass();
        try {
            final Method getActualMaximum = cls.getMethod("getActualMaximum", new Class[] { int.class });
            final Integer dayOfMonth = Integer.valueOf(Calendar.DAY_OF_MONTH);
            return ((Integer) getActualMaximum.invoke(originalCalendar, new Object[] { dayOfMonth })).intValue();
        } catch (final NoSuchMethodException ignore) {
            // expected if pre java 1.2 - fall through
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        }
        return getEndDayOfMonthOneDotOne(originalCalendar);
    }

    /**
     * Calculates, and returns, a date representing the last day of the month relative to the current date.
     * 
     * @author Joshua Cassidy
     */
    public Date endOfMonth() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, getEndDayOfMonth(c));
        return createDate(c.getTime());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Date)) {
            return false;
        }
        final Date date1 = (Date) o;
        if (!date.equals(date1.date)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    /**
     * Return the day from this date, in the range 1 - 31.
     */
    public int getDay() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Calculates, and returns, an int representing the day of the week relative to the current date. With Mon = 0
     * through to Sun = 6
     * 
     * @author Joshua Cassidy
     */
    public int getDayOfWeek() {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        final int day = c.get(Calendar.DAY_OF_WEEK);
        // Calendar day is 1 - 7 for sun - sat
        if (day == 1) {
            return 6;
        } else {
            return day - 2;
        }
    }

    /**
     * Return the month from this date, in the range 1 - 12.
     */
    public int getMonth() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    /**
     * Return the year from this date.
     */
    public int getYear() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    /**
     * Returns true if the date of this object has the same value as the specified date
     */
    @Override
    public boolean isEqualTo(final Date date) {
        return this.date.equals((date).date);
    }

    /**
     * Returns true if the time of this object is earlier than the specified time
     */
    @Override
    public boolean isLessThan(final Date date) {
        return this.date.before((date).date);
    }

    private boolean sameAs(final Date as, final int field) {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);

        final Calendar c2 = Calendar.getInstance();
        c2.setTimeZone(UTC_TIME_ZONE);
        c2.setTime(as.date);

        return c.get(field) == c2.get(field);
    }

    /**
     * Determines if this date and the specified date represent the same day of the month, eg both dates are for the
     * 3rd.
     */
    public boolean sameDayOfMonthAs(final Date as) {
        return sameAs(as, Calendar.DAY_OF_MONTH);
    }

    /**
     * Determines if this date and the specified date represent the same day of the week, eg both dates are on a
     * Tuesday.
     */
    public boolean sameDayOfWeekAs(final Date as) {
        return sameAs(as, Calendar.DAY_OF_WEEK);
    }

    /**
     * Determines if this date and the specified date represent the same day of the year, eg both dates are for the
     * 108th day of the year.
     */
    public boolean sameDayOfYearAs(final Date as) {
        return sameAs(as, Calendar.DAY_OF_YEAR);
    }

    /**
     * Determines if this date and the specified date represent the same month, eg both dates are for the March.
     */
    public boolean sameMonthAs(final Date as) {
        return sameAs(as, Calendar.MONTH);
    }

    /**
     * Determines if this date and the specified date represent the same week in the year, eg both dates are the for the
     * 18th week of the year.
     */
    public boolean sameWeekAs(final Date as) {
        return sameAs(as, Calendar.WEEK_OF_YEAR);
    }

    /**
     * Determines if this date and the specified date represent the same year.
     */
    public boolean sameYearAs(final Date as) {
        return sameAs(as, Calendar.YEAR);
    }

    /**
     * Calculates, and returns, a date representing the first day of the month relative to the current date.
     */
    public Date startOfMonth() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getMinimum(Calendar.DAY_OF_MONTH));
        return createDate(c.getTime());
    }

    /**
     * Calculates, and returns, a date representing the first day of the week relative to the current date.
     */
    public Date startOfWeek() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        return createDate(c.getTime());
    }

    /**
     * Calculates, and returns, a date representing the first day of the year relative to the current date.
     */
    public Date startOfYear() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC_TIME_ZONE);
        c.setTime(date);
        c.set(Calendar.DAY_OF_YEAR, c.getMinimum(Calendar.DAY_OF_YEAR));
        return createDate(c.getTime());
    }

    public String title() {
        final DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateInstance.format(date);
    }

    @Override
    public String toString() {
        return getYear() + "-" + getMonth() + "-" + getDay();
    }
}
