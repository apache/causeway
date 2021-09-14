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
package org.apache.isis.core.metamodel.facets.value.doubles;

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

public abstract class DoubleValueSemanticsProviderAbstract
extends ValueSemanticsProviderAndFacetAbstract<Double>
implements DoubleFloatingPointValueFacet {

    private static Class<? extends Facet> type() {
        return DoubleFloatingPointValueFacet.class;
    }

    private static final Double DEFAULT_VALUE = Double.valueOf(0.0d);
    private static final int MAX_LENGTH = 25;
    private static final int TYPICAL_LENGTH = 10;

    private final NumberFormat format;

    public DoubleValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Double> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
        final String formatRequired = getConfiguration().getValueTypes().getJavaLang().getDouble().getFormat();

        format = formatRequired != null
                ? new DecimalFormat(formatRequired)
                : NumberFormat.getNumberInstance(getConfiguration().getCore().getRuntime().getLocale().map(LocaleUtil::findLocale).orElse(Locale.getDefault()));
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Double doParse(final Parser.Context context, final String entry) {
        try {
            return Double.valueOf(format.parse(entry).doubleValue());
        } catch (final ParseException e) {
            throw new TextEntryParseException("Not floating point number " + entry, e);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String titleString(final Object value) {
        return titleString(format, value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final Double object) {
        return object.toString();
    }

    @Override
    public Double fromEncodedString(final String data) {
        return Double.valueOf(data);
    }

    // //////////////////////////////////////////////////////////////////
    // DoubleValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Double doubleValue(final ManagedObject object) {
        return (Double) (object == null ? null : object.getPojo());
    }

    @Override
    public ManagedObject createValue(final Double value) {
        return getObjectManager().adapt(value);
    }

    // /////// toString ///////
    @Override
    public String toString() {
        return "DoubleValueSemanticsProvider: " + format;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("format", format);
    }

}
