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

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyOid;


public class InMemoryObjectStore_serviceRegistry extends AbstractInMemoryObjectStoreTest {

    private TestProxyOid oid14;

	public void noServicesRegisteredWhenEmpty() throws Exception {
        final Oid oidForService = store.getOidForService("service name");
        assertEquals(null, oidForService);
    }

    public void testOidForService() throws Exception {
        registerService14();

        final Oid oidForService = store.getOidForService("service name");
        assertEquals(oid14, oidForService);
    }

    public void testCantRegisterServiceMoreThanOnce() throws Exception {
    	registerService14();
        try {
        	registerService14();
            fail();
        } catch (final IsisException expected) {}
    }

    public void testCanRegisterMoreThanOneService() throws Exception {
    	registerService14();
    	registerService15();
    }

	private TestProxyOid registerService14() {
		return oid14 = registerService(14);
	}

	private TestProxyOid registerService15() {
		return registerService(15);
	}

	private TestProxyOid registerService(int id) {
		TestProxyOid oid = new TestProxyOid(id);
        store.registerService("service name", oid);
        resetIdentityMap();
		return oid;
	}

}

