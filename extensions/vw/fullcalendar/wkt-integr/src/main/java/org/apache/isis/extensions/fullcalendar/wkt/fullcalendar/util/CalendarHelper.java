package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.util;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CalendarHelper {

    /**
     * Converts start and end of a calendar event between local (server) and remote (client) time zone.
     * @param calendar FullCalendar to get the timezone config from
     * @return Pair of DateTimes (start, end) representing the converted dates/times of the event
     */
    public ImmutablePair<ZonedDateTime, ZonedDateTime> getInterval(final FullCalendar calendar) {
        val clientZoneOffset = calendar.clientZoneOffset();
        var start = ZonedDateTime.ofInstant(calendar.startInstant(), clientZoneOffset);
        var end = ZonedDateTime.ofInstant(calendar.endInstant(), clientZoneOffset);
        return ImmutablePair.of(start, end);
    }

}

