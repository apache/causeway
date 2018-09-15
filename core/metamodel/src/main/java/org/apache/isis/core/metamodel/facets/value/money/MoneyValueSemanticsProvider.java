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

package org.apache.isis.core.metamodel.facets.value.money;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Money;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class MoneyValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<Money> implements MoneyValueFacet {

    private static Class<? extends Facet> type() {
        return MoneyValueFacet.class;
    }

    private static final NumberFormat DEFAULT_NUMBER_FORMAT;
    private static final NumberFormat DEFAULT_CURRENCY_FORMAT;
    private static final String LOCAL_CURRENCY_CODE;
    private static final int TYPICAL_LENGTH = 18;
    private static final Money DEFAULT_VALUE = null; // no default

    private final String defaultCurrencyCode;

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
        } catch (final UnsupportedOperationException e) {
            return "";
        }
    }

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public MoneyValueSemanticsProvider() {
        this(null, null);
    }

    public MoneyValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, Money.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, context);

        final String property = ConfigurationConstants.ROOT + "value.money.currency";
        defaultCurrencyCode = getConfiguration().getString(property, LOCAL_CURRENCY_CODE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Money doParse(final Object context, final String text) {
        final String entry = text.trim();
        final int pos = entry.lastIndexOf(' ');
        if (endsWithCurrencyCode(entry, pos)) {
            final String value = entry.substring(0, pos);
            final String code = entry.substring(pos + 1);
            return parseNumberAndCurrencyCode(value, code);
        } else {
            return parseDerivedValue(context, entry);
        }
    }

    private boolean endsWithCurrencyCode(final String entry, final int pos) {
        final String suffix = entry.substring(pos + 1);
        final boolean isCurrencyCode = suffix.length() == 3 && Character.isLetter(suffix.charAt(0)) && Character.isLetter(suffix.charAt(1)) && Character.isLetter(suffix.charAt(2));
        return isCurrencyCode;
    }

    private Money parseDerivedValue(final Object original, final String entry) {
        Money money = (Money) original;
        if (money == null || money.getCurrency().equals(LOCAL_CURRENCY_CODE)) {
            try {
                final double value = DEFAULT_CURRENCY_FORMAT.parse(entry).doubleValue();
                money = new Money(value, LOCAL_CURRENCY_CODE);
                return money;
            } catch (final ParseException ignore) {
            }
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

    private Money parseNumberAndCurrencyCode(final String amount, final String code) {
        final String currencyCode = code.toUpperCase();
        try {
            Currency.getInstance(currencyCode.toUpperCase());
        } catch (final IllegalArgumentException e) {
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

    @Override
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
    protected Money doRestore(final String data) {
        final String dataString = data;
        final int pos = dataString.indexOf(' ');
        final String amount = dataString.substring(0, pos);
        final String currency = dataString.substring(pos + 1);
        return new Money(Double.valueOf(amount).doubleValue(), currency);
    }

    // //////////////////////////////////////////////////////////////////
    // MoneyValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public float getAmount(final ObjectAdapter object) {
        final Money money = (Money) object.getObject();
        if (money == null) {
            return 0.0f;
        } else {
            return money.floatValue();
        }
    }

    @Override
    public String getCurrencyCode(final ObjectAdapter object) {
        final Money money = (Money) object.getObject();
        if (money == null) {
            return "";
        } else {
            return money.getCurrency();
        }
    }

    @Override
    public ObjectAdapter createValue(final float amount, final String currencyCode) {
        return getObjectAdapterProvider().adapterFor(new Money(amount, currencyCode));
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "MoneyValueSemanticsProvider: " + getDefaultCurrencyCode();
    }

}
