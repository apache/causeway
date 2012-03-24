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
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojo;

public class InMemoryObjectStoreTest_retrieve extends InMemoryObjectStoreTestAbstract {

    @Test
    public void getObject_whenDoesNotExist() {
        final ObjectSpecification spec = system.loadSpecification(TestPojo.class);
        final Oid oid = RootOidDefault.create("CUS|10");
        try {
            store.getObject(oid, spec);
            fail();
        } catch (final ObjectNotFoundException expected) {
        }
    }

    @Test
    public void getObject_whenExists_returnsAdapter() throws Exception {
        
        // given
        persistToObjectStore(adapter2);
        system.resetMaps();

        final ObjectSpecification specification = adapter2.getSpecification();
        
        final ObjectAdapter retrievedAdapter = store.getObject(adapter2.getOid(), specification);
        
        assertNotSame(adapter2, retrievedAdapter);
        assertEquals(((TestPojo)adapter2.getObject()).getPropertyUsedForTitle(), ((TestPojo)retrievedAdapter.getObject()).getPropertyUsedForTitle());
        assertEquals(adapter2.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void getInstances_whenDoesNotExist() throws Exception {
        final ObjectAdapter[] retrievedAdapters = store.getInstances(new PersistenceQueryFindByTitle(specification, adapter2.titleString()));
        assertEquals(0, retrievedAdapters.length);
    }

    @Test
    public void getInstances_findByTitle() throws Exception {
        // given
        persistToObjectStore(adapter2);
        system.resetMaps();

        // when
        final ObjectAdapter[] retrievedAdapters = store.getInstances(new PersistenceQueryFindByTitle(specification, adapter2.titleString()));
        
        // then
        assertEquals(1, retrievedAdapters.length);
        final ObjectAdapter retrievedAdapter = retrievedAdapters[0];

        assertNotSame(adapter2, retrievedAdapter);
        assertEquals(((TestPojo)adapter2.getObject()).getPropertyUsedForTitle(), ((TestPojo)retrievedAdapter.getObject()).getPropertyUsedForTitle());
        assertEquals(adapter2.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void getInstances_findAll() throws Exception {
        // given
        persistToObjectStore(adapter2);
        system.resetMaps();

        // when
        final ObjectAdapter[] retrievedAdapters = store.getInstances(new PersistenceQueryFindAllInstances(specification));
        
        // then
        assertEquals(1, retrievedAdapters.length);
        final ObjectAdapter retrievedAdapter = retrievedAdapters[0];

        assertNotSame(adapter2, retrievedAdapter);
        assertEquals(((TestPojo)adapter2.getObject()).getPropertyUsedForTitle(), ((TestPojo)retrievedAdapter.getObject()).getPropertyUsedForTitle());
        assertEquals(adapter2.getOid(), retrievedAdapter.getOid());
    }

    @Test
    public void hasInstances_whenEmpty() throws Exception {
        assertEquals(false, store.hasInstances(specification));
    }

    @Test
    public void hasInstances_whenHasSome() throws Exception {
        persistToObjectStore(adapter2);
        system.resetMaps();

        assertEquals(true, store.hasInstances(specification));
    }


}
