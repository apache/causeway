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

import org.junit.Test;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;
import org.apache.isis.core.metamodel.facets.members.hidden.annotprop.HiddenFacetOnMemberAnnotation;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethod;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MemberHiddenTest extends AbstractTest {

    @Test
    public void whenValueHiddenImperativelyForValueThenModifyThrowsException() {
        custJsDO.hideFirstName = true;
        try {
            custJsWO.setFirstName("Dick");
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("First Name"));
        }
    }

    @Test
    public void whenValueHiddenImperativelyForNullThenModifyThrowsException() {
        custJsDO.hideFirstName = true;
        try {
            custJsWO.setFirstName("Dick");
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("First Name"));
        }
    }

    @Test
    public void whenValueHiddenImperativelyThenReadThrowsException() {
        custJsDO.hideFirstName = true;
        try {
            custJsWO.getFirstName();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("First Name"));
        }
    }

    @Test
    public void whenAssociationHiddenImperativelyForValueThenModifyThrowsException() {
        custJsDO.hideCountryOfBirth = true;
        try {
            custJsWO.setCountryOfBirth(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Country Of Birth"));
        }
    }

    @Test
    public void whenAssociationHiddenImperativelyForNullThenModifyThrowsException() {
        custJsDO.hideCountryOfBirth = true;
        try {
            custJsWO.setCountryOfBirth(null);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Country Of Birth"));
        }
    }

    @Test
    public void whenAssociationHiddenImperativelyThenReadThrowsException() {
        custJsDO.hideCountryOfBirth = true;
        try {
            custJsWO.getCountryOfBirth();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Country Of Birth"));
        }
    }

    @Test
    public void whenIfCollectionHiddenImperativelyThenAddToThrowsException() {
        custJsDO.hideVisitedCountries = true;
        try {
            custJsWO.addToVisitedCountries(countryGbrDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Visited Countries"));
        }
    }

    @Test
    public void whenCollectionHiddenImperativelyThenRemoveFromThrowsException() {
        custJsDO.hideVisitedCountries = true;
        custJsDO.addToVisitedCountries(countryGbrDO);
        try {
            custJsWO.removeFromVisitedCountries(countryGbrDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Visited Countries"));
        }
    }

    @Test
    public void whenCollectionHiddenImperativelyThenReadThrowsException() {
        custJsDO.hideVisitedCountries = true;
        custJsDO.addToVisitedCountries(countryGbrDO);
        try {
            custJsWO.getVisitedCountries();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Visited Countries"));
        }
    }

    @Test
    public void whenActionHiddenImperativelyThenThrowsException() {
        custJsDO.hidePlaceOrder = true;
        try {
            custJsWO.placeOrder(product355DO, 3);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForContextFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Place Order"));
        }
    }

    @Test
    public void whenValueHiddenDeclarativelyForValueThenModifyThrowsException() {
        try {
            custJsWO.setAlwaysHiddenValue("Dick");
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Value"));
        }
    }

    @Test
    public void whenValueHiddenDeclarativelyForNullThenModifyThrowsException() {
        try {
            custJsWO.setAlwaysHiddenValue(null);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Value"));
        }
    }

    @Test
    public void whenValueHiddenDeclarativelyThenReadThrowsException() {
        try {
            custJsWO.getAlwaysHiddenValue();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Value"));
        }
    }

    @Test
    public void whenAssociationHiddenDeclarativelyThenModifyThrowsException() {
        final Country[] values = new Country[] { countryUsaDO, null };
        for (final Country value : values) {
            try {
                custJsWO.setAlwaysHiddenAssociation(value);
                fail("Should have thrown exception");
            } catch (final HiddenException ex) {
                assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Association"));
            }
        }
    }

    @Test
    public void whenAssociationHiddenDeclarativelyThenReadThrowsException() {
        try {
            custJsWO.getAlwaysHiddenAssociation();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Association"));
        }
    }

    @Test
    public void whenCollectionHiddenDeclarativelyThenAddToThrowsException() {
        try {
            custJsWO.addToAlwaysHiddenCollection(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Collection"));
        }
    }

    @Test
    public void whenCollectionHiddenDeclarativelyThenRemoveFromThrowsException() {
        custJsDO.removeFromAlwaysHiddenCollection(countryUsaDO);
        try {
            custJsWO.removeFromAlwaysHiddenCollection(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Collection"));
        }
    }

    @Test
    public void whenCollectionHiddenDeclarativelyThenReadThrowsException() {
        try {
            custJsWO.getAlwaysHiddenCollection();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Collection"));
        }
    }

    @Test
    public void whenActionHiddenDeclarativelyThenThrowsException() {
        try {
            custJsWO.alwaysHiddenAction();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HiddenFacetOnMemberAnnotation.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Always Hidden Action"));
        }
    }

    @Test
    public void whenValueHiddenNotAuthorizedThenModifyThrowsException() {
        final String[] values = new String[] { "Dick", null };
        for (final String value : values) {
            try {
                custJsWO.setSessionHiddenValue(value);
                fail("Should have thrown exception");
            } catch (final HiddenException ex) {
                assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Value"));
            }
        }
    }

    @Test
    public void whenValueHiddenNotAuthorizedThenReadThrowsException() {
        try {
            custJsWO.getSessionHiddenValue();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Value"));
        }
    }

    @Test
    public void whenAssociationHiddenNotAuthorizedThenModifyThrowsException() {
        final Country[] values = new Country[] { countryUsaDO, null };
        for (final Country value : values) {
            try {
                custJsWO.setSessionHiddenAssociation(value);
                fail("Should have thrown exception");
            } catch (final HiddenException ex) {
                assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
                assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Association"));
            }
        }
    }

    @Test
    public void whenAssociationHiddenNotAuthorizedThenReadThrowsException() {
        try {
            custJsWO.getSessionHiddenAssociation();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Association"));
        }
    }

    @Test
    public void whenCollectionHiddenNotAuthorizedThenAddToThrowsException() {
        try {
            custJsWO.addToSessionHiddenCollection(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Collection"));
        }
    }

    @Test
    public void whenCollectionHiddenNotAuthorizedThenRemoveFromThrowsException() {
        custJsDO.addToSessionHiddenCollection(countryUsaDO);
        try {
            custJsWO.removeFromSessionHiddenCollection(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Collection"));
        }
    }

    @Test
    public void whenCollectionHiddenNotAuthorizedThenReadThrowsException() {
        try {
            custJsWO.getSessionHiddenCollection();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Collection"));
        }
    }

    @Test
    public void whenActionHiddenNotAuthorizedThenThrowsException() {
        try {
            custJsWO.sessionHiddenAction();
            fail("Should have thrown exception");
        } catch (final HiddenException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(HideForSessionFacetViaMethod.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Session Hidden Action"));
        }
    }

}
