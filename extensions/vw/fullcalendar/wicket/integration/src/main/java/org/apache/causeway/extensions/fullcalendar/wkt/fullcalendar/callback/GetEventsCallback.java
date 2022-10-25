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

import java.util.Map;

import org.apache.wicket.request.handler.TextRequestHandler;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventSource;

import lombok.val;

public class GetEventsCallback extends AbstractCallback {
    private static final long serialVersionUID = 1L;

    private static final String SOURCE_ID = "sid";

	public String getUrl(final EventSource source) {
		return getUrl(Map.of(SOURCE_ID, source.getId()));
	}

    @Override
    protected void respond() {
        val request = getCalendar().getRequest();

        var start = getCalendar().startInstant();
        var end = getCalendar().endInstant();

        val sid = request.getRequestParameters().getParameterValue(SOURCE_ID).toString();
        val eventSource = getCalendar().getEventSource(sid);
        val eventProvider = eventSource.getEventProvider();
        final String response = getCalendar().toJson(eventProvider.getEvents(start, end));

        getCalendar().getRequestCycle()
                .scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", response));

    }

}
