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

import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.wicket.request.handler.TextRequestHandler;

import org.apache.isis.extensions.fullcalendar.ui.wkt.EventSource;

import lombok.NonNull;
import lombok.val;

public class GetEventsCallback extends AbstractCallback {

    private static final long serialVersionUID = 1L;

    private static final String SOURCE_ID = "sid";

    public String getUrl(final @NonNull EventSource source) {
        return getUrl(Map.of(SOURCE_ID, source.getUuid()));
    }

    @Override
    protected void respond() {
        val request = getCalendar().getRequest();

        //val interval = CalendarHelper.getInterval(getCalendar());

        val clientZoneOffset = getCalendar().clientZoneOffset();
        var start = ZonedDateTime.ofInstant(getCalendar().startInstant(), clientZoneOffset);
        var end = ZonedDateTime.ofInstant(getCalendar().endInstant(), clientZoneOffset);

        val sid = request.getRequestParameters().getParameterValue(SOURCE_ID).toString();
        val eventSource = getCalendar().getEventManager().getEventSource(sid);
        val eventProvider = eventSource.getEventProvider();
        final String response = getCalendar().toJson(eventProvider.getEvents(start, end));

        getCalendar().getRequestCycle()
                .scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", response));

    }

    // -- HELPER



}
