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

package org.apache.isis.core.metamodel.facets.value.percentage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;

import org.apache.isis.core.metamodel.facets.value.floats.FloatingPointValueFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class PercentageValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<Percentage> implements FloatingPointValueFacet {

    private static final NumberFormat PERCENTAGE_FORMAT = NumberFormat.getPercentInstance();
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();

    private static final Percentage DEFAULT_VALUE = new Percentage(0.0f);
    private static final int TYPICAL_LENGTH = 12;

    public static Class<? extends Facet> type() {
        return FloatingPointValueFacet.class;
    }

    private NumberFormat format = PERCENTAGE_FORMAT;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public PercentageValueSemanticsProvider() {
        this(null, null);
    }

    public PercentageValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, Percentage.class, TYPICAL_LENGTH, null, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, context);

        final String formatRequired = getConfiguration().getString(ConfigurationConstants.ROOT + "value.format.percentage");
        if (formatRequired == null) {
            format = PERCENTAGE_FORMAT;
        } else {
            format = new DecimalFormat(formatRequired);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Percentage doParse(final Object context, final String text) {
        try {
            return new Percentage(new Float(format.parse(text).floatValue()));
        } catch (final ParseException e) {
            try {
                return new Percentage(asFloat(text));
            } catch (final ParseException ee) {
                throw new TextEntryParseException("Not a number " + text, ee);
            }
        }
    }

    private Float asFloat(final String text) throws ParseException {
        return new Float(DECIMAL_FORMAT.parse(text).floatValue());
    }

    @Override
    public String titleString(final Object value) {
        return titleString(format, value);
    }

    private String titleString(final NumberFormat formatter, final Object value) {
        return value == null ? "" : format.format(((Percentage) value).floatValue());
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(new DecimalFormat(usingMask), value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final Percentage per = (Percentage) object;
        return String.valueOf(per.floatValue());
    }

    @Override
    protected Percentage doRestore(final String data) {
        return new Percentage(Float.valueOf(data).floatValue());
    }

    // //////////////////////////////////////////////////////////////////
    // FloatingPointValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Float floatValue(final ObjectAdapter object) {
        final Percentage per = (Percentage) object.getObject();
        return new Float(per.floatValue());
    }

    @Override
    public ObjectAdapter createValue(final Float value) {
        return getObjectAdapterProvider().adapterFor(value);
    }

    // //////////////////////////////////////////////////////////////////
    // PropertyDefaultFacet
    // //////////////////////////////////////////////////////////////////

    public Object getDefault(final ObjectAdapter inObject) {
        return Float.valueOf(0.0f);
    }

    // //// toString ////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "PercentageValueSemanticsProvider: " + format;
    }

}
