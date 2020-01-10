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

package org.apache.isis.core.metamodel.facets.value.datejdk8local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.joda.time.format.DateTimeFormat;

import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;

final class Jdk8LocalDateUtil  {

    private Jdk8LocalDateUtil(){}

    // -- CONVERSION (LEGACY OF JODA TIME LIBRARY)

    static TimeParser parserOfStyle(String style) {
        return parserOf(DateTimeFormat.forStyle(style));
    }

    private static TimeParser parserOf(org.joda.time.format.DateTimeFormatter jodaFormatter) {
        return t->
        toJava8(jodaFormatter.withLocale(Locale.getDefault()).parseLocalDate(t));
    }

    private static LocalDate toJava8(org.joda.time.LocalDate x) {
        return LocalDate.of(x.getYear(), x.getMonthOfYear(), x.getDayOfMonth());
    }

    static TimeFormatter formatterOfStyle(String style) {
        return formatterOf(DateTimeFormat.forStyle(style));
    }

    private static TimeFormatter formatterOf(org.joda.time.format.DateTimeFormatter jodaFormatter) {
        return t->
        jodaFormatter.withLocale(Locale.getDefault()).print(toJoda(t));
    }

    private static org.joda.time.LocalDate toJoda(LocalDate x) {
        return new org.joda.time.LocalDate(x.getYear(), x.getMonthValue(), x.getDayOfMonth());
    }

    // -- PARSING AND FORMATING GENERALIZATION

    static TimeFormatter formatterOf(DateTimeFormatter formatter) {
        return t->
        formatter.withLocale(Locale.getDefault()).format(t);
    }

    static TimeParser parserOf(DateTimeFormatter formatter) {
        return text->
        LocalDate.parse(text, formatter.withLocale(Locale.getDefault()));
    }

    // //////////////////////////////////////

    static LocalDate parseDate(
            final String dateStr,
            List<TimeParser> parsers) {
        LocalDate parsedDate = _parseDate(dateStr, parsers);
        return parsedDate;
    }

    private static LocalDate _parseDate(String dateStr, Iterable<TimeParser> parsers) {
        for(TimeParser parser: parsers) {
            try {
                return parser.apply(dateStr);
            } catch (final IllegalArgumentException e) {
                // continue to next
            }
        }
        throw new TextEntryParseException("Not recognised as a date: " + dateStr);
    }

    // //////////////////////////////////////

    static LocalDate relativeDate(final LocalDate contextDate, final String str, final boolean add) {
        LocalDate relativeDate = contextDate;
        if (str.equals("")) {
            return contextDate;
        }

        try {
            final StringTokenizer st = new StringTokenizer(str.substring(1), " ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                relativeDate = adjustDate(relativeDate, token, add);
            }
            return relativeDate;
        } catch (final Exception e) {
            return contextDate;
        }
    }

    private static LocalDate adjustDate(final LocalDate contextDate, String str, final boolean add) {
        int hours = 0;
        int minutes = 0;
        int days = 0;
        int months = 0;
        int years = 0;

        if (str.endsWith("H")) {
            str = str.substring(0, str.length() - 1);
            hours = Integer.valueOf(str).intValue();
        } else if (str.endsWith("M")) {
            str = str.substring(0, str.length() - 1);
            minutes = Integer.valueOf(str).intValue();
        } else if (str.endsWith("w")) {
            str = str.substring(0, str.length() - 1);
            days = 7 * Integer.valueOf(str).intValue();
        } else if (str.endsWith("y")) {
            str = str.substring(0, str.length() - 1);
            years = Integer.valueOf(str).intValue();
        } else if (str.endsWith("m")) {
            str = str.substring(0, str.length() - 1);
            months = Integer.valueOf(str).intValue();
        } else if (str.endsWith("d")) {
            str = str.substring(0, str.length() - 1);
            days = Integer.valueOf(str).intValue();
        } else {
            days = Integer.valueOf(str).intValue();
        }

        if (add) {
            return add(contextDate, years, months, days, hours, minutes);
        } else {
            return add(contextDate, -years, -months, -days, -hours, -minutes);
        }
    }

    private static LocalDate add(final LocalDate original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a LocalDate");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }


    // //////////////////////////////////////

    public static String titleString(final TimeFormatter formatter, final LocalDate date) {
        return date == null ? "" : formatter.apply(date);
    }





}
