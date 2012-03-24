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

import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.testsupport.TestSystemExpectations;
import org.apache.isis.runtimes.dflt.testsupport.TestSystem;

public class InMemoryObjectStoreTest_serviceRegistry extends InMemoryObjectStoreTestAbstract {

    @Test
    public void getOidForServices_whenEmpty() throws Exception {
        final Oid oidForService = store.getOidForService(serviceSpecification);
        assertEquals(null, oidForService);
    }


    @Test
    public void xx() throws Exception {
        fail(); // review the stuff below, commented out; 

    }

//    @Test
//    public void registerService_canBeRetrieved() throws Exception {
//        registerService14();
//
//        final Oid oidForService = store.getOidForService(serviceSpecification);
//        assertEquals(oid14, oidForService);
//    }
//
//    @Test
//    public void testCantRegisterServiceMoreThanOnce() throws Exception {
//        registerService14();
//        try {
//            registerService14();
//            fail();
//        } catch (final IsisException expected) {
//        }
//    }
//
//    @Test
//    public void testCanRegisterMoreThanOneService() throws Exception {
//        registerService14();
//        registerService15();
//    }
//
//
//    private void resetIdentityMap() {
//        IsisContext.getPersistenceSession().testReset();
//    }
//
//    protected void addObjectToStore(final ObjectAdapter object) {
//        final PersistenceCommand command = store.createCreateObjectCommand(object);
//        assertEquals(object, command.onObject());
//        store.execute(Collections.<PersistenceCommand> singletonList(command));
//    }
//
//    private RootOidDefault registerService14() {
//        return oid14 = registerService(""+14);
//    }
//
//    private RootOidDefault registerService15() {
//        return registerService(""+15);
//    }
//
//    private RootOidDefault registerService(final String id) {
//        final RootOidDefault oid = RootOidDefault.createPersistent("SVC", id);
//        store.registerService(oid);
//        resetIdentityMap();
//        return oid;
//    }

    
}
