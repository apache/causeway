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
package org.apache.isis.commons.struct;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.base._Strings;

class StructTest {

    @Test
    void pseudoStruct() {
        final var comp = Struct.of("a");

        assertEquals(List.of("a"), comp.flatten());
        assertEquals(1, comp.size());
    }

    @Test
    void constructingElements() {
        final var comp = Struct.of("a", "b");

        assertEquals(List.of("a", "b"), comp.flatten());
        assertEquals(2, comp.size());
    }

    @Test
    void constructingStructs() {

        final var comp = Struct.<String>of(
                Struct.of("a", "b"),
                Struct.of("c", "d"));

        assertEquals(List.of("a", "b", "c", "d"), comp.flatten());
        assertEquals(4, comp.size());
    }

    @Test
    void constructingElementAndStruct() {

        final var comp = Struct.<String>of(
                "a",
                Struct.of("c", "d"));

        assertEquals(List.of("a", "c", "d"), comp.flatten());
        assertEquals(3, comp.size());
    }

    @Test
    void constructingStructAndElement() {

        final var comp = Struct.<String>of(
                Struct.of("a", "b"),
                "d");

        assertEquals(List.of("a", "b", "d"), comp.flatten());
        assertEquals(3, comp.size());
    }

    @Test
    void constructionNesting() {

        final var comp = Struct.of(
                Struct.<String>of(
                        Struct.of("a", "b"),
                        Struct.of("c", "d")),
                Struct.<String>of(
                        Struct.of("e", "f"),
                        Struct.of("g", "h")));

        assertEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h"), comp.flatten());
        assertEquals(8, comp.size());
    }

    // -- CONVERTER COMPOSITION

    @lombok.Value
    static class CalEntry {
        String name;
        LocalDateTime at;
        Duration duration;

        static CalEntry sample() {
            return new CalEntry(
                    "entry",
                    LocalDateTime.of(LocalDate.of(2021, 9, 27), LocalTime.of(6, 45)),
                    Duration.of(30, ChronoUnit.MINUTES));
        }
    }

    final Converter<String, String> strIdentity = str->str;
    final Converter<Duration, Long> durCon1 = dur->dur.toMinutes();
    final Converter<Duration, Enum<?>> durCon2 = dur->ChronoUnit.MINUTES;
    final Converter<Long, String> longCon = lon->lon.toString();
    final Converter<Enum<?>, String> enumCon = enu->_Strings.capitalize(enu.name().toLowerCase());
    final Converter<LocalDateTime, LocalDate> ldtCon1 = ldt->ldt.toLocalDate();
    final Converter<LocalDateTime, LocalTime> ldtCon2 = ldt->ldt.toLocalTime();
    final Converter<LocalDate, String> ldCon = ld->ld.toString();
    final Converter<LocalTime, String> ltCon = lt->lt.toString();
    final Converter<CalEntry, String> ceCon1 = ce->ce.getName();
    final Converter<CalEntry, LocalDateTime> ceCon2 = ce->ce.getAt();
    final Converter<CalEntry, Duration> ceCon3 = ce->ce.getDuration();

    final Struct<Converter<LocalDateTime, String>> localDateTimeCC = Struct.of(
            ldtCon1.andThen(ldCon),
            ldtCon2.andThen(ltCon));

    final Struct<Converter<Duration, String>> durationCC = Struct.of(
            durCon1.andThen(longCon),
            durCon2.andThen(enumCon));

    final FunStruct<LocalDateTime, String> localDateTimeFC = FunStruct.of(
            FunStruct.of(ldtCon1::convert).map(ldCon::convert),
            FunStruct.of(ldtCon2::convert).map(ltCon::convert));

    final FunStruct<Duration, String> durationFC = FunStruct.of(
            FunStruct.of(durCon1::convert).map(longCon::convert),
            FunStruct.of(durCon2::convert).map(enumCon::convert));


    final FunStruct<CalEntry, String> calEntryFC = FunStruct.of(
            ceCon1::convert,
            FunStruct.<CalEntry, String>of(
                    FunStruct.of(ceCon2::convert).compose(localDateTimeFC),
                    FunStruct.of(ceCon3::convert).compose(durationFC)
                    ));

    @Test
    void converterComposition() {

        assertEquals(List.of("30", "Minutes"),
                durationCC
                .map(conv->conv.convert(CalEntry.sample().getDuration()))
                .flatten());

        assertEquals(List.of("entry", "2021-09-27", "06:45", "30", "Minutes"),
                calEntryFC
                .apply(CalEntry.sample())
                .flatten());

    }

}
