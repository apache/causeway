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
package org.apache.isis.extensions.fullcalendar.applib.value;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;

/**
 * Value type representing an event on a calendar.
 * @since 2.0 {@index}
 * @apiNote implements Comparable<CalendarEvent> based on epochMillis
 */
@org.apache.isis.applib.annotation.Value(
        logicalTypeName = IsisModuleApplib.NAMESPACE + ".value.CalendarEvent")
@Getter @With
@ToString @EqualsAndHashCode
@AllArgsConstructor
public class CalendarEvent
implements
    Comparable<CalendarEvent>,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final long epochMillis;
    private final @NonNull String calendarName;
    private final @NonNull String title;
    private final @Nullable String notes;

    // -- FACTORIES

    public static CalendarEvent of(
            final @NonNull ZonedDateTime dateTime,
            final @NonNull String calendarName,
            final @NonNull String title) {
        return of(dateTime, calendarName, title, null);
    }

    public static CalendarEvent of(
            final @NonNull ZonedDateTime dateTime,
            final @NonNull String calendarName,
            final @NonNull String title,
            final @Nullable String notes) {
        return new CalendarEvent(dateTime.toInstant().toEpochMilli(), calendarName, title, notes);
    }

    // -- ADDITIONAL WITHERS

    public CalendarEvent withDateTime(final @NonNull ZonedDateTime dateTime) {
        return new CalendarEvent(dateTime.toInstant().toEpochMilli(), calendarName, title, notes);
    }

    public CalendarEvent withDateTime(final @NonNull LocalDateTime localDateTime, final @NonNull ZoneId zoneId) {
        return withDateTime(ZonedDateTime.of(localDateTime, zoneId));
    }

    // -- CONVERTERS

    public ZonedDateTime asDateTime(final @NonNull ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId);
    }

    //XXX potential misuse
//    public ZonedDateTime asDateTime() {
//        return asDateTime(ZoneId.systemDefault());
//    }

    // -- OBJECT CONTRACT

    @Override
    public int compareTo(final CalendarEvent other) {
        return Long.compare(this.epochMillis, other.getEpochMillis());
    }

}
