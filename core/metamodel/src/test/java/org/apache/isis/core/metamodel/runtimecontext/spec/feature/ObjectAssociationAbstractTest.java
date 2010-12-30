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


package org.apache.isis.core.metamodel.runtimecontext.spec.feature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetedmethod.FacetedMethod;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectAssociationAbstract;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;


@RunWith(JMock.class)
public class ObjectAssociationAbstractTest {

    private ObjectAssociationAbstract objectAssociation;
    private FacetedMethod facetedMethod;
    
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    public static class Customer {
        private String firstName;
        public String getFirstName() {
            return firstName;
        }
    }
    @Before
    public void setup() {
        facetedMethod = FacetedMethod.createProperty(Customer.class, "firstName");
        objectAssociation = new ObjectAssociationAbstract(facetedMethod, FeatureType.PROPERTY,
            new TestProxySpecification("test"), new ObjectMemberContext(null, null, null, null)) {

            @Override
            public ObjectAdapter get(ObjectAdapter fromObject) {
                return null;
            }

            @Override
            public boolean isEmpty(ObjectAdapter adapter) {
                return false;
            }

            @Override
            public ObjectAdapter[] getChoices(ObjectAdapter object) {
                return null;
            }

            @Override
            public ObjectAdapter getDefault(ObjectAdapter adapter) {
                return null;
            }

            @Override
            public void toDefault(ObjectAdapter target) {}

            @Override
            public UsabilityContext<?> createUsableInteractionContext(
                    AuthenticationSession session,
                    InteractionInvocationMethod invocationMethod,
                    ObjectAdapter target) {
                return null;
            }

            @Override
            public VisibilityContext<?> createVisibleInteractionContext(
                    AuthenticationSession session,
                    InteractionInvocationMethod invocationMethod,
                    ObjectAdapter targetObjectAdapter) {
                return null;
            }

            @Override
            public String debugData() {
                return null;
            }

            @Override
            public Instance getInstance(ObjectAdapter adapter) {
                return null;
            }
        };
    }

    @Test
    public void notPersistedWhenDerived() throws Exception {
    	// TODO: ISIS-5, need to reinstate DerivedFacet
        final NotPersistedFacet mockFacet = mockFacetIgnoring(NotPersistedFacet.class);
		facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isNotPersisted());
    }

    @Test
    public void notPersistedWhenFlaggedAsNotPersisted() throws Exception {
    	NotPersistedFacet mockFacet = mockFacetIgnoring(NotPersistedFacet.class);
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
    public void hidden() throws Exception {
    	HiddenFacet mockFacet = mockFacetIgnoring(HiddenFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isAlwaysHidden());
    }

    @Test
    public void optional() throws Exception {
        assertFalse(objectAssociation.isMandatory());
    }

    @Test
    public void mandatory() throws Exception {
    	MandatoryFacet mockFacet = mockFacetIgnoring(MandatoryFacet.class);
        facetedMethod.addFacet(mockFacet);
        assertTrue(objectAssociation.isMandatory());
    }

    @Test
    public void hasNoChoices() throws Exception {
        assertFalse(objectAssociation.hasChoices());
    }

    @Test
    public void hasChoices() throws Exception {
    	PropertyChoicesFacet mockFacet = mockFacetIgnoring(PropertyChoicesFacet.class);
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

