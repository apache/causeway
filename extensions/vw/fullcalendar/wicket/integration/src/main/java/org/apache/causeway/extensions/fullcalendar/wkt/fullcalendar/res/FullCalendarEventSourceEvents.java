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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res;

import java.util.Map;
import java.util.function.Function;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventSource;
import org.apache.causeway.viewer.wicket.ui.util.LicensedTextTemplate;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

public class FullCalendarEventSourceEvents
extends LicensedTextTemplate {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final FullCalendarEventSourceEvents instance =
        new FullCalendarEventSourceEvents();

    private FullCalendarEventSourceEvents() {
        // skip 23 leading lines in referenced java-script
        super(FullCalendarEventSourceEvents.class, "fullcalendar-event-source-events.js", 23);
    }

    public static void setupEventSourceUrls(
            final CalendarConfig calendarConfig,
            final Function<EventSource, String> eventSourceUrlProvider) {
        val instance = instance();
        for (val eventSource : calendarConfig.getEventSources()) {
            eventSource.setEvents(instance.asString(Map.of("url", eventSourceUrlProvider.apply(eventSource))));
        }
    }

}
