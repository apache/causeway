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
package org.apache.isis.applib.value.semantics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.x {@index}
 */
public abstract class ValueSemanticsAbstract<T>
implements
    ValueSemanticsProvider<T> {

    public static final String NULL_REPRESENTATION = "(none)";
    protected static final ValueType UNREPRESENTED = ValueType.STRING;

    @SuppressWarnings("unchecked")
    @Override
    public OrderRelation<T, ?> getOrderRelation() {
        return this instanceof OrderRelation ? (OrderRelation<T, ?>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Converter<T, ?> getConverter() {
        return this instanceof Converter ? (Converter<T, ?>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Renderer<T> getRenderer() {
        return this instanceof Renderer ? (Renderer<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EncoderDecoder<T> getEncoderDecoder() {
        return this instanceof EncoderDecoder ? (EncoderDecoder<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser<T> getParser() {
        return this instanceof Parser ? (Parser<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return this instanceof DefaultsProvider ? (DefaultsProvider<T>)this : null;
    }

    /**
     * JUnit support.
     */
    public Can<T> getExamples() { return Can.empty(); }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link Locale} from given context or else system's default
     */
    protected UserLocale getUserLocale(final @Nullable ValueSemanticsProvider.Context context) {
        return Optional.ofNullable(context)
        .map(ValueSemanticsProvider.Context::getInteractionContext)
        .map(InteractionContext::getLocale)
        .orElseGet(UserLocale::getDefault);
    }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link NumberFormat} the default from from given context's locale
     * or else system's default locale
     *
     * @implNote the format's MaximumFractionDigits are initialized to 16, as
     * 64 bit IEEE 754 double has 15 decimal digits of precision;
     * this is typically overruled later by implementations of
     * {@link #configureDecimalFormat(org.apache.isis.applib.adapters.ValueSemanticsProvider.Context, DecimalFormat) configureDecimalFormat}
     */
   protected DecimalFormat getNumberFormat(final @Nullable ValueSemanticsProvider.Context context) {
        val format = (DecimalFormat)NumberFormat.getNumberInstance(getUserLocale(context).getNumberFormatLocale());
        // prime w/ 16 (64 bit IEEE 754 double has 15 decimal digits of precision)
        format.setMaximumFractionDigits(16);
        return format;
    }

    protected String render(final T value, final Function<T, String> toString) {
        return Optional.ofNullable(value)
                .map(toString)
                .orElse(NULL_REPRESENTATION);
    }

    // -- NUMBER FORMATTING/PARSING

    protected @Nullable BigInteger parseInteger(
            final @Nullable ValueSemanticsProvider.Context context,
            final @Nullable String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return parseDecimal(context, input).toBigIntegerExact();
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not an integer value " + text, e);
        }
    }

    protected @Nullable BigDecimal parseDecimal(
            final @Nullable ValueSemanticsProvider.Context context,
            final @Nullable String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        val format = getNumberFormat(context);
        format.setParseBigDecimal(true);
        configureDecimalFormat(context, format);

        val position = new ParsePosition(0);
        try {
            val number = (BigDecimal)format.parse(input, position);
            if (position.getErrorIndex() != -1) {
                throw new ParseException("could not parse input='" + input + "'", position.getErrorIndex());
            } else if (position.getIndex() < input.length()) {
                throw new ParseException("input='" + input + "' was not processed completely", position.getIndex());
            }
            // check for maxFractionDigits if required ...
            final int maxFractionDigits = format.getMaximumFractionDigits();
            if(maxFractionDigits>-1
                    && number.scale()>format.getMaximumFractionDigits()) {
                throw new TextEntryParseException(String.format(
                        "No more than %d digits can be entered after the decimal separator, "
                        + "got %d in '%s'.", maxFractionDigits, number.scale(), input));
            }
            return number;
        } catch (final NumberFormatException | ParseException e) {
            throw new TextEntryParseException(String.format(
                    "Not a decimal value '%s': %s", input, e.getMessage()),
                    e);
        }
    }

    /**
     * Typically overridden by BigDecimalValueSemantics to set MaximumFractionDigits.
     */
    protected void configureDecimalFormat(final Context context, final DecimalFormat format) {}

    // -- TEMPORAL FORMATTING/PARSING

    protected DateTimeFormatter getTemporalRenderingFormat(
            final @Nullable ValueSemanticsProvider.Context context,
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull FormatStyle dateFormatStyle,
            final @NonNull FormatStyle timeFormatStyle) {

        //FIXME[ISIS-2882] honor OffsetCharacteristic
        switch (temporalCharacteristic) {
        case DATE_TIME:
            return DateTimeFormatter.ofLocalizedDateTime(dateFormatStyle, timeFormatStyle)
                    .withLocale(getUserLocale(context).getTimeFormatLocale());
        case DATE_ONLY:
            return DateTimeFormatter.ofLocalizedDate(dateFormatStyle)
                    .withLocale(getUserLocale(context).getTimeFormatLocale());
        case TIME_ONLY:
            return DateTimeFormatter.ofLocalizedTime(timeFormatStyle)
                    .withLocale(getUserLocale(context).getTimeFormatLocale());
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
    }

    protected DateTimeFormatter getEditingFormat(
            final @Nullable ValueSemanticsProvider.Context context,
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull String datePattern,
            final @NonNull String timePattern,
            final @NonNull String zonePattern) {

        return getEditingFormatAsBuilder(
                temporalCharacteristic, offsetCharacteristic, datePattern, timePattern, zonePattern)
                .parseLenient()
                .parseCaseInsensitive()
                .toFormatter(getUserLocale(context).getTimeFormatLocale());
    }

    protected DateTimeFormatterBuilder getEditingFormatAsBuilder(
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull String datePattern,
            final @NonNull String timePattern,
            final @NonNull String zonePattern) {

        return new DateTimeFormatterBuilder()
            .appendPattern(getEditingFormatAsPattern(temporalCharacteristic, offsetCharacteristic, datePattern, timePattern, zonePattern));
    }

    protected String getEditingFormatAsPattern(
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull String datePattern,
            final @NonNull String timePattern,
            final @NonNull String zonePattern) {

        switch (temporalCharacteristic) {
        case DATE_TIME:
            return offsetCharacteristic.isLocal()
                    ? datePattern + " " + timePattern
                    : datePattern + " " + timePattern + " " + zonePattern;
        case DATE_ONLY:
            return offsetCharacteristic.isLocal()
                    ? datePattern
                    : datePattern + " " + zonePattern;
        case TIME_ONLY:
            return offsetCharacteristic.isLocal()
                    ? timePattern
                    : timePattern + " " + zonePattern;
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
    }


    protected DateTimeFormatter getTemporalIsoFormat(
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic) {

        switch (temporalCharacteristic) {
        case DATE_TIME:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    : DateTimeFormatter.ISO_DATE_TIME;
        case DATE_ONLY:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_DATE
                    : DateTimeFormatter.ISO_DATE;
        case TIME_ONLY:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_TIME
                    : DateTimeFormatter.ISO_TIME;
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
    }



}
