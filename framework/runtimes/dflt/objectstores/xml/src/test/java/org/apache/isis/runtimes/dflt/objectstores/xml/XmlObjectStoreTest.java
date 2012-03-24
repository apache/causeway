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

package org.apache.isis.runtimes.dflt.objectstores.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.objectstores.xml.internal.clock.DefaultClock;
import org.apache.isis.runtimes.dflt.objectstores.xml.internal.data.MockDataManager;
import org.apache.isis.runtimes.dflt.objectstores.xml.internal.services.ServiceManager;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.testsupport.TestSystem;
import org.apache.isis.runtimes.dflt.testsupport.TestSystemExpectations;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.runtimes.dflt.testsupport.TestSystemWithObjectStoreTestAbstract;

public class XmlObjectStoreTest extends TestSystemWithObjectStoreTestAbstract {
    
    @Override
    protected PersistenceMechanismInstaller createPersistenceMechanismInstaller() {
        return new XmlPersistenceMechanismInstaller();
    }

    @Mock
    protected ObjectAdapter mockAdapter;

    @Mock
    private ServiceManager mockServiceManager;
    
    @Mock
    private IsisConfiguration mockConfiguration;
    
    
    private XmlObjectStore objectStore;
    private MockDataManager dataManager;
    
    private ObjectStorePersistenceHelper persistenceHelper;
    private PersistenceCommandContext transaction;



    @Before
    public void setUpSystem() throws Exception {

        // system
        dataManager = new MockDataManager();
        objectStore = new XmlObjectStore(dataManager, mockServiceManager);
        objectStore.setClock(new DefaultClock());

        // objects
//        adapter.setOptimisticLock(new SerialNumberVersion(23, null, null));
//        adapter.setupOid(RootOidDefault.createPersistent(objectType, ""+1));
        
        persistenceHelper = new ObjectStorePersistenceHelper(specification);
        transaction = null;
    }

    @Test
    public void name() throws Exception {
        assertTrue(objectStore.name().equals("XML"));
    }

    @Test
    public void createSaveObjectCommand_setsVersionOnAdapter() throws Exception {
        
        allowingGetOidAndGetObjectAndTitleStringFromAdapter();
        context.checking(new Expectations() {
            {
                one(mockAdapter).setVersion(with(any(Version.class)));
            }
        });
        objectStore.createSaveObjectCommand(mockAdapter);
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void createCreateObjectCommand_andExecute_persistsNewInstance() throws Exception {
        // given
        final CreateObjectCommand command = objectStore.createCreateObjectCommand(adapter1);
        // when
        command.execute(transaction);
        // then
        assertFalse(objectStore.hasInstances(specification));
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesDestroyObjectCommand() throws Exception {
        final DestroyObjectCommand command = objectStore.createDestroyObjectCommand(adapter1);
        command.execute(transaction);
        assertFalse(objectStore.hasInstances(specification));
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesSaveObjectCommand() throws Exception {
        final SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter1);
        command.execute(transaction);
        assertTrue(objectStore.hasInstances(specification));
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesGettingObjectStoreInstances() throws Exception {
        final SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter1);
        objectStore.execute(Collections.<PersistenceCommand> singletonList(command));
        final ObjectAdapter[] array = objectStore.getInstances(persistenceHelper);
        assertTrue(array.length == 1);
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesObjectStoreHasInstances() throws Exception {
        final SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter1);
        objectStore.execute(Collections.<PersistenceCommand> singletonList(command));
        assertTrue(objectStore.hasInstances(specification));
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesObjectStoreIfFixtureIsInstalled() throws Exception {
        final SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter1);
        objectStore.execute(Collections.<PersistenceCommand> singletonList(command));
        objectStore.open();
        assertTrue(objectStore.isFixturesInstalled());
    }

    @Ignore // DKH: refactor to use contract tests (see in-memory object store for basis)
    @Test
    public void validatesObjectStoreGetObject() throws Exception {
        final SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter1);
        objectStore.execute(Collections.<PersistenceCommand> singletonList(command));
        assertTrue(objectStore.getObject(adapter1.getOid(), adapter1.getSpecification()).getOid().equals(adapter1.getOid()));
    }

    @Test
    public void validateObjectStoreCreationWithProxyConfiguration() throws Exception {
        final XmlObjectStore objectStore = new XmlObjectStore(mockConfiguration);
        assertFalse(objectStore.isFixturesInstalled());
    }

    private void allowingGetOidAndGetObjectAndTitleStringFromAdapter() {
        context.checking(new Expectations() {
            {
                allowing(mockAdapter).getOid();
                allowing(mockAdapter).getObject();
                allowing(mockAdapter).titleString();
            }
        });
    }

}
