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

import java.util.NoSuchElementException;

import org.apache.wicket.util.lang.Objects;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class EventManager {

    private @NonNull FullCalendar calendar;

	public EventSource getEventSource(final String id) throws NoSuchElementException {
		for (EventSource source : calendar.getConfig().getEventSources()) {
			if (Objects.equal(id, source.getId())) {
				return source;
			}
		}
		throw new NoSuchElementException("Event source with uuid: " + id + " not found");
	}

	public Event getEvent(final String sourceId, final String eventId) throws NoSuchElementException {
		return getEventSource(sourceId).getEventProvider().getEventForId(eventId);
	}
}
