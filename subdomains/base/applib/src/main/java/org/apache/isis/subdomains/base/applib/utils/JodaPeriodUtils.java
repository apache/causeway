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
