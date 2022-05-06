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

import org.apache.wicket.util.lang.Objects;

public class EventManager {
	private FullCalendar calendar;

	EventManager(FullCalendar calendar) {
		this.calendar = calendar;
	}

	public EventSource getEventSource(String id) throws EventSourceNotFoundException {

		for (EventSource source : calendar.getConfig().getEventSources()) {
			if (Objects.equal(id, source.getId())) {
				return source;
			}
		}
		throw new EventSourceNotFoundException("Event source with uuid: " + id + " not found");
	}

	public Event getEvent(String sourceId, String eventId) throws EventSourceNotFoundException, EventNotFoundException {
		return getEventSource(sourceId).getEventProvider().getEventForId(eventId);
	}
}
