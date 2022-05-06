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
package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Getter;
import lombok.Setter;

public class ConfigNew implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private Header headerToolbar = new Header();
    private List<EventSource> eventSources = new ArrayList<EventSource>();

    @Getter @Setter
    private String themeSystem;

    @Getter @Setter
    private boolean selectable = true;

    // events
    @Setter
    private String eventClick;
    @Setter
    private String select;

    public ConfigNew add(final EventSource eventSource) {
        eventSources.add(eventSource);
        return this;
    }

    public Collection<EventSource> getEventSources() {
        return Collections.unmodifiableList(eventSources);
    }

    @JsonRawValue
    public String getEventClick() {
        return eventClick;
    }


    @JsonRawValue
    public String getSelect() {
        return select;
    }



}
