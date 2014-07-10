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

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import org.apache.isis.applib.Defaults;
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
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.date.DateValueSemanticsProvider")
public class Date extends Magnitude<Date> {
    private static final long serialVersionUID = 1L;
    private final DateTime date;

    /**
     * Create a Date object for today's date.
     */
    public Date() {
        final DateTime time = Clock.getTimeAsDateTime().withTime(0, 0, 0, 0);
        date = new DateTime(time, Defaults.getTimeZone());
    }

    /**
     * Create a Date object set to the specified day, month and year.
     */
    public Date(final int year, final int month, final int day) {
        checkDate(year, month, day);
        date = newDateTime(year, month, day);
    }

    /**
     * Create a Date object based on the specified Java date object. The time
     * portion of the Java date is disposed of.
     */
    public Date(final java.util.Date date) {
        this.date = new DateTime(date.getTime(), Defaults.getTimeZone());
    }

    public Date(final long millisSinceEpoch) {
        this.date = new DateTime(millisSinceEpoch);
    }

    public Date(final DateTime date) {
        this.date = new DateTime(date);
    }

    private DateTime newDateTime(final int year, final int month, final int day) {
        return new DateTime(year, month, day, 0, 0, 0, 0, Defaults.getTimeZone());
    }

    protected Date createDate(final DateTime date) {
        final Date newDate = new Date(date);
        return newDate;
    }

    /**
     * Add the specified days, years and months to this date value and return a
     * new date object containing the result.
     */
    public Date add(final int years, final int months, final int days) {
        final Period add = new Period(years, months, 0, days, 0, 0, 0, 0);
        final DateTime newDate = date.plus(add);
        return new Date(newDate);
    }

    private void checkDate(final int year, final int month, final int day) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException("Month must be in the range 1 - 12 inclusive");
        }
        final DateTime newDate = newDateTime(year, month, 1);
        final int lastDayOfMonth = newDate.dayOfMonth().getMaximumValue();
        ;
        if ((day < 1) || (day > lastDayOfMonth)) {
            throw new IllegalArgumentException("Day must be in the range 1 - " + lastDayOfMonth + " inclusive: " + day);
        }
    }

    /**
     * Return this date value as a Java Date object.
     * 
     * @see java.util.Date
     */
    public java.util.Date dateValue() {
        final java.util.Date javaDate = date.toDate();
        return javaDate;
    }

    /**
     * 
     * @return the milliseconds from 1970-01-01T00:00:00Z
     */
    public long getMillisSinceEpoch() {
        return date.getMillis();
    }

    /**
     * Calculates, and returns, a date representing the last day of the month
     * relative to the current date.
     * 
     * @author Joshua Cassidy
     */
    public Date endOfMonth() {
        return new Date(date.dayOfMonth().withMaximumValue());
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
        return date.getDayOfMonth();
    }

    /**
     * Calculates, and returns, an int representing the day of the week relative
     * to the current date. With Mon = 0 through to Sun = 6
     * 
     * @author Joshua Cassidy
     */
    public int getDayOfWeek() {
        return date.getDayOfWeek() - 1; // Mon - Sun == 1 - 7
    }

    /**
     * Return the month from this date, in the range 1 - 12.
     */
    public int getMonth() {
        return date.getMonthOfYear();
    }

    /**
     * Return the year from this date.
     */
    public int getYear() {
        return date.getYear();
    }

    /**
     * Returns true if the date of this object has the same value as the
     * specified date
     */
    @Override
    public boolean isEqualTo(final Date date) {
        return this.date.equals((date).date);
    }

    /**
     * Returns true if the time of this object is earlier than the specified
     * time
     */
    @Override
    public boolean isLessThan(final Date date) {
        return this.date.isBefore((date).date);
    }

    private boolean sameAs(final Date as, final DateTimeFieldType field) {

        return date.get(field) == as.date.get(field);
    }

    /**
     * Determines if this date and the specified date represent the same day of
     * the month, eg both dates are for the 3rd.
     */
    public boolean sameDayOfMonthAs(final Date as) {
        return sameAs(as, DateTimeFieldType.dayOfMonth());
    }

    /**
     * Determines if this date and the specified date represent the same day of
     * the week, eg both dates are on a Tuesday.
     */
    public boolean sameDayOfWeekAs(final Date as) {
        return sameAs(as, DateTimeFieldType.dayOfWeek());
    }

    /**
     * Determines if this date and the specified date represent the same day of
     * the year, eg both dates are for the 108th day of the year.
     */
    public boolean sameDayOfYearAs(final Date as) {
        return sameAs(as, DateTimeFieldType.dayOfYear());
    }

    /**
     * Determines if this date and the specified date represent the same month,
     * eg both dates are for the March.
     */
    public boolean sameMonthAs(final Date as) {
        return sameAs(as, DateTimeFieldType.monthOfYear());
    }

    /**
     * Determines if this date and the specified date represent the same week in
     * the year, eg both dates are the for the 18th week of the year.
     */
    public boolean sameWeekAs(final Date as) {
        return sameAs(as, DateTimeFieldType.weekOfWeekyear());
    }

    /**
     * Determines if this date and the specified date represent the same year.
     */
    public boolean sameYearAs(final Date as) {
        return sameAs(as, DateTimeFieldType.year());
    }

    /**
     * Calculates, and returns, a date representing the first day of the month
     * relative to the current date.
     */
    public Date startOfMonth() {
        return new Date(date.dayOfMonth().withMinimumValue());
    }

    /**
     * Calculates, and returns, a date representing the first day of the week
     * relative to the current date.
     */
    public Date startOfWeek() {
        return new Date(date.dayOfWeek().withMinimumValue());
    }

    /**
     * Calculates, and returns, a date representing the first day of the year
     * relative to the current date.
     */
    public Date startOfYear() {
        return new Date(date.dayOfYear().withMinimumValue());
    }

    public String title() {
        return DateTimeFormat.mediumDate().print(date);
    }

    @Override
    public String toString() {
        // return getYear() + "-" + getMonth() + "-" + getDay();
        return String.format("%04d-%02d-%02d", getYear(), getMonth(), getDay());

    }
}
