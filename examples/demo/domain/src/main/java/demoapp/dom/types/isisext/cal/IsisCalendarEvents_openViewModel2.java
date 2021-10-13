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
package demoapp.dom.types.isisext.cal;

import java.time.LocalDateTime;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEventSemanticsProvider.Parameters;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.types.isisext.cal.vm.IsisCalendarEventVm;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
//FIXME[ISIS-2877] intermediate, remove when resolved
public class IsisCalendarEvents_openViewModel2 {

    private final IsisCalendarEvents mixee;

    @MemberSupport
    public IsisCalendarEventVm act(
            final LocalDateTime dateTime,
            final String calendarName,
            final String title,
            final String notes) {

        val p = new Parameters(
                dateTime,
                calendarName,
                title,
                notes);

        return new IsisCalendarEventVm(p.construct());
    }

    @MemberSupport
    public LocalDateTime defaultDateTime(final Parameters p) {
        return Parameters.deconstruct(currentValue()).dateTime();
    }

    @MemberSupport
    public String defaultCalendarName(final Parameters p) {
        return Parameters.deconstruct(currentValue()).calendarName();
    }

    @MemberSupport
    public String defaultTitle(final Parameters p) {
        return Parameters.deconstruct(currentValue()).title();
    }

    @MemberSupport
    public String defaultNotes(final Parameters p) {
        return Parameters.deconstruct(currentValue()).notes();
    }

    // -- HELPER

    private CalendarEvent currentValue() {
        return mixee.default0OpenViewModel();
    }

}
