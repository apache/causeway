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

package org.apache.isis.core.metamodel.facets.value.datetimejdk8local;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;

final class Jdk8LocalDateTimeUtil  {

    private Jdk8LocalDateTimeUtil(){}

    // -- CONVERSION (LEGACY OF JODA TIME LIBRARY)

    private final static ZoneId zId = ZoneId.systemDefault();

    static TimeParser parserOf(org.joda.time.format.DateTimeFormatter jodaFormatter) {
        return t->
        toJava8(jodaFormatter.withLocale(Locale.getDefault()).parseLocalDateTime(t));
    }

    private static LocalDateTime toJava8(org.joda.time.LocalDateTime x){
        if(x==null)
            return null;
        return toLocalDateTime(x.toDate().getTime());
    }

    private static LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli),zId);
    }

    static TimeFormatter formatterOf(org.joda.time.format.DateTimeFormatter jodaFormatter) {
        return t->
        jodaFormatter.withLocale(Locale.getDefault()).print(toJoda(t));

    }

    private static org.joda.time.LocalDateTime toJoda(LocalDateTime x){
        if(x==null)
            return null;
        return org.joda.time.LocalDateTime.fromDateFields(new Date(toEpochMilli(x)));
    }

    private static long toEpochMilli(LocalDateTime localDateTime){
        return localDateTime.atZone(zId).toInstant().toEpochMilli();
    }

    // //////////////////////////////////////

    static LocalDateTime parseDate(
            final String dateStr,
            List<TimeParser> parseFormatters) {
        return parseDateTime(dateStr, parseFormatters);
    }

    private static LocalDateTime parseDateTime(String dateStr, Iterable<TimeParser> parsers) {
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


    static LocalDateTime relativeDateTime(final LocalDateTime contextDate, final String str, final boolean add) {
        LocalDateTime relativeDate = contextDate;
        if (str.equals("")) {
            return contextDate;
        }

        try {
            final StringTokenizer st = new StringTokenizer(str.substring(1), " ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                relativeDate = adjustDateTime(relativeDate, token, add);
            }
            return relativeDate;
        } catch (final Exception e) {
            return contextDate;
        }
    }

    private static LocalDateTime adjustDateTime(final LocalDateTime contextDateTime, String str, final boolean add) {
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
            return add(contextDateTime, years, months, days, hours, minutes);
        } else {
            return add(contextDateTime, -years, -months, -days, -hours, -minutes);
        }
    }

    private static LocalDateTime add(final LocalDateTime original, final int years, final int months, final int days, final int hours, final int minutes) {
        return original.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes);
    }

    // //////////////////////////////////////

    static String titleString(TimeFormatter titleFormatter, LocalDateTime dateTime) {
        return dateTime == null ? "" : titleFormatter.apply(dateTime);
    }

}
