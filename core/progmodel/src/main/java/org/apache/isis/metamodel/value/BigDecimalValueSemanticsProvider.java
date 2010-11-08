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


package org.apache.isis.metamodel.value;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.TextEntryParseException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.progmodel.facets.value.BigDecimalValueFacet;


public class BigDecimalValueSemanticsProvider extends ValueSemanticsProviderAbstract implements BigDecimalValueFacet {

    private static final int TYPICAL_LENGTH = 19;

    private static Class<? extends Facet> type() {
        return BigDecimalValueFacet.class;
    }

    private static final boolean IMMUTABLE = true;
    private static final boolean EQUAL_BY_CONTENT = true;
    private static final Object DEFAULT_VALUE = new BigDecimal(0);

    private final NumberFormat format;

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public BigDecimalValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public BigDecimalValueSemanticsProvider(
    		final FacetHolder holder, 
            final IsisConfiguration configuration, 
            final SpecificationLoader specificationLoader, 
            final RuntimeContext runtimeContext) {
        super(type(), holder, BigDecimal.class, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, DEFAULT_VALUE, configuration, specificationLoader, runtimeContext);
        format = determineNumberFormat("value.format.decimal");
    }


    public void setLocale(Locale l) {
        // TODO Auto-generated method stub
        
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Object doParse(final Object original, final String entry) {
        try {
            return new BigDecimal(entry);
        } catch (final NumberFormatException e) {
            throw new TextEntryParseException("Not an decimal " + entry, e);
        }
    }

    @Override
    public String titleString(final Object object) {
        return titleString(format, object);
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
        // for dotnet compatibility - toString pre 1.3 was equivalent to toPlainString later.
        try {
            final Class<?> type = object.getClass();
            try {
                return (String) type.getMethod("toPlainString", (Class[]) null).invoke(object, (Object[]) null);
            } catch (final NoSuchMethodException nsm) {
                return (String) type.getMethod("toString", (Class[]) null).invoke(object, (Object[]) null);
            }
        } catch (final Exception e) {
            throw new IsisException(e);
        }

    }

    @Override
    protected Object doRestore(final String data) {
        return new BigDecimal(data);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "BigDecimalValueSemanticsProvider: " + format;
    }


}
