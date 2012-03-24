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

package org.apache.isis.runtimes.dflt.testsupport;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.testsupport.domain.ExamplePojoRepository;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojo;
import org.apache.isis.runtimes.dflt.testsupport.domain.TestPojoRepository;
import org.apache.isis.runtimes.embedded.EmbeddedContext;
import org.apache.isis.runtimes.embedded.IsisMetaModel;

public abstract class TestSystemWithObjectStoreTestAbstract {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    protected TestSystem system;
    protected IsisMetaModel isisMetaModel;

    @Mock
    protected EmbeddedContext mockEmbeddedContext;
    @Mock
    protected UserProfileStore mockUserProfileStore;
    @Mock
    protected UserProfile mockUserProfile;
    @Mock
    private Localization mockLocalization;

    protected PersistenceSessionFactory persistenceSessionFactory;
    protected ObjectStore store;
    
    protected PersistenceSession persistenceSession;

    protected TestPojo testPojo1;
    protected TestPojo testPojo2;
    
    protected ObjectAdapter adapter1;
    protected ObjectAdapter adapter2;

    // as a convenience for subclasses
    protected ObjectSpecification specification;
    protected ObjectSpecification serviceSpecification;


    @Before
    public void setUpSystem() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        TestSystemExpectations.allowingUserProfileLoaderToCreateUserProfile(context, mockUserProfileStore, mockUserProfile);
        TestSystemExpectations.allowingUserProfileToReturnLocalization(context, mockUserProfile, mockLocalization);
        
        isisMetaModel = new IsisMetaModel(mockEmbeddedContext, new TestPojoRepository(), new ExamplePojoRepository());
        isisMetaModel.init();
        system = new TestSystem(isisMetaModel);

        final PersistenceMechanismInstaller persistenceMechanismInstaller = createPersistenceMechanismInstaller();
        isisMetaModel.getConfiguration().injectInto(persistenceMechanismInstaller);
        
        system.openSession(mockUserProfileStore, persistenceMechanismInstaller);

        store = system.getObjectStore(ObjectStore.class);
        

        specification = system.loadSpecification(TestPojo.class);
        serviceSpecification = system.loadSpecification(TestPojoRepository.class);

        testPojo1 = new TestPojo();
        testPojo1.setPropertyUsedForTitle("object 1");
        adapter1 = system.createTransient(testPojo1, RootOidDefault.createTransient("CUS|1"));
        
        testPojo2 = new TestPojo();
        testPojo2.setPropertyUsedForTitle("object 2");
        adapter2 = system.recreateAdapter(testPojo2, RootOidDefault.create("CUS|2"));
        adapter2.setVersion(new SerialNumberVersion(1, "USER", new Date(0)));  // would be done by the object store, we must do ourselves.
    }

    @After
    public void tearDownSystem() throws Exception {
        system.closeSession();
    }

    /**
     * Mandatory hook method for test to provide an instance.
     */
    protected abstract PersistenceMechanismInstaller createPersistenceMechanismInstaller();
    

    /**
     * convenience for subclasses
     */
    protected void persistToObjectStore(final ObjectAdapter persistentAdapter) {
        final PersistenceCommand command = store.createCreateObjectCommand(persistentAdapter);
        assertEquals(persistentAdapter, command.onObject());
        store.execute(Collections.<PersistenceCommand> singletonList(command));
    }

}
