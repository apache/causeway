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


package org.apache.isis.extensions.headless.junit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.apache.isis.extensions.headless.applib.ViewObject;
import org.apache.isis.extensions.headless.junit.sample.domain.Country;
import org.apache.isis.extensions.headless.junit.sample.domain.Customer;


public class ViewObjectTest extends AbstractTest {

    @SuppressWarnings("unchecked")
    private ViewObject<Customer> asViewObject() {
        return (ViewObject<Customer>) custJsVO;
    }

	@Test
    public void canCastViewsToViewObject() {
        @SuppressWarnings("unused")
        final ViewObject<Customer> custRpVOAsViewObject = asViewObject();
    }

    @Test
    public void shouldBeAbleToCreateAView() {
        final Customer custRpVO = getHeadlessViewer().view(custJsDO);
        assertThat(custRpVO, instanceOf(Customer.class));
        custRpVO.setFirstName("Dick");

        assertThat("Dick", equalTo(custRpVO.getFirstName()));
    }

    @Test
    public void viewShouldPassesThroughSetterToUnderlyingDomainObject() {
        final Customer custRpVO = getHeadlessViewer().view(custJsDO);
        custRpVO.setFirstName("Dick");

        assertThat("Dick", equalTo(custRpVO.getFirstName()));
    }

    @Test
    public void objectIsViewShouldReturnTrueWhenDealingWithView() {
        final Customer custRpVO = getHeadlessViewer().view(custJsDO);
        assertThat(getHeadlessViewer().isView(custRpVO), is(true));
    }

    @Test
    public void objectIsViewShouldReturnFalseWhenDealingWithUnderlying() {
        assertThat(getHeadlessViewer().isView(custJsDO), is(false));
    }

    @Test
    public void collectionInstanceOfViewObjectShouldReturnTrueWhenDealingWithView() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.addToVisitedCountries(countryUsaDO);
        final List<Country> visitedCountries = custJsVO.getVisitedCountries();
        assertThat(visitedCountries instanceof ViewObject, is(true));
    }

    @Test
    public void containsOnViewedCollectionShouldIntercept() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.addToVisitedCountries(countryUsaDO);
        final List<Country> visitedCountries = custJsVO.getVisitedCountries();
        assertThat(visitedCountries.contains(countryGbrDO), is(true));
    }

}
