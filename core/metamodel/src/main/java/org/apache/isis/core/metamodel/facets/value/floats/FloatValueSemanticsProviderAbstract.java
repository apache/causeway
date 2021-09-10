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
package org.apache.isis.core.metamodel.facets.value.floats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.core.metamodel.commons.LocaleUtil;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class FloatValueSemanticsProviderAbstract
extends ValueSemanticsProviderAndFacetAbstract<Float>
implements FloatingPointValueFacet {

    private static final Class<? extends Facet> type() {
        return FloatingPointValueFacet.class;
    }

    private static final Float DEFAULT_VALUE = Float.valueOf(0.0f);
    private static final int MAX_LENGTH = 20;
    private static final int TYPICAL_LENGTH = 12;

    private final NumberFormat format;

    public FloatValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Float> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
        final String formatRequired = getConfiguration().getValueTypes().getJavaLang().getFloat().getFormat();

        format = formatRequired != null
                ? new DecimalFormat(formatRequired)
                : NumberFormat.getNumberInstance(getConfiguration().getCore().getRuntime().getLocale().map(LocaleUtil::findLocale).orElse(Locale.getDefault()));
    }


    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Float doParse(final Object context, final String entry) {
        try {
            return Float.valueOf(format.parse(entry).floatValue());
        } catch (final ParseException e) {
            throw new TextEntryParseException("Not a floating point number " + entry, e);
        }
    }

    @Override
    public String titleString(final Object value) {
        return titleString(format, value);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(new DecimalFormat(usingMask), value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Float object) {
        return object.toString();
    }

    @Override
    protected Float doRestore(final String data) {
        return new Float(data);
    }

    // //////////////////////////////////////////////////////////////////
    // FloatingPointValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Float floatValue(final ManagedObject object) {
        return object == null ? null : (Float) object.getPojo();
    }

    @Override
    public ManagedObject createValue(final Float value) {
        return getObjectManager().adapt(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "FloatValueSemanticsProvider: " + format;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("format", format);
    }
}
