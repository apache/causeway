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

package org.apache.isis.core.runtime.system;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OneToManyAssociationDefaultTest {

    private static final String COLLECTION_ID = "orders";

    public static class Customer {
    }

    public static class Order {
    }

    private static final Class<?> COLLECTION_TYPE = Order.class;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ObjectAdapter mockOwnerAdapter;
    @Mock
    private ObjectAdapter mockAssociatedAdapter;
    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private MessageService mockMessageService;
    @Mock
    private PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;
    @Mock
    private FacetedMethod mockPeer;
    @Mock
    private NamedFacet mockNamedFacet;

    @Mock
    private CollectionAddToFacet mockCollectionAddToFacet;

    private ServicesInjector stubServicesInjector;

    private OneToManyAssociation association;

    @Before
    public void setUp() {
        stubServicesInjector = ServicesInjector.builderForTesting()
                .addServices(_Lists.of(
                mockAuthenticationSessionProvider,
                mockSpecificationLoader,
                mockMessageService,
                mockPersistenceSessionServiceInternal))
                .build();

        allowingPeerToReturnCollectionType();
        allowingPeerToReturnIdentifier();
        allowingSpecLoaderToReturnSpecs();
        association = new OneToManyAssociationDefault(mockPeer, stubServicesInjector);
    }

    private void allowingSpecLoaderToReturnSpecs() {
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLoader).loadSpecification(Order.class);
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

    @Test
    public void delegatesToUnderlying() {
        final ObjectSpecification spec = association.getSpecification();
    }

    @Test
    public void canAddPersistable() {
        context.checking(new Expectations() {
            {
            	oneOf(mockPeer).containsFacet(NotPersistedFacet.class);
                will(returnValue(false));

                oneOf(mockOwnerAdapter).representsPersistent();
                will(returnValue(true));

                oneOf(mockAssociatedAdapter).isTransient();
                will(returnValue(false));

                oneOf(mockPeer).getFacet(CollectionAddToFacet.class);
                will(returnValue(mockCollectionAddToFacet));

                oneOf(mockCollectionAddToFacet).add(mockOwnerAdapter, mockAssociatedAdapter, InteractionInitiatedBy.USER);
            }
        });
        association.addElement(mockOwnerAdapter, mockAssociatedAdapter, InteractionInitiatedBy.USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotRemoveNull() {
        association.removeElement(mockOwnerAdapter, null, InteractionInitiatedBy.USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddNull() {
        association.addElement(mockOwnerAdapter, null, InteractionInitiatedBy.USER);
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
                will(returnValue(Identifier.propertyOrCollectionIdentifier(Customer.class, COLLECTION_ID)));
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
