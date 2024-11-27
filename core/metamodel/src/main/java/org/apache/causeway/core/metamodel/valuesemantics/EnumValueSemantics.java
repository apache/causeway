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
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.util.Enums;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.schema.common.v2.EnumDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.Getter;
import lombok.experimental.Accessors;

public class EnumValueSemantics<T extends Enum<T>>
extends ValueSemanticsAbstract<T>
implements
    DefaultsProvider<T>,
    Parser<T>,
    Renderer<T>,
    OrderRelation<T, Void>{

    @Getter(onMethod_ = {@Override}) private final Class<T> correspondingClass;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) private final int maxLength;

    private final _Lazy<ObjectSpecification> enumSpecLazy;

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.ENUM;
    }

    public static <T extends Enum<T>> EnumValueSemantics<T> create(
            final TranslationService translationService,
            final Class<T> correspondingClass) {
        return new EnumValueSemantics<>(
                translationService,
                correspondingClass);
    }

    // -- CONSTRUCTION

    protected EnumValueSemantics(
            final TranslationService translationService,
            final Class<T> enumClass) {
        super();
        this.translationService = translationService;
        this.correspondingClass = enumClass;
        this.maxLength = maxLengthFor(enumClass);
        this.enumSpecLazy = _Lazy.threadSafe(()->loadEnumSpec(enumClass));
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

    // -- ORDER RELATION

    @Override public Void epsilon() { return null; }
    @Override public int compare(final T a, final T b, final Void epsilon) {
        if(a==null) {
            return b==null ? 0 : -1; // null first semantic
        }
        if(b==null) return 1; // null first semantic
        return _Ints.compare(a.ordinal(), b.ordinal()); // strictly returning -1, 0, or 1
    }
    @Override public boolean equals(final T a, final T b, final Void epsilon) {
        return a == b; // ordinal is only ever equal if object-ids are same (null-safe)
    }

    // --

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

    private String friendlyName(final Context context, final T objectAsEnum) {
        var friendlyNameOfEnum = Optional.ofNullable(enumSpecLazy.get())
            .map(enumSpec->ManagedObject.value(enumSpec, objectAsEnum))
            .map(MmTitleUtils::titleOf)
            .orElseGet(()->Enums.getFriendlyNameOf(objectAsEnum.name()));

        return Optional.ofNullable(translationService)
                .map(ts->ts.translate(TranslationContext.forEnum(objectAsEnum), friendlyNameOfEnum))
                .orElse(friendlyNameOfEnum);
    }

    private static <T extends Enum<T>> int maxLengthFor(final Class<T> enumClass) {
        int max = Integer.MIN_VALUE;
        for(T e: enumClass.getEnumConstants()) {
            final int nameLength = e.name().length();
            final int toStringLength = e.toString().length();
            max = Math.max(max, Math.max(nameLength, toStringLength));
        }
        return max;
    }

    @Nullable
    private static ObjectSpecification loadEnumSpec(
        final Class<?> enumClass) {
        var enumSpec = Try.call(()->
                MetaModelContext.instance()
                .map(MetaModelContext::getSpecificationLoader)
                .flatMap(specLoader->specLoader.specForType(enumClass))
                .orElse(null))
            .valueAsNullableElseFail();
        return enumSpec;
    }

}
