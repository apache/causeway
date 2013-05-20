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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Order;

public class MemberModifyTest extends AbstractTest {

    @Test
    public void valueModifiedToNonNull() {
        custJsWO.setFirstName("Dick");

        assertThat(custJsWO.getFirstName(), equalTo("Dick"));
    }

    @Test
    public void valueModifiedToNull() {
        custJsWO.setFirstName(null);

        assertThat(custJsWO.getFirstName(), nullValue());
    }

    @Test
    public void whenValueModifyCalledRatherThanSetForNonNull() {
        custJsWO.setFirstName("Dick");
        assertThat(custJsDO.modifyFirstNameCalled, is(true));
    }

    @Test
    public void whenValueClearCalledRatherThanSetForNull() {
        custJsWO.setFirstName(null);
        assertThat(custJsDO.clearFirstNameCalled, is(true));
    }

    @Test
    public void whenAssociationModifyCalledRatherThanSetForNonNull() {
        custJsWO.setCountryOfBirth(countryUsaDO);
        assertThat(custJsDO.modifyCountryOfBirthCalled, is(true));
    }

    @Test
    public void whenAssociationClearCalledRatherThanSetForNull() {
        custJsWO.setCountryOfBirth(null);
        assertThat(custJsDO.clearCountryOfBirthCalled, is(true));
    }

    @Test
    public void cannotUseAddDirectlyOnCollections() {
        final List<Country> visitedCountries = custJsWO.getVisitedCountries();
        try {
            visitedCountries.add(countryGbrDO);
            fail("UnsupportedOperationException should have been thrown.");
        } catch (final UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void cannotUseRemoveDirectlyOnCollections() {
        final List<Country> visitedCountries = custJsWO.getVisitedCountries();
        try {
            visitedCountries.remove(countryGbrDO);
            fail("UnsupportedOperationException should have been thrown.");
        } catch (final UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void cannotUseClearDirectlyOnCollections() {
        final List<Country> visitedCountries = custJsWO.getVisitedCountries();
        try {
            visitedCountries.clear();
            fail("UnsupportedOperationException should have been thrown.");
        } catch (final UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void sttemptingToAddNullObjectIntoCollectionThrowsException() {
        try {
            custJsWO.addToVisitedCountries(null);
            fail("Exception should have been raised.");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void removingNonExistentRemoveObjectFromCollectionDoesNothing() {
        assertThat(custJsDO.getVisitedCountries().contains(countryGbrDO), is(false));

        custJsWO.removeFromVisitedCountries(countryGbrDO);
        // no exception raised.
    }

    @Test
    public void canInvokeAction() {
        final int sizeBefore = custJsWO.getOrders().size();
        final Order orderBefore = custJsWO.getLastOrder();
        custJsWO.placeOrder(product355DO, 3);
        final Order orderAfter = custJsWO.getLastOrder();

        final int sizeAfter = custJsWO.getOrders().size();
        assertThat(sizeAfter, is(sizeBefore + 1));
        assertThat(orderAfter, is(not(orderBefore)));
    }

    @Test
    public void canInvokeActionIfOptionalValueParameterAndNullArgumentProvided() {
        custJsWO.actionWithOptionalValueParameter(null);
        assertThat(custJsDO.actionWithOptionalValueParameterArgument, nullValue());
    }

    @Test
    public void cannotInvokeActionIfMandatoryValueParameterAndNullArgumentProvided() {
        try {
            custJsWO.actionWithMandatoryValueParameter(null);
            fail("InvalidMandatoryException should have been thrown");
        } catch (final InvalidException ex) {
            assertThat(custJsDO.actionWithMandatoryValueParameterArgument, equalTo(Long.MAX_VALUE)); // ie
        }
    }

    @Test
    public void canInvokeActionIfOptionalReferenceParameterAndNullArgumentProvided() {
        custJsWO.actionWithOptionalReferenceParameter(null);
        assertThat(custJsDO.actionWithOptionalReferenceParameterArgument, nullValue());
    }

    @Test
    public void cannotInvokeActionIfMandatoryReferenceParameterAndNullArgumentProvided() {
        try {
            custJsWO.actionWithMandatoryReferenceParameter(null);
            fail("InvalidMandatoryException should have been thrown");
        } catch (final InvalidException ex) {
            assertThat(custJsDO.actionWithMandatoryReferenceParameterArgument, not(nullValue()));
        }
    }

    @Test
    public void canInvokeActionIfOptionalStringParameterAndEmptyStringProvidedAsArgument() {
        custJsWO.actionWithOptionalStringParameter("");
        assertThat(custJsDO.actionWithOptionalStringParameterArgument, equalTo(""));
    }

    @Test
    public void cannotInvokeActionIfMandatoryStringParameterAndEmptyStringProvidedAsArgument() {
        try {
            custJsWO.actionWithMandatoryStringParameter("");
            fail("InvalidMandatoryException should have been thrown");
        } catch (final InvalidException ex) {
            assertThat(custJsDO.actionWithMandatoryStringParameterArgument, equalTo("original value")); // ie
        }
    }

    @Test
    public void canInvokeActionIfParameterMatchRegularExpression() {
        custJsWO.actionWithRegExStringParameter("6789");
        assertThat(custJsDO.actionWithRegExStringParameterArgument, equalTo("6789"));
    }

    @Test
    public void cannotInvokeActionIfParameterDoesNotMatchRegularExpression() {
        try {
            custJsWO.actionWithRegExStringParameter("abcd"); // doesn't match
                                                             // [0-9]{4}
            fail("InvalidRegExException should have been thrown");
        } catch (final InvalidException ex) {
            assertThat(custJsDO.actionWithRegExStringParameterArgument, equalTo("1234")); // ie
                                                                                          // unchanged
        }
    }

    @Test
    public void canInvokeActionIfParameterNoLongerMaximumLength() {
        custJsWO.actionWithMaxLengthStringParameter("abcd");
        assertThat(custJsDO.actionWithMaxLengthStringParameterArgument, equalTo("abcd"));
    }

    @Test
    public void cannotInvokeActionIfParameterExceedsMaximumLength() {
        try {
            custJsWO.actionWithMaxLengthStringParameter("abcde");
            fail("InvalidMaxLengthException should have been thrown");
        } catch (final InvalidException ex) {
            assertThat(custJsDO.actionWithMaxLengthStringParameterArgument, equalTo("1234")); // ie
                                                                                              // unchanged
        }
    }

}
