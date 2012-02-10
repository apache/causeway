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

package org.apache.isis.runtimes.dflt.remoting.common.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtimes.dflt.remoting.common.client.transaction.ClientTransactionEvent;
import org.apache.isis.runtimes.dflt.remoting.common.data.DummyIdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.data.DummyObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ReferenceData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteClientActionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteClientActionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.KnownObjectsRequest;
import org.apache.isis.runtimes.dflt.remoting.common.facade.impl.ServerFacadeImpl;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtimes.dflt.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxySession;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxySystem;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyVersion;

@RunWith(JMock.class)
public class ServerFacadeImpl_ClientActionTest {

    private final Mockery mockery = new JUnit4Mockery();

    private AuthenticationManager mockAuthenticationManager;
    private ObjectEncoderDecoder mockEncoder;

    private ServerFacadeImpl server;
    private AuthenticationSession session;
    private TestProxySystem system;

    /*
     * Testing the Distribution implementation ServerDistribution. This uses the
     * encoder to unmarshall objects and then calls the persistor and reflector;
     * all of which should be mocked.
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

        session = IsisContext.getAuthenticationSession();
    }

    @After
    public void tearDown() throws Exception {
        system.shutdown();
    }

    @Test
    public void testExecuteClientActionWithNoWork() {

        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeClientActionResult(with(equalTo(new ObjectData[0])), with(equalTo(new Version[0])), with(equalTo(new ObjectData[0])));
                will(returnValue(new ExecuteClientActionResponse(new ObjectData[0], new Version[0], null)));
            }
        });

        final ExecuteClientActionRequest request = new ExecuteClientActionRequest(session, new ReferenceData[0], new int[0]);

        // don't start xactn here, since within call.
        final ExecuteClientActionResponse result = server.executeClientAction(request);

        assertEquals(0, result.getPersisted().length);
        assertEquals(0, result.getChanged().length);
    }

    @Test
    public void testExecuteClientActionWhereObjectChanged() {
        final ObjectAdapter adapter = system.createPersistentTestObject();

        final DummyObjectData data = new DummyObjectData(adapter.getOid(), "none", true, new TestProxyVersion(1));

        // prepare the update data to return
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).decode(data, new KnownObjectsRequest());
                will(returnValue(adapter));

            }
        });

        // results returned in their own container
        final ExecuteClientActionResponse results = new ExecuteClientActionResponse(new ObjectData[0], new Version[0], null);
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeClientActionResult(with(equalTo(new ReferenceData[1])), with(equalTo(new Version[] { new TestProxyVersion(2) })), with(equalTo(new ObjectData[0])));
                will(returnValue(results));
            }
        });

        final ExecuteClientActionRequest request = new ExecuteClientActionRequest(session, new ReferenceData[] { data }, new int[] { ClientTransactionEvent.CHANGE });
        // don't start xactn here, since within call.
        final ExecuteClientActionResponse result = server.executeClientAction(request);
        final ObjectAdapter object = IsisContext.getPersistenceSession().loadObject(adapter.getOid(), adapter.getSpecification());

        assertEquals(new TestProxyVersion(2), object.getVersion());

        assertEquals(results, result);
    }

    @Test
    public void testExecuteClientActionWhereObjectMadePersistent() {
        final ObjectAdapter adapter = system.createTransientTestObject();

        final DummyObjectData data = new DummyObjectData(adapter.getOid(), "none", true, new TestProxyVersion(1));

        // restore the object on the server
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).decode(data, new KnownObjectsRequest());
                will(returnValue(adapter));

                one(mockEncoder).encodeIdentityData(adapter);
                will(returnValue(null));
            }
        });

        // return results
        final ExecuteClientActionResponse results = new ExecuteClientActionResponse(new ObjectData[0], new Version[0], new ObjectData[0]);
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeClientActionResult(with(equalTo(new ReferenceData[1])), with(equalTo(new Version[1])), with(equalTo(new ObjectData[0])));
                will(returnValue(results));
            }
        });

        // don't start xactn here, since within call.

        final ExecuteClientActionRequest request = new ExecuteClientActionRequest(session, new ReferenceData[] { data }, new int[] { ClientTransactionEvent.ADD });
        final ExecuteClientActionResponse response = server.executeClientAction(request);

        final ObjectAdapter object = IsisContext.getPersistenceSession().loadObject(adapter.getOid(), adapter.getSpecification());

        assertEquals(results, response);
        assertEquals(adapter, object);
        assertEquals(new TestProxyVersion(1), object.getVersion());
    }

    @Test
    public void testExecuteClientActionFailsWithConcurrencyError() {
        final ObjectAdapter adapter = system.createPersistentTestObject();
        adapter.setOptimisticLock(new TestProxyVersion(7));

        final Oid oid = adapter.getOid();
        final DummyIdentityData identityData = new DummyIdentityData(oid, TestProxyAdapter.class.getName(), new TestProxyVersion(6));

        try {
            final ExecuteClientActionRequest request = new ExecuteClientActionRequest(new TestProxySession(), new ReferenceData[] { identityData }, new int[] { ClientTransactionEvent.DELETE });
            server.executeClientAction(request);
            fail();
        } catch (final ConcurrencyException expected) {
        }
    }

    @Test
    public void testExecuteClientActionWhereObjectDeleted() {
        final ObjectAdapter adapter = system.createPersistentTestObject();

        final Oid oid = adapter.getOid();
        final DummyIdentityData identityData = new DummyIdentityData(oid, TestProxyAdapter.class.getName(), new TestProxyVersion(1));

        // return results
        final ExecuteClientActionResponse results = new ExecuteClientActionResponse(new ObjectData[0], new Version[0], null);
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeClientActionResult(with(equalTo(new ReferenceData[1])), with(equalTo(new Version[1])), with(equalTo(new ObjectData[0])));
                will(returnValue(results));
            }
        });

        // don't start xactn here, since within call.
        final ExecuteClientActionRequest request = new ExecuteClientActionRequest(new TestProxySession(), new ReferenceData[] { identityData }, new int[] { ClientTransactionEvent.DELETE });
        final ExecuteClientActionResponse result = server.executeClientAction(request);

        assertEquals(results, result);
    }

}
