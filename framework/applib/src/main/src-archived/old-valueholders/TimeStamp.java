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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Value object representing a date and time value.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */
public class TimeStamp extends Magnitude {
    // TODO check the ISO representations
    private static final DateFormat ISO_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    // private static final DateFormat ISO_SHORT = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");

    private boolean isNull = true;
    private java.util.Date date;
    private static Clock clock;

    public static void setClock(final Clock clock) {
        TimeStamp.clock = clock;
    }

    /**
     * Create a Time object for storing a timeStamp set to the current time.
     */
    public TimeStamp() {
        this((BusinessObject) null);
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified time.
     */
    public TimeStamp(final TimeStamp timeStamp) {
        this(null, timeStamp);
    }

    /**
     * Create a Time object for storing a timeStamp set to the current time.
     */
    public TimeStamp(final BusinessObject parent) {
        super(parent);
        if (clock == null) {
            throw new ApplicationException("Clock not set up");
        }
        reset();
    }

    /**
     * Create a Time object for storing a timeStamp set to the specified time.
     */
    public TimeStamp(final BusinessObject parent, final TimeStamp timeStamp) {
        super(parent);
        date = timeStamp.date;
        isNull = timeStamp.isNull;
    }

    public void clear() {
        setValuesInternal(date, true, true);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof TimeStamp)) {
            throw new IllegalArgumentException("Can only copy the value of  a TimeStamp object");
        }
        TimeStamp ts = (TimeStamp) object;
        setValuesInternal(ts.date, ts.isNull, true);
    }

    /**
     * Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();
        return cal;
    }

    public java.util.Date dateValue() {
        ensureAtLeastPartResolved();
        return isNull ? null : date;
    }

    /**
     * Return true if the time stamp is blank
     */
    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     * returns true if the time stamp of this object has the same value as the specified time
     */
    public boolean isEqualTo(final Magnitude timeStamp) {
        ensureAtLeastPartResolved();
        if (timeStamp instanceof TimeStamp) {
            if (isNull) {
                return timeStamp.isEmpty();
            }

            return this.date.equals(((TimeStamp) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     * returns true if the timeStamp of this object is earlier than the specified timeStamp
     */
    public boolean isLessThan(final Magnitude timeStamp) {
        ensureAtLeastPartResolved();
        if (timeStamp instanceof TimeStamp) {
            return !isNull && !timeStamp.isEmpty() && date.before(((TimeStamp) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    public long longValue() {
        ensureAtLeastPartResolved();
        return date.getTime();
    }

    public void parseUserEntry(final String text) throws ValueParseException {}

    /**
     * Reset this time so it contains the current time.
     */
    public void reset() {
        setValuesInternal(new Date(clock.getTime()), false, true);
    }

    private void setValuesInternal(final java.util.Date value, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.date = value;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title(isNull ? "" : ISO_LONG.format(date));
    }

    public Calendar calendarValue() {
        ensureAtLeastPartResolved();
        if (isNull) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c;
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(date, true, false);
        } else {
            int year = Integer.valueOf(data.substring(0, 4)).intValue();
            int month = Integer.valueOf(data.substring(4, 6)).intValue();
            int day = Integer.valueOf(data.substring(6, 8)).intValue();
            int hour = Integer.valueOf(data.substring(8, 10)).intValue();
            int minute = Integer.valueOf(data.substring(10, 12)).intValue();
            int second = Integer.valueOf(data.substring(12, 14)).intValue();
            int millisecond = Integer.valueOf(data.substring(14, 17)).intValue();

            Calendar cal = createCalendar();

            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, millisecond);
            setValuesInternal(cal.getTime(), false, true);
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

            int millisecond = cal.get(Calendar.MILLISECOND);
            data.append((millisecond <= 99) ? "0" : "");
            data.append((millisecond <= 9) ? "0" : "");
            data.append(millisecond);

            return data.toString();
        }
    }

    public String toString() {
        return title() + " " + longValue() + " [TimeStamp]";
    }

    public void setValue(final TimeStamp ts) {
        if ((ts == null)) {
            this.clear();
        } else {
            setValuesInternal(ts.date, ts.isNull, true);
        }
    }

    public void setValue(final java.util.Date date) {
        setValuesInternal(date, date == null, true);
    }
}
