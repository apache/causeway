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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.TickingFixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.isis.applib.fixturescripts.FixtureScripts;


@Programmatic
public class TickingClockFixture
extends FixtureScript
implements FixtureScriptWithExecutionStrategy {

    // -- date property
    private String date;
    public String getDate() {
        return date;
    }
    public TickingClockFixture setDate(final String date) {
        this.date = date;
        return this;
    }


    @Override
    protected void execute(ExecutionContext ec) {

        // check that some value has been set
        checkParam("date", ec, String.class);

        final Clock instance = Clock.getInstance();

        if(instance instanceof TickingFixtureClock) {
            try {
                TickingFixtureClock.reinstateExisting();
                setTo(date);
            } finally {
                TickingFixtureClock.replaceExisting();
            }
        }

        if(instance instanceof FixtureClock) {
            setTo(date);
        }
    }

    private void setTo(final String date) {

        if (!(Clock.getInstance() instanceof FixtureClock)) {
            throw new IllegalStateException("Clock has not been initialized as a FixtureClock");
        }
        final FixtureClock fixtureClock = (FixtureClock) FixtureClock.getInstance();

        // process if can be parsed as a LocalDateTime
        LocalDateTime ldt = parseAsLocalDateTime(date);
        if (ldt != null) {
            fixtureClock.setDate(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfMonth());
            fixtureClock.setTime(ldt.getHourOfDay(), ldt.getMinuteOfHour());
            return;
        }

        // else process if can be parsed as a LocalDate
        LocalDate ld = parseAsLocalDate(date);
        if (ld != null) {
            fixtureClock.setDate(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth());
            return;
        }

        // else
        throw new IllegalArgumentException(String.format(
                "'%s' could not be parsed as a local date/time or local date", date));

    }

    private static LocalDate parseAsLocalDate(String dateStr) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[] {
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
        for (DateTimeFormatter formatter : new DateTimeFormatter[] {
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

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return FixtureScripts.MultipleExecutionStrategy.EXECUTE;
    }

}
