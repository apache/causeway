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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore;

import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.memento.RuntimeTestPojo;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerTestSupport;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.dummy.DummyPersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.ObjectStoreTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionDefault;
import org.apache.isis.runtimes.embedded.EmbeddedContext;
import org.apache.isis.runtimes.embedded.IsisMetaModel;

public class PersistenceSessionObjectStoreTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ServicesInjectorDefault servicesInjector;
    private AdapterManagerExtended adapterManager;
    private ObjectAdapterFactory adapterFactory;
    
    
    private PersistenceSessionObjectStore persistenceSession;
    private ObjectStoreTransactionManager transactionManager;
    
    private ObjectAdapter persistentAdapter;
    private PojoAdapter transientAdapter;
    
    @Mock
    private PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    private ObjectStore mockObjectStore;
    @Mock
    private ObjectFactory objectFactory;

    @Mock
    private CreateObjectCommand createObjectCommand;
    @Mock
    private SaveObjectCommand saveObjectCommand;
    @Mock
    private DestroyObjectCommand destroyObjectCommand;

    @Mock
    private Version mockVersion;

    @Mock
    private EmbeddedContext mockMetaModelContext;

    private IsisMetaModel isisMetaModel;


    public static class Customer {
    }

    public static class CustomerRepository {
        public Customer x() {return null;}
    }
    
    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        isisMetaModel = new IsisMetaModel(mockMetaModelContext, new CustomerRepository());
        isisMetaModel.init();
        
        context.checking(new Expectations() {
            {
                ignoring(mockObjectStore).open();
                ignoring(mockObjectStore).close();
                
                ignoring(createObjectCommand);
                ignoring(saveObjectCommand);
                ignoring(destroyObjectCommand);
                ignoring(mockVersion);
            }
        });

        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        final DomainObjectContainerDefault container = new DomainObjectContainerDefault();

        runtimeContext.injectInto(container);
        runtimeContext.setContainer(container);

        servicesInjector = new ServicesInjectorDefault();
        servicesInjector.setContainer(container);

        adapterManager = new AdapterManagerDefault();
        adapterFactory = new PojoAdapterFactory();
        persistenceSession = new PersistenceSessionObjectStore(mockPersistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, new RootOidGenerator(), adapterManager, new DummyPersistAlgorithm(), mockObjectStore);
        
        transactionManager = new ObjectStoreTransactionManager(persistenceSession, mockObjectStore);
        transactionManager.injectInto(persistenceSession);

        servicesInjector.setServices(Collections.emptyList());
        persistenceSession.setSpecificationLoader(isisMetaModel.getSpecificationLoader());

        persistentAdapter = PojoAdapterBuilder.create().withOid("CUS|1").withPojo(new Customer()).with(Persistence.PERSISTENT).with(mockVersion).with(isisMetaModel.getSpecificationLoader()).build();
        transientAdapter = PojoAdapterBuilder.create().withOid("CUS|2").withPojo(new Customer()).with(Persistence.TRANSIENT).with(isisMetaModel.getSpecificationLoader()).build();
    }


    @Test
    public void destroyObjectThenAbort() {
        
        final Sequence tran = context.sequence("tran");
        context.checking(new Expectations() {
            {
                one(mockObjectStore).startTransaction();
                inSequence(tran);

                one(mockObjectStore).createDestroyObjectCommand(persistentAdapter);
                inSequence(tran);

                one(mockObjectStore).abortTransaction();
                inSequence(tran);
            }
        });
        
        transactionManager.startTransaction();
        persistenceSession.destroyObject(persistentAdapter);
        transactionManager.abortTransaction();
    }

    @Test
    public void destroyObject_thenCommit() {

        final Sequence tran = context.sequence("tran");
        context.checking(new Expectations() {
            {
                one(mockObjectStore).startTransaction();
                inSequence(tran);

                one(mockObjectStore).createDestroyObjectCommand(persistentAdapter);
                inSequence(tran);
                will(returnValue(destroyObjectCommand));
                
                one(mockObjectStore).execute(with(IsisMatchers.listContaining((PersistenceCommand)destroyObjectCommand)));
                inSequence(tran);

                one(mockObjectStore).endTransaction();
                inSequence(tran);
            }

        });

        transactionManager.startTransaction();
        persistenceSession.destroyObject(persistentAdapter);
        transactionManager.endTransaction();
    }

    @Test
    public void makePersistent() {

        final Sequence tran = context.sequence("tran");
        context.checking(new Expectations() {
            {
                one(mockObjectStore).startTransaction();
                inSequence(tran);

                one(mockObjectStore).createCreateObjectCommand(with(any(ObjectAdapter.class)));
                inSequence(tran);
                will(returnValue(createObjectCommand));

                one(mockObjectStore).execute(with(IsisMatchers.listContaining((PersistenceCommand)createObjectCommand)));
                inSequence(tran);

                one(mockObjectStore).endTransaction();
                inSequence(tran);
            }
        });

        // note that the persist algorithm also gets a look-in (not tested here...)
        
        transactionManager.startTransaction();
        persistenceSession.makePersistent(transientAdapter);
        transactionManager.endTransaction();
    }
}
