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
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.commons.internal.base._Strings;

/**
 * A base for all numerical value types.
 *
 * @since 4.0
 */
public abstract class NumericValueSemantics<T>
extends ValueSemanticsAbstract<T>
implements
    Renderer<T>,
    Parser<T> {

    public final static String NO_GROUPING = "no-grouping";
    public final static String LOCALE_GROUPING = "locale-grouping";

    /**
     * Specifies the grouping separation behavior for parsing and rendering.
     *
     * @apiNote Subclasses may provide their own to customize grouping behavior.
     */
    public interface GroupingSeparatorProvider {
        @Nullable String separator(@Nullable Context context, FormatUsageFor usedFor);

        static GroupingSeparatorProvider NO_GROUPING = (context, usedFor) -> null;
        static GroupingSeparatorProvider SPACED_GROUPING = (context, usedFor) -> switch(usedFor) {
            case RENDERING_AS_TEXT -> " "; // UTF8 U+2009
            case RENDERING_AS_HTML -> "&#8239;"; // small space
            case PARSING -> null;
        };
        static GroupingSeparatorProvider LOCALE_GROUPING = (context, usedFor) -> "" + localeGroupingSeparator(context);
    }

    /**
     * Specifies the grouping separation behavior for parsing and rendering.
     *
     * @apiNote Subclasses may override to customize grouping behavior.
     */
    protected GroupingSeparatorProvider grouping() {
        return GroupingSeparatorProvider.SPACED_GROUPING;
    }

    protected record DecimalFormatEx(
            UnaryOperator<String> preprocess,
            DecimalFormat format,
            UnaryOperator<String> postprocess) {
        protected DecimalFormatEx {
            format = Objects.requireNonNull(format, ()->"must specify a DecimalFormat");
            preprocess = preprocess!=null
                    ? preprocess
                    : UnaryOperator.identity();
            postprocess = postprocess!=null
                    ? postprocess
                    : UnaryOperator.identity();
        }
        static DecimalFormatEx of(final DecimalFormat format, @Nullable final String groupingSeparator) {
            if(StringUtils.hasLength(groupingSeparator)) {
                format.setGroupingUsed(true);
                var decimalFormatSymbols = format.getDecimalFormatSymbols();
                decimalFormatSymbols.setGroupingSeparator('_');
                format.setDecimalFormatSymbols(decimalFormatSymbols);
                return new DecimalFormatEx(
                        in->in.replace(groupingSeparator, "").replaceAll("\\s+", ""),
                        format,
                        out->out.replace("_", groupingSeparator));
            } else {
                format.setGroupingUsed(false);
                return new DecimalFormatEx(
                        in->in.replaceAll("\\s+", ""),
                        format,
                        UnaryOperator.identity());
            }
        }
        public String format(final double number) {
            return postprocess.apply(format.format(number));
        }
        public String format(final long number) {
            return postprocess.apply(format.format(number));
        }
        public String format(final Object number) {
            return postprocess.apply(format.format(number));
        }
        public BigDecimal parse(final String rawInput) throws ParseException {
            var position = new ParsePosition(0);
            var input = preprocess.apply(rawInput);
            format.setParseBigDecimal(true);
            var decimal = (BigDecimal) format.parse(input, position);
            if (position.getErrorIndex() != -1)
                throw new ParseException("could not parse input='" + rawInput + "'", position.getErrorIndex());
            else if (position.getIndex() < input.length())
                throw new ParseException("input='" + rawInput + "' was not processed completely", position.getIndex());
            return decimal;
        }
    }

    /**
     * @param context nullable in support of JUnit testing
     * @return {@link NumberFormat} the default from given context's locale
     *      or else system's default locale
     *
     * @implNote the format's MaximumFractionDigits are initialized to 16, as
     *      64 bit IEEE 754 double has 15 decimal digits of precision;
     *      this is typically overruled later by implementations of
     *      {@link #configureDecimalFormat(org.apache.causeway.applib.adapters.ValueSemanticsProvider.Context, DecimalFormat) configureDecimalFormat}
     */
    private DecimalFormatEx getNumberFormat(
            final ValueSemanticsProvider.@Nullable Context context,
            final @NonNull FormatUsageFor usedFor) {
        var format = (DecimalFormat)NumberFormat.getNumberInstance(ValueSemanticsProvider.getUserLocale(context).numberFormatLocale());
        // prime w/ 16 (64 bit IEEE 754 double has 15 decimal digits of precision)
        format.setMaximumFractionDigits(16);

        configureDecimalFormat(context, format, usedFor);

        return DecimalFormatEx.of(format, grouping().separator(context, usedFor));
    }

    /**
     * Typically overridden by custom {@link NumericValueSemantics} to set min/max fractional digits.
     *
     * @apiNote setting a grouping separator here has no effect, this is done via {@link DecimalFormatEx}
     *      based on {@link #grouping()}
     */
    protected void configureDecimalFormat(
            final Context context, final DecimalFormat format, final FormatUsageFor usedFor) {
    }

    protected Optional<BigInteger> parseInteger(
            final @Nullable Context context,
            final @Nullable String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return Optional.empty();
        try {
            return parseDecimal(context, input)
                    .map(BigDecimal::toBigIntegerExact);
        } catch (final ArithmeticException e) {
            throw new TextEntryParseException("Not an integer value " + text, e);
        }
    }

    protected Optional<BigDecimal> parseDecimal(
            final @Nullable Context context,
            final @Nullable String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return Optional.empty();

        validateNumericalInput(context, input);

        var formatEx = getNumberFormat(context, FormatUsageFor.PARSING);

        try {
            var number = formatEx.parse(input);

            // check for maxFractionDigits if required ...
            final int maxFractionDigits = formatEx.format().getMaximumFractionDigits();
            if(maxFractionDigits>-1
                    && number.scale()>formatEx.format().getMaximumFractionDigits())
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

    /**
     * The default implementation disallows appearance of the user- or system-locale specific grouping (thousands) separator
     * in any of the numeric value inputs. Subclasses may override this behavior.
     *
     * <p> A common use of {@link java.math.BigDecimal} say is as a money value. In some locales (eg English), the
     * ',' (comma) is the grouping separator while the '.' (period) acts as a
     * decimal point, but in others (eg France, Italy, Germany) it is the other way around.
     *
     * <p> Surprisingly perhaps, a string such as "123,99", when parsed ((by {@link java.text.DecimalFormat})
     * in an English locale, is not rejected but instead is evaluated as the value 12_399L.  That's almost
     * certainly not what the end-user would have expected, and results in a money value 100x too large.
     *
     * <p>The purpose of this validation method is to remove the confusion by simply disallowing the
     * grouping separator from being part of the input string.
     */
    protected void validateNumericalInput(@Nullable final Context context, final String input) {
        var localeGroupingSeparator = "" + localeGroupingSeparator(context);
        if(StringUtils.hasText(localeGroupingSeparator)) { // ignores whitespace
            if (input.contains(localeGroupingSeparator))
                throw new TextEntryParseException("Invalid value '" + input + "'; do not use the '" + localeGroupingSeparator + "' grouping separator");
        }
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final T value) {
        return renderTitle(value, getNumberFormat(context, FormatUsageFor.RENDERING_AS_TEXT)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final T value) {
        return renderTitle(value, pipe(getNumberFormat(context, FormatUsageFor.RENDERING_AS_TEXT)::format, super::toLightFont));
    }

    // -- PARSER

    @Override
    public final String parseableTextRepresentation(final Context context, final T value) {
        return value!=null
            ? getNumberFormat(context, FormatUsageFor.PARSING).format(value)
            : null;
    }

    // -- UTIL

    /**
     * Returns locale based grouping separator. If no context is given, system defaults apply.
     */
    public static char localeGroupingSeparator(@Nullable final Context context) {
        var userLocale = ValueSemanticsProvider.getUserLocale(context);
        return new DecimalFormatSymbols(userLocale.numberFormatLocale()).getGroupingSeparator();
    }

}
