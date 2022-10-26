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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.selector.EventSourceSelector;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain = true)
@ToString
public class EventSource implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Const {
        public static final String TITLE = "fcxTitle";
        public static final String UUID = "fcxUuid";
    }

	private String id;
	private String color;
	private String backgroundColor;
	private String borderColor;
	private String textColor;
	private String className;
	private boolean editable;

	private boolean defaultAllDay;

	private Map<String, Object> extraParams = new HashMap<String, Object>();

	@JsonRawValue
    private String events;

	@JsonIgnore
	private EventProvider eventProvider;

    /** If <var>enableInSelector</var> is {@code true} then the check box for this EventSource, if included in a
     * {@link EventSourceSelector} to begin with, will be enabled. If {@code false} then the check box will not be
     * enabled. Default is {@code true}.
     */
	private boolean enableInSelector = true;

	/**
     * If <var>includeInSelector</var> is {@code true} then this EventSource will be included in a
     * {@link EventSourceSelector}, if one exists for the {@link FullCalendar} containing this EventSource. If
     * {@code false} then this EventSource will not be included. Default is {@code true}.
     */
	private boolean includeInSelector = true;

	public EventSource setTitle(final String title) {
		extraParams.put(Const.TITLE, title);
		return this;
	}

	@JsonIgnore
	public String getTitle() {
		return (String) extraParams.get(Const.TITLE);
	}

	@JsonIgnore
	public Event getEventById(final String id) {
	    return eventProvider!=null
	            ? getEventProvider().getEventForId(id)
                : null;
	}

}
