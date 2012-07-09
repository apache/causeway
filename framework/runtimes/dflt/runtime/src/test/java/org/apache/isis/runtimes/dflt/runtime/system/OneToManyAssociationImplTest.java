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

package org.apache.isis.runtimes.dflt.runtime.system;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationImpl;

@RunWith(JMock.class)
public class OneToManyAssociationImplTest {

    private static final String COLLECTION_ID = "orders";

    public static class Customer {
    }

    public static class Order {
    }

    private static final Class<?> COLLECTION_TYPE = Order.class;

    private final Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ObjectAdapter mockOwnerAdapter;
    private ObjectAdapter mockAssociatedAdapter;
    private OneToManyAssociation association;

    private FacetedMethod mockPeer;

    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    private SpecificationLookup mockSpecificationLookup;
    private AdapterMap mockAdapterManager;
    private QuerySubmitter mockQuerySubmitter;

    private NamedFacet mockNamedFacet;
    private CollectionAddToFacet mockCollectionAddToFacet;
    private CollectionTypeRegistry mockCollectionTypeRegistry;

    @Before
    public void setUp() {

        mockOwnerAdapter = context.mock(ObjectAdapter.class, "owner");
        mockAssociatedAdapter = context.mock(ObjectAdapter.class, "associated");

        mockAuthenticationSessionProvider = context.mock(AuthenticationSessionProvider.class);
        mockSpecificationLookup = context.mock(SpecificationLookup.class);
        mockAdapterManager = context.mock(AdapterMap.class);
        mockQuerySubmitter = context.mock(QuerySubmitter.class);
        mockCollectionTypeRegistry = context.mock(CollectionTypeRegistry.class);
        mockPeer = context.mock(FacetedMethod.class);

        mockNamedFacet = context.mock(NamedFacet.class);
        mockCollectionAddToFacet = context.mock(CollectionAddToFacet.class);

        allowingPeerToReturnCollectionType();
        allowingPeerToReturnIdentifier();
        allowingSpecLoaderToReturnSpecs();
        association = new OneToManyAssociationImpl(mockPeer, new ObjectMemberContext(mockAuthenticationSessionProvider, mockSpecificationLookup, mockAdapterManager, mockQuerySubmitter, mockCollectionTypeRegistry));
    }

    private void allowingSpecLoaderToReturnSpecs() {
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLookup).loadSpecification(Order.class);
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
                one(mockPeer).containsFacet(NotPersistedFacet.class);
                will(returnValue(false));

                one(mockOwnerAdapter).isPersistent();
                will(returnValue(true));

                one(mockAssociatedAdapter).representsTransient();
                will(returnValue(false));

                one(mockPeer).getFacet(CollectionAddToFacet.class);
                will(returnValue(mockCollectionAddToFacet));

                one(mockCollectionAddToFacet).add(mockOwnerAdapter, mockAssociatedAdapter);
            }
        });
        association.addElement(mockOwnerAdapter, mockAssociatedAdapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotRemoveNull() {
        association.removeElement(mockOwnerAdapter, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddNull() {
        association.addElement(mockOwnerAdapter, null);
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
                one(mockPeer).getIdentifier();
                will(returnValue(Identifier.propertyOrCollectionIdentifier(Customer.class, COLLECTION_ID)));
            }
        });
    }

    private void expectPeerToReturnNamedFacet() {
        context.checking(new Expectations() {
            {
                one(mockPeer).getFacet(NamedFacet.class);
                will(returnValue(mockNamedFacet));

                one(mockNamedFacet).value();
                will(returnValue("My name"));
            }
        });
    }

}
