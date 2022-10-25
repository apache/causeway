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

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarIntegrationJsReference;

public class CalendarResponse {
	private final FullCalendar calendar;
	private final AjaxRequestTarget target;

	public CalendarResponse(final FullCalendar calendar, final AjaxRequestTarget target) {
		this.calendar = calendar;
		this.target = target;
	}

	public CalendarResponse refetchEvents() {
		return execute(q("refetchEvents"));
	}

	public CalendarResponse refetchEvents(final EventSource source) {
		toggleEventSource(source, false);
		return toggleEventSource(source, true);
	}

	public CalendarResponse refetchEvents(final String sourceId) {
		toggleEventSource(sourceId, false);
		return toggleEventSource(sourceId, true);
	}

	public CalendarResponse refetchEvent(final EventSource source, final Event event) {
		// for now we have an unoptimized implementation
		// later we can replace this by searching for the affected event in the
		// clientside buffer
		// and refetching it

		return refetchEvents(source);
	}

	public CalendarResponse refetchEvent(final String sourceId, final String eventId) {
		// for now we have an unoptimized implementation
		// later we can replace this by searching for the affected event in the
		// clientside buffer
		// and refetching it

		return refetchEvents(sourceId);
	}

	public CalendarResponse toggleEventSource(final String sourceId, final boolean enabled) {
		return execute(q("toggleSource"), q(sourceId), String.valueOf(enabled));
	}

	public CalendarResponse toggleEventSource(final EventSource source, final boolean enabled) {
		return execute(q("toggleSource"), q(source.getId()), String.valueOf(enabled));
	}

	public CalendarResponse removeEvent(final String id) {
		return execute(q("removeEvents"), q(id));
	}

	public CalendarResponse removeEvent(final Event event) {
		return execute(q("removeEvents"), q(event.getId()));
	}

	public CalendarResponse gotoDate(final Date date) {
		return execute(q("gotoDate"), "new Date(" + date.getTime() + ")");
	}

	public AjaxRequestTarget getTarget() {
		return target;
	}

	private CalendarResponse execute(final String... args) {
		target.appendJavaScript(
		        FullCalendarIntegrationJsReference.calendarResponseScript(
		                calendar.getMarkupId(), args));
		return this;
	}

	private static final String q(final Object o) {
		if (o == null) {
			return "null";
		}
		return "'" + o.toString() + "'";
	}

	/**
	 * Clears the client-side selection highlight.
	 *
	 * @return this for chaining
	 *
	 */
	public CalendarResponse clearSelection() {
		return execute(q("unselect"));
	}

}
