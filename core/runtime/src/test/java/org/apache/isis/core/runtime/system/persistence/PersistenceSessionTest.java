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

package org.apache.isis.core.runtime.system.persistence;

import java.util.Collections;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.services.audit.AuditingService3;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.app.IsisMetaModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.adaptermanager.PojoRecreatorUnified;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.*;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import static org.hamcrest.CoreMatchers.equalTo;

public class PersistenceSessionTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ServicesInjectorDefault servicesInjector;
    private AdapterManagerDefault adapterManager;
    private ObjectAdapterFactory adapterFactory;
    
    
    private PersistenceSession persistenceSession;
    private IsisTransactionManager transactionManager;
    
    private ObjectAdapter persistentAdapter;
    private PojoAdapter transientAdapter;
    
    @Mock
    private PersistenceSessionFactory mockPersistenceSessionFactory;
    
    @Mock
    private AuthenticationSession mockAuthenticationSession;

    @Mock
    private ObjectStore mockObjectStore;
    @Mock
    private AuditingService3 mockAuditingService3;
    @Mock
    private PublishingServiceWithDefaultPayloadFactories mockPublishingService;

    @Mock
    private CreateObjectCommand createObjectCommand;
    @Mock
    private SaveObjectCommand saveObjectCommand;
    @Mock
    private DestroyObjectCommand destroyObjectCommand;

    @Mock
    private Version mockVersion;

    @Mock
    private RuntimeContext mockRuntimeContext;

    @Mock
    private IsisConfiguration mockConfiguration;
    
    @Mock
    private MessageBroker mockMessageBroker;
    
    
    private IsisMetaModel isisMetaModel;



    public static class Customer {
    }

    public static class CustomerRepository {
        public Customer x() {return null;}
    }
    
    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockRuntimeContext);
        context.ignoring(mockConfiguration);
        context.ignoring(mockAuditingService3);

        isisMetaModel = new IsisMetaModel(mockRuntimeContext, new ProgrammingModelFacetsJava5(), new CustomerRepository());
        isisMetaModel.init();
        
        context.checking(new Expectations() {
            {
                ignoring(mockObjectStore).open();
                ignoring(mockObjectStore).close();
                ignoring(mockObjectStore).name();
                ignoring(mockConfiguration);

                ignoring(createObjectCommand);
                ignoring(saveObjectCommand);
                ignoring(destroyObjectCommand);
                ignoring(mockVersion);
                
                allowing(mockAuthenticationSession).getMessageBroker();
                will(returnValue(mockMessageBroker));
            }
        });

        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        final DomainObjectContainerDefault container = new DomainObjectContainerDefault();

        runtimeContext.injectInto(container);

        servicesInjector = new ServicesInjectorDefault(new InjectorMethodEvaluatorDefault());

        adapterManager = new AdapterManagerDefault(new PojoRecreatorUnified(mockConfiguration));
        adapterFactory = new PojoAdapterFactory();
        persistenceSession = new PersistenceSession(mockPersistenceSessionFactory, servicesInjector, mockObjectStore, mockConfiguration) {
            @Override
            protected SpecificationLoaderSpi getSpecificationLoader() {
                return isisMetaModel.getSpecificationLoader();
            }
            
        };
        
        servicesInjector.setServices(Collections.<Object>singletonList(container));
        
        context.checking(new Expectations(){{
            allowing(mockAuthenticationSession).getUserName();
            will(returnValue("sven"));
        }});

        
        transactionManager = persistenceSession.getTransactionManager();
        new IsisTransactionManager(persistenceSession, mockObjectStore, servicesInjector) {
            @Override
            public AuthenticationSession getAuthenticationSession() {
                return mockAuthenticationSession;
            }
        };
        persistenceSession.setTransactionManager(transactionManager);

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
                oneOf(mockObjectStore).startTransaction();
                inSequence(tran);

                oneOf(mockObjectStore).createDestroyObjectCommand(persistentAdapter);
                inSequence(tran);
                will(returnValue(destroyObjectCommand));
                
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContaining((PersistenceCommand)destroyObjectCommand)));
                inSequence(tran);

                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(tran);

                // third flush after commands
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(tran);

                oneOf(mockObjectStore).endTransaction();
                inSequence(tran);
            }

        });

        transactionManager.startTransaction();
        persistenceSession.destroyObject(persistentAdapter);
        transactionManager.endTransaction();
    }

    @Test
    public void makePersistent_happyCase() {

        final Sequence tran = context.sequence("tran");
        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).startTransaction();
                inSequence(tran);

                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(tran);
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(tran);
                // third flush after commands
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(tran);

                oneOf(mockObjectStore).endTransaction();
                inSequence(tran);
            }
        });

        transactionManager.startTransaction();
        persistenceSession.makePersistent(transientAdapter);
        transactionManager.endTransaction();
    }
}
