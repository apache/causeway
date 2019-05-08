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
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;


public abstract class DoubleValueSemanticsProviderAbstract extends ValueSemanticsProviderAndFacetAbstract<Double> implements DoubleFloatingPointValueFacet {

    private static Class<? extends Facet> type() {
        return DoubleFloatingPointValueFacet.class;
    }

    private static final Double DEFAULT_VALUE = new Double(0.0d);
    private static final int MAX_LENGTH = 25;
    private static final int TYPICAL_LENGTH = 10;

    private final NumberFormat format;

    public DoubleValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Double> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
        format = determineNumberFormat("value.format.double");
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Double doParse(final Object context, final String entry) {
        try {
            return new Double(format.parse(entry).doubleValue());
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

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(new DecimalFormat(usingMask), value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        return object.toString();
    }

    @Override
    protected Double doRestore(final String data) {
        return new Double(data);
    }

    // //////////////////////////////////////////////////////////////////
    // DoubleValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Double doubleValue(final ObjectAdapter object) {
        return (Double) (object == null ? null : object.getPojo());
    }

    @Override
    public ObjectAdapter createValue(final Double value) {
        return getObjectAdapterProvider().adapterFor(value);
    }

    // /////// toString ///////
    @Override
    public String toString() {
        return "DoubleValueSemanticsProvider: " + format;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("format", format);
    }

}
