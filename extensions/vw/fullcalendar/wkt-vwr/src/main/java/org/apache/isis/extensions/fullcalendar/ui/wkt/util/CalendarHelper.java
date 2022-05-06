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
package org.apache.isis.extensions.fullcalendar.ui.wkt.util;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTimeZone;

import org.apache.isis.extensions.fullcalendar.ui.wkt.FullCalendar;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CalendarHelper {

    /**
     * Converts start and end of a calendar event between local (server) and remote (client) time zone.
     *
     * @param calendar FullCalendar to get the timezone config from
     * @param start DateTime start of calendar event
     * @param end DateTime end of calendar event
     * @return Pair of DateTimes (start, end) representing the converted dates/times of the event
     */
    @Deprecated
    public ImmutablePair<ZonedDateTime, ZonedDateTime> convertTimezone(
            @NonNull final FullCalendar calendar,
            @NonNull ZonedDateTime start,
            @NonNull ZonedDateTime end) {

        //time zone offset given by client via Ajax
        int remoteOffset = calendar.getRequest().getRequestParameters().getParameterValue("timezoneOffset").toInt();

        if (calendar.getConfig().isIgnoreTimezone()) {
            // Convert to same DateTime in local time zone.
            int localOffset = DateTimeZone.getDefault().getOffset(null) / 60000;
            int minutesAdjustment = remoteOffset - localOffset;
            start = start.plusMinutes(minutesAdjustment);
            end = end.plusMinutes(minutesAdjustment);
        }
        return ImmutablePair.of(start, end);
    }

    public ImmutablePair<ZonedDateTime, ZonedDateTime> getInterval(final FullCalendar calendar) {
        val clientZoneOffset = calendar.clientZoneOffset();
        var start = ZonedDateTime.ofInstant(calendar.startInstant(), clientZoneOffset);
        var end = ZonedDateTime.ofInstant(calendar.endInstant(), clientZoneOffset);
        return ImmutablePair.of(start, end);
    }

}
