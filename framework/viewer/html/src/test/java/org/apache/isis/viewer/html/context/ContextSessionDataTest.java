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

package org.apache.isis.viewer.html.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class ContextSessionDataTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectAdapter originalAdapter;
    private Oid oid;
    private ObjectAdapter restoredAdapter;


    @Before
    public void setUp() throws Exception {
        
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(new TestPojo(), transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//
//        originalAdapter = adapterForTransient;
//        oid = originalAdapter.getOid();

//        final Context context = new Context(null);
//        context.mapObject(originalAdapter);
//
//        assertNotNull("loader still has the object", getAdapterManager().getAdapterFor(oid));
//        mockPersistenceSession.testReset();
//        assertNull("loader no longer has the object", getAdapterManager().getAdapterFor(oid));
//
//        context.restoreAllObjectsToLoader();
//        restoredAdapter = getAdapterManager().getAdapterFor(oid);
    }

    @Test
    public void testExistsInLoader() {
        assertNotNull("loaders is missing the object", getAdapterManager().getAdapterFor(oid));
        assertNotSame("expect the loader to have a new adapter", originalAdapter, restoredAdapter);
    }

    @Test
    public void testHasSameOid() {
        assertEquals(originalAdapter.getOid(), restoredAdapter.getOid());
    }

    @Test
    public void testNotSameAdapter() {
        assertNotSame(originalAdapter, restoredAdapter);
    }

    @Test
    public void testSamePojo() {
        assertEquals(originalAdapter.getObject().getClass(), restoredAdapter.getObject().getClass());
    }

    @Test
    public void testHasSameVersion() {
        assertEquals(originalAdapter.getVersion(), restoredAdapter.getVersion());
    }

    @Test
    public void testHasResolveStateOfTransient() {
        assertEquals(ResolveState.TRANSIENT, restoredAdapter.getResolveState());
    }

    
    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}

