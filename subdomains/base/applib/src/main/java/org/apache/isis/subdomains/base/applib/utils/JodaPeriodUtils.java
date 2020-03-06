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
package org.apache.isis.subdomains.base.applib.utils;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Utilities for manipulating parsing JODA {@link Period}s.
 */
public final class JodaPeriodUtils {

    private JodaPeriodUtils() {
    }

    public static Period asPeriod(final String inputStr) {
        if (inputStr == null) {
            return null;
        }
        final String inputStrNormalized = inputStr.replaceAll(" ", "").toLowerCase();
        PeriodFormatter formatter = simpleFormatter();
        try {
            return formatter.parsePeriod(inputStrNormalized);
        } catch (Exception e) {
            return null;
        }
    }

    public static String asString(final Period period) {

        PeriodFormatter formatter = complexFormatter();
        return formatter.print(period).trim();

    }

    public static String asSimpleString(final Period period) {

        PeriodFormatter formatter = simpleFormatter();
        return formatter.print(period).trim();

    }

    private static PeriodFormatter simpleFormatter() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().
                appendYears().appendSuffix("y").
                appendMonths().appendSuffix("m").
                appendDays().appendSuffix("d").
                appendHours().appendSuffix("h").
                appendMinutes().appendSuffix("min").
                toFormatter();
        return formatter;
    }

    private static PeriodFormatter complexFormatter() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().
                appendYears().appendSuffix(" year", " years").
                appendSeparator(", ", " & ").
                appendMonths().appendSuffix(" month", " months").
                appendSeparator(", ", " & ").
                appendDays().appendSuffix(" day", " days").
                appendSeparator(", ", " & ").
                appendHours().appendSuffix(" hours", "hours").
                appendSeparator(", ", " & ").
                appendMinutes().appendSuffix(" minute", " minutes").
                appendSeparator(", ", " & ").
                appendSeconds().appendSuffix(" second", " seconds").
                toFormatter();
        return formatter;
    }
}
