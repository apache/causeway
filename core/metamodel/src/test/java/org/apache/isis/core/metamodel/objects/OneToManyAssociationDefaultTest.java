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

package org.apache.isis.core.metamodel.objects;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import org.apache.isis.applib.id.Identifier;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.id.TypeIdentifierTestFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.isis.core.security.authentication.AuthenticationContext;

public class OneToManyAssociationDefaultTest {

    private static final String COLLECTION_ID = "orders";

    public static class Order {
    }

    private static final Class<?> COLLECTION_TYPE = Order.class;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock ManagedObject mockOwnerAdapter;
    @Mock ManagedObject mockAssociatedAdapter;
    @Mock AuthenticationContext mockAuthenticationContext;
    @Mock SpecificationLoader mockSpecificationLoader;
    @Mock ObjectSpecification mockOwnerAdapterSpec;
    @Mock MessageService mockMessageService;
    @Mock FacetedMethod mockPeer;
    @Mock NamedFacet mockNamedFacet;

    private OneToManyAssociation association;
    private MetaModelContext_forTesting metaModelContext;

    @Before
    public void setUp() {

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .authenticationContext(mockAuthenticationContext)
                .singleton(mockMessageService)
                .build();

        allowingPeerToReturnCollectionType();
        allowingPeerToReturnIdentifier();
        allowingSpecLoaderToReturnSpecs();
        association = new OneToManyAssociationDefault(mockPeer);
    }

    private void allowingSpecLoaderToReturnSpecs() {
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLoader).loadSpecification(Order.class);

                allowing(mockPeer).getMetaModelContext();
                will(returnValue(metaModelContext));

            }
        });
    }

    @Test
    public void id() {
        assertThat(association.getId(), is(equalTo(COLLECTION_ID)));
    }

    @Test
    public void name() {
        expectPeerToReturnNamedFacet();
        assertThat(association.getName(), is(equalTo("My name")));
    }

    private void allowingPeerToReturnCollectionType() {
        context.checking(new Expectations() {
            {
                allowing(mockPeer).getType();
                will(returnValue(COLLECTION_TYPE));
            }
        });
    }

    private void allowingPeerToReturnIdentifier() {
        context.checking(new Expectations() {
            {
                oneOf(mockPeer).getIdentifier();
                will(returnValue(Identifier.propertyOrCollectionIdentifier(
                        TypeIdentifierTestFactory.newCustomer(), COLLECTION_ID)));
            }
        });
    }

    private void expectPeerToReturnNamedFacet() {
        context.checking(new Expectations() {
            {
                oneOf(mockPeer).getFacet(NamedFacet.class);
                will(returnValue(mockNamedFacet));

                oneOf(mockNamedFacet).value();
                will(returnValue("My name"));
            }
        });
    }

}
