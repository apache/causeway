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
package org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.callback;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.CalendarResponse;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.ViewType;

/**
 * A base callback that passes back calendar's starting date
 */
public abstract class ViewDisplayCallback
extends AbstractAjaxCallback
implements CallbackWithHandler {

    private static final long serialVersionUID = 1L;

    @Override
	protected String configureCallbackScript(final String script, final String urlTail) {
		return script
			.replace(
				urlTail,
				"&view=\"+v.name+\"&start=\"+fullCalendarExtIsoDate(v.start)+\"&end=\"+fullCalendarExtIsoDate(v.end)+\"&visibleStart=\"+fullCalendarExtIsoDate(v.visStart)+\"&visibleEnd=\"+fullCalendarExtIsoDate(v.visEnd)+\"");
	}

	@Override
	public String getHandlerScript() {
		return String.format("function(v) {%s;}", getCallbackScript());
	}

	@Override
	protected void respond(final AjaxRequestTarget target) {
		var request = target.getPage().getRequest();
		var view = new View(
		    ViewType.forCode(request.getRequestParameters().getParameterValue("view").toString()),
		    request.getRequestParameters().getParameterValue("start").toString(),
		    request.getRequestParameters().getParameterValue("end").toString(),
		    request.getRequestParameters().getParameterValue("visibleStart").toString(),
		    request.getRequestParameters().getParameterValue("visibleEnd").toString());
		var response = new CalendarResponse(getCalendar(), target);
		onViewDisplayed(view, response);
	}

	protected abstract void onViewDisplayed(View view, CalendarResponse response);
}
