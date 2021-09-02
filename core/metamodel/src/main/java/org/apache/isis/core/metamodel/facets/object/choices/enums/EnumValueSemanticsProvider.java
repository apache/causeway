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

package org.apache.isis.core.metamodel.facets.object.choices.enums;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.util.Enums;
import org.apache.isis.core.metamodel.commons.MethodExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;

import lombok.val;

public class EnumValueSemanticsProvider<T extends Enum<T>>
extends ValueSemanticsProviderAndFacetAbstract<T>
implements EnumFacet {


    private static Class<? extends Facet> type() {
        return EnumFacet.class;
    }

    private static <T> T defaultFor(final Class<T> adaptedClass) {
        return adaptedClass.getEnumConstants()[0];
    }

    private static <T extends Enum<T>> int maxLengthFor(final Class<T> adaptedClass) {
        int max = Integer.MIN_VALUE;
        for(T e: adaptedClass.getEnumConstants()) {
            final int nameLength = e.name().length();
            final int toStringLength = e.toString().length();
            max = Math.max(max, Math.max(nameLength, toStringLength));
        }
        return max;
    }


    private static final String TITLE = "title";

    private final Method titleMethod;

//    /**
//     * Required because {@link Parser} and {@link EncoderDecoder}.
//     */
//    public EnumValueSemanticsProvider() {
//        this(EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED, null, null);
//    }

    public EnumValueSemanticsProvider(
            final IntrospectionPolicy introspectionPolicy,
            final FacetHolder holder,
            final Class<T> adaptedClass) {
        super(
                type(), holder,  adaptedClass,
                maxLengthFor(adaptedClass),
                maxLengthFor(adaptedClass), Immutability.IMMUTABLE,
                EqualByContent.HONOURED,
                defaultFor(adaptedClass));

        titleMethod = MethodFinderUtils.findMethod_returningText(
                MethodFinderOptions.objectSupport(introspectionPolicy),
                getAdaptedClass(),
                TITLE,
                null);

    }

    @Override
    protected T doParse(final Object context, final String entry) {
        final T[] enumConstants = getAdaptedClass().getEnumConstants();
        for (final T enumConstant : enumConstants) {
            if (doEncode(enumConstant).equals(entry)) {
                return enumConstant;
            }
        }
        // fallback
        for (final T enumConstant : enumConstants) {
            if (enumConstant.toString().equals(entry)) {
                return enumConstant;
            }
        }
        throw new TextEntryParseException("Unknown enum constant '" + entry + "'");
    }

    @Override
    protected String doEncode(final T object) {
        return titleString(object);
    }

    @Override
    protected T doRestore(final String data) {
        return doParse((Object)null, data);
    }


    @Override
    protected String titleString(final Object object) {
        val translationService = getTranslationService();

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
        val objectAsEnum = (Enum<?>) object;
        val translationContext = TranslationContext.forEnum(objectAsEnum);
        final String friendlyNameOfEnum = Enums.getFriendlyNameOf(objectAsEnum.name());
        return translationService.translate(translationContext, friendlyNameOfEnum);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("titleMethod", titleMethod);
    }
}
