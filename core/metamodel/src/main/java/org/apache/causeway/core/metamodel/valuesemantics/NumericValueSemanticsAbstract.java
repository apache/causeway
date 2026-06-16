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
            ? getNumberFormat(context, FormatUsageFor.PARSING).format(value)
            : null;
    }

    // -- IMPL DETAILS

    /**
     * Whether underlying number type cannot represent floating point numbers. That is,
     * never has fractional digits.
     */
    protected abstract boolean isIntegerOnly();

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

        if(!isIntegerOnly()) {
            // lookup meta-model for any facets that provide the MaximumFractionDigits
            // applies to display and input alike
            // @see ValueSemantics#maxFractionalDigits
            Facets.maxFractionalDigits(feature)
                .ifPresent(format::setMaximumFractionDigits);

            // lookup meta-model for any facets that provide the MinimumFractionDigits
            // applies to display and input prompting alike,
            // but should not be enforced on UI input, as can always pad with zeros)
            // @see ValueSemantics#minFractionalDigits
            Facets.minFractionalDigits(feature)
                .ifPresent(format::setMinimumFractionDigits);
        }

        switch (usedFor) {
            case RENDERING_AS_TEXT, RENDERING_AS_HTML -> {

            }
            case PARSING -> {

            }
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
     * <p>This validation method conditionally disallows the
     * grouping separator (being part of the input string) or
     * at least validates, that the grouping is correctly placed.
     */
    protected void validateNumericalInput(@Nullable final Context context, final String input) {
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
                    throw new TextEntryParseException("Invalid value '" + input + "'; inconsistent use of the '" + localeGroupingSeparator + "' grouping separator");
                    break;
                case MULTIPLE:
                    if(groupedDigits.getFirstElseFail().length()>3)
                        throw new TextEntryParseException("Invalid value '" + input + "'; inconsistent use of the '" + localeGroupingSeparator + "' grouping separator");
                    for (String chunk : groupedDigits.subCan(1)) {
                        if(chunk.length()!=3)
                            throw new TextEntryParseException("Invalid value '" + input + "'; inconsistent use of the '" + localeGroupingSeparator + "' grouping separator");
                    }
                    break;
            }

            return; // all tests passed -> valid
        }

        if(StringUtils.hasText(localeGroupingSeparator)) { // ignores whitespace
            if (input.contains(localeGroupingSeparator))
                throw new TextEntryParseException("Invalid value '" + input + "'; do not use the '" + localeGroupingSeparator + "' grouping separator");
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
    }

    /**
     * @param context nullable in support of JUnit testing
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

}
