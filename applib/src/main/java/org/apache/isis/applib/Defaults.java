package org.apache.isis.applib;

import java.util.Calendar;

import org.joda.time.DateTimeZone;

public class Defaults {
    static {
        setTimeZone(DateTimeZone.UTC);
    }

    static DateTimeZone timeZone = DateTimeZone.UTC;

    public static DateTimeZone getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(DateTimeZone timezone) {
        timeZone = timezone;
        calendar = Calendar.getInstance(timezone.toTimeZone());
    }

    private static Calendar calendar;

    public static Calendar getCalendar() {
        return calendar;
    }

}
