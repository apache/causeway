package org.nakedobjects.application.valueholder;

import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;
import org.nakedobjects.application.system.Clock;

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
	
	public static void setClock(Clock clock) {
	    TimeStamp.clock = clock;
	}


    /**
     Create a Time object for storing a timeStamp set to the current time.
     */
    public TimeStamp() {
        if(clock == null) {
            throw new NakedObjectRuntimeException("Clock not set up");
        }
       reset();
        isNull = false;
    }

    /**
     Create a Time object for storing a timeStamp set to the specified time.
     */
    public TimeStamp(TimeStamp timeStamp) {
        date = timeStamp.date;
        isNull = timeStamp.isNull;
    }

    public void clear() {
        isNull = true;
    }

    public void copyObject(BusinessValueHolder object) {
        if (!(object instanceof TimeStamp)) {
            throw new IllegalArgumentException(
                "Can only copy the value of  a TimeStamp object");
        }

        date = ((TimeStamp) object).date;
        isNull = ((TimeStamp) object).isNull;
    }

    /**
     Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();
        return cal;
    }

    public java.util.Date dateValue() {
        return isNull ? null : date;
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
     returns true if the timeStamp of this object is earlier than the specified timeStamp
     */
    public boolean isLessThan(Magnitude timeStamp) {
        if (timeStamp instanceof TimeStamp) {
            return !isNull && !timeStamp.isEmpty() &&
            date.before(((TimeStamp) timeStamp).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    public long longValue() {
        return date.getTime();
    }

    public void parseUserEntry(String text) throws ValueParseException {
    }

    /**
     * Reset this time so it contains the current time.
     */
    public void reset() {
        date = new Date(clock.getTime());
        isNull = false;
    }

    private void set(Calendar cal) {
        date = cal.getTime();
    }

    public Title title() {
        return new Title(isNull ? "" : ISO_LONG.format(date));
    }

    public Calendar calendarValue() {
        if (isNull) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c;
    }

    public void restoreFromEncodedString(String data) {
        if (data == null || data.equals("NULL")) {
            clear();
        } else {
            int year = Integer.valueOf(data.substring(0, 4)).intValue();
            int month = Integer.valueOf(data.substring(4, 6)).intValue();
            int day = Integer.valueOf(data.substring(6, 8)).intValue();
            int hour = Integer.valueOf(data.substring(8, 10)).intValue();
            int minute = Integer.valueOf(data.substring(10, 12)).intValue();
            int second = Integer.valueOf(data.substring(12,14)).intValue();
            int millisecond = Integer.valueOf(data.substring(14,17)).intValue();
            
            Calendar cal = createCalendar();
            
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, millisecond);
            set(cal);
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
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
