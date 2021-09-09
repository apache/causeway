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
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Value;

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
@Value(semanticsProviderClass=CalendarEventSemanticsProvider.class)
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

    public ZonedDateTime asDateTime(final ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId);
    }

    public ZonedDateTime asDateTime() {
        return asDateTime(ZoneId.systemDefault());
    }

    @Override
    public int compareTo(final CalendarEvent other) {
        return Long.compare(this.epochMillis, other.getEpochMillis());
    }

}
