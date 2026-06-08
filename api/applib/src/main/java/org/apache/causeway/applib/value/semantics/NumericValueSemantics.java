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
package org.apache.causeway.applib.value.semantics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract.FormatUsageFor;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.commons.internal.base._Strings;

/**
 * A base for all numerical value types.
 */
public interface NumericValueSemantics {

    enum GroupingSeparatorPolicy {
        ALLOW,
        DISALLOW;
    }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link NumberFormat} the default from given context's locale
     * or else system's default locale
     *
     * @implNote the format's MaximumFractionDigits are initialized to 16, as
     * 64 bit IEEE 754 double has 15 decimal digits of precision;
     * this is typically overruled later by implementations of
     * {@link #configureDecimalFormat(org.apache.causeway.applib.adapters.ValueSemanticsProvider.Context, DecimalFormat) configureDecimalFormat}
     */
    default DecimalFormat getNumberFormat(
            final ValueSemanticsProvider.@Nullable Context context) {
        return getNumberFormat(context, FormatUsageFor.RENDERING);
    }

    default DecimalFormat getNumberFormat(
            final ValueSemanticsProvider.@Nullable Context context,
            final @NonNull FormatUsageFor usedFor) {
        var format = (DecimalFormat)NumberFormat.getNumberInstance(ValueSemanticsProvider.getUserLocale(context).numberFormatLocale());
        // prime w/ 16 (64 bit IEEE 754 double has 15 decimal digits of precision)
        format.setMaximumFractionDigits(16);
        configureDecimalFormat(context, format, usedFor);
        return format;
    }

    /**
     * Typically overridden by BigDecimalValueSemantics to set min/max fractional digits.
     */
    default void configureDecimalFormat(
            final Context context, final DecimalFormat format, final FormatUsageFor usedFor) {}


    default Optional<BigInteger> parseInteger(
            final ValueSemanticsProvider.@Nullable Context context,
            final @Nullable String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return Optional.empty();
        try {
            return parseDecimal(context, input, GroupingSeparatorPolicy.ALLOW)
                    .map(BigDecimal::toBigIntegerExact);
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not an integer value " + text, e);
        }
    }

    default Optional<BigDecimal> parseDecimal(
            final @Nullable Context context,
            final @Nullable String text,
            final GroupingSeparatorPolicy groupingSeparatorPolicy) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return Optional.empty();

        if (groupingSeparatorPolicy == GroupingSeparatorPolicy.DISALLOW) {
            var userLocale = ValueSemanticsProvider.getUserLocale(context);
            var decimalFormatSymbols = new DecimalFormatSymbols(userLocale.numberFormatLocale());
            var groupingSeparatorChar = decimalFormatSymbols.getGroupingSeparator();
            if (input.contains(""+groupingSeparatorChar))
                throw new TextEntryParseException("Invalid value '" + input + "'; do not use the '" + groupingSeparatorChar + "' grouping separator");
        }

        var format = getNumberFormat(context, FormatUsageFor.PARSING);
        format.setParseBigDecimal(true);

        var position = new ParsePosition(0);
        try {
            var number = (BigDecimal)format.parse(input, position);
            if (position.getErrorIndex() != -1)
                throw new ParseException("could not parse input='" + input + "'", position.getErrorIndex());
            else if (position.getIndex() < input.length())
                throw new ParseException("input='" + input + "' was not processed completely", position.getIndex());
            // check for maxFractionDigits if required ...
            final int maxFractionDigits = format.getMaximumFractionDigits();
            if(maxFractionDigits>-1
                    && number.scale()>format.getMaximumFractionDigits())
                throw new TextEntryParseException(String.format(
                        "No more than %d digits can be entered after the decimal separator, "
                                + "got %d in '%s'.", maxFractionDigits, number.scale(), input));
            return Optional.of(number);
        } catch (final NumberFormatException | ParseException e) {
            throw new TextEntryParseException(String.format(
                    "Not a decimal value '%s': %s", input, e.getMessage()),
                    e);
        }
    }

}
