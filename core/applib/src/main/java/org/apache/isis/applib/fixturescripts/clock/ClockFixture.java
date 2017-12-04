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
package org.apache.isis.applib.fixturescripts.clock;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;

public class ClockFixture extends FixtureScript {

    //region > factory methods, constructors
    public static ClockFixture setTo(final String date) {
        return new ClockFixture(date);
    }

    private LocalDateTime localDateTime;
    private LocalDate localDate;

    public ClockFixture() {
        super(null, "clock");
    }

    public ClockFixture(String dateStr) {
        super(null, "clock");
        if(!parse(dateStr)) {
            throw new IllegalArgumentException(dateStr + " could not be parsed as a date/time or date");
        }
    }
    //endregion

    //region > parseAsLocalDateTime
    private boolean parse(String dateStr) {
        return dateStr == null ? true : parseNonNull(dateStr);
    }

    private boolean parseNonNull(String dateStr) {
        this.localDateTime = parseAsLocalDateTime(dateStr);
        if(localDateTime == null) {
            this.localDate = parseAsLocalDate(dateStr);
        }
        return this.localDateTime != null || this.localDate != null;
    }

    private static LocalDate parseAsLocalDate(String dateStr) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                DateTimeFormat.fullDateTime(),
                DateTimeFormat.mediumDateTime(),
                DateTimeFormat.shortDateTime(),
                DateTimeFormat.forPattern("yyyy-MM-dd"),
                DateTimeFormat.forPattern("yyyyMMdd"),
        }) {
            try {
                return formatter.parseLocalDate(dateStr);
            } catch (Exception e) {
                // continue;
            }
        }
        return null;
    }

    private static LocalDateTime parseAsLocalDateTime(String dateStr) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                DateTimeFormat.fullDateTime(),
                DateTimeFormat.mediumDateTime(),
                DateTimeFormat.shortDateTime(),
                DateTimeFormat.forPattern("yyyyMMddhhmmss"),
                DateTimeFormat.forPattern("yyyyMMddhhmm")
        }) {
            try {
                return formatter.parseLocalDateTime(dateStr);
            } catch (Exception e) {
                // continue;
            }
        }
        return null;
    }
    //endregion

    @Override
    protected void execute(ExecutionContext fixtureResults) {
        if(!(Clock.getInstance() instanceof FixtureClock)) {
            throw new IllegalStateException("Clock has not been initialized as a FixtureClock");
        }
        final FixtureClock fixtureClock = (FixtureClock) FixtureClock.getInstance();

        if(localDateTime != null) {
            fixtureClock.setDate(localDateTime.getYear(), localDateTime.getMonthOfYear(), localDateTime.getDayOfMonth());
            fixtureClock.setTime(localDateTime.getHourOfDay(), localDateTime.getMinuteOfHour());
            return;
        }
        if(localDate != null) {
            fixtureClock.setDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
            return;
        }
    }

    @Override
    public String validateRun(String parameters) {
        return parseAsLocalDateTime(parameters) == null && parseAsLocalDate(parameters) == null
                ? "Parameter must be parseable as a date/time or as a date" : null;
    }
}
