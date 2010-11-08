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

package org.apache.isis.runtime.persistence.objectstore;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtime.persistence.objectstore.algorithm.dummy.DummyPersistAlgorithm;
import org.apache.isis.runtime.persistence.objectstore.transaction.ObjectStoreTransactionManager;
import org.apache.isis.runtime.testsystem.TestObjectFactory;
import org.apache.isis.runtime.testsystem.TestProxyOidGenerator;
import org.apache.isis.runtime.testsystem.TestProxySystem;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class PersistenceSessionObjectStoreTest {

    private final Mockery mockery = new JUnit4Mockery();

    private PersistenceSessionFactory mockPersistenceSessionFactory;
    private PersistenceSessionObjectStore persistenceSession;
    private ObjectStoreTransactionManager transactionManager;
    private ObjectStoreSpy objectStore;
    private ObjectAdapter testObjectAdapter;
    private TestProxySystem system;

    private ServicesInjectorDefault servicesInjector;

    private AdapterManagerExtended adapterManager;

    private AdapterFactory adapterFactory;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);

        system = new TestProxySystem();

        objectStore = new ObjectStoreSpy();

        RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        DomainObjectContainerDefault container = new DomainObjectContainerDefault();

        runtimeContext.injectInto(container);
        runtimeContext.setContainer(container);

        servicesInjector = new ServicesInjectorDefault();
        servicesInjector.setContainer(container);

        // implicitly created by the system, so reuse
        adapterManager = (AdapterManagerExtended) system.getAdapterManager();
        adapterFactory = system.getAdapterFactory();

        persistenceSession =
            new PersistenceSessionObjectStore(mockPersistenceSessionFactory, adapterFactory, new TestObjectFactory(),
                servicesInjector, new TestProxyOidGenerator(), adapterManager, new DummyPersistAlgorithm(), objectStore);
        transactionManager = new ObjectStoreTransactionManager(persistenceSession, objectStore);
        transactionManager.injectInto(persistenceSession);

        servicesInjector.setServices(Collections.emptyList());
        persistenceSession.setSpecificationLoader(system.getReflector());

        system.setPersistenceSession(persistenceSession);
        system.init();

        testObjectAdapter = system.createPersistentTestObject();
        // objectSpecification = new TestSpecification();
        // testObjectAdapter.setupSpecification(objectSpecification);

    }

    @After
    public void tearDown() throws Exception {
        system.shutdown();
    }

    @Test
    public void testAbort() {
        // testObjectAdapter.changeState(ResolveState.GHOST);
        // testObjectAdapter.changeState(ResolveState.RESOLVED);
        objectStore.reset();

        transactionManager.startTransaction();
        persistenceSession.destroyObject(testObjectAdapter);
        transactionManager.abortTransaction();

        objectStore.assertAction(0, "startTransaction");
        objectStore.assertAction(1, "destroyObject " + testObjectAdapter);
        objectStore.assertAction(2, "abortTransaction");
        objectStore.assertLastAction(2);
    }

    @Test
    public void testDestroy() {
        // testObjectAdapter.changeState(ResolveState.GHOST);
        // testObjectAdapter.changeState(ResolveState.RESOLVED);
        objectStore.reset();

        final String action = "destroyObject " + testObjectAdapter;
        transactionManager.startTransaction();
        persistenceSession.destroyObject(testObjectAdapter);
        transactionManager.endTransaction();

        objectStore.assertAction(0, "startTransaction");
        objectStore.assertAction(1, action);

        // Nov2008 refactoring has inverted the order.
        // objectStore.assertAction(2, "endTransaction");
        // objectStore.assertAction(3, command);

        objectStore.assertAction(2, "execute DestroyObjectCommand " + testObjectAdapter);
        objectStore.assertAction(3, "endTransaction");

        assertEquals(4, objectStore.getActions().size());
    }

    public void testMakePersistent() {
        testObjectAdapter = system.createTransientTestObject();

        objectStore.reset();

        transactionManager.startTransaction();
        persistenceSession.makePersistent(testObjectAdapter);
        transactionManager.endTransaction();

        objectStore.assertAction(0, "startTransaction");
        objectStore.assertAction(1, "createObject " + testObjectAdapter);
        objectStore.assertAction(2, "endTransaction");
        objectStore.assertAction(3, "run CreateObjectCommand " + testObjectAdapter);

        assertEquals(4, objectStore.getActions().size());
    }
}
