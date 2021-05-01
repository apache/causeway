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
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OneToOneAssociationAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectSpecification objectSpecification;
    @Mock private ServiceInjector mockServicesInjector;
    @Mock private SpecificationLoader mockSpecificationLoader;

    private OneToOneAssociation objectAssociation;
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

        objectAssociation = new OneToOneAssociationDefault(facetedMethod, objectSpecification) {

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
            public Can<ManagedObject> getChoices(
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
            public UsabilityContext createUsableInteractionContext(
                    final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy,
                    Where where) {
                return null;
            }

            @Override
            public VisibilityContext createVisibleInteractionContext(
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
            public Can<ManagedObject> getAutoComplete(
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
        final SnapshotExcludeFacet mockFacet = mockFacetIgnoring(SnapshotExcludeFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void notPersistedWhenFlaggedAsNotPersisted() throws Exception {
        final SnapshotExcludeFacet mockFacet = mockFacetIgnoring(SnapshotExcludeFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void persisted() throws Exception {
        assertFalse(objectAssociation.isNotPersisted());
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
