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


package org.apache.isis.remoting.server;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.testspec.TestProxySpecification;
import org.apache.isis.remoting.data.DummyIdentityData;
import org.apache.isis.remoting.data.DummyObjectData;
import org.apache.isis.remoting.data.DummyReferenceData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.exchange.ExecuteServerActionRequest;
import org.apache.isis.remoting.exchange.ExecuteServerActionResponse;
import org.apache.isis.remoting.facade.impl.ServerFacadeImpl;
import org.apache.isis.remoting.protocol.encoding.internal.ObjectEncoderDecoder;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.testdomain.Movie;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtime.testsystem.TestProxySession;
import org.apache.isis.runtime.testsystem.TestProxySystem;
import org.apache.isis.runtime.testsystem.TestProxyVersion;



@RunWith(JMock.class)
public class ServerFacadeImpl_RemoteActionTest {

    private Mockery mockery = new JUnit4Mockery();

    private AuthenticationManager mockAuthenticationManager;
    private ObjectEncoderDecoder mockEncoder;

    private ServerFacadeImpl server;
    private TestProxySystem system;
    private ObjectAdapter adapter;
    private Oid oid;
    private DummyIdentityData targetData;
    private ReferenceData[] parameterData;
    private ObjectAction mockAction;

    /*
     * Testing the Distribution implementation ServerDistribution. This uses the encoder to unmarshall objects
     * and then calls the persistor and reflector; all of which should be mocked.
     */
    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF);

        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockEncoder = mockery.mock(ObjectEncoderDecoder.class);

        server = new ServerFacadeImpl(mockAuthenticationManager);
        server.setEncoder(mockEncoder);

        server.init();
        

        system = new TestProxySystem();
        system.init();

        adapter = system.createPersistentTestObject();
        oid = adapter.getOid();

        targetData = new DummyIdentityData(oid, TestProxyAdapter.class.getName(), new TestProxyVersion(1));
        parameterData = new ReferenceData[] {};
        final TestProxySpecification proxySpecification = (TestProxySpecification) adapter.getSpecification();
        
        mockAction = mockery.mock(ObjectAction.class);
        proxySpecification.setupAction(mockAction);
        
        mockery.checking(new Expectations() {
            {
                one(mockAction).getId();
                will(returnValue("action"));
                
                one(mockAction).execute(with(equalTo(adapter)), with(equalTo(new ObjectAdapter[0])));
                will(returnValue(adapter));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        system.shutdown();
    }

    @Test
    public void testExecuteOK() {
        final ExecuteServerActionResponse results = null;
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeServerActionResult(
                        with(equalTo(adapter)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new ReferenceData[0])), 
                        with(nullValue(ObjectData.class)), 
                        with(equalTo(new ObjectData[0])), 
                        with(equalTo(new String[0])),
                        with(equalTo(new String[0])));
                will(returnValue(results));
                
            }
        });

        IsisContext.getTransactionManager().startTransaction();
        ExecuteServerActionRequest request = new ExecuteServerActionRequest(new TestProxySession(), 
                ObjectActionType.USER, 
                "action()", 
                targetData, 
                parameterData);
		final ExecuteServerActionResponse result = 
            server.executeServerAction(
                    request);
        IsisContext.getTransactionManager().endTransaction();

        assertEquals(results, result);
    }

    @Test
    public void testExecuteWhereObjectDeleted() {
        final ExecuteServerActionResponse results = null;
        
        mockery.checking(new Expectations() {
            {
                final ReferenceData deletedObjectIdentityData = new DummyReferenceData(adapter.getOid(), "", adapter.getVersion());
                one(mockEncoder).encodeIdentityData(adapter);
                will(returnValue(deletedObjectIdentityData));
                
                one(mockEncoder).encodeServerActionResult(
                        with(equalTo(adapter)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new ReferenceData[] { deletedObjectIdentityData })), 
                        with(nullValue(ObjectData.class)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new String[0])), 
                        with(equalTo(new String[0])));
                will(returnValue(null));
            }
        });

        IsisContext.getTransactionManager().startTransaction();

        IsisContext.getUpdateNotifier().addDisposedObject(adapter);
        ExecuteServerActionRequest request = new ExecuteServerActionRequest(new TestProxySession(), 
                ObjectActionType.USER, 
                "action()", 
                targetData, 
                parameterData);
		final ExecuteServerActionResponse result = server.executeServerAction(
                request );

        IsisContext.getTransactionManager().endTransaction();

        assertEquals(results, result);
    }

    @Test
    public void testExecuteWhereObjectChanged() {
        
        final ExecuteServerActionResponse results = null;
        mockery.checking(new Expectations() {
            {
                final ObjectData changedObjectData = new DummyObjectData();
                one(mockEncoder).encodeForUpdate(adapter);
                will(returnValue(changedObjectData));
                
                one(mockEncoder).encodeServerActionResult(
                        with(equalTo(adapter)), 
                        with(equalTo(new ObjectData[] { changedObjectData })),
                        with(equalTo(new ReferenceData[0] )), 
                        with(nullValue(ObjectData.class)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new String[0])), 
                        with(equalTo(new String[0])));
                will(returnValue(null));
            }
        });

        IsisContext.getTransactionManager().startTransaction();

        IsisContext.getUpdateNotifier().addChangedObject(adapter);
        
        ExecuteServerActionRequest request = new ExecuteServerActionRequest(new TestProxySession(), 
                ObjectActionType.USER, 
                "action()", 
                targetData, 
                parameterData);
		final ExecuteServerActionResponse result = server.executeServerAction(
                request);

        IsisContext.getTransactionManager().endTransaction();

        assertEquals(results, result);
    }

    @Test
    public void testExecuteWhereMessagesAndWarningGenerated() {
        final ExecuteServerActionResponse results = null;
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeServerActionResult(
                        with(equalTo(adapter)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new ReferenceData[0] )), 
                        with(nullValue(ObjectData.class)), 
                        with(equalTo(new ObjectData[0])),
                        with(equalTo(new String[] { "message 1", "message 2" })), 
                        with(equalTo(new String[] { "warning 1", "warning 2" })));
                will(returnValue(null));
            }
        });

        IsisContext.getTransactionManager().startTransaction();
        
        IsisContext.getMessageBroker().addMessage("message 1");
        IsisContext.getMessageBroker().addMessage("message 2");

        IsisContext.getMessageBroker().addWarning("warning 1");
        IsisContext.getMessageBroker().addWarning("warning 2");


        ExecuteServerActionRequest request = new ExecuteServerActionRequest(new TestProxySession(), 
                ObjectActionType.USER, 
                "action()", 
                targetData, 
                parameterData);
		final ExecuteServerActionResponse result = server.executeServerAction(
                request);

        IsisContext.getTransactionManager().endTransaction();

        assertEquals(results, result);
    }

}
