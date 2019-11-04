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

package org.apache.isis.metamodel.specloader.specimpl;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectAssociationAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectSpecification objectSpecification;
    @Mock private ServiceInjector mockServicesInjector;
    @Mock private SpecificationLoader mockSpecificationLoader;

    private ObjectAssociationAbstract objectAssociation;
    private FacetedMethod facetedMethod;


    public static class Customer {
        private String firstName;

        public String getFirstName() {
            return firstName;
        }
    }

    @Before
    public void setup() {
        facetedMethod = FacetedMethod.createForProperty(Customer.class, "firstName");

        context.checking(new Expectations() {{
            //            allowing(mockServicesInjector).getSpecificationLoader();
            //            will(returnValue(mockSpecificationLoader));
            //            allowing(mockServicesInjector).getPersistenceSessionServiceInternal();
            //            will(returnValue(mockPersistenceSessionServiceInternal));
        }});

        objectAssociation = new ObjectAssociationAbstract(
                facetedMethod, FeatureType.PROPERTY, objectSpecification) {

            @Override
            public ManagedObject get(
                    final ManagedObject fromObject,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }

            @Override
            public boolean isEmpty(final ManagedObject adapter, final InteractionInitiatedBy interactionInitiatedBy) {
                return false;
            }

            @Override
            public ManagedObject[] getChoices(
                    final ManagedObject object,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }

            @Override
            public ManagedObject getDefault(final ManagedObject adapter) {
                return null;
            }

            @Override
            public void toDefault(final ManagedObject target) {
            }

            @Override
            public UsabilityContext<?> createUsableInteractionContext(
                    final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy,
                    Where where) {
                return null;
            }

            @Override
            public VisibilityContext<?> createVisibleInteractionContext(
                    final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
                    Where where) {
                return null;
            }

            @Override
            public boolean containsNonFallbackFacet(final Class<? extends Facet> facetType) {
                return false;
            }

            @Override
            public boolean hasAutoComplete() {
                return false;
            }

            @Override
            public ManagedObject[] getAutoComplete(
                    ManagedObject object,
                    String searchArg,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }
            @Override
            public int getAutoCompleteMinLength() {
                return 0;
            }


        };
    }

    @Test
    public void notPersistedWhenDerived() throws Exception {
        final NotPersistedFacet mockFacet = mockFacetIgnoring(NotPersistedFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void notPersistedWhenFlaggedAsNotPersisted() throws Exception {
        final NotPersistedFacet mockFacet = mockFacetIgnoring(NotPersistedFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void persisted() throws Exception {
        assertFalse(objectAssociation.isNotPersisted());
    }

    @Test
    public void notHidden() throws Exception {
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void optional() throws Exception {
        assertFalse(objectAssociation.isMandatory());
    }

    @Test
    public void mandatory() throws Exception {
        final MandatoryFacet mockFacet = mockFacetIgnoring(MandatoryFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isMandatory());
    }

    @Test
    public void hasNoChoices() throws Exception {
        assertFalse(objectAssociation.hasChoices());
    }

    @Test
    public void hasChoices() throws Exception {
        final PropertyChoicesFacet mockFacet = mockFacetIgnoring(PropertyChoicesFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.hasChoices());
    }

    private <T extends Facet> T mockFacetIgnoring(final Class<T> typeToMock) {
        final T facet = context.mock(typeToMock);
        context.checking(new Expectations() {
            {
                allowing(facet).facetType();
                will(returnValue(typeToMock));
                ignoring(facet);
            }
        });
        return facet;
    }
}
