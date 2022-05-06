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

package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.string.StringValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;

public abstract class DateRangeSelectedCallback extends AbstractAjaxCallback implements CallbackWithHandler {
	private final boolean ignoreTimezone;

	/**
	 * If <var>ignoreTimezone</var> is {@code true}, then the remote client\"s time zone will be ignored when
	 * determining the selected date range, resulting in a range with the selected start and end values, but in the
	 * server\"s time zone.
	 * 
	 * @param ignoreTimezone
	 *            whether or not to ignore the remote client\"s time zone when determining the selected date range
	 */
	public DateRangeSelectedCallback(final boolean ignoreTimezone) {
		this.ignoreTimezone = ignoreTimezone;
	}

	@Override
	protected String configureCallbackScript(String script, String urlTail) {
		return script
			.replace(
				urlTail,
				"&startDate=\"+info.start[Symbol.toPrimitive]('number')+\"&endDate=\"+info.end[Symbol.toPrimitive]('number')+\"&allDay=\"+info.allDay+\"");  //&timezoneOffset="+info.start.getTimezoneOffset()+"
	}

	@Override
	public String getHandlerScript() {
		return "function(info) { " + getCallbackScript() + "}";
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		Request r = getCalendar().getRequest();

		DateTime start = new DateTime(r.getRequestParameters().getParameterValue("startDate").toLong());
		DateTime end = new DateTime(r.getRequestParameters().getParameterValue("endDate").toLong());

//		DateTime start = new DateTime(parseDateTime(r, "startDate"));
//		DateTime end = new DateTime(parseDateTime(r, "endDate"));

//		if (ignoreTimezone) {
//			// Convert to same DateTime in local time zone.
//			int remoteOffset = -r.getRequestParameters().getParameterValue("timezoneOffset").toInt();
//			int localOffset = DateTimeZone.getDefault().getOffset(null) / 60000;
//			int minutesAdjustment = remoteOffset - localOffset;
//			start = start.plusMinutes(minutesAdjustment);
//			end = end.plusMinutes(minutesAdjustment);
//		}
		boolean allDay = r.getRequestParameters().getParameterValue("allDay").toBoolean();
		onSelect(new SelectedRange(start, end, allDay), new CalendarResponse(getCalendar(), target));

	}

	private DateTime parseDateTime(Request r, String param) {
		StringValue value = r.getRequestParameters().getParameterValue(param);
		if (value == null) {
			return null;
		}
		return DateTime.parse(value.toString());
	}

	protected abstract void onSelect(SelectedRange range, CalendarResponse response);

}
