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

package org.apache.isis.runtimes.dflt.objectstores.dflt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojo;

public class InMemoryObjectStoreTest_persist extends InMemoryObjectStoreTestAbstract {


    @Test
    public void getInstances_usingFindByTitle() throws Exception {

        final String titleString = adapter2.titleString();
        
        // given not yet persisted
        
        // when locate
        ObjectAdapter[] retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, titleString));
        
        // then none
        assertEquals(0, retrievedInstance.length);

        
        // given now persisted
        final PersistenceCommand command = store.createCreateObjectCommand(adapter2);
        assertEquals(adapter2, command.onObject());
        store.execute(Collections.<PersistenceCommand> singletonList(command));

        system.resetMaps();
        
        // when locate
        retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, titleString));
        
        // then find
        assertEquals(1, retrievedInstance.length);
        final ObjectAdapter retrievedAdapter = retrievedInstance[0];

        assertNotSame(adapter2, retrievedAdapter);
        assertEquals(((TestPojo)adapter2.getObject()).getPropertyUsedForTitle(), ((TestPojo)retrievedAdapter.getObject()).getPropertyUsedForTitle());
        assertEquals(adapter2.getOid(), retrievedAdapter.getOid());


        // and then don't find by other title
        retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, "some other title"));
        assertEquals(0, retrievedInstance.length);
    }


    @Test
    public void saveInstance() throws Exception {

        // given persisted
        persistToObjectStore(adapter2);
        system.resetMaps();
        
        // when save
        testPojo2.setPropertyUsedForTitle("changed");
        
        final PersistenceCommand command = store.createSaveObjectCommand(adapter2);
        assertEquals(adapter2, command.onObject());
        store.execute(Collections.<PersistenceCommand> singletonList(command));

        system.resetMaps();

        // then found
        ObjectAdapter[] retrievedInstance = store.getInstances(new PersistenceQueryFindByTitle(specification, "changed"));
        assertEquals(1, retrievedInstance.length);
        
        final ObjectAdapter retrievedAdapter = retrievedInstance[0];
        assertNotSame(adapter2, retrievedAdapter);
        assertEquals(((TestPojo)adapter2.getObject()).getPropertyUsedForTitle(), ((TestPojo)retrievedAdapter.getObject()).getPropertyUsedForTitle());
        assertEquals(adapter2.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void removeInstance() throws Exception {

        // given persisted
        persistToObjectStore(adapter2);
        system.resetMaps();

        // when destroy
        final PersistenceCommand command = store.createDestroyObjectCommand(adapter2);
        assertEquals(adapter2, command.onObject());
        store.execute(Collections.<PersistenceCommand> singletonList(command));
        system.resetMaps();

        // then not found
        assertEquals(false, store.hasInstances(specification));
    }
}
