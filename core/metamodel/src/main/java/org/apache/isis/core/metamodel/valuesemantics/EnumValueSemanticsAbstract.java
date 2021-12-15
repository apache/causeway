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
package org.apache.isis.core.metamodel.valuesemantics;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.util.Enums;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.isis.core.metamodel.commons.MethodExtensions;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

public class EnumValueSemanticsAbstract<T extends Enum<T>>
extends ValueSemanticsAbstract<T>
implements
    DefaultsProvider<T>,
    EncoderDecoder<T>,
    Parser<T>,
    Renderer<T> {

    private final TranslationService translationService;
    private final Method titleMethod;
    @Getter(onMethod_ = {@Override}) private final Class<T> correspondingClass;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) private final int maxLength;

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.ENUM;
    }

    public static EnumValueSemanticsAbstract create(
            final TranslationService translationService,
            final IntrospectionPolicy introspectionPolicy,
            final Class<?> correspondingClass) {
        return new EnumValueSemanticsAbstract(
                translationService,
                introspectionPolicy,
                correspondingClass){};
    }

    // -- CONSTRUCTION

    protected EnumValueSemanticsAbstract(
            final TranslationService translationService,
            final IntrospectionPolicy introspectionPolicy,
            final Class<T> correspondingClass) {
        super();

        this.translationService = translationService;
        this.correspondingClass = correspondingClass;
        this.maxLength = maxLengthFor(correspondingClass);

        val supportMethodEnum = ObjectSupportMethod.TITLE;

        titleMethod =

        MethodFinder
        .objectSupport(
                correspondingClass,
                supportMethodEnum.getMethodNames(),
                introspectionPolicy)
        .withReturnTypeAnyOf(supportMethodEnum.getReturnTypeCategory().getReturnTypes())
        .streamMethodsMatchingSignature(MethodFinder.NO_ARG)
        .findFirst()
        .orElse(null);

    }

    // -- DEFAULTS PROVIDER

    @Override
    public T getDefaultValue() {
        return correspondingClass.getEnumConstants()[0];
    }

    // -- ENCODER/DECODER

    @Override
    public String toEncodedString(final T object) {
        return object!=null ? object.name() : null;
    }

    @Override
    public T fromEncodedString(final String data) {
        return data!=null
                ? Stream.of(correspondingClass.getEnumConstants())
                        .filter(e->e.name().equals(data))
                        .findFirst()
                        .orElseThrow(()->_Exceptions
                                .noSuchElement("enum %s has no matching %s",
                                        correspondingClass.getName(),
                                        data))
                : null;
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final T value) {
        return render(value, v->friendlyName(context, v));
    }

    private String friendlyName(final Context context, final T object) {
        if (titleMethod != null) {
            // sadness: same as in TranslationFactory
            val translationContext = TranslationContext.forMethod(titleMethod);

            try {
                final Object returnValue = MethodExtensions.invoke(titleMethod, object);
                if(returnValue instanceof String) {
                    return (String) returnValue;
                }
                if(returnValue instanceof TranslatableString) {
                    final TranslatableString ts = (TranslatableString) returnValue;
                    return ts.translate(translationService, translationContext);
                }
                return null;
            } catch (final RuntimeException ex) {
                // fall through
            }
        }

        // simply translate the enum constant's name
        val objectAsEnum = object;
        val translationContext = TranslationContext.forEnum(objectAsEnum);
        final String friendlyNameOfEnum = Enums.getFriendlyNameOf(objectAsEnum.name());
        return translationService.translate(translationContext, friendlyNameOfEnum);
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
        val input = _Strings.blankToNullOrTrim(text);
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

    @Override
    public Can<T> getExamples() {
        return Stream.of(correspondingClass.getEnumConstants())
                .limit(2)
                .collect(Can.toCan());
    }

}
