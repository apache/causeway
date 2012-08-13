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

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionImpl;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class ObjectActionImplTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    
    private ObjectActionImpl action;
    
    @Mock
    private FacetedMethod mockFacetedMethod;

    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private SpecificationLoader mockSpecificationLookup;
    @Mock
    private AdapterManager mockAdapterManager;
    @Mock
    private ServicesProvider mockServicesProvider;
    @Mock
    private QuerySubmitter mockQuerySubmitter;
    @Mock
    private CollectionTypeRegistry mockCollectionTypeRegistry;

//    protected TestProxySystem system;
//
//    private TestProxyConfiguration mockConfiguration;
//    private TestProxyReflector mockReflector;
//    private AuthenticationSession mockAuthSession;
//    private TestProxyPersistenceSessionFactory mockPersistenceSessionFactory;
//    private TestProxyPersistenceSession mockPersistenceSession;
//    private TestUserProfileStore mockUserProfileStore;

    @Before
    public void setUp() throws Exception {
//        Logger.getRootLogger().setLevel(Level.OFF);
//        
//        system = new TestProxySystem();
//        
//        mockConfiguration = new TestProxyConfiguration();
//        mockReflector = new TestProxyReflector();
//        mockAuthSession = new TestProxySession();
//        mockPersistenceSessionFactory = new TestProxyPersistenceSessionFactory();
//        mockPersistenceSession = new TestProxyPersistenceSession(mockPersistenceSessionFactory);
//        mockPersistenceSessionFactory.setPersistenceSessionToCreate(mockPersistenceSession);
//        mockUserProfileStore = new TestUserProfileStore();
//        
//        system.openSession(mockConfiguration, mockReflector, mockAuthSession, null, null, null, mockUserProfileStore, null, mockPersistenceSessionFactory, null);

        context.checking(new Expectations() {
            {
                one(mockFacetedMethod).getIdentifier();
                will(returnValue(Identifier.actionIdentifier("Customer", "reduceheadcount")));
            }
        });

        action = new ObjectActionImpl(mockFacetedMethod, new ObjectMemberContext(mockAuthenticationSessionProvider, mockSpecificationLookup, mockAdapterManager, mockQuerySubmitter, mockCollectionTypeRegistry), mockServicesProvider);
    }

    @Ignore // DKH
    @Test
    public void testExecutePassedOnToPeer() {
//        final TestProxyAdapter target = new TestProxyAdapter();
//        target.setupSpecification(new TestSpecification());
//        final ObjectAdapter[] parameters = new ObjectAdapter[2];
//
//        final TestProxyAdapter result = new TestProxyAdapter();
//        final ActionInvocationFacet facet = new ActionInvocationFacetAbstract(mockFacetedMethod) {
//            @Override
//            public ObjectAdapter invoke(final ObjectAdapter target, final ObjectAdapter[] parameters) {
//                return result;
//            }
//
//            @Override
//            public ObjectSpecification getReturnType() {
//                return null;
//            }
//
//            @Override
//            public ObjectSpecification getOnType() {
//                return new TestSpecification();
//            }
//        };
//
//        context.checking(new Expectations() {
//            {
//                exactly(2).of(mockFacetedMethod).getFacet(ActionInvocationFacet.class);
//                will(returnValue(facet));
//            }
//        });
//
//        final ObjectAdapter returnObject = action.execute(target, parameters);
//        assertEquals(returnObject, result);
    }

    @Test
    public void testNameDefaultsToActionsMethodName() {
        final NamedFacet facet = new NamedFacetAbstract("Reduceheadcount", mockFacetedMethod) {
        };
        context.checking(new Expectations() {
            {
                one(mockFacetedMethod).getFacet(NamedFacet.class);
                will(returnValue(facet));
            }
        });
        assertEquals("Reduceheadcount", action.getName());
    }

    @Test
    public void testId() {
        assertEquals("reduceheadcount", action.getId());
    }

}
