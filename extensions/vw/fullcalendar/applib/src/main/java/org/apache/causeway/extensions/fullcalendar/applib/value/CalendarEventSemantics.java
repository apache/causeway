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
package org.apache.causeway.extensions.fullcalendar.applib.value;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._StringInterpolation;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@Component
@Import({
    CalendarEventSemantics.CalendarEvent_default.class
})
public class CalendarEventSemantics
extends ValueSemanticsAbstract<CalendarEvent>
implements
    DefaultsProvider<CalendarEvent>,
    Renderer<CalendarEvent> {

    @Inject private TemporalValueSemantics<ZonedDateTime> zonedDateTimeValueSemantics;

    @Override
    public Class<CalendarEvent> getCorrespondingClass() {
        return CalendarEvent.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.COMPOSITE;
    }

    // -- DEFAULTS PROVIDER

    @Override
    public CalendarEvent getDefaultValue() {
        return new CalendarEvent(
                Instant.now().toEpochMilli(), "Default Calendar", "New Event", "empty");
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final CalendarEvent value) {
        return CommonDtoUtils.typedTupleBuilder(value)
                .addFundamentalType(ValueType.LONG, "epochMillis", CalendarEvent::getEpochMillis)
                .addFundamentalType(ValueType.STRING, "calendarName", CalendarEvent::getCalendarName)
                .addFundamentalType(ValueType.STRING, "title", CalendarEvent::getTitle)
                .addFundamentalType(ValueType.STRING, "notes", CalendarEvent::getNotes)
                .buildAsDecomposition();
    }

    @Override
    public CalendarEvent compose(final ValueDecomposition decomposition) {

        final TypedTupleDto dto = decomposition.rightIfAny();

        val elementMap = CommonDtoUtils.typedTupleAsMap(dto);

        final ZonedDateTime dateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli((long)elementMap.get("epochMillis")),
                ZoneId.systemDefault());
        final String calendarName = (String)elementMap.get("calendarName");
        final String title = (String)elementMap.get("title");
        final String notes = (String)elementMap.get("notes");

        return CalendarEvent.of(dateTime, calendarName, title, notes);
    }

    // -- RENDERER

    private final String titleTemplate =
            "[${calendar-name}] ${title} @ ${timestamp}";

    @Override
    public String titlePresentation(final Context context, final CalendarEvent value) {
        return renderTitle(value, v->{
            val title = new _StringInterpolation(toMap(context, value))
                    .applyTo(titleTemplate);
            return title;
        });
    }

    private final Can<String> htmlTemplate = _Text.readLinesFromResource(this.getClass(),
            "CalendarEvent.html", StandardCharsets.UTF_8)
            .stream()
            .skip(20)
            .collect(Can.toCan());

    @Override
    public String htmlPresentation(final Context context, final CalendarEvent value) {
        return renderHtml(value, v->{
            val html = new _StringInterpolation(toMapHtmlEscaped(context, value))
                    .applyTo(htmlTemplate)
                    .stream()
                    .collect(Collectors.joining());
            return html;
        });
    }

    private Map<String, @NonNull String> toMap(
            final Context context,
            final CalendarEvent v) {
        return Map.of(
                "title", v.getTitle(),
                "calendar-name", v.getCalendarName(),
                "timestamp", zonedDateTimeValueSemantics
                    .htmlPresentation(context,
                            v.asDateTime(context.getInteractionContext().getTimeZone())),
                "notes", _Strings.nullToEmpty(v.getNotes()));
    }

    private Map<String, @NonNull String> toMapHtmlEscaped(
            final Context context,
            final CalendarEvent v) {
        return Map.of(
                "title", _Strings.htmlEscape(v.getTitle()),
                "calendar-name", _Strings.htmlEscape(v.getCalendarName()),
                "timestamp", zonedDateTimeValueSemantics
                    .htmlPresentation(context,
                            v.asDateTime(context.getInteractionContext().getTimeZone())),
                "notes", _Strings.htmlEscape(_Strings.nullToEmpty(v.getNotes())));
    }


    // -- EXAMPLES

    @Override
    public Can<CalendarEvent> getExamples() {

        val a = CalendarEvent.of(
                ZonedDateTime.of(2022, 05, 13, 17, 30, 15, 0, ZoneOffset.ofHours(3)),
                "Business",
                "Weekly Meetup",
                "Calendar Notes");

        val b = CalendarEvent.of(
                ZonedDateTime.of(2022, 06, 14, 18, 31, 16, 0, ZoneOffset.ofHours(4)),
                "Private",
                "Dentist Appointment",
                "Calendar Notes");

        val c = CalendarEvent.of(
                ZonedDateTime.of(2022, 07, 15, 19, 32, 17, 0, ZoneOffset.ofHours(5)),
                "Family and Friends",
                "Birthday Party");

        return Can.of(a, b, c);
    }

    // -- EMBEDDING

    // typed tuple of fundamental types
    @Value @Accessors(fluent = true)
    public static class Parameters {
        final LocalDateTime dateTime;
        final String calendarName;
        final String title;
        final String notes;

        public CalendarEvent construct() {
            val zoneId = //context.getInteractionContext().getTimeZone();
                    ZoneId.systemDefault();
            return new CalendarEvent(
                    dateTime().atZone(zoneId).toInstant().toEpochMilli(),
                    calendarName(),
                    title(),
                    notes());
        }

        public static Parameters deconstruct(
                //final ValueSemanticsProvider.Context context,
                final CalendarEvent value) {
            val zoneId = //context.getInteractionContext().getTimeZone();
                    ZoneId.systemDefault();
            return new Parameters(
                    value.asDateTime(zoneId).toLocalDateTime(),
                    value.getCalendarName(),
                    value.getTitle(),
                    value.getNotes());
        }

    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
    @RequiredArgsConstructor
    public static class CalendarEvent_default {

        private final CalendarEvent mixee;

        @MemberSupport
        public CalendarEvent act(
                final LocalDateTime dateTime,
                final String calendarName,
                final String title,
                @Parameter(optionality = Optionality.OPTIONAL)
                @ParameterLayout(multiLine = 4)
                final String notes) {

            val p = new Parameters(
                    dateTime,
                    calendarName,
                    title,
                    notes);

            return p.construct();
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
            return mixee;
        }

    }



}
