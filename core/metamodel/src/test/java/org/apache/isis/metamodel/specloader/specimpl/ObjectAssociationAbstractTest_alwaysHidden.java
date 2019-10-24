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
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ObjectAssociationAbstractTest_alwaysHidden {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectAssociationAbstract objectAssociation;
    private FacetedMethod facetedMethod;

    @Mock
    private ObjectSpecification mockObjectSpecification;
    @Mock
    private ObjectSpecification mockOnType;

    @Mock
    private ServiceInjector mockServicesInjector;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private ObjectAdapterService mockPersistenceSessionServiceInternal;

    public static class Customer {
        public String getFirstName() {
            return null;
        }
    }

    @Before
    public void setup() {
        facetedMethod = FacetedMethod.createForProperty(Customer.class, "firstName");

        context.checking(new Expectations() {{
            //            allowing(mockServicesInjector).getSpecificationLoader();
            //            will(returnValue(mockSpecificationLoader));
            //
            //            allowing(mockServicesInjector).getPersistenceSessionServiceInternal();
            //            will(returnValue(mockPersistenceSessionServiceInternal));
        }});

        objectAssociation = new ObjectAssociationAbstract(
                facetedMethod, FeatureType.PROPERTY, mockObjectSpecification) {

            @Override
            public ObjectAdapter get(
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
                    final Where where) {
                return null;
            }

            @Override
            public VisibilityContext<?> createVisibleInteractionContext(
                    final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
                    final Where where) {
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
            public ManagedObject[] getAutoComplete(
                    final ManagedObject object,
                    final String searchArg,
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
    public void whenNone() throws Exception {

        // given (none)

        // when, then
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void whenNoop() throws Exception {

        // given
        addHiddenFacet(Where.EVERYWHERE, facetedMethod, true);

        // when, then
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void whenNotAlwaysEverywhere() throws Exception {

        // given
        addHiddenFacet(Where.EVERYWHERE, facetedMethod, false);

        // when, then
        assertThat(objectAssociation.isAlwaysHidden(), is(true));
    }

    @Test
    public void whenAlwaysNotEverywhere() throws Exception {

        // given
        addHiddenFacet(Where.OBJECT_FORMS, facetedMethod, false);

        // when, then
        assertFalse(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void whenAlwaysEverywhere() throws Exception {

        // given
        addHiddenFacet(Where.EVERYWHERE, facetedMethod, false);

        // when, then
        assertTrue(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void whenAlwaysAnywhere() throws Exception {

        // given
        addHiddenFacet(Where.ANYWHERE, facetedMethod, false);

        // when, then
        assertTrue(objectAssociation.isAlwaysHidden());
    }

    private static void addHiddenFacet(
            final Where where,
            final FacetedMethod holder,
            final boolean noop) {
        HiddenFacet facet = new HiddenFacetAbstract(HiddenFacet.class, where, holder) {
            @Override
            protected String hiddenReason(final ManagedObject target, final Where whereContext) {
                return null;
            }

            @Override
            public boolean isNoop() {
                return noop;
            }
        };
        FacetUtil.addFacet(facet);
    }
}
