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

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderContext;

public class EnumValueSemanticsProvider<T extends Enum<T>> extends ValueSemanticsProviderAndFacetAbstract<T> implements EnumFacet {

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
    
    /**
     * Required because {@link Parser} and {@link EncoderDecoder}.
     */
    public EnumValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public EnumValueSemanticsProvider(final FacetHolder holder, final Class<T> adaptedClass, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super(
                type(), holder,  adaptedClass, 
                maxLengthFor(adaptedClass),
                maxLengthFor(adaptedClass), Immutability.IMMUTABLE,
                EqualByContent.HONOURED, 
                defaultFor(adaptedClass), 
                configuration, context);
    }

    @Override
    protected T doParse(final Object context, final String entry) {
        final T[] enumConstants = getAdaptedClass().getEnumConstants();
        for (final T enumConstant : enumConstants) {
            if (enumConstant.toString().equals(entry)) {
                return enumConstant;
            }
        }
        throw new TextEntryParseException("Unknown enum constant '" + entry + "'");
    }

    @Override
    protected String doEncode(final Object object) {
        return titleString(object, null);
    }

    @Override
    protected T doRestore(final String data) {
        return doParse(null, data);
    }

    @Override
    protected String titleString(final Object object, final Localization localization) {
        return object.toString();
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value, null);
    }

}
