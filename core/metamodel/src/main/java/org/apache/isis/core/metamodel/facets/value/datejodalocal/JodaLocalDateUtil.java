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

package org.apache.isis.core.metamodel.facets.value.datejodalocal;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.value.JodaFunctions;

public final class JodaLocalDateUtil  {

    private JodaLocalDateUtil(){}

    static LocalDate parseDate(
            final String dateStr,
            List<DateTimeFormatter> parseFormatters) {
        final Locale locale = Locale.getDefault();

        Iterable<DateTimeFormatter> elements = _Lists.transform(parseFormatters, JodaFunctions.withLocale(locale));
        LocalDate parsedDate = parseDate(dateStr, elements);
        return parsedDate;
    }


    private static LocalDate parseDate(String dateStr, Iterable<DateTimeFormatter> formatters) {
        for(DateTimeFormatter formatter: formatters) {
            try {
                return formatter.parseLocalDate(dateStr);
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

    public static String titleString(final DateTimeFormatter formatter, final LocalDate date) {
        return date == null ? "" : formatter.print(date);
    }


}
