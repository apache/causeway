package org.apache.isis.applib.jaxb;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JodaTimeXMLGregorianCalendarMarshalling {

    public static DateTime toDateTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final GregorianCalendar gc = xgc.toGregorianCalendar();
        final Date time = gc.getTime();
        final TimeZone timeZone = gc.getTimeZone();

        final DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        return new DateTime(time, dateTimeZone);
    }

    public static LocalDate toLocalDate(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final int year = xgc.getYear();
        final int month = xgc.getMonth();
        final int day = xgc.getDay();

        return new LocalDate(year, month, day);
    }

    public static LocalDateTime toLocalDateTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final int year = xgc.getYear();
        final int month = xgc.getMonth();
        final int day = xgc.getDay();
        final int hour = xgc.getHour();
        final int minute = xgc.getMinute();
        final int second = xgc.getSecond();
        final int millisecond = xgc.getMillisecond();

        return new LocalDateTime(year, month, day, hour, minute, second, millisecond);
    }

    public static LocalTime toLocalTime(final XMLGregorianCalendar xgc) {
        if(xgc == null) {
            return null;
        }

        final int hour = xgc.getHour();
        final int minute = xgc.getMinute();
        final int second = xgc.getSecond();
        final int millisecond = xgc.getMillisecond();

        return new LocalTime(hour, minute, second, millisecond);
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(DateTime dateTime) {
        return dateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        dateTime.toGregorianCalendar()))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalDateTime localDateTime) {
        return localDateTime !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        localDateTime.getYear(),
                        localDateTime.getMonthOfYear(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHourOfDay(),
                        localDateTime.getMinuteOfHour(),
                        localDateTime.getSecondOfMinute(),
                        localDateTime.getMillisOfSecond(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalDate localDate) {
        return localDate !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
                        localDate.getYear(),
                        localDate.getMonthOfYear(),
                        localDate.getDayOfMonth(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final LocalTime localTime) {
        return localTime !=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        localTime.getHourOfDay(),
                        localTime.getMinuteOfHour(),
                        localTime.getSecondOfMinute(),
                        localTime.getMillisOfSecond(),
                        DatatypeConstants.FIELD_UNDEFINED
                        ))
                : null;
    }

}
