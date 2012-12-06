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


package org.apache.isis.application.valueholder;

import org.apache.isis.application.ApplicationException;
import org.apache.isis.application.BusinessObject;
import org.apache.isis.application.Clock;
import org.apache.isis.application.Title;
import org.apache.isis.application.value.ValueParseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Value object representing a date and time value.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */
public class DateTime extends Magnitude {
    private static final long serialVersionUID = 1L;
    private static final DateFormat SHORT_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    private static final DateFormat LONG_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    private static final DateFormat ISO_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final DateFormat ISO_SHORT = new SimpleDateFormat("yyyyMMdd'T'HHmm");

    private transient DateFormat format = MEDIUM_FORMAT;
    private boolean isNull = true;
    private java.util.Date date;
    private static Clock clock;

    public static void setClock(final Clock clock) {
        DateTime.clock = clock;

        ISO_LONG.setLenient(false);
        ISO_SHORT.setLenient(false);
        LONG_FORMAT.setLenient(false);
        MEDIUM_FORMAT.setLenient(false);
        SHORT_FORMAT.setLenient(false);
    }

    /**
     * Create a Time object for storing a timeStamp set to the current time.
     */
    public DateTime() {
        this((BusinessObject) null);
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified hours and minutes.
     * 
     * @deprecated replaced by TimeStamp(int year, int month, int day, int hour, int minute, int second)
     */
    public DateTime(final int year, final int month, final int day, final int hour, final int minute) {
        this(null, year, month, day, hour, minute, 0);
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified hours and minutes.
     */
    public DateTime(final int year, final int month, final int day, final int hour, final int minute, final int second) {
        this(null, year, month, day, hour, minute, second);
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified time.
     */
    public DateTime(final DateTime timeStamp) {
        this(null, timeStamp);
    }

    /**
     * Create a Time object for storing a timeStamp set to the current time.
     */
    public DateTime(final BusinessObject parent) {
        super(parent);
        if (clock == null) {
            throw new ApplicationException("Clock not set up");
        }
        setValue(new java.util.Date(clock.getTime()));
        isNull = false;
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified hours and minutes.
     */
    public DateTime(final BusinessObject parent, final int year, final int month, final int day, final int hour, final int minute, final int second) {
        super(parent);
        setValue(year, month, day, hour, minute, second);
        isNull = false;
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified time.
     */
    public DateTime(final BusinessObject parent, final DateTime timeStamp) {
        super(parent);
        date = timeStamp.date;
        isNull = timeStamp.isNull;
    }

    /**
     * Add the specified days, years and months to this date value.
     */
    public void add(final int hours, final int minutes, final int seconds) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        set(cal);
    }

    private void checkTime(final int year, final int month, final int day, final int hour, final int minute, final int second) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException("Month must be in the range 1 - 12 inclusive " + month);
        }

        Calendar cal = Calendar.getInstance();

        cal.set(year, month - 1, 0);

        int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);

        if ((day < 1) || (day > lastDayOfMonth)) {
            throw new IllegalArgumentException("Day must be in the range 1 - " + lastDayOfMonth + " inclusive " + day);
        }

        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException("Hour must be in the range 0 - 23 inclusive " + hour);
        }

        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException("Minute must be in the range 0 - 59 inclusive " + minute);
        }

