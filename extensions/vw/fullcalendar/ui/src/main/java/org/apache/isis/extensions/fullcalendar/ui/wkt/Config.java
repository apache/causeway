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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * This class represents the configuration of the calendar which will be passed to FullCalendar as JSON object.
 */
@Getter @Setter
@ToString
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<EventSource> eventSources = new ArrayList<>();
    private Header header = new Header();
    private ButtonText buttonText = new ButtonText();

    @JsonRawValue
    private String loading;

    private Boolean editable;

    @JsonRawValue
    private String eventDrop;

    @JsonRawValue
    private String eventResize;

    @JsonRawValue
    private String eventClick;

    @JsonRawValue
    private String viewRender;

    private Boolean selectable;
    private Boolean selectHelper;

    /**
     * A callback that will fire after a selection is made
     */
    @JsonRawValue
    private String select;

    private String initialView;

    private LocalTime minTime;
    private LocalTime maxTime;
    private Integer firstHour;
    private Boolean allDaySlot;

    private String timeFormat;
    private String locale;

    private Integer height;

    @JsonRawValue
    private String eventRender;

    private Boolean disableDragging;
    private Boolean disableResizing;
    private Integer slotMinutes;
    private Float aspectRatio;

    @JsonIgnore
    private boolean ignoreTimezone = false;

    private boolean weekends = true;
    private int firstDay = 1;

    private boolean weekNumbers = true;
    private boolean weekNumbersWithinDays = true;

    /**
     * Whether or not to display a marker indicating the current time in agenda views
     */
    private boolean nowIndicator = true;

    /**
     * Determines if day names and week names are clickable.
     */
    private boolean navLinks = true;

    /**
     * Renders the calendar with a given theme system.
     *
     * @see <a href="https://fullcalendar.io/docs/themeSystem">https://fullcalendar.io/docs/themeSystem</a>
     */
    private String themeSystem = "standard";

    public Config add(@NonNull final EventSource eventSource) {
        eventSources.add(eventSource);
        return this;
    }

    public Collection<EventSource> getEventSources() {
        return Collections.unmodifiableList(eventSources);
    }

}

