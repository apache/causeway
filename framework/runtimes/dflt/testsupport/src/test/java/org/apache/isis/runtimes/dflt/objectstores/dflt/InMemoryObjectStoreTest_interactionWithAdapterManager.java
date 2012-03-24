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
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojo;

public class InMemoryObjectStoreTest_interactionWithAdapterManager extends InMemoryObjectStoreTestAbstract {
    
    /**
     * Testing, indirectly, that the adapter manager doesn't automatically save
     * objects in the objectstore.
     */
    @Test
    public void testObjectNotPersistedWhenCreated() throws Exception {
        
        // given persistentAdapter mapped to AdapterManager, but hasn't actually been persisted...
        final ObjectSpecification specification = system.loadSpecification(TestPojo.class);
        
        // when
        final boolean hasInstances = store.hasInstances(specification);
        final ObjectAdapter[] instances = store.getInstances(new PersistenceQueryFindAllInstances(specification));
        
        // then
        assertEquals(false, hasInstances);
        // and then
        assertEquals(0, instances.length);
        // and then
        try {
            store.getObject(adapter2.getOid(), specification);
            fail();
        } catch (final ObjectNotFoundException expected) {
        }
    }

}
