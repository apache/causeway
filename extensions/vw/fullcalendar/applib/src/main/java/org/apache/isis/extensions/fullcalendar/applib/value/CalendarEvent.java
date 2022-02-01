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
package org.apache.isis.extensions.fullcalendar.applib.value;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent.CalendarEventDeserializer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import lombok.val;

/**
 * Value type representing an event on a calendar.
 * @since 2.0 {@index}
 * @apiNote implements Comparable<CalendarEvent> based on epochMillis
 */
@org.apache.isis.applib.annotation.Value(
        logicalTypeName = IsisModuleApplib.NAMESPACE + ".value.CalendarEvent")
@XmlJavaTypeAdapter(CalendarEvent.JaxbAdapter.class)
@JsonDeserialize(using = CalendarEventDeserializer.class)
@Getter @With
@ToString @EqualsAndHashCode
@AllArgsConstructor
public class CalendarEvent
implements
    Comparable<CalendarEvent>,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final long epochMillis;
    private final @NonNull String calendarName;
    private final @NonNull String title;
    private final @Nullable String notes;

    // -- FACTORIES

    public static CalendarEvent of(
            final @NonNull ZonedDateTime dateTime,
            final @NonNull String calendarName,
            final @NonNull String title) {
        return of(dateTime, calendarName, title, null);
    }

    public static CalendarEvent of(
            final @NonNull ZonedDateTime dateTime,
            final @NonNull String calendarName,
            final @NonNull String title,
            final @Nullable String notes) {
        return new CalendarEvent(dateTime.toInstant().toEpochMilli(), calendarName, title, notes);
    }

    // -- ADDITIONAL WITHERS

    public CalendarEvent withDateTime(final @NonNull ZonedDateTime dateTime) {
        return new CalendarEvent(dateTime.toInstant().toEpochMilli(), calendarName, title, notes);
    }

    public CalendarEvent withDateTime(final @NonNull LocalDateTime localDateTime, final @NonNull ZoneId zoneId) {
        return withDateTime(ZonedDateTime.of(localDateTime, zoneId));
    }

    // -- CONVERTERS

    public ZonedDateTime asDateTime(final @NonNull ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId);
    }

    //XXX potential misuse
//    public ZonedDateTime asDateTime() {
//        return asDateTime(ZoneId.systemDefault());
//    }

    // -- OBJECT CONTRACT

    /** FIXME[ISIS-2877] 'null' for null bug */
    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof CalendarEvent) {
            val other = (CalendarEvent) obj;
            return this.epochMillis == other.epochMillis
                    && Objects.equals(this.getCalendarName(), other.getCalendarName())
                    && Objects.equals(this.getTitle(), other.getTitle()
                    //&& Objects.equals(this.getNotes(), other.getNotes()
                            );
        }
        return false;
    }

    @Override
    public int compareTo(final CalendarEvent other) {
        return Long.compare(this.epochMillis, other.getEpochMillis());
    }

    // -- UTILITY

    public static class CalendarEventDeserializer
    extends StdDeserializer<CalendarEvent> {
        private static final long serialVersionUID = 1L;

        public CalendarEventDeserializer() {
            this(null);
        }

        protected CalendarEventDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public CalendarEvent deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JacksonException {

            final JsonNode node = jp.getCodec().readTree(jp);

            final ZonedDateTime dateTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(node.get("epochMillis").asLong()),
                    ZoneId.systemDefault());
            final String calendarName = node.get("calendarName").asText();
            final String title = node.get("title").asText();
            final String notes = node.get("notes").asText();

            return CalendarEvent.of(dateTime, calendarName, title, notes);
        }

    }

    public static final class JaxbAdapter
    extends XmlAdapter<String, CalendarEvent> {

        @Override
        public CalendarEvent unmarshal(final String v) {
            return _Strings.isNotEmpty(v)
                    ? _Json.readJson(CalendarEvent.class, v).presentElseFail()
                    : null;
        }

        @Override
        public String marshal(final CalendarEvent v) {
            return v!=null
                    ? _Json.toString(v).presentElseFail()
                    : null;
        }

    }

}
