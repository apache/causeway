package org.nakedobjects.object.defaults.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.system.Clock;

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
    private static final DateFormat SHORT_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            DateFormat.SHORT);
    private static final DateFormat LONG_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG,
            DateFormat.LONG);
    private static final DateFormat ISO_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final DateFormat ISO_SHORT = new SimpleDateFormat("yyyyMMdd'T'HHmm");

 
    private transient DateFormat format = MEDIUM_FORMAT;
    private boolean isNull = true;
    private java.util.Date date;
	private static Clock clock;
	
	public static void setClock(Clock clock) {
	    DateTime.clock = clock;
	    
        ISO_LONG.setLenient(false);
        ISO_SHORT.setLenient(false);
        LONG_FORMAT.setLenient(false);
        MEDIUM_FORMAT.setLenient(false);
        SHORT_FORMAT.setLenient(false);
	}


    /**
     Create a Time object for storing a timeStamp set to the current time.
     */
    public DateTime() {
        if(clock == null) {
            throw new NakedObjectRuntimeException("Clock not set up");
        }
        setValue(new java.util.Date(clock.getTime()));
        isNull = false;
    }

    /**
     Create a Time object for storing a timeStamp set to the specified hours and minutes.
     @deprecated replaced by TimeStamp(int year, int month, int day, int hour, int minute, int second)
     */
    public DateTime(int year, int month, int day, int hour, int minute) {
        this(year, month, day, hour, minute, 0);
    }

    /**
     Create a Time object for storing a timeStamp set to the specified hours and minutes.
     */
    public DateTime(int year, int month, int day, int hour, int minute,
        int second) {
        setValue(year, month, day, hour, minute, second);
        isNull = false;
    }

    /**
     Create a Time object for storing a timeStamp set to the specified time.
     */
    public DateTime(DateTime timeStamp) {
        date = timeStamp.date;
        isNull = timeStamp.isNull;
    }

    /**
     Add the specified days, years and months to this date value.
     */
    public void add(int hours, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        set(cal);
    }

    private void checkTime(int year, int month, int day, int hour, int minute,
        int second) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException(
                "Month must be in the range 1 - 12 inclusive " + month);
        }

        Calendar cal = Calendar.getInstance();

        cal.set(year, month - 1, 0);

        int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);

        if ((day < 1) || (day > lastDayOfMonth)) {
            throw new IllegalArgumentException("Day must be in the range 1 - " +
                lastDayOfMonth + " inclusive " + day);
        }

        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException(
                "Hour must be in the range 0 - 23 inclusive " + hour);
        }

        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException(
                "Minute must be in the range 0 - 59 inclusive " + minute);
        }

        if ((second < 0) || (second > 59)) {
            throw new IllegalArgumentException(
                "Second must be in the range 0 - 59 inclusive " + second);
        }
    }

    public void clear() {
        isNull = true;
    }

    public void copyObject(Naked object) {
        if (!(object instanceof DateTime)) {
            throw new IllegalArgumentException(
                "Can only copy the value of  a TimeStamp object");
        }

        date = ((DateTime) object).date;
        isNull = ((DateTime) object).isNull;
    }

    /**
     Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();

        // clear all aspects of the time that are not used
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public java.util.Date dateValue() {
        return isNull ? null : date;
    }

    public boolean equals(Object obj) {
        if(obj instanceof DateTime) {
            DateTime d = (DateTime) obj;
            return d.date.equals(date);
        }
 	    return super.equals(obj);
    }
    
    /**
     *  Return true if the time stamp is blank
     */
    public boolean isEmpty() {
        return isNull;
    }

    /**
     returns true if the time stamp of this object has the same value as the specified time
     */
    public boolean isEqualTo(Magnitude timeStamp) {
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
     returns true if the timeStamp of this object is earlier than the specified timeStamp
     */
    public boolean isLessThan(Magnitude timeStamp) {
        if (timeStamp instanceof DateTime) {
            return !isNull && !timeStamp.isEmpty() &&
            date.before(((DateTime) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    public int getDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    public int getHour() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR);
    }

    public int getMinute() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    public long longValue() {
        return date.getTime();
    }

    public void parse(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            text = text.trim();

            String str = text.toLowerCase();
            Calendar cal = createCalendar();

            if (str.equals("today") || str.equals("now")) {
            } else if (str.startsWith("+")) {
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
                DateFormat[] formats = new DateFormat[] {
                        LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT
                    };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        cal.setTime(formats[i].parse(text));

                        break;
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new ValueParseException(e,
                                "Invalid timeStamp " + text);
                        }
                    }
                }
            }

            set(cal);
            isNull = false;
        }
    }

    /**
     * Reset this time so it contains the current time.
     * @see org.nakedobjects.object.NakedValue#reset()
     */
    public void reset() {
        date = new Date(clock.getTime());
        isNull = false;
    }

    private void set(Calendar cal) {
        date = cal.getTime();
    }

    public void setValue(java.util.Date date) {
        if (date == null) {
            isNull = true;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            this.date = cal.getTime();
        }
    }

    public void setValue(long time) {
        isNull = false;
        this.date.setTime(time);
    }

    public void setValue(DateTime timeStamp) {
        if (timeStamp == null) {
            isNull = true;
        } else {
            date = new Date(timeStamp.date.getTime());
        }
    }

    /*
     Sets this object's timeStamp to be the same as the specified hour, minute and second.
     */
    public void setValue(int year, int month, int day, int hour, int minute,
        int second) {
        checkTime(year, month, day, hour, minute, second);

        Calendar cal = createCalendar();

        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        set(cal);
    }

    public Title title() {
        return new Title(isNull ? "" : format.format(date));
    }

    public Calendar calendarValue() {
        if (isNull) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c;
    }

    public void restoreString(String data) {
        if (data == null || data.equals("NULL")) {
            clear();
        } else {
            int year = Integer.valueOf(data.substring(0, 4)).intValue();
            int month = Integer.valueOf(data.substring(4, 6)).intValue();
            int day = Integer.valueOf(data.substring(6, 8)).intValue();
            int hour = Integer.valueOf(data.substring(8, 10)).intValue();
            int minute = Integer.valueOf(data.substring(10, 12)).intValue();
            int second = Integer.valueOf(data.substring(12,14)).intValue();
            setValue(year, month, day, hour, minute, second);
        }
    }

    public String saveString() {
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
        return title() + " " + longValue() + " [DateTime]";
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
