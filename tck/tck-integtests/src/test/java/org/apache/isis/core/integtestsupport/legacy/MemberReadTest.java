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

package org.apache.isis.core.integtestsupport.legacy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;

public class MemberReadTest extends AbstractTest {

    @Test
    public void value() {
        assertThat(custJsWO.getFirstName(), equalTo("Richard"));
    }

    @Test
    public void valueWhenNull() {
        custJsDO.setFirstName(null);
        assertThat(custJsWO.getFirstName(), nullValue());
    }

    @Test
    public void association() {
        assertThat(custJsWO.getCountryOfBirth(), equalTo(countryGbrDO));
    }

    @Test
    public void associationWhenNull() {
        custJsDO.setCountryOfBirth(null);
        assertThat(custJsWO.getCountryOfBirth(), nullValue());
    }

    @Test
    public void collectionContainsWhenDoesAndDoesNot() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.addToVisitedCountries(countryUsaDO);
        final List<Country> visitedCountries = custJsWO.getVisitedCountries();
        assertThat(visitedCountries.contains(countryGbrDO), is(true));
        assertThat(visitedCountries.contains(countryUsaDO), is(true));
        assertThat(visitedCountries.contains(countryAusDO), is(false));
    }

    @Test
    public void collectionSizeWhenEmpty() {
        assertThat(custJsWO.getVisitedCountries().size(), is(0));
    }

    @Test
    public void collectionSizeWhenNotEmpty() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.addToVisitedCountries(countryUsaDO);

        assertThat(custJsWO.getVisitedCountries().size(), is(2));
    }

    @Test
    public void isEmptySizeWhenEmpty() {
        assertThat(custJsWO.getVisitedCountries().isEmpty(), is(true));
    }

    @Test
    public void isEmptySizeWhenNotEmpty() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.addToVisitedCountries(countryUsaDO);

        assertThat(custJsWO.getVisitedCountries().isEmpty(), is(false));
    }

}
