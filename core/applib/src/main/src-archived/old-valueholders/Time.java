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
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;


/**
 * Value object representing a time value.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */

/*
 * other methods to implement
 * 
 * comparision methods
 * 
 * sameHourAs() hour ==hour sameMinuteAs() minutes = minutes sameTimeAs(hour, min) hour == hour & minutes ==
 * minutes
 * 
 * withinNextTimePeriod(int hours, int minutes); withinTimePeriod(Date d, int hours, int minutes);
 * withinPreviousTimePeriod(int hours, int minutes); d.hour >= this.hour >= d.hour + hours & d.minutes >=
 * this.minutes >= d.minutes + minutes
 */
public class Time extends Magnitude {
    private static Clock clock;
    private static final DateFormat ISO_LONG = new SimpleDateFormat("HH:mm");
    private static final DateFormat ISO_SHORT = new SimpleDateFormat("HHmm");
    private static final Logger LOG = Logger.getLogger(Time.class);
    private static final DateFormat LONG_FORMAT = DateFormat.getTimeInstance(DateFormat.LONG);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    public static final int MINUTE = 60;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;
    private static final long serialVersionUID = 1L;
    private static final DateFormat SHORT_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final TimeZone timeZone;
    private final static long zero;

    static {
        timeZone = TimeZone.getTimeZone("GMT");
        ISO_LONG.setTimeZone(timeZone);
        ISO_SHORT.setTimeZone(timeZone);
        LONG_FORMAT.setTimeZone(timeZone);
        MEDIUM_FORMAT.setTimeZone(timeZone);
        SHORT_FORMAT.setTimeZone(timeZone);

        ISO_LONG.setLenient(false);
        ISO_SHORT.setLenient(false);
        LONG_FORMAT.setLenient(false);
        MEDIUM_FORMAT.setLenient(false);
        SHORT_FORMAT.setLenient(false);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        // set to 1-Jan-1970 00:00:00 (the epoch)
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.HOUR);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);
        zero = cal.getTime().getTime();

        LOG.debug("locale " + Locale.getDefault());
        LOG.debug("short fomat " + SHORT_FORMAT.format(new Date()));
        LOG.debug("medium fomat " + MEDIUM_FORMAT.format(new Date()));
        LOG.debug("long fomat " + LONG_FORMAT.format(new Date()));
    }

    static long getZero() {
        return zero / 1000;
    }

    public static void setClock(final Clock clock) {
        Time.clock = clock;
    }

    private java.util.Date date;

    /*
     * Create a Time object for storing a time with the time set to the current time.
     */
    public Time() {
        this((BusinessObject) null);
    }

    /*
     * Create a Time object for storing a time with the time set to the specified hours and minutes.
     */
    public Time(final int hour, final int minute) {
        this(null, hour, minute);
    }

    /*
     * Create a Time object for storing a time with the time set to the specified time.
     */
    public Time(final Time time) {
        this(null, time);
    }

    /*
     * Create a Time object for storing a time with the time set to the current time.
     */
    public Time(final BusinessObject parent) {
        super(parent);
        if (clock == null) {
            throw new ApplicationException("Clock not set up");
        }
        setValue(new java.util.Date(clock.getTime()));
    }

    /*
     * Create a Time object for storing a time with the time set to the specified hours and minutes.
     */
    public Time(final BusinessObject parent, final int hour, final int minute) {
        super(parent);
        setValue(hour, minute);
    }

    /*
     * Create a Time object for storing a time with the time set to the specified time.
     */
    public Time(final BusinessObject parent, final Time time) {
        super(parent);
        date = time.date;
    }

    /**
     * Add the specified hours and minutes to this time value.
     */
    public void add(final int hours, final int minutes) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        setValuesInternal(cal, true);
    }

    public Calendar calendarValue() {
        ensureAtLeastPartResolved();
        if (date == null) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.setTime(date);

        return c;
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

    public void clear() {
        setValuesInternal((Date) null, true);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (object == null) {
            clear();
        } else if (!(object instanceof Time)) {
            throw new IllegalArgumentException("Can only copy the value of  a Date object");
        } else {
            setValue((Time) object);
        }
    }

    /**
     * Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);

        // clear all aspects of the time that are not used
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.HOUR);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);

        return cal;
    }

    public java.util.Date dateValue() {
        ensureAtLeastPartResolved();
        return (date == null) ? null : date;
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Time)) {
            return false;
        }
        Time object = (Time) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.date.equals(date);
    }

    /**
     * @deprecated replaced by dateValue
     * @see #dateValue
     */
    public java.util.Date getDate() {
        ensureAtLeastPartResolved();
        return date;
    }

    public int getHour() {
        ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.setTime(date);
        return c.get(Calendar.HOUR);
    }

    public int getMinute() {
        ensureAtLeastPartResolved();
        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    /**
     * Return true if the date is blank
     */
    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return date == null;
    }

    /**
     * returns true if the time of this object has the same value as the specified time
     */
    public boolean isEqualTo(final Magnitude time) {
        ensureAtLeastPartResolved();
        if (time instanceof Time) {
            return (date == null) ? false : (date.equals(((Time) time).date));
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     * returns true if the time of this object is earlier than the specified time
     */
    public boolean isLessThan(final Magnitude time) {
        ensureAtLeastPartResolved();
        if (time instanceof Time) {
            return (date != null) && !time.isEmpty() && date.before(((Time) time).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     * The number of seconds since midnight.
     */
    public long longValue() {
        ensureAtLeastPartResolved();
        return date.getTime() / 1000;
    }

    public void parseUserEntry(final String entry) throws ValueParseException {
        if (entry.trim().equals("")) {
            clear();
        } else {
            String text = entry.trim();

            String str = text.toLowerCase();
            Calendar cal = createCalendar();

            if (str.equals("now")) {} else if (str.startsWith("+")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR_OF_DAY, hours);
            } else if (str.startsWith("-")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR_OF_DAY, -hours);
            } else {
                DateFormat[] formats = new DateFormat[] { LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        cal.setTime(formats[i].parse(text));

                        break;
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new ValueParseException("Invalid time '" + text + "' for locale " + Locale.getDefault(), e);
                        }
                    }
                }
            }

            setValuesInternal(cal, true);
        }
    }

    /**
     * Reset this time so it contains the current time.
     * 
     * 
     */
    public void reset() {
        setValue(new Date(clock.getTime()));
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal((Date) null, false);
        } else {
            int hour = Integer.valueOf(data.substring(0, 2)).intValue();
            int minute = Integer.valueOf(data.substring(2)).intValue();
            setValue(hour, minute);
            setValuesInternal(hour, minute, false);
        }
    }

    public String asEncodedString() {
        Calendar cal = calendarValue();

        if (cal == null) {
            return "NULL";
        } else {
            StringBuffer data = new StringBuffer(4);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            data.append((hour <= 9) ? "0" : "");
            data.append(hour);

            int minute = cal.get(Calendar.MINUTE);
            data.append((minute <= 9) ? "0" : "");
            data.append(minute);

            return data.toString();
        }
    }

    /*
     * Sets this object's time to be the same as the specified hour, minute and second.
     */
    public void setValue(final int hour, final int minute) {
        setValuesInternal(hour, minute, true);
    }

    public void setValue(final java.util.Date date) {
        if (date == null) {
            setValuesInternal((Date) null, true);
        } else {
            Calendar cal = Calendar.getInstance();

            cal.setTime(date);
            setValuesInternal(cal, true);
        }
    }

    public void setValue(final long time) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(new Date(time * 1000));
        setValuesInternal(cal, true);
    }

    public void setValue(final Time time) {
        if (time == null || time.date == null) {
            setValuesInternal((Date) null, true);
        } else {
            setValuesInternal(new Date(time.date.getTime()), true);
        }
    }

    private void setValuesInternal(final int hour, final int minute, final boolean notify) {
        checkTime(hour, minute, 0);

        Calendar cal = createCalendar();
        cal.setTimeZone(timeZone);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        setValuesInternal(cal, notify);
    }

    private void setValuesInternal(final Calendar cal, final boolean notify) {
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);
        setValuesInternal(cal.getTime(), notify);
    }

    private void setValuesInternal(final java.util.Date date, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.date = date;
        if (notify) {
            parentChanged();
        }
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title((date == null) ? "" : SHORT_FORMAT.format(date));
    }
}
