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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.inject.Provider;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.util.Enums;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.common.v2.EnumDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.Getter;
import lombok.experimental.Accessors;

public class EnumValueSemanticsAbstract<T extends Enum<T>>
extends ValueSemanticsAbstract<T>
implements
    DefaultsProvider<T>,
    Parser<T>,
    Renderer<T> {

    private final TranslationService translationService;
    private final Provider<SpecificationLoader> specificationLoaderProvider;
    @Getter(onMethod_ = {@Override}) private final Class<T> correspondingClass;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) private final int maxLength;

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.ENUM;
    }

    public static <T extends Enum<T>> EnumValueSemanticsAbstract<T> create(
            final Provider<SpecificationLoader> specificationLoaderProvider,
            final TranslationService translationService,
            final Class<T> correspondingClass) {
        return new EnumValueSemanticsAbstract<>(
                specificationLoaderProvider,
                translationService,
                correspondingClass){};
    }

    // -- CONSTRUCTION

    protected EnumValueSemanticsAbstract(
            final Provider<SpecificationLoader> specificationLoaderProvider,
            final TranslationService translationService,
            final Class<T> correspondingClass) {
        super();
        this.specificationLoaderProvider = specificationLoaderProvider;
        this.translationService = translationService;
        this.correspondingClass = correspondingClass;
        this.maxLength = maxLengthFor(correspondingClass);
    }

    // -- DEFAULTS PROVIDER

    @Override
    public T getDefaultValue() {
        return correspondingClass.getEnumConstants()[0];
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final T value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public T compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getEnum, this::fromEnumName, ()->null);
    }

    private T fromEnumName(final EnumDto enumDto) {

        var enumName = enumDto.getEnumName();

        return enumName!=null
                ? Stream.of(correspondingClass.getEnumConstants())
                        .filter(e->e.name().equals(enumName))
                        .findFirst()
                        .orElseThrow(()->_Exceptions
                                .noSuchElement("enum %s has no matching %s",
                                        correspondingClass.getName(),
                                        enumName))
                : null;
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final T value) {
        return renderTitle(value, v->friendlyName(context, v));
    }

    @Override
    public String htmlPresentation(final Context context, final T value) {
        return renderHtml(value, v->friendlyName(context, v));
    }

    private String friendlyName(final Context context, final T objectAsEnum) {

        var friendlyNameOfEnum = Optional.ofNullable(loadEnumSpec())
            .map(enumSpec->ManagedObject.value(enumSpec, objectAsEnum))
            .map(MmTitleUtils::titleOf)
            .orElseGet(()->Enums.getFriendlyNameOf(objectAsEnum.name()));

        return Optional.ofNullable(translationService)
                .map(ts->ts.translate(TranslationContext.forEnum(objectAsEnum), friendlyNameOfEnum))
                .orElse(friendlyNameOfEnum);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final T enumValue) {
        if(enumValue==null) {
            return null;
        }
        return Enums.getFriendlyNameOf(enumValue);
    }

    @Override
    public T parseTextRepresentation(
            final ValueSemanticsProvider.Context context,
            final String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        return Enums.parseFriendlyName(correspondingClass, input)
                .orElseThrow(()->new TextEntryParseException("Unknown enum constant '" + input + "'"));
    }

    @Override
    public final int typicalLength() {
        return maxLength();
    }

    @Override
    public Can<T> getExamples() {
        return Stream.of(correspondingClass.getEnumConstants())
                .limit(2)
                .collect(Can.toCan());
    }

    // -- HELPER

    private static <T extends Enum<T>> int maxLengthFor(final Class<T> adaptedClass) {
        int max = Integer.MIN_VALUE;
        for(T e: adaptedClass.getEnumConstants()) {
            final int nameLength = e.name().length();
            final int toStringLength = e.toString().length();
            max = Math.max(max, Math.max(nameLength, toStringLength));
        }
        return max;
    }

    private AtomicBoolean isLoadingEnumSpec = new AtomicBoolean(false);
    private AtomicReference<ObjectSpecification> enumSpecRef = new AtomicReference<>();

    private ObjectSpecification loadEnumSpec() {

        var cachedEnumSpec = enumSpecRef.get();
        if(cachedEnumSpec!=null) {
            return cachedEnumSpec;
        }

        if(isLoadingEnumSpec.get()) {
            return null;
        }

        var enumSpec = Try.call(()->{
            /* if not guarded by recursionDepth logic above,
             * might causes recursive call of lazy getter */
            return Optional.ofNullable(specificationLoaderProvider)
                .map(provider->provider.get())
                .flatMap(specLoader->specLoader.specForType(correspondingClass))
                .orElse(null);

        }).getValue().orElse(null);

        isLoadingEnumSpec.set(false);

        if(enumSpec!=null) {
            enumSpecRef.set(enumSpec);
        }

        return enumSpec;
    }

}
