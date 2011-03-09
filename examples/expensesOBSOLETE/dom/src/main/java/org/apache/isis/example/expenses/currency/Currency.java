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


package org.apache.isis.example.expenses.currency;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.When;


@Bounded
@Immutable(When.ONCE_PERSISTED)
public class Currency extends AbstractDomainObject {

    private String currencyCode;
    private String currencyName;
    private String currencyCountry;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCountry() {
        return currencyCountry;
    }

    public void setCurrencyCountry(final String currencyCountry) {
        this.currencyCountry = currencyCountry;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(final String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public String toString() {
        return currencyCode;
    }

}
