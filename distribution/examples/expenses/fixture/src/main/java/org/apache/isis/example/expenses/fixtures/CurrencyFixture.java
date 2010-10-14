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


package org.apache.isis.example.expenses.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.expenses.currency.Currency;


public class CurrencyFixture extends AbstractFixture {


    public static Currency EUR;
    public static Currency USD;
    public static Currency GBP;

    @Override
    public void install() {
        EUR = createCurrency("EUR", "Euro Member Countries", "Euro");
        GBP = createCurrency("GBP", "United Kingdom", "Pounds");
        USD = createCurrency("USD", "United States of America", "Dollars");
    }

    private Currency createCurrency(final String code, final String country, final String name) {
        final Currency currency = newTransientInstance(Currency.class);
        currency.setCurrencyCode(code);
        currency.setCurrencyCountry(country);
        currency.setCurrencyName(name);
        persist(currency);
        return currency;
    }

}
