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
package org.apache.causeway.extensions.fullcalendar.wkt.integration.fc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import org.apache.causeway.commons.io.JsonUtils;

import lombok.Getter;
import lombok.Setter;

public class CalendarConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @see "https://fullcalendar.io/docs/headerToolbar"
     */
    @Getter
    private Header headerToolbar = new Header();

    /**
     * @see "https://fullcalendar.io/docs/themeSystem"
     */
    @Getter @Setter
    private String themeSystem; //= "bootstrap5" TODO needs some CSS overrides

    /**
     * @see "https://fullcalendar.io/docs/selectable"
     */
    @Getter @Setter
    private boolean selectable = true;

    /**
     * @see "https://fullcalendar.io/docs/allDaySlot"
     */
    @Getter @Setter
    private boolean allDaySlot = true;

    // -- EVENTS

    /**
     * @see "https://fullcalendar.io/docs/loading"
     */
    @JsonRawValue
    @Getter @Setter
    private String loading = "function(b) { let el = $('#fullCalendar-loading'); if (b) el.show(); else el.hide(); }";

    /**
     * @see "https://fullcalendar.io/docs/eventClick"
     */
    @JsonRawValue
    @Getter @Setter
    private String eventClick;

    /**
     * @see "https://fullcalendar.io/docs/select"
     */
    @JsonRawValue
    @Getter @Setter
    private String select;

    private List<EventSource> eventSources = new ArrayList<EventSource>();
    public Collection<EventSource> getEventSources() {
        return Collections.unmodifiableList(eventSources);
    }

    public CalendarConfig addEventSource(final EventSource eventSource) {
        eventSources.add(eventSource);
        return this;
    }

    // -- SERIALIZE

    public String toJson() {
        return JsonUtils.toStringUtf8(this);
    }

}
