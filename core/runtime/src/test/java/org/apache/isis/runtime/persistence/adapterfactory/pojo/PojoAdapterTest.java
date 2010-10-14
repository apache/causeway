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


package org.apache.isis.runtime.persistence.adapterfactory.pojo;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtime.testsystem.TestPojo;
import org.apache.isis.runtime.testsystem.TestProxyOid;
import org.apache.isis.runtime.testsystem.TestProxyVersion;


public class PojoAdapterTest extends ProxyJunit3TestCase {

    private ObjectAdapter adapter;
    private TestPojo domainObject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainObject = new TestPojo();
        adapter = new PojoAdapter(domainObject, new TestProxyOid(1));
        adapter.setOptimisticLock(new TestProxyVersion());
    }

    public void testOid() {
        assertEquals(new TestProxyOid(1), adapter.getOid());
    }

    public void testObject() {
        assertEquals(domainObject, adapter.getObject());
    }

    public void testInitialResolvedState() {
        assertEquals(ResolveState.NEW, adapter.getResolveState());
    }

    public void testChangeResolvedState() {
        adapter.changeState(ResolveState.TRANSIENT);
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
    }

    public void testVersion() throws Exception {
        assertEquals(new TestProxyVersion(), adapter.getVersion());
    }

    public void testVersionConflict() throws Exception {
        try {
            adapter.checkLock(new TestProxyVersion(2));
            fail();
        } catch (final ConcurrencyException expected) {}
    }
}
