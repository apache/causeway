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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.string.Strings;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;

import lombok.Getter;
import lombok.experimental.Accessors;

public class FullCalendarIntegrationJsReference
extends JavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final FullCalendarIntegrationJsReference instance =
        new FullCalendarIntegrationJsReference();

    private FullCalendarIntegrationJsReference() {
        super(FullCalendarIntegrationJsReference.class, "fullcalendar-integration.js");
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }

    public static OnDomReadyHeaderItem domReadyScript(
            final String markupId,
            final CalendarConfig calendarConfig) {
        return OnDomReadyHeaderItem.forScript(
                calendarResponseScript(
                markupId,
                calendarConfig.toJson()));
    }

    public static String calendarResponseScript(
            final String markupId,
            final String... args) {
        return String.format("$('#%s').fullCalendarExt(%s);",
                markupId,
                Strings.join(",", args));
    }

}
