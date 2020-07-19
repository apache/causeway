package org.apache.isis.applib.jaxb;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JavaTimeXMLGregorianCalendarMarshalling {

    public static LocalDate toLocalDate(XMLGregorianCalendar cal) {
        return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
    }

    public static LocalTime toLocalTime(XMLGregorianCalendar cal) {
        return LocalTime.of(cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000);
    }

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar cal) {
        return LocalDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000);
    }

    public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar cal) {
        return OffsetDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static OffsetTime toOffsetTime(XMLGregorianCalendar cal) {
        return OffsetTime.of(
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static ZonedDateTime toZonedDateTime(XMLGregorianCalendar cal) {
        return ZonedDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(),
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(LocalDate localDate) {
        return localDate!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
                        localDate.getYear(),
                        localDate.getMonthValue(),
                        localDate.getDayOfMonth(),
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(LocalTime localTime) {
        return localTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        localTime.getHour(),
                        localTime.getMinute(),
                        localTime.getSecond(),
                        localTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(LocalDateTime localDateTime) {
        return localDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        localDateTime.getYear(),
                        localDateTime.getMonthValue(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHour(),
                        localDateTime.getMinute(),
                        localDateTime.getSecond(),
                        localDateTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(OffsetTime offsetTime) {
        return offsetTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        offsetTime.getHour(),
                        offsetTime.getMinute(),
                        offsetTime.getSecond(),
                        offsetTime.getNano()/1000_000, // millis
                        offsetTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(OffsetDateTime offsetDateTime) {
        return offsetDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        offsetDateTime.getYear(),
                        offsetDateTime.getMonthValue(),
                        offsetDateTime.getDayOfMonth(),
                        offsetDateTime.getHour(),
                        offsetDateTime.getMinute(),
                        offsetDateTime.getSecond(),
                        offsetDateTime.getNano()/1000_000, // millis
                        offsetDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar2(ZonedDateTime zonedDateTime) {
        return zonedDateTime!=null
                ? DataTypeFactory.withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        zonedDateTime.getYear(),
                        zonedDateTime.getMonthValue(),
                        zonedDateTime.getDayOfMonth(),
                        zonedDateTime.getHour(),
                        zonedDateTime.getMinute(),
                        zonedDateTime.getSecond(),
                        zonedDateTime.getNano()/1000_000, // millis
                        zonedDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))
                : null;
    }

}
