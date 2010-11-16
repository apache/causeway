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
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.defaults.objectstore.InMemoryObjectStore;


public class InMemoryObjectStore_interactionWithAdapterManager extends ProxyJunit3TestCase {
    private InMemoryObjectStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = new InMemoryObjectStore();
        system.getPersistenceSession().injectInto(store);
        store.open();
    }

    @Override
    protected void tearDown() throws Exception {
        store.close();
        super.tearDown();
    }

    /**
     * Testing, indirectly, that the adapter manager doesn't automatically save objects in the objectstore.
     */
    public void testObjectNotPersistedWhenCreated() throws Exception {
        final ObjectAdapter object = system.createPersistentTestObject();

        final ObjectSpecification specification = object.getSpecification();
        assertEquals(false, store.hasInstances(specification));
        assertEquals(0, store.getInstances(new PersistenceQueryFindAllInstances(specification)).length);
        try {
            store.getObject(object.getOid(), specification);
            fail();
        } catch (final ObjectNotFoundException expected) {}
    }


}

