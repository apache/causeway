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


package org.apache.isis.alternatives.remoting.common.client.facets;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.alternatives.remoting.common.data.Data;
import org.apache.isis.alternatives.remoting.common.data.DummyNullValue;
import org.apache.isis.alternatives.remoting.common.data.DummyReferenceData;
import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.data.common.ReferenceData;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteServerActionRequest;
import org.apache.isis.alternatives.remoting.common.exchange.ExecuteServerActionResponse;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetedmethod.FacetedMethod;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.testsystem.TestProxySystem;

public class ProxyAction_ExecuteRemotelyTest {

    //private Mockery mockery = new JUnit4Mockery();

    private ActionInvocationFacetWrapProxy proxy;
    private FacetedMethod mockObjectActionPeer;
    private ObjectEncoderDecoder mockEncoder;
    private ServerFacade mockDistribution;
    private ObjectAdapter target;
    //private ObjectAdapter param1;
    private TestProxySystem system;
    //private Identifier identifier;
    //private ActionInvocationFacet mockActionInvocationFacet;
    private ReferenceData targetData;

    private Data[] parameterData;
    private ObjectAdapter[] parameters;
    private String identifierString;

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF);

//        system = new TestProxySystem();
//        system.init();
//
//        mockObjectActionPeer = mockery.mock(ObjectActionPeer.class);
//        mockEncoder = mockery.mock(ObjectEncoder.class);
//        mockDistribution = mockery.mock(Distribution.class);
//        mockActionInvocationFacet = mockery.mock(ActionInvocationFacet.class);
//
//        identifier = Identifier.propertyOrCollectionIdentifier("A", "b");
//
//        mockObjectActionPeer.getIdentifier();
//        expectLastCall().andStubReturn(identifier);
//        identifierString = identifier.getClassName() + "#" + identifier.getMemberName();
//        target = system.createTransientTestObject();
//        parameters = new ObjectAdapter[] { param1, param1 };
//        final ObjectSpecification[] parameterTypes = new ObjectSpecification[] {
//                system.getSpecification(TestPojo.class), system.getSpecification(TestPojo.class) };
//
//        // actionPeer.getParameterTypes();
//        // expectLastCall().andStubReturn(parameterTypes);
//
//        // actionPeer.getType();
//        // expectLastCall().andReturn(ObjectAction.USER);
//
//        final KnownObjects encodersKnownObjects = new KnownObjects();
//
//        targetData = new DummyReferenceData();
//        parameterData = new Data[] { null, null };
//        mockEncoder.createParameters(parameterTypes, parameters, encodersKnownObjects);
//        expectLastCall().andReturn(parameterData);
//
//        mockEncoder.createActionTarget(target, encodersKnownObjects);
//        expectLastCall().andReturn(targetData);
//
//        mockEncoder.madePersistent(null, null);
//        expectLastCall().times(2);


    }

    // to prevent a warning
    @Test
    public void testDummy() {}

    @Ignore("was commented out, don't know details")
    @Test
    public void testOnTransientObjectWithRemoteAnnotation() throws Exception {
        // actionPeer.getTarget();
        // expectLastCall().andStubReturn(ObjectAction.REMOTE);

        ExecuteServerActionRequest request =
        	new ExecuteServerActionRequest(
        		IsisContext.getAuthenticationSession(),
        		ActionType.USER,
                identifierString, targetData, parameterData);
		mockDistribution.executeServerAction(request );
        final ExecuteServerActionResponse result = new ExecuteServerActionResponse(new DummyNullValue("type"), new ObjectData[0],
                new ReferenceData[0], null, new ObjectData[2], new String[0], new String[0]);
        expectLastCall().andReturn(result);

        mockEncoder.madePersistent(target, null);
        expectLastCall();

        replay(mockObjectActionPeer, mockEncoder, mockDistribution);
        proxy.invoke(target, parameters);
        verify(mockObjectActionPeer, mockEncoder, mockDistribution);
    }

    @Ignore("was commented out, don't know details")
    @Test
    public void testOnPersistent() throws Exception {
        // actionPeer.getTarget();
        // expectLastCall().andStubReturn(null);

        IsisContext.getPersistenceSession().makePersistent(target);

        ExecuteServerActionRequest request = new ExecuteServerActionRequest(
        		IsisContext.getAuthenticationSession(),
        		ActionType.USER,
                identifierString, targetData, parameterData);
		mockDistribution.executeServerAction(request);
        final ExecuteServerActionResponse result = new ExecuteServerActionResponse(new DummyNullValue("type"), new ObjectData[0],
                new ReferenceData[0], null, new ObjectData[2], new String[0], new String[0]);
        expectLastCall().andReturn(result);

        replay(mockObjectActionPeer, mockEncoder, mockDistribution);
        proxy.invoke(target, parameters);
        verify(mockObjectActionPeer, mockEncoder, mockDistribution);
    }

    @Ignore("was commented out, don't know details")
    @Test
    public void testObjectsDestroyed() throws Exception {
        // actionPeer.getTarget();
        // expectLastCall().andStubReturn(null);

        IsisContext.getPersistenceSession().makePersistent(target);

        final ObjectAdapter object = system.createPersistentTestObject();

        ExecuteServerActionRequest request = new ExecuteServerActionRequest(
        		IsisContext.getAuthenticationSession(),
        		ActionType.USER,
                identifierString, targetData, parameterData);
		mockDistribution.executeServerAction(request);
        final ReferenceData[] disposedReferenceData = new ReferenceData[] { new DummyReferenceData(object.getOid(), object
                .getSpecification().getFullIdentifier(), null) };
        final ExecuteServerActionResponse result = new ExecuteServerActionResponse(new DummyNullValue("type"), new ObjectData[0],
                disposedReferenceData, null, new ObjectData[2], new String[0], new String[0]);
        expectLastCall().andReturn(result);

        replay(mockObjectActionPeer, mockEncoder, mockDistribution);
        proxy.invoke(target, parameters);
        verify(mockObjectActionPeer, mockEncoder, mockDistribution);

        final List<ObjectAdapter> allDisposedObjects = IsisContext.getUpdateNotifier().getDisposedObjects();
        assertEquals(false, allDisposedObjects.isEmpty());
        assertEquals(object, allDisposedObjects.get(0));
        assertEquals(true, allDisposedObjects.isEmpty());
    }
}

