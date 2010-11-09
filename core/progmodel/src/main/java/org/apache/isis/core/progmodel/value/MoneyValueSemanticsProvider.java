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


package org.apache.isis.core.progmodel.value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Money;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.TextEntryParseException;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.progmodel.facets.value.MoneyValueFacet;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


public class MoneyValueSemanticsProvider extends ValueSemanticsProviderAbstract implements MoneyValueFacet {

    private static Class<? extends Facet> type() {
        return MoneyValueFacet.class;
    }

    private static final NumberFormat DEFAULT_NUMBER_FORMAT;
    private static final NumberFormat DEFAULT_CURRENCY_FORMAT;
    private static final String LOCAL_CURRENCY_CODE;
    private static final int TYPICAL_LENGTH = 18;
    private static final boolean IMMUTABLE = true;
    private static final boolean EQUAL_BY_CONTENT = true;
    private static final Object DEFAULT_VALUE = null; // no default

    private String defaultCurrencyCode;

    static {
        DEFAULT_NUMBER_FORMAT = NumberFormat.getNumberInstance();
        DEFAULT_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
        DEFAULT_NUMBER_FORMAT.setMinimumFractionDigits(DEFAULT_CURRENCY_FORMAT.getMinimumFractionDigits());
        DEFAULT_NUMBER_FORMAT.setMaximumFractionDigits(DEFAULT_CURRENCY_FORMAT.getMaximumFractionDigits());
        LOCAL_CURRENCY_CODE = getDefaultCurrencyCode();
    }

    static final boolean isAPropertyDefaultFacet() {
        return PropertyDefaultFacet.class.isAssignableFrom(MoneyValueSemanticsProvider.class);
    }

    private static String getDefaultCurrencyCode() {
        try {
            return DEFAULT_CURRENCY_FORMAT.getCurrency().getCurrencyCode();
        } catch (UnsupportedOperationException e) {
            return "";
        }
    }


    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    @SuppressWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public MoneyValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public MoneyValueSemanticsProvider(
            final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(type(), holder, Money.class, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, DEFAULT_VALUE, configuration,
                specificationLoader, runtimeContext);

        final String property = ConfigurationConstants.ROOT + "value.money.currency";
        defaultCurrencyCode = configuration.getString(property, LOCAL_CURRENCY_CODE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Object doParse(final Object original, final String text) {
        final String entry = text.trim();
        final int pos = entry.lastIndexOf(' ');
        if (endsWithCurrencyCode(entry, pos)) {
            final String value = entry.substring(0, pos);
            final String code = entry.substring(pos + 1);
            return parseNumberAndCurrencyCode(value, code);
        } else {
            return parseDerivedValue(original, entry);
        }
    }

    private boolean endsWithCurrencyCode(final String entry, final int pos) {
        String suffix = entry.substring(pos + 1);
        boolean isCurrencyCode = suffix.length() == 3 && Character.isLetter(suffix.charAt(0)) && Character.isLetter(suffix.charAt(1))
                && Character.isLetter(suffix.charAt(2));
        return isCurrencyCode;
    }

    private Object parseDerivedValue(final Object original, final String entry) {
        Money money = (Money) original;
        if (money == null || money.getCurrency().equals(LOCAL_CURRENCY_CODE)) {
            try {
                final double value = DEFAULT_CURRENCY_FORMAT.parse(entry).doubleValue();
                money = new Money(value, LOCAL_CURRENCY_CODE);
                return money;
            } catch (final ParseException ignore) { }
        }

        try {
            final double value = DEFAULT_NUMBER_FORMAT.parse(entry).doubleValue();
            final String currencyCode = money == null ? defaultCurrencyCode : money.getCurrency();
            money = new Money(value, currencyCode);
            return money;
        } catch (final ParseException ex) {
            throw new TextEntryParseException("Not a distinguishable money value " + entry, ex);
        }
    }

    private Object parseNumberAndCurrencyCode(final String amount, final String code) {
        final String currencyCode = code.toUpperCase();
        try {
            Currency.getInstance(currencyCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TextEntryParseException("Invalid currency code " + currencyCode, e);
        }
        try {
            final Money money = new Money(DEFAULT_NUMBER_FORMAT.parse(amount).doubleValue(), currencyCode);
            return money;
        } catch (final ParseException e) {
            throw new TextEntryParseException("Invalid money entry", e);
        }
     }


    @Override
    public String titleString(final Object object) {
        if (object == null) {
            return "";
        }
        final Money money = (Money) object;
        final boolean localCurrency = LOCAL_CURRENCY_CODE.equals(money.getCurrency());
        if (localCurrency) {
            return DEFAULT_CURRENCY_FORMAT.format(money.doubleValue());
        } else {
            return DEFAULT_NUMBER_FORMAT.format(money.doubleValue()) + " " + money.getCurrency();
        }
    }

    public String titleStringWithMask(final Object value, final String usingMask) {
        if (value == null) {
            return "";
        }
        final Money money = (Money) value;
        return new DecimalFormat(usingMask).format(money.doubleValue());
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final Money money = (Money) object;
        final String value = String.valueOf(money.doubleValue()) + " " + money.getCurrency();
        return value;
    }

    @Override
    protected Object doRestore(final String data) {
        final String dataString = data;
        final int pos = dataString.indexOf(' ');
        final String amount = dataString.substring(0, pos);
        final String currency = dataString.substring(pos + 1);
        return new Money(Double.valueOf(amount).doubleValue(), currency);
    }

    // //////////////////////////////////////////////////////////////////
    // MoneyValueFacet
    // //////////////////////////////////////////////////////////////////

    public float getAmount(final ObjectAdapter object) {
        final Money money = (Money) object.getObject();
        if (money == null) {
            return 0.0f;
        } else {
            return money.floatValue();
        }
    }

    public String getCurrencyCode(final ObjectAdapter object) {
        final Money money = (Money) object.getObject();
        if (money == null) {
            return "";
        } else {
            return money.getCurrency();
        }
    }

    public ObjectAdapter createValue(final float amount, final String currencyCode) {
        return getRuntimeContext().adapterFor(new Money(amount, currencyCode));
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "MoneyValueSemanticsProvider: " + getDefaultCurrencyCode();
    }

}
