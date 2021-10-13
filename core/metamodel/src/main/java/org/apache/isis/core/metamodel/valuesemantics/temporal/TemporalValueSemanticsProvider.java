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
package org.apache.isis.core.metamodel.valuesemantics.temporal;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Common base for {@link java.time.temporal.Temporal} types.
 *
 * @since 2.0
 *
 * @param <T> implementing {@link java.time.temporal.Temporal} type
 */
@Log4j2
public abstract class TemporalValueSemanticsProvider<T extends Temporal>
extends ValueSemanticsAbstract<T>
implements TemporalValueSemantics<T> {

    @Getter(onMethod_ = {@Override}) protected final TemporalCharacteristic temporalCharacteristic;
    @Getter(onMethod_ = {@Override}) protected final OffsetCharacteristic offsetCharacteristic;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) protected final int typicalLength;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) protected final int maxLength;

    @Getter private DateTimeFormatter encodingFormatter;
    @Getter @Setter private DateTimeFormatter titleFormatter;

    /**
     * Keys represent the values which can be configured,
     * and which are used for the rendering of dates.
     */
    protected final Map<String, DateTimeFormatter> namedFormatters;
    protected Can<Function<String, T>> parsers;

    protected final TemporalQuery<T> query;
    protected final BiFunction<TemporalAdjust, T, T> adjuster;

    protected TemporalValueSemanticsProvider(
            final TemporalCharacteristic temporalCharacteristic,
            final OffsetCharacteristic offsetCharacteristic,
            final int typicalLength,
            final int maxLength,
            final TemporalQuery<T> query,
            final BiFunction<TemporalAdjust, T, T> adjuster) {

        super();

        this.temporalCharacteristic = temporalCharacteristic;
        this.offsetCharacteristic = offsetCharacteristic;
        this.typicalLength = typicalLength;
        this.maxLength = maxLength;

        this.query = query;
        this.adjuster = adjuster;

        namedFormatters = _Maps.newLinkedHashMap();
        namedFormatters.put("internal_encoding", this.getEncodingFormatter());
        updateParsers();
    }


    protected void setEncodingFormatter(final DateTimeFormatter encodingFormatter) {
        this.encodingFormatter = encodingFormatter;
    }

    protected void addNamedFormat(final String name, final String pattern) {
        namedFormatters.put(name, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    }

    protected Optional<FormatStyle> lookupFormatStyle(final String styleName) {
        if(styleName==null) {
            return Optional.empty();
        }
        return Stream.of(FormatStyle.values())
        .filter(style->style.name().toLowerCase().equals(styleName))
        .findFirst();
    }

    protected Optional<DateTimeFormatter> lookupNamedFormatter(final String formatName) {
        return Optional.ofNullable(namedFormatters.get(formatName));
    }

    protected DateTimeFormatter lookupNamedFormatterElseFail(final String formatName) {
        return lookupNamedFormatter(formatName)
                .orElseThrow(()->_Exceptions.noSuchElement("unknown format name %s", formatName));
    }

    protected Optional<DateTimeFormatter> formatterFromPattern(final String pattern) {
        try {
            return Optional.of(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
        } catch (Exception e) {
            log.warn("cannot parse pattern '{}'", pattern, e);
        }
        return Optional.empty();
    }

    protected void updateParsers() {
        parsers = Can.ofCollection(namedFormatters.values())
                .map(formatter->{
                    return $->formatter.parse($, query);
                });
    }

    protected Optional<DateTimeFormatter> formatterFirstOf(
            final @NonNull Can<Supplier<Optional<DateTimeFormatter>>> formatterProviders) {
        return formatterProviders.stream()
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    // -- ENCODER/DECODER

    @Override
    public String toEncodedString(final T temporal) {
        if(temporal==null) {
            return null;
        }
        return encodingFormatter.format(temporal);
    }

    @Override
    public T fromEncodedString(final String data) {
        if(data==null) {
            return null;
        }
        try {
            return encodingFormatter.parse(data, query);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(
            final ValueSemanticsProvider.Context context,
            final T value) {
        val temporal = _Casts.<T>uncheckedCast(value);
        return render(temporal, titleFormatter::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final T value) {
        return toEncodedString(value);
    }

    @Override
    public T parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        T contextTemporal = _Casts.uncheckedCast(context);
        val temporalString = text.trim().toUpperCase();
        if(contextTemporal != null) {
            val adjusted = TemporalAdjust
                    .parseAdjustment(adjuster, contextTemporal, temporalString);
            if(adjusted!=null) {
                return adjusted;
            }
        }
        for(val parser: parsers) {
            try {
                return parser.apply(temporalString);
            } catch (final IllegalArgumentException e) {
                // continue to next
            }
        }
        val msg = String.format("Not recognised as a %s: %s",
                getCorrespondingClass().getName(),
                temporalString);
        throw new TextEntryParseException(msg);
    }


}
