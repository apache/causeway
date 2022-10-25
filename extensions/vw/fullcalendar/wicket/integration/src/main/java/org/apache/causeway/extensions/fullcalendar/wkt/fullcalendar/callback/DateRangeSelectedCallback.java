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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Request;
import org.joda.time.DateTime;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;

public abstract class DateRangeSelectedCallback
extends AbstractAjaxCallback
implements CallbackWithHandler {

    private static final long serialVersionUID = 1L;

	@Override
	protected String configureCallbackScript(final String script, final String urlTail) {
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
	protected void respond(final AjaxRequestTarget target) {
		Request r = getCalendar().getRequest();

		DateTime start = new DateTime(r.getRequestParameters().getParameterValue("startDate").toLong());
		DateTime end = new DateTime(r.getRequestParameters().getParameterValue("endDate").toLong());

		boolean allDay = r.getRequestParameters().getParameterValue("allDay").toBoolean();
		onSelect(new SelectedRange(start, end, allDay), new CalendarResponse(getCalendar(), target));

	}

	protected abstract void onSelect(SelectedRange range, CalendarResponse response);

}
