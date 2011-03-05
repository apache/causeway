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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestPojo;
import org.apache.isis.viewer.html.context.Context;


public class ContextSessionDataTest extends ProxyJunit3TestCase {

    private ObjectAdapter originalAdapter;
    private Oid oid;
    private ObjectAdapter restoredAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        originalAdapter = system.createTransientTestObject(new TestPojo());
        oid = originalAdapter.getOid();

        final Context context = new Context(null);
        context.mapObject(originalAdapter);

        assertNotNull("loader still has the object", getAdapterManager().getAdapterFor(oid));
        system.resetLoader();
        assertNull("loader no longer has the object", getAdapterManager().getAdapterFor(oid));

        context.restoreAllObjectsToLoader();
        restoredAdapter = getAdapterManager().getAdapterFor(oid);
    }


    public void testExistsInLoader() {
        assertNotNull("loaders is missing the object", getAdapterManager().getAdapterFor(oid));
        assertNotSame("expect the loader to have a new adapter", originalAdapter, restoredAdapter);
    }

    public void testHasSameOid() {
        assertEquals(originalAdapter.getOid(), restoredAdapter.getOid());
    }

    public void testNotSameAdapter() {
        assertNotSame(originalAdapter, restoredAdapter);
    }

    public void testSamePojo() {
        assertEquals(originalAdapter.getObject().getClass(), restoredAdapter.getObject().getClass());
    }

    public void testHasSameVersion() {
        assertEquals(originalAdapter.getVersion(), restoredAdapter.getVersion());
    }

    public void testHasResolveStateOfTransient() {
        assertEquals(ResolveState.TRANSIENT, restoredAdapter.getResolveState());
    }

}

