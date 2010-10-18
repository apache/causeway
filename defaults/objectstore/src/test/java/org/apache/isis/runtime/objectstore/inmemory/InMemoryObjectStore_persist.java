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


package org.apache.isis.runtime.objectstore.inmemory;

import java.util.Collections;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;


public class InMemoryObjectStore_persist extends AbstractInMemoryObjectStoreTest {


	private ObjectAdapter object;

	public void setUp() throws Exception {
		super.setUp();
		addObjectToStore();
	}

	private void addObjectToStore() {
		object = system.createPersistentTestObject();
		addObjectToStore(object);
		resetIdentityMap();
	}
	
    public void testSaveInstance() throws Exception {

        final ObjectSpecification specification = object.getSpecification();
        ObjectAdapter[] retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, "changed"));
        assertEquals(0, retrievedInstance.length);

        ((TestProxyAdapter) object).setupTitleString("changed title");
        final PersistenceCommand command = store.createSaveObjectCommand(object);
        assertEquals(object, command.onObject());
        store.execute(Collections.<PersistenceCommand>singletonList(command));

        resetIdentityMap();

        retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, "changed"));
        assertEquals(1, retrievedInstance.length);
        assertNotSame(object, retrievedInstance[0]);
    }

    public void testRemoveInstance() throws Exception {
        final PersistenceCommand command = store.createDestroyObjectCommand(object);
        assertEquals(object, command.onObject());
        store.execute(Collections.<PersistenceCommand>singletonList(command));

        resetIdentityMap();

        final ObjectSpecification specification = object.getSpecification();
        assertEquals(false, store.hasInstances(specification));
    }

}

