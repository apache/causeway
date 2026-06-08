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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

/**
 * @since 2.x {@index}
 */
public abstract class ValueSemanticsAbstract<T>
implements
ValueSemanticsProvider<T> {

    public enum FormatUsageFor {
        PARSING,
        RENDERING;
        public boolean isParsing() { return this==PARSING; }
        public boolean isRendering() { return this==RENDERING; }
    }

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
    public Parser<T> getParser() {
        return this instanceof Parser ? (Parser<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return this instanceof DefaultsProvider ? (DefaultsProvider<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdStringifier<T> getIdStringifier() {
        return this instanceof IdStringifier ? (IdStringifier<T>)this : null;
    }

    /**
     * JUnit support.
     */
    public Can<T> getExamples() { return Can.empty(); }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link Locale} from given context or else system's default
     */
    protected UserLocale getUserLocale(final ValueSemanticsProvider.@Nullable Context context) {
        return ValueSemanticsProvider.getUserLocale(context);
    }

    protected String renderTitle(final T value, final Function<T, String> toString) {
        return Optional.ofNullable(value)
                .map(toString)
                .orElseGet(()->getPlaceholderRenderService().asText(PlaceholderLiteral.NULL_REPRESENTATION));
    }

    protected String renderHtml(final T value, final Function<T, String> toString) {
        return Optional.ofNullable(value)
                .map(toString)
                .orElseGet(()->getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
    }

    // -- COMPOSITION UTILS

    protected ValueDecomposition decomposeAsString(
            final @Nullable T value,
            final @NonNull Function<T, String> toString,
            final @NonNull Supplier<String> onNull) {
        _Assert.assertEquals(getSchemaValueType(), ValueType.STRING);
        return CommonDtoUtils.fundamentalTypeAsDecomposition(ValueType.STRING,
                value!=null
                ? toString.apply(value)
                : onNull.get());
    }

    protected T composeFromString(
            final @Nullable ValueDecomposition decomposition,
            final @NonNull Function<String, T> fromString,
            final @NonNull Supplier<T> onNullOrEmpty) {
        var string = decomposition!=null
                ? decomposition.fundamentalAsOptional().map(ValueWithTypeDto::getString).orElse(null)
                : null;
        return _Strings.isNotEmpty(string)
                ? fromString.apply(string)
                : onNullOrEmpty.get();
    }

    /**
     * @param <F> - the underlying fundamental value-type
     */
    protected <F> ValueDecomposition decomposeAsNullable(
            final @Nullable T value,
            final @NonNull Function<T, F> onNonNull,
            final @NonNull Supplier<F> onNull) {
        return CommonDtoUtils.fundamentalTypeAsDecomposition(getSchemaValueType(),
                value!=null
                ? onNonNull.apply(value)
                : onNull.get());
    }

    /**
     * @param <F> - the underlying fundamental value-type
     */
    protected <F> T composeFromNullable(
            final @Nullable ValueDecomposition decomposition,
            final @NonNull Function<ValueWithTypeDto, F> fundamentalValueExtractor,
            final @NonNull Function<F, T> onNonNull,
            final @NonNull Supplier<T> onNull) {

        var valuePojo = decomposition!=null
                ? decomposition.fundamentalAsOptional().map(fundamentalValueExtractor).orElse(null)
                : null;

        return valuePojo!=null
                ? onNonNull.apply(valuePojo)
                : onNull.get();
    }

    // -- TEMPORAL RENDERING

    protected DateTimeFormatter getTemporalNoZoneRenderingFormat(
            final @Nullable Context context,
            final TemporalValueSemantics.@NonNull TemporalCharacteristic temporalCharacteristic,
            final TemporalValueSemantics.@NonNull OffsetCharacteristic offsetCharacteristic,
            final @NonNull FormatStyle dateFormatStyle,
            final @NonNull FormatStyle timeFormatStyle,
            final @Nullable String datePattern,
            final @Nullable String dateTimePattern) {

        final DateTimeFormatter noZoneOutputFormat = switch (temporalCharacteristic) {
            case DATE_TIME -> dateTimePattern != null
                                ? DateTimeFormatter.ofPattern(dateTimePattern)
                                : DateTimeFormatter.ofLocalizedDateTime(dateFormatStyle, timeFormatStyle);
            case DATE_ONLY -> datePattern != null
                                ? DateTimeFormatter.ofPattern(datePattern)
                                : DateTimeFormatter.ofLocalizedDate(dateFormatStyle);
            case TIME_ONLY -> DateTimeFormatter.ofLocalizedTime(timeFormatStyle);
        };

        return noZoneOutputFormat
                .withLocale(getUserLocale(context).timeFormatLocale());
    }

    protected Optional<DateTimeFormatter> getTemporalZoneOnlyRenderingFormat(
            final ValueSemanticsProvider.@Nullable Context context,
            final TemporalValueSemantics.@NonNull TemporalCharacteristic temporalCharacteristic,
            final TemporalValueSemantics.@NonNull OffsetCharacteristic offsetCharacteristic) {

        return switch (offsetCharacteristic) {
            case LOCAL -> Optional.empty();
            case OFFSET -> Optional.of(_Temporals.ISO_OFFSET_ONLY_FORMAT);
            case ZONED -> Optional.of(_Temporals.DEFAULT_ZONEID_ONLY_FORMAT);
        };
    }

    // -- TRANSLATION SUPPORT

    @Autowired(required = false) // nullable (JUnit support)
    protected Provider<TranslationService> translationService;
    protected String translate(final String text) {
        return translationService!=null
                ? translationService.get().translate(TranslationContext.empty(), text)
                : text;
    }

    // -- PLACEHOLDER RENDERING

    @Autowired(required = false) // nullable (JUnit support)
    private Optional<Provider<PlaceholderRenderService>> placeholderRenderService = Optional.empty();
    protected PlaceholderRenderService getPlaceholderRenderService() {
        return placeholderRenderService.map(Provider::get).orElseGet(PlaceholderRenderService::fallback);
    }

    // -- UTILS

    /**
     * Uses bootstrap CSS.
     */
    protected final String toMonospace(final String html) {
        return """
            <span class="font-monospace fw-light">%s</span>""".formatted(html);
    }

    /**
     * Uses Fontawesome.
     */
    protected final String faIconAndTitle(final FontAwesomeLayers faLayers, final String titleHtml) {
        return """
            <span>%s%s</span>""".formatted(faLayers.toHtml(), titleHtml);
    }

}
