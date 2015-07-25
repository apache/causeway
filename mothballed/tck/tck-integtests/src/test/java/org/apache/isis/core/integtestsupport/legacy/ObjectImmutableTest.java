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

import org.junit.Test;

import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.core.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;

public class ObjectImmutableTest extends AbstractTest {

    @Test
    public void settingValueOnImmutableObjectThrowsException() {
        try {
            product355WO.setDescription("Changed");
            fail("Should have thrown exception");
        } catch (final DisabledException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(DisabledFacetOnPropertyDerivedFromImmutable.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Description"));
        }
    }

    @Test
    public void settingAssociationOnImmutableObjectThrowsException() {
        try {
            product355WO.setPlaceOfManufacture(countryUsaDO);
            fail("Should have thrown exception");
        } catch (final DisabledException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(DisabledFacetOnPropertyDerivedFromImmutable.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Place Of Manufacture"));
        }
    }

    @Test
    public void addingToCollectionOnImmutableObjectThrowsException() {
        try {
            product355WO.addToSimilarProducts(product850DO);
            fail("Should have thrown exception");
        } catch (final DisabledException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(DisabledFacetOnCollectionDerivedFromImmutable.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Similar Products"));
        }
    }

    @Test
    public void removingFromCollectionOnImmutableObjectThrowsException() {
        product355DO.addToSimilarProducts(product850DO); // TODO: can't setup,
                                                         // throws
        // ObjectPersistenceException
        try {
            product355WO.removeFromSimilarProducts(product850DO);
            fail("Should have thrown exception");
        } catch (final DisabledException ex) {
            assertThat(ex.getAdvisorClass(), classEqualTo(DisabledFacetOnCollectionDerivedFromImmutable.class));
            assertThat(ex.getIdentifier().getMemberNaturalName(), equalTo("Similar Products"));
        }
    }

    @Test
    public void canInvokingOnImmutableObject() {
        product355WO.foobar();
    }

}
