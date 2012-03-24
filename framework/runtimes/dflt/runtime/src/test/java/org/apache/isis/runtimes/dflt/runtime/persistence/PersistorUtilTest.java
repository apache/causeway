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

package org.apache.isis.runtimes.dflt.runtime.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtimes.dflt.runtime.memento.RuntimeTestPojo;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerPersist;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerTestSupport;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class PersistorUtilTest {

//    protected TestProxySystem system;
//
//    private TestProxyConfiguration mockConfiguration;
//    private TestProxyReflector mockReflector;
//    private AuthenticationSession mockAuthSession;
//    private TestProxyPersistenceSessionFactory mockPersistenceSessionFactory;
//    private TestProxyPersistenceSession mockPersistenceSession;
//    private TestUserProfileStore mockUserProfileStore;

//    @Override
//    protected void setUp() throws Exception {
//        Logger.getRootLogger().setLevel(Level.OFF);
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
//    }

    @Ignore // DKH
    @Test
    public void testRecreateTransientAdapter() {
        final RootOid oid = RootOidDefault.createTransient("CUS", ""+13);
        final Object object = new RuntimeTestPojo();
        final ObjectAdapter adapter = getAdapterManagerTestSupport().testCreateTransient(object, oid);
        assertEquals(oid, adapter.getOid());
        assertEquals(object, adapter.getObject());
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
        assertEquals(null, adapter.getVersion());
    }

    @Ignore // DKH
    @Test
    public void testRecreatePersistentAdapter() {
        final Oid oid = RootOidDefault.create("CUS", ""+15);
        final Object object = new RuntimeTestPojo();
        final ObjectAdapter adapter = getAdapterManagerPersist().recreateAdapter(oid, object);
        assertEquals(oid, adapter.getOid());
        assertEquals(object, adapter.getObject());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());

        assertEquals(null, adapter.getVersion());
    }


    private AdapterManagerTestSupport getAdapterManagerTestSupport() {
        return (AdapterManagerTestSupport) IsisContext.getPersistenceSession().getAdapterManager();
    }

    private AdapterManagerPersist getAdapterManagerPersist() {
        return (AdapterManagerPersist) IsisContext.getPersistenceSession().getAdapterManager();
    }

}