        if ((second < 0) || (second > 59)) {
            throw new IllegalArgumentException("Second must be in the range 0 - 59 inclusive " + second);
        }
    }

    public void clear() {
        setValuesInternal(date, true, true);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (object == null) {
            this.clear();
        } else if (!(object instanceof DateTime)) {
            throw new IllegalArgumentException("Can only copy the value of  a TimeStamp object");
        } else {
            setValue((DateTime) object);
        }
    }

    /**
     * Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();

        // clear all aspects of the time that are not used
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public java.util.Date dateValue() {
        this.ensureAtLeastPartResolved();
        return isNull ? null : date;
    }

    public boolean equals(final Object obj) {
        this.ensureAtLeastPartResolved();
        if (obj instanceof DateTime) {
            DateTime d = (DateTime) obj;
            return d.date.equals(date);
        }
        return super.equals(obj);
    }

    /**
     * Return true if the time stamp is blank
     */
    public boolean isEmpty() {
        this.ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     * returns true if the time stamp of this object has the same value as the specified time
     */
    public boolean isEqualTo(final Magnitude timeStamp) {
        this.ensureAtLeastPartResolved();
        if (timeStamp instanceof DateTime) {
            if (isNull) {
                return timeStamp.isEmpty();
            }

            return this.date.equals(((DateTime) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     * returns true if the timeStamp of this object is earlier than the specified timeStamp
     */
    public boolean isLessThan(final Magnitude timeStamp) {
        this.ensureAtLeastPartResolved();
        if (timeStamp instanceof DateTime) {
            return !isNull && !timeStamp.isEmpty() && date.before(((DateTime) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    public int getDay() {
        this.ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        this.ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        this.ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    public int getHour() {
        this.ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR);
    }

    public int getMinute() {
        this.ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    public long longValue() {
        this.ensureAtLeastPartResolved();
        return date.getTime();
    }

    public void parseUserEntry(final String entry) throws ValueParseException {
        if (entry.trim().equals("")) {
            clear();
        } else {
            String text = entry.trim();

            String str = text.toLowerCase();
            Calendar cal = createCalendar();

            if (str.equals("today") || str.equals("now")) {} else if (str.startsWith("+")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR, hours);
            } else if (str.startsWith("-")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR, -hours);
            } else {
                DateFormat[] formats = new DateFormat[] { LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        cal.setTime(formats[i].parse(text));

                        break;
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new ValueParseException("Invalid timeStamp " + text, e);
                        }
                    }
                }
            }

            set(cal);
        }
    }

    /**
     * Reset this time so it contains the current time.
     * 
     */
    public void reset() {
        setValuesInternal(new Date(clock.getTime()), false, true);
    }

    private void set(final Calendar cal) {
        setValuesInternal(cal.getTime(), false, true);
    }

    private void setValuesInternal(final Date date, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.date = date;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    private void setValuesInternal(
            final int year, final int month, final int day, final int hour, final int minute, final int second, final boolean notify) {
        checkTime(year, month, day, hour, minute, second);

        Calendar cal = createCalendar();

        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        setValuesInternal(cal.getTime(), false, notify);
    }

    public void setValue(final java.util.Date date) {
        if (date == null) {
            setValuesInternal(date, true, true);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            set(cal);
        }
    }

    public void setValue(final long time) {
        setValuesInternal(new Date(time), false, true);
    }

    public void setValue(final DateTime timeStamp) {
        if (timeStamp == null) {
            setValuesInternal(date, true, true);
        } else {
            setValuesInternal(new Date(timeStamp.date.getTime()), false, true);
        }
    }

    /*
     * Sets this object's timeStamp to be the same as the specified hour, minute and second.
     */
    public void setValue(final int year, final int month, final int day, final int hour, final int minute, final int second) {
        setValuesInternal(year, month, day, hour, minute, second, false);
    }

    public Title title() {
        this.ensureAtLeastPartResolved();
        return new Title(isNull ? "" : format.format(date));
    }

    public Calendar calendarValue() {
        this.ensureAtLeastPartResolved();
        if (isNull) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c;
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            this.setValuesInternal(date, true, false);
        } else {
            int year = Integer.valueOf(data.substring(0, 4)).intValue();
            int month = Integer.valueOf(data.substring(4, 6)).intValue();
            int day = Integer.valueOf(data.substring(6, 8)).intValue();
            int hour = Integer.valueOf(data.substring(8, 10)).intValue();
            int minute = Integer.valueOf(data.substring(10, 12)).intValue();
            int second = Integer.valueOf(data.substring(12, 14)).intValue();
            setValuesInternal(year, month, day, hour, minute, second, false);
        }
    }

    public String asEncodedString() {
        if (isEmpty()) {
            return "NULL";
        } else {
            Calendar cal = calendarValue();
            StringBuffer data = new StringBuffer(8);
            String year = String.valueOf(cal.get(Calendar.YEAR));
            data.append("0000".substring(0, 4 - year.length()));
            data.append(year);

            int month = cal.get(Calendar.MONTH) + 1;
            data.append((month <= 9) ? "0" : "");
            data.append(month);

            int day = cal.get(Calendar.DAY_OF_MONTH);
            data.append((day <= 9) ? "0" : "");
            data.append(day);

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            data.append((hour <= 9) ? "0" : "");
            data.append(hour);

            int minute = cal.get(Calendar.MINUTE);
            data.append((minute <= 9) ? "0" : "");
            data.append(minute);

            int second = cal.get(Calendar.SECOND);
            data.append((second <= 9) ? "0" : "");
            data.append(second);

            return data.toString();
        }
    }

    public String toString() {
        // title() ensures this is resolved
        return title() + " " + longValue() + " [DateTime]";
    }
}
