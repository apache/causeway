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
package demoapp.dom.types.isisext.cal.jpa;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEventSemanticsProvider.Parameters;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Profile("demo-jpa")
@Action(
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "readWriteProperty"
        //promptStyle = PromptStyle.INLINE_AS_IF_EDIT
        )
@RequiredArgsConstructor
//FIXME[ISIS-2877] intermediate, remove when resolved
public class IsisCalendarEventJpa_update {

    private final IsisCalendarEventJpa mixee;

    @MemberSupport
    public IsisCalendarEventJpa act(
            final LocalDateTime dateTime,
            final String calendarName,
            final String title,
            final String notes) {

        val p = new Parameters(
                dateTime,
                calendarName,
                title,
                notes);

        mixee.setReadWriteProperty(p.construct());
        return mixee;
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
        return mixee.getReadWriteProperty();
    }

}
