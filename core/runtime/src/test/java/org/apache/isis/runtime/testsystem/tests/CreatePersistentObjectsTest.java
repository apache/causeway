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


package org.apache.isis.runtime.testsystem.tests;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtime.testsystem.TestPojo;
import org.apache.isis.runtime.testsystem.TestProxyOid;
import org.apache.isis.runtime.testsystem.TestProxyVersion;


public class CreatePersistentObjectsTest extends ProxyJunit3TestCase {

    private ObjectAdapter adapter;
    private TestPojo pojo;
    private TestProxyOid oid;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pojo = new TestPojo();
        adapter = system.createPersistentTestObject(pojo);
        oid = (TestProxyOid) adapter.getOid();
    }

    public void testStateOfCreatedAdapted() {
        assertNotNull(adapter);
        assertEquals(pojo, adapter.getObject());
    }

    public void testResolveState() throws Exception {
        assertEquals(ResolveState.RESOLVED, adapter.getResolveState());
    }

    public void testGivenVersion() throws Exception {
        assertEquals(new TestProxyVersion(1), adapter.getVersion());
    }

    public void testSpecification() {
        assertNotNull(adapter.getSpecification());
        assertNotNull(TestPojo.class.getName(), adapter.getSpecification().getFullName());
    }

    public void test2ndPersistentCreationHasDifferentOid() {
        final TestPojo pojo = new TestPojo();
        final ObjectAdapter adapter2 = system.createPersistentTestObject(pojo);

        assertNotNull(adapter2);
        assertEquals(new TestProxyOid(90001, true), adapter2.getOid());
    }

    public void testOidChanged() throws Exception {
        assertEquals(new TestProxyOid(90000, true), oid);
    }

    
    public void testOidHasPrevious() throws Exception {
    	assertNotNull(oid.getPrevious());
        assertEquals(new TestProxyOid(1, false), oid.getPrevious());
    }
    
    public void testPreviousOidIsRemovedFromMap() throws Exception {
        final ObjectAdapter a = getAdapterManager().getAdapterFor(oid.getPrevious());
        assertNull(a);
    }



    public void testIsAddedToMap() throws Exception {
        final ObjectAdapter a = getAdapterManager().getAdapterFor(oid);
        assertEquals(adapter, a);
    }

    public void testAddedToPersistor() throws Exception {
        final ObjectAdapter a = getPersistenceSession().loadObject(oid, adapter.getSpecification());
        assertEquals(adapter, a);
    }

    

}

