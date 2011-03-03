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


package org.apache.isis.defaults.objectstore;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.core.runtime.testsystem.TestPojo;
import org.apache.isis.core.runtime.testsystem.TestProxyOid;


public class InMemoryObjectStore_retrieve extends AbstractInMemoryObjectStoreTest {


	private ObjectAdapter originalAdapter;

	public void setUp() throws Exception {
		super.setUp();
		originalAdapter = system.createPersistentTestObject();
	}

	private void addObjectToStoreAndDiscardAdapters() {
		addObjectToStore(originalAdapter);
		resetIdentityMap();
	}
	
    public void testGetObjectByOidWhenEmpty() {
        final ObjectSpecification spec = system.getSpecification(TestPojo.class);
        final Oid oid = new TestProxyOid(10, true);
        try {
            store.getObject(oid, spec);
            fail();
        } catch (final ObjectNotFoundException expected) {}
    }
    
    public void testGetObjectReturnsANewAdapter() throws Exception {
		addObjectToStoreAndDiscardAdapters();

        final ObjectSpecification specification = originalAdapter.getSpecification();
        final ObjectAdapter retrievedObject = store.getObject(originalAdapter.getOid(), specification);
        assertNotSame(originalAdapter, retrievedObject);
        assertEquals(originalAdapter.getObject(), retrievedObject.getObject());
    }

    public void testGetInstancesWhenEmpty() throws Exception {
        final ObjectSpecification spec = system.getSpecification(TestPojo.class);
        final ObjectAdapter[] instances = store.getInstances(new PersistenceQueryFindByTitle(spec, "title"));
        assertEquals(0, instances.length);
    }

    public void testGetInstancesByTitle() throws Exception {
		addObjectToStoreAndDiscardAdapters();

		final ObjectSpecification specification = originalAdapter.getSpecification();
        final ObjectAdapter[] retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, "le STR"));
        assertEquals(1, retrievedInstance.length);
        assertNotSame(originalAdapter, retrievedInstance[0]);
        assertSame(originalAdapter.getObject(), retrievedInstance[0].getObject());
    }

    public void testGetInstancesReturnsANewAdapter() throws Exception {
		addObjectToStoreAndDiscardAdapters();

		final ObjectSpecification specification = originalAdapter.getSpecification();
        final ObjectAdapter[] retrievedAdapters = store.getInstances(new PersistenceQueryFindAllInstances(specification));
        assertEquals(1, retrievedAdapters.length);
        assertSame(originalAdapter.getObject(), retrievedAdapters[0].getObject());
        assertNotSame(originalAdapter, retrievedAdapters[0]);
    }


    public void testHasInstancesWhenEmpty() throws Exception {
        final ObjectSpecification spec = system.getSpecification(TestPojo.class);
        assertEquals(false, store.hasInstances(spec));
    }

    public void testHasInstances() throws Exception {
		addObjectToStoreAndDiscardAdapters();

        final ObjectSpecification specification = originalAdapter.getSpecification();
        assertEquals(true, store.hasInstances(specification));
    }


}

