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
package org.apache.isis.core.metamodel.facets.value.longs;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.core.metamodel.commons.LocaleUtil;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public abstract class LongValueSemanticsProviderAbstract
extends ValueSemanticsProviderAndFacetAbstract<Long>
implements LongValueFacet {

    private static final Class<? extends Facet> type() {
        return LongValueFacet.class;
    }

    private static final Long DEFAULT_VALUE = Long.valueOf(0L);
    private static final int MAX_LENGTH = 18;
    private static final int TYPICAL_LENGTH = MAX_LENGTH;

    private final NumberFormat format;

    public LongValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Long> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
        final String formatRequired = getConfiguration().getValueTypes().getJavaLang().getLong().getFormat();

        format = formatRequired != null
                ? new DecimalFormat(formatRequired)
                : NumberFormat.getNumberInstance(getConfiguration().getCore().getRuntime().getLocale().map(LocaleUtil::findLocale).orElse(Locale.getDefault()));
    }


    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Long doParse(final Parser.Context context, final String entry) {
        try {
            return Long.valueOf(format.parse(entry).longValue());
        } catch (final ParseException e) {
            throw new TextEntryParseException("Not a whole number " + entry, e);
        }
    }

    @Override
    public String titleString(final Object value) {
        return titleString(format, value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final Long object) {
        return object.toString();
    }

    @Override
    public Long fromEncodedString(final String data) {
        return Long.parseLong(data);
    }

    // //////////////////////////////////////////////////////////////////
    // LongValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Long longValue(final ManagedObject object) {
        return (Long) (object == null ? null : object.getPojo());
    }

    @Override
    public ManagedObject createValue(final Long value) {
        return value == null ? null : getObjectManager().adapt(value);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("format", format);
    }
}
