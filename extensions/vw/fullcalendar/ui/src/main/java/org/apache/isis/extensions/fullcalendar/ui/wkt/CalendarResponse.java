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
package org.apache.isis.extensions.fullcalendar.ui.wkt;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class CalendarResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final @NonNull FullCalendar calendar;
    @Getter private final @NonNull AjaxRequestTarget target;

    public CalendarResponse refetchEvents() {
        return execute(q("refetchEvents"));
    }

    public CalendarResponse refetchEvents(@NonNull final EventSource source) {
        toggleEventSource(source, false);
        return toggleEventSource(source, true);
    }

    public CalendarResponse refetchEvents(@Nullable final String sourceId) {
        toggleEventSource(sourceId, false);
        return toggleEventSource(sourceId, true);
    }

    public CalendarResponse refetchEvent(@NonNull final EventSource source, @Nullable final Event event) {
        // for now we have an unoptimized implementation
        // later we can replace this by searching for the affected event in the
        // clientside buffer
        // and refetching it
        return refetchEvents(source);
    }

    public CalendarResponse refetchEvent(@Nullable final String sourceId, @Nullable final String eventId) {
        // for now we have an unoptimized implementation
        // later we can replace this by searching for the affected event in the
        // clientside buffer
        // and refetching it
        return refetchEvents(sourceId);
    }

    public CalendarResponse toggleEventSource(@Nullable final String sourceId, final boolean enabled) {
        return execute(q("toggleSource"), q(sourceId), String.valueOf(enabled));
    }

    public CalendarResponse toggleEventSource(@NonNull final EventSource source, final boolean enabled) {
        return execute(q("toggleSource"), q(source.getUuid()), String.valueOf(enabled));
    }

    public CalendarResponse removeEvent(@Nullable final String id) {
        return execute(q("removeEvents"), q(id));
    }

    public CalendarResponse removeEvent(@NonNull final Event event) {
        return execute(q("removeEvents"), q(event.getId()));
    }

    public CalendarResponse gotoDate(@NonNull final Date date) {
        return execute(q("gotoDate"), "new Date(" + date.getTime() + ")");
    }

    private CalendarResponse execute(@NonNull final String... args) {
        String js = String.format("$('#%s').fullCalendarExt(" + Strings.join(",", args) + ");",
                calendar.getMarkupId());
        target.appendJavaScript(js);
        return this;
    }

    private static String q(@Nullable final Object o) {
        if (o == null) {
            return "null";
        }
        return "'" + o.toString() + "'";
    }

    /**
     * Clears the client-side selection highlight.
     *
     * @return this for chaining
     */
    public CalendarResponse clearSelection() {
        return execute(q("unselect"));
    }

}
