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

package org.apache.isis.runtimes.dflt.runtime.testsystem.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestPojo;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyException;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyOid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxySystem;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class CreateTransientObjectsTest {

    private TestProxySystem system;
    private ObjectAdapter adapter;
    private TestPojo pojo;
    private Oid oid;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        system = new TestProxySystem();
        system.init();

        pojo = new TestPojo();
        adapter = system.createTransientTestObject(pojo);
        oid = adapter.getOid();
    }

    @Test
    public void testSpecification() {
        assertNotNull(adapter.getSpecification());
        assertNotNull(TestPojo.class.getName(), adapter.getSpecification().getFullIdentifier());
    }

    @Test
    public void testStateOfCreatedAdapted() {
        assertNotNull(adapter);
        assertEquals(pojo, adapter.getObject());
    }

    @Test
    public void testResolveStateShowsTransient() throws Exception {
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
    }

    @Test
    public void testOid() throws Exception {
        assertEquals(new TestProxyOid(1, false), oid);
    }

    @Test
    public void test2ndPersistentCreationHasDifferentOid() {
        final TestPojo pojo = new TestPojo();
        final ObjectAdapter adapter2 = system.createTransientTestObject(pojo);

        assertNotNull(adapter2);
        assertEquals(new TestProxyOid(2, false), adapter2.getOid());
    }

    @Test
    public void testIsAddedToObjectLoader() {
        final ObjectAdapter a = getAdapterManager().getAdapterFor(oid);
        assertEquals(adapter, a);
    }

    @Test
    public void testAddedToPersistor() {
        system.resetLoader();
        try {
            getPersistenceSession().loadObject(oid, adapter.getSpecification());
            fail();
        } catch (final TestProxyException expected) {
        }
    }

    @Test
    public void testNotGivenVersion() throws Exception {
        assertNull(adapter.getVersion());
    }

    @Test
    public void testOidHasNoPrevious() throws Exception {
        assertNull(oid.getPrevious());
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}
