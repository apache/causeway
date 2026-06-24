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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
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
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.NumericValueSemantics;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.util.Facets;

public abstract class NumericValueSemanticsAbstract<T>
extends ValueSemanticsAbstract<T>
implements
    NumericValueSemantics<T>,
    DefaultsProvider<T>,
    Parser<T> {

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final T value) {
        return renderTitle(value, getNumberFormat(context, FormatUsageFor.RENDERING_AS_TEXT)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final T value) {
        return renderTitle(value, pipe(getNumberFormat(context, FormatUsageFor.RENDERING_AS_HTML)::format, super::toLightFont));
    }

    // -- PARSER

    @Override
    public final String parseableTextRepresentation(final Context context, final T value) {
        return value!=null
            ? getNumberFormat(context, FormatUsageFor.RENDERING_AS_TEXT).format(value)
            : null;
    }

    // -- IMPL DETAILS

    /**
     * Whether underlying number type can represent floating point numbers. That is,
     * can have fractional digits.
     */
    protected abstract boolean isFloatingPoint();

    /**
     * Can be overridden by custom {@link NumericValueSemantics} to modify the {@link DecimalFormat}.
     *
     * @apiNote setting a grouping separator here has no effect, this is done via {@link DecimalFormatEx}
     *      based on {@link #grouping()}
     */
    protected void configureDecimalFormat(
            final ValueSemanticsProvider.@Nullable Context context,
            final DecimalFormat format,
            final FormatUsageFor usedFor) {
        if(context==null)
            return;
        var feature = MetaModelContext.instanceElseFail().getSpecificationLoader()
            .loadFeature(context.featureIdentifier())
            .orElse(null);
        if(feature==null)
            return;

        // lookup meta-model for any facets that provide the MaximumIntegerDigits;
        // applies only to input (we don't want to remove most significant digits when rendering)
        // @see ValueSemantics#maxIntegerDigits
        if(usedFor.isParsing()) {
            Facets.maxIntegerDigits(feature)
                .ifPresent(format::setMaximumIntegerDigits);
        }

        // lookup meta-model for any facets that provide the MinimumIntegerDigits;
        // applies to display and input prompting alike,
        // @see ValueSemantics#minIntegerDigits
        Facets.minIntegerDigits(feature)
            .ifPresent(format::setMinimumIntegerDigits);

        if(isFloatingPoint()) {
            // lookup meta-model for any facets that provide the MaximumFractionDigits;
            // applies to display and input alike
            // @see ValueSemantics#maxFractionalDigits
            Facets.maxFractionalDigits(feature)
                .ifPresent(format::setMaximumFractionDigits);

            // lookup meta-model for any facets that provide the MinimumFractionDigits;
            // applies to display and input prompting alike,
            // but should not be enforced on UI input, as can always pad with zeros)
            // @see ValueSemantics#minFractionalDigits
            Facets.minFractionalDigits(feature)
                .ifPresent(format::setMinimumFractionDigits);
        }
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
            int integerDigitCount = integerDigitCount(number);
            int fractionDigitCount = fractionDigitCount(number);

            // check for maxIntegerDigits
            if(integerDigitCount > formatEx.maxIntegerDigits())
                throw new TextEntryParseException(String.format(
                    "No more than %d integer digits (digits before the decimal separator) can be entered, "
                            + "got %d in '%s'.", formatEx.maxIntegerDigits(), integerDigitCount, input));

            // check for minIntegerDigits
            if(integerDigitCount < formatEx.minIntegerDigits())
                throw new TextEntryParseException(String.format(
                    "A minimum of %d integer digits (digits before the decimal separator) must be entered, "
                            + "got %d in '%s'.", formatEx.minIntegerDigits(), integerDigitCount, input));

            // check for maxFractionDigits
            if(fractionDigitCount>formatEx.maxFractionDigits())
                throw new TextEntryParseException(String.format(
                        "No more than %d digits can be entered after the decimal separator, "
                                + "got %d in '%s'.", formatEx.maxFractionDigits(), fractionDigitCount, input));
            return Optional.of(number);
        } catch (final NumberFormatException | ParseException e) {
            throw new TextEntryParseException(String.format(
                    "Not a decimal value '%s': %s", input, e.getMessage()),
                    e);
        }
    }

    static int fractionDigitCount(final BigDecimal number) {
        return number.scale()>=0
            ? number.scale()
            : 0;
    }

    static int integerDigitCount(final BigDecimal number) {
        return number.toBigInteger().abs().toString().length();
    }

    /**
     * Conditionally disallows the user- or system-locale specific grouping (thousands) separator
     * (being part of the input string) or at least validates, that the grouping is correctly placed.
     *
     * <p> A common use of {@link java.math.BigDecimal} say is as a money value. In some locales (eg English), the
     * ',' (comma) is the grouping separator while the '.' (period) acts as a
     * decimal point, but in others (eg France, Italy, Germany) it is the other way around.
     *
     * <p> Motivation: A string such as "123,99", when parsed (by {@link java.text.DecimalFormat})
     * in an English locale, is not rejected but instead is evaluated as the value 12_399L.  That's almost
     * certainly not what the end-user would have expected, and results in a money value 100x too large.
     *
     * <p> Hence we disallow grouped input, unless explicitly enabled via provider qualifier 'locale-grouping-all'.
     */
    protected void validateNumericalInput(
            final ValueSemanticsProvider.@Nullable Context context,
            final String input) {
        var localeGroupingSeparator = "" + NumericValueSemantics.localeGroupingSeparator(context);
        var parsingGroupingSeparator = grouping().separator(context, FormatUsageFor.PARSING);
        if(parsingGroupingSeparator.equals(localeGroupingSeparator)) {
            // if parsing format explicitly uses the localeGroupingSeparator, then allow it, unless grouping is badly placed

            if(!input.contains(localeGroupingSeparator))
                return; // no group separator used -> valid

            var formatEx = getNumberFormat(context, FormatUsageFor.PARSING);
            var integerPartInputLiteral = TextUtils.cutter(_Strings.condenseWhitespaces(input, ""))
                    .keepBefore("" + formatEx.format.getDecimalFormatSymbols().getDecimalSeparator())
                    .getValue();

            // every chunk must exactly be 3 digits long, except for the first one which can be max 3 digits long
            var groupedDigits = _Strings.splitThenStream(integerPartInputLiteral, parsingGroupingSeparator)
                .collect(Can.toCan());

            switch (groupedDigits.getCardinality()) {
                case ZERO: break; // no leading digits
                case ONE: if(groupedDigits.getFirstElseFail().length()>3)
                    throw parseExceptionForInconstentGrouping(localeGroupingSeparator, input);
                    break;
                case MULTIPLE:
                    if(groupedDigits.getFirstElseFail().length()>3)
                        throw parseExceptionForInconstentGrouping(localeGroupingSeparator, input);
                    for (String chunk : groupedDigits.subCan(1)) {
                        if(chunk.length()!=3)
                            throw parseExceptionForInconstentGrouping(localeGroupingSeparator, input);
                    }
                    break;
            }

            return; // all tests passed -> valid
        }

        if(StringUtils.hasText(localeGroupingSeparator)) { // ignores whitespace
            if (input.contains(localeGroupingSeparator))
                throw parseExceptionForGroupingNotAllowed(localeGroupingSeparator, input);
        }
    }

    // -- HELPER

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
        public int maxIntegerDigits() { return format.getMaximumIntegerDigits(); }
        public int minIntegerDigits() { return format.getMinimumIntegerDigits(); }
        public int maxFractionDigits() { return format.getMaximumFractionDigits(); }
        public int minFractionDigits() { return format.getMinimumFractionDigits(); }
    }

    /**
     * @return {@link NumberFormat} the default from given context's locale
     *      or else system's default locale
     */
    private DecimalFormatEx getNumberFormat(
            final ValueSemanticsProvider.@Nullable Context context,
            final @NonNull FormatUsageFor usedFor) {
        var format = NumericValueSemantics.localeDecimalFormat(context);
        // prime as unconstraint
        format.setMaximumFractionDigits(340);
        configureDecimalFormat(context, format, usedFor);
        return DecimalFormatEx.of(format, grouping().separator(context, usedFor));
    }

    private static TextEntryParseException parseExceptionForGroupingNotAllowed(final String localeGroupingSeparator, final String input) {
        return new TextEntryParseException("Invalid value '" + input + "'; do not use the '" + localeGroupingSeparator + "' grouping separator");
    }
    private static TextEntryParseException parseExceptionForInconstentGrouping(final String localeGroupingSeparator, final String input) {
        return new TextEntryParseException("Invalid value '" + input + "'; inconsistent use of the '" + localeGroupingSeparator + "' grouping separator");
    }

}
