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


package org.apache.isis.runtimes.dflt.remoting.common.client.facets;

import static org.hamcrest.CoreMatchers.equalTo;

import org.apache.isis.runtimes.dflt.remoting.common.client.facets.ActionInvocationFacetWrapProxy;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxySystem;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ProxyAction_ExecuteLocallyTest {

    private Mockery mockery = new JUnit4Mockery();

    private ActionInvocationFacetWrapProxy proxy;
    private ObjectAction mockObjectAction;
    private ObjectEncoderDecoder mockEncoder;
    private ServerFacade mockDistribution;
    private ObjectAdapter target;
    private ObjectAdapter param1;
    
    private TestProxySystem system;
    private Identifier identifier;
    private ActionInvocationFacet mockActionInvocationFacet;

    private FacetHolder mockFacetHolder;

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF);

        system = new TestProxySystem();
        system.init();

        mockObjectAction = mockery.mock(ObjectAction.class);
        mockEncoder = mockery.mock(ObjectEncoderDecoder.class);
        mockDistribution = mockery.mock(ServerFacade.class);
        mockActionInvocationFacet = mockery.mock(ActionInvocationFacet.class);
        mockFacetHolder = mockery.mock(FacetHolder.class);
        
        identifier = Identifier.classIdentifier("");

        target = system.createTransientTestObject();
        mockery.checking(new Expectations() {
            {
                one(mockActionInvocationFacet).getFacetHolder();
                will(returnValue(mockFacetHolder));
                
                allowing(mockObjectAction).getIdentifier();
                will(returnValue((identifier)));

                allowing(mockObjectAction).execute(with(equalTo(target)), with(any(ObjectAdapter[].class)));
                will(returnValue(null));
            }
        });
        
        proxy = new ActionInvocationFacetWrapProxy(mockActionInvocationFacet, mockDistribution, mockEncoder, mockObjectAction);

    }

    // to prevent a warning
    @Test
    public void testDummy() {}

    @Ignore("was commented out, don't know details")
    @Test
    public void testOnTransientExecutionIsPassedToDelegate() throws Exception {
        // actionPeer.getTarget();
        // expectLastCall().andStubReturn(null);


        proxy.invoke(target, new ObjectAdapter[] { param1, param1 });

    }

    @Ignore("was commented out, don't know details")
    @Test
    public void testOnPersistentAnnotatedAsLocalIsPassedToDelegate() throws Exception {
        // actionPeer.getTarget();
        // expectLastCall().andStubReturn(ObjectAction.LOCAL);

        proxy.invoke(target, new ObjectAdapter[] { param1, param1 });
    }
}

