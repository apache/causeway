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

import org.apache.isis.extensions.fullcalendar.ui.wkt.CalendarResponse;
import org.apache.isis.extensions.fullcalendar.ui.wkt.util.CalendarHelper;

import lombok.NonNull;
import lombok.val;

/**
 * Callback that's executed when a range of dates is selected in the calendar.
 */
public abstract class DateRangeSelectedCallback
extends AbstractAjaxCallback
implements CallbackWithHandler {

    private static final long serialVersionUID = 1L;

    @Override
    protected String configureCallbackScript(@NonNull final String script, @NonNull final String urlTail) {
        /*
         * According to https://fullcalendar.io/docs/select-callback the prior "allDay" parameter is now obsolete
         * and can be reproduced by checking start.hasTime() and end.hasTime().
         * */
        return script.replace(urlTail,
                "&timezoneOffset=\"+startDate.utcOffset()+\"&"
                + "startDate=\"+startDate.valueOf()+\"&"
                + "endDate=\"+endDate.valueOf()+\"&"
                + "allDay=\"+(!(startDate"
                        + ".hasTime()||endDate.hasTime()))+\"");
    }

    /**
     * @see <a href="https://fullcalendar.io/docs/select-callback">https://fullcalendar.io/docs/select-callback</a>
     */
    @Override
    public String getHandlerScript() {
        return "function(startDate, endDate, jsEvent, view) { " + getCallbackScript() + "}";
    }

    @Override
    protected void respond(@NonNull final AjaxRequestTarget target) {

        boolean allDay = getCalendar().getRequest().getRequestParameters().getParameterValue("allDay").toBoolean();

        val dateRange = CalendarHelper.getInterval(getCalendar());
        val start = dateRange.getLeft().toLocalDateTime();
        val end = dateRange.getRight().toLocalDateTime();

        // create response / run callback method
        onSelect(new SelectedRange(start, end, allDay), new CalendarResponse(getCalendar(), target));
    }

    protected abstract void onSelect(@NonNull SelectedRange range, @NonNull CalendarResponse response);

}
