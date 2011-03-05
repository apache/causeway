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

import junit.framework.TestSuite;

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
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionImpl;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestSpecification;


@RunWith(JMock.class)
public class ObjectActionImplTest extends ProxyJunit3TestCase {
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ObjectActionImplTest.class));
    }

    private final Mockery mockery = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private ObjectActionImpl action;
    private FacetedMethod mockFacetedMethod;

    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    private SpecificationLookup mockSpecificationLookup;
    private AdapterMap mockAdapterManager;
    private ServicesProvider mockServicesProvider;
    private QuerySubmitter mockQuerySubmitter;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        mockFacetedMethod = mockery.mock(FacetedMethod.class);
        mockAuthenticationSessionProvider = mockery.mock(AuthenticationSessionProvider.class);
        mockSpecificationLookup = mockery.mock(SpecificationLookup.class);
        mockAdapterManager = mockery.mock(AdapterMap.class);
        mockServicesProvider = mockery.mock(ServicesProvider.class);
        mockQuerySubmitter = mockery.mock(QuerySubmitter.class);

        mockery.checking(new Expectations() {
            {
                one(mockFacetedMethod).getIdentifier();
                will(returnValue(Identifier.actionIdentifier("Customer", "reduceheadcount")));
            }
        });

        action = new ObjectActionImpl(mockFacetedMethod, new ObjectMemberContext(mockAuthenticationSessionProvider, mockSpecificationLookup, mockAdapterManager, mockQuerySubmitter), mockServicesProvider);
    }

    @Test
    public void testExecutePassedOnToPeer() {
        final TestProxyAdapter target = new TestProxyAdapter();
        target.setupSpecification(new TestSpecification());
        final ObjectAdapter[] parameters = new ObjectAdapter[2];

        final TestProxyAdapter result = new TestProxyAdapter();
        final ActionInvocationFacet facet = new ActionInvocationFacetAbstract(mockFacetedMethod) {
            @Override
            public ObjectAdapter invoke(ObjectAdapter target, ObjectAdapter[] parameters) {
                return result;
            }

            @Override
            public ObjectSpecification getReturnType() {
                return null;
            }

            @Override
            public ObjectSpecification getOnType() {
                return new TestSpecification();
            }
        };

        mockery.checking(new Expectations() {
            {
                exactly(2).of(mockFacetedMethod).getFacet(ActionInvocationFacet.class);
                will(returnValue(facet));
            }
        });

        final ObjectAdapter returnObject = action.execute(target, parameters);
        assertEquals(returnObject, result);
    }

    @Test
    public void testNameDefaultsToActionsMethodName() {
        final NamedFacet facet = new NamedFacetAbstract("Reduceheadcount", mockFacetedMethod) {};
        mockery.checking(new Expectations() {
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
