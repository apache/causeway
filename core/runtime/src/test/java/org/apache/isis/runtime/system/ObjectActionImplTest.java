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


package org.apache.isis.runtime.system;

import junit.framework.TestSuite;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.metamodel.facets.actions.invoke.ActionInvocationFacetAbstract;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacetAbstract;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.internal.ObjectActionImpl;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectActionPeer;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtime.testsystem.TestSpecification;


@RunWith(JMock.class)
public class ObjectActionImplTest extends ProxyJunit3TestCase {
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ObjectActionImplTest.class));
    }

    private final Mockery mockery = new JUnit4Mockery();

    private ObjectActionImpl action;
    private ObjectActionPeer mockObjectActionPeer;
    private RuntimeContext mockRuntimeContext;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        mockObjectActionPeer = mockery.mock(ObjectActionPeer.class);
        mockRuntimeContext = mockery.mock(RuntimeContext.class);

        action = new ObjectActionImpl("reduceheadcount", mockObjectActionPeer, mockRuntimeContext);
    }

    @Test
    public void testExecutePassedOnToPeer() {
        final TestProxyAdapter target = new TestProxyAdapter();
        target.setupSpecification(new TestSpecification());
        final ObjectAdapter[] parameters = new ObjectAdapter[2];

        final TestProxyAdapter result = new TestProxyAdapter();
        final ActionInvocationFacet facet = new ActionInvocationFacetAbstract(mockObjectActionPeer) {
            public ObjectAdapter invoke(ObjectAdapter target, ObjectAdapter[] parameters) {
                return result;
            }

            public ObjectSpecification getReturnType() {
                return null;
            }

            public ObjectSpecification getOnType() {
                return new TestSpecification();
            }
        };

        mockery.checking(new Expectations() {
            {
                exactly(2).of(mockObjectActionPeer).getFacet(ActionInvocationFacet.class);
                will(returnValue(facet));
            }
        });

        final ObjectAdapter returnObject = action.execute(target, parameters);
        assertEquals(returnObject, result);
    }

    @Test
    public void testNameDefaultsToActionsMethodName() {
        final NamedFacet facet = new NamedFacetAbstract("Reduceheadcount", mockObjectActionPeer) {};
        mockery.checking(new Expectations() {
            {
                one(mockObjectActionPeer).getFacet(NamedFacet.class);
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
