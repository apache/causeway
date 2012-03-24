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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * This is really just a test of the test infrastructure, not a real test per se.
 */
public class InMemoryObjectStoreTest_reset extends InMemoryObjectStoreTestAbstract {

    @Test
    public void rest_ClearsAdapterFromLoader() throws Exception {
        
        persistToObjectStore(adapter2);

        assertNotNull(getAdapterManager().getAdapterFor(adapter2.getObject()));

        IsisContext.getPersistenceSession().testReset();

        assertNull(getAdapterManager().getAdapterFor(adapter2.getObject()));
    }

    
    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}
