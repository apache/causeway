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

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;
import org.apache.isis.core.metamodel.facets.properties.validating.maxlenannot.MaxLengthFacetOnPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.validating.regexannot.RegExFacetOnPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethod;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateAddToFacetViaMethod;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateRemoveFromFacetViaMethod;
import org.apache.isis.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethod;

public class MemberInvalidTest extends AbstractTest {

    @Test
    public void whenValueInvalidImperativelyThenThrowsException() {
        final String[] values = new String[] { "Dick", "Harry" };
        for (final String value : values) {
            custJsDO.validateFirstNameExpectedArg = value;
            custJsDO.validateFirstName = "bad first name";
            try {
                custJsWO.setFirstName(value);
                fail("Should have thrown exception");
            } catch (final InvalidException ex) {
                assertThat(ex.getAdvisorClass(), classEqualTo(PropertyValidateFacetViaMethod.class));
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("First Name"));
                assertThat(ex.getMessage(), Matchers.containsString("bad first name"));
            }
        }
    }

    @Test
    public void whenValueNullThenDoesNotThrowException() {
        final String[] values = new String[] { null };
        for (final String value : values) {
            custJsDO.validateFirstNameExpectedArg = value;
            custJsDO.validateFirstName = "bad first name";
            // should not trigger exception
            custJsWO.setFirstName(value);
        }
    }

    
    @Ignore // different behaviour testing in Eclipse vs Maven (different exception type thrown); not sure why
    @Test
    public void whenValueInvalidImperativelyOnMandatoryThenThrowsException() {
        final String[] values = new String[] { null };
        for (final String value : values) {
            custJsDO.validateFirstNameMandatoryExpectedArg = value;
            custJsDO.validateFirstNameMandatory = "bad first name";
            try {
                custJsWO.setFirstNameMandatory(value);
                fail("Should have thrown exception");
            } catch (final InvalidException ex) {
                //assertThat(ex.getAdvisorClass(), classEqualTo(MandatoryFacetDefault.class)); // in Eclipse?
                assertThat(ex.getAdvisorClass(), classEqualTo(PropertyValidateFacetViaMethod.class)); // in Maven?
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("First Name Mandatory"));
                assertThat(ex.getMessage(), Matchers.containsString("bad first name"));
            }
        }
    }


    
    @Test
    public void whenAssociationInvalidImperativelyThenThrowsException() {
        custJsDO.validateCountryOfBirth = "bad country of birth";
        final Country[] values = new Country[] { countryUsaDO };
        for (final Country value : values) {
            try {
                custJsWO.setCountryOfBirth(value);
                fail("Should have thrown exception");
            } catch (final InvalidException ex) {
                assertThat(ex.getAdvisorClass(), classEqualTo(PropertyValidateFacetViaMethod.class));
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Country Of Birth"));
                assertThat(ex.getMessage(), Matchers.containsString("bad country of birth"));
            }
        }
    }

    @Test
    public void whenAssociationNullOnOptionalThenDoesNotThrowException() {
        final Country[] values = new Country[] { null };
        for (final Country value : values) {
            custJsDO.validateCountryOfBirth = "bad country of birth";
            // should not throw exception
            custJsWO.setCountryOfBirth(value);
        }
    }

    
    @Ignore // different behaviour testing in Eclipse vs Maven (different exception type thrown); not sure why
    @Test
    public void whenAssociationNullOnMandatoryImperativelyThenThrowsException() {
        custJsDO.validateCountryOfBirthMandatory = "bad country of birth";
        final Country[] values = new Country[] { null };
        for (final Country value : values) {
            try {
                custJsWO.setCountryOfBirthMandatory(value);
                fail("Should have thrown exception");
            } catch (final InvalidException ex) {
                //assertThat(ex.getAdvisorClass(), classEqualTo(MandatoryFacetDefault.class)); // in Eclipse?
                assertThat(ex.getAdvisorClass(), classEqualTo(PropertyValidateFacetViaMethod.class)); // in Maven?
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Country Of Birth Mandatory"));
                assertThat(ex.getMessage(), Matchers.containsString("bad country of birth"));
            }
        }
    }


    
    @Test
    public void whenCollectionInvalidImperativelyThenAddToThrowsException() {
        custJsDO.validateAddToVisitedCountries = "bad country";
        try {
            custJsWO.addToVisitedCountries(countryGbrDO);
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(CollectionValidateAddToFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Visited Countries"));
            assertThat(ex.getMessage(), Matchers.containsString("bad country"));
        }
    }

    @Test
    public void whenCollectionInvalidImperativelyThenRemoveFromThrowsException() {
        custJsDO.addToVisitedCountries(countryGbrDO);
        custJsDO.validateRemoveFromVisitedCountries = "bad country";
        try {
            custJsWO.removeFromVisitedCountries(countryGbrDO);
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(CollectionValidateRemoveFromFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Visited Countries"));
            assertThat(ex.getMessage(), Matchers.containsString("bad country"));
        }
    }

    @Test
    public void whenActionInvalidImperativelyThenThrowsException() {
        custJsDO.validatePlaceOrder = "can't place order";
        try {
            custJsWO.placeOrder(product355DO, 3);
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(ActionValidationFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Place Order"));
            assertThat(ex.getMessage(), Matchers.containsString("can't place order"));
        }
    }

    @Test
    public void whenValueCanSetNullOnOptionalField() {
        custJsWO.setOptionalValue(null);
    }

    @Test
    public void whenAssociationCanSetNullOnOptionalField() {
        custJsWO.setOptionalAssociation(null);
    }

    @Test
    public void whenValueInvalidMandatoryThenThrowsException() {
        try {
            custJsWO.setMandatoryValue(null);
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(MandatoryFacetDefault.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Mandatory Value"));
        }
    }

    @Test
    public void whenAssociationInvalidMandatoryThenThrowsException() {
        try {
            custJsWO.setMandatoryAssociation(null);
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(MandatoryFacetDefault.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Mandatory Association"));
        }
    }

    @Test
    public void whenInvalidMaxLengthThenThrowsException() {
        try {
            custJsWO.setMaxLengthField("This is far too long");
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(MaxLengthFacetOnPropertyAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Max Length Field"));
        }
    }

    @Test
    public void whenInvalidRegExCaseSensitiveThenThrowsException() {
        try {
            custJsWO.setRegExCaseSensitiveField("abCfoobar");
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Reg Ex Case Sensitive Field"));
        }
    }

    @Test
    public void whenCanSetValidRegExCaseSensitive() {
        custJsWO.setRegExCaseInsensitiveField("abcfoobar");

    }

    @Test
    public void whenInvalidRegExCaseInsensitiveThenThrowsException() {
        try {
            custJsWO.setRegExCaseInsensitiveField("abXfoobar");
            fail("Should have thrown exception");
        } catch (final InvalidException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(RegExFacetOnPropertyAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Reg Ex Case Insensitive Field"));
        }
    }

    @Test
    public void whenCanSetValidRegExCaseInsensitive() {
        custJsWO.setRegExCaseInsensitiveField("AbCfoobar");
    }

}
