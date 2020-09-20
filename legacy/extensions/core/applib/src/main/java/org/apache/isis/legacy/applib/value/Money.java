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

package org.apache.isis.legacy.applib.value;

import java.math.BigDecimal;

import org.apache.isis.applib.annotation.Value;

/**
 * @deprecated
 */
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.money.MoneyValueSemanticsProvider")
@Deprecated
public class Money extends Magnitude<Money> {

    private static final long serialVersionUID = 1L;
    private static final int[] cents = new int[] { 1, 10, 100, 100 };
    private final long amount;
    private final String currency;

    public Money(final double amount, final String currency) {
        assertCurrencySet(currency);
        this.currency = currency.toUpperCase();
        this.amount = Math.round(amount * centFactor());
    }

    public Money(final long amount, final String currency) {
        assertCurrencySet(currency);
        this.currency = currency.toUpperCase();
        this.amount = amount * centFactor();
    }

    private void assertCurrencySet(final String currency) {
        if (currency == null || currency.equals("")) {
            throw new IllegalArgumentException("Currency must be specified");
        }
        if (currency.length() != 3) {
            throw new IllegalArgumentException("Invalid currency code '" + currency + "'");
        }
    }

    /**
     * Add the specified money to this money.
     */
    public Money add(final Money money) {
        assertSameCurrency(money);
        return newMoney(amount + money.amount);
    }

    private void assertSameCurrency(final Money money) {
        if (!money.getCurrency().equals(getCurrency())) {
            throw new IllegalArgumentException("Not the same currency: " + getCurrency() + " & " + money.getCurrency());
        }
    }

    private int centFactor() {
        return cents[getFractionalDigits()];
    }

    /**
     * Returns this value as a double.
     */
    public double doubleValue() {
        return amount / (double) centFactor();
    }

    /**
     * Returns this value as a float.
     */
    public float floatValue() {
        return amount;
    }

    public BigDecimal getAmount() {
        return BigDecimal.valueOf(amount, getFractionalDigits());
    }

    public String getCurrency() {
        return currency;
    }

    private int getFractionalDigits() {
        return 2;
    }

    public boolean hasSameCurrency(final Money money) {
        return currency.equals(money.currency);
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        return (int) amount;
    }

    @Override
    public boolean isEqualTo(final Money magnitude) {
        if (!hasSameCurrency(magnitude)) {
            throw new IllegalArgumentException("Parameter must be of type Money and have the same currency");
        }
        return (magnitude).amount == amount;
    }

    public boolean isGreaterThanZero() {
        return amount > 0;
    }

    @Override
    public boolean isLessThan(final Money magnitude) {
        if (!hasSameCurrency(magnitude)) {
            throw new IllegalArgumentException("Parameter must be of type Money and have the same currency");
        }
        return amount < (magnitude).amount;
    }

    /**
     * Returns true if this value is less than zero.
     */
    public boolean isLessThanZero() {
        return amount < 0;
    }

    public boolean isZero() {
        return amount == 0;
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        return amount;
    }

    private Money newMoney(final long amount) {
        return new Money(amount / (centFactor() * 1.0), this.currency);
    }

    /**
     * Subtract the specified amount from this value.
     */
    public Money subtract(final Money money) {
        assertSameCurrency(money);
        return newMoney(amount - money.amount);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return other.getClass() == this.getClass() && equals((Money) other);
    }

    public boolean equals(final Money other) {
        return other.currency.equals(currency) && other.amount == amount;
    }

    @Override
    public int hashCode() {
        return (int) amount;
    }

    @Override
    public String toString() {
        return amount / (centFactor() * 1.0) + " " + currency;
    }
}
