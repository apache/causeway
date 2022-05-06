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

package org.apache.isis.extensions.fullcalendar.ui.wkt.callback;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Request;

import org.apache.isis.extensions.fullcalendar.ui.wkt.CalendarResponse;
import org.apache.isis.extensions.fullcalendar.ui.wkt.Event;
import org.apache.isis.extensions.fullcalendar.ui.wkt.EventSource;

import lombok.NonNull;

public abstract class EventDroppedCallback
extends AbstractAjaxCallbackWithClientsideRevert
implements CallbackWithHandler {

    private static final long serialVersionUID = 1L;

    @Override
    protected String configureCallbackScript(@NonNull final String script, @NonNull final String urlTail) {
        return script.replace(urlTail, "&eventId=\"+event.id+\"&"
                + "sourceId=\"+event.source.id+\"&"
                + "minuteDelta=\"+delta.asMinutes()+\"&"
                + "allDay=\"+event.start"
                + ".hasTime()+\"");
    }

    @Override
    public String getHandlerScript() {
        return "function(event, delta, revertFunc, jsEvent, ui, view) {" + getCallbackScript() + "}";
    }

	@Override
	protected boolean onEvent(final @NonNull AjaxRequestTarget target) {
		Request r = getCalendar().getRequest();
		String eventId = r.getRequestParameters().getParameterValue("eventId").toString();
		String sourceId = r.getRequestParameters().getParameterValue("sourceId").toString();

		EventSource source = getCalendar().getEventManager().getEventSource(sourceId);
		Event event = source.getEventProvider().getEventForId(eventId);

        // minuteDelta already contains the complete delta in minutes, so we can set daysDelta to 0
		int dayDelta = 0;
        int minuteDelta = r.getRequestParameters().getParameterValue("minuteDelta").toInt();
        boolean allDay = r.getRequestParameters().getParameterValue("allDay").toBoolean();

		return onEventDropped(new DroppedEvent(source, event, dayDelta, minuteDelta, allDay), new CalendarResponse(
			getCalendar(), target));
	}

	protected abstract boolean onEventDropped(@NonNull DroppedEvent event, @NonNull CalendarResponse response);

	@Override
	protected String getRevertScript() {
		return "revertFunc();";
	}

}
