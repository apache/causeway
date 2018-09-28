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

package org.apache.isis.core.metamodel.specloader.specimpl;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectAssociationAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);


    private ObjectAssociationAbstract objectAssociation;
    private FacetedMethod facetedMethod;


    @Mock
    private ObjectSpecification objectSpecification;
    @Mock
    private ServicesInjector mockServicesInjector;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;


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
            allowing(mockServicesInjector).getSpecificationLoader();
            will(returnValue(mockSpecificationLoader));
            allowing(mockServicesInjector).getPersistenceSessionServiceInternal();
            will(returnValue(mockPersistenceSessionServiceInternal));
        }});

        objectAssociation = new ObjectAssociationAbstract(
                facetedMethod, FeatureType.PROPERTY, objectSpecification, mockServicesInjector) {

            @Override
            public ObjectAdapter get(
                    final ObjectAdapter fromObject,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }
            
            @Override
            public boolean isEmpty(final ObjectAdapter adapter, final InteractionInitiatedBy interactionInitiatedBy) {
                return false;
            }

            @Override
            public ObjectAdapter[] getChoices(
                    final ObjectAdapter object,
                    final InteractionInitiatedBy interactionInitiatedBy) {
                return null;
            }

            @Override
            public ObjectAdapter getDefault(final ObjectAdapter adapter) {
                return null;
            }

            @Override
            public void toDefault(final ObjectAdapter target) {
            }

            @Override
            public UsabilityContext<?> createUsableInteractionContext(
                    final ObjectAdapter target, final InteractionInitiatedBy interactionInitiatedBy,
                    Where where) {
                return null;
            }

            @Override
            public VisibilityContext<?> createVisibleInteractionContext(
                    final ObjectAdapter targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
                    Where where) {
                return null;
            }

            @Override
            public boolean containsDoOpFacet(final Class<? extends Facet> facetType) {
                return false;
            }

            @Override
            public boolean hasAutoComplete() {
                return false;
            }

            @Override
            public ObjectAdapter[] getAutoComplete(
                    ObjectAdapter object,
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
