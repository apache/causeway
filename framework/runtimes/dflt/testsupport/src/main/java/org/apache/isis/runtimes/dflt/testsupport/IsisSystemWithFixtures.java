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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerPersist;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction.State;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures.Fixtures.Initialization;
import org.apache.isis.security.dflt.authentication.AuthenticationRequestDefault;
import org.apache.isis.tck.dom.refs.AggregatedEntity;
import org.apache.isis.tck.dom.refs.AssociatedEntitiesRepository;
import org.apache.isis.tck.dom.refs.ParentEntity;
import org.apache.isis.tck.dom.refs.ReferencingEntity;
import org.apache.isis.tck.dom.refs.SimpleEntity;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.tck.dom.scalars.JdkValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.WrapperValuedEntity;

/**
 * Wraps a plain {@link IsisSystemDefault}, and provides a number of features to assist with testing.
 */
public class IsisSystemWithFixtures implements org.junit.rules.TestRule {

    public interface Listener {

        void init(IsisConfiguration configuration) throws Exception;
        
        void preSetupSystem(boolean firstTime) throws Exception;
        void postSetupSystem(boolean firstTime) throws Exception;
        
        void preBounceSystem() throws Exception;
        void postBounceSystem() throws Exception;

        void preTeardownSystem() throws Exception;
        void postTeardownSystem() throws Exception;
        
    }
    
    public static abstract class ListenerAdapter implements Listener {
        
        private IsisConfiguration configuration;

        public void init(IsisConfiguration configuration) throws Exception {
            this.configuration = configuration;
        }
        
        protected IsisConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        public void preSetupSystem(boolean firstTime) throws Exception {
        }

        @Override
        public void postSetupSystem(boolean firstTime) throws Exception {
        }

        @Override
        public void preBounceSystem() throws Exception {
        }

        @Override
        public void postBounceSystem() throws Exception {
        }

        @Override
        public void preTeardownSystem() throws Exception {
        }

        @Override
        public void postTeardownSystem() throws Exception {
        }

    }



    /**
     * A precanned set of fixtures for use by tests if desired.
     */
    public static class Fixtures {

        public enum Initialization {
            INIT,
            NO_INIT
        }

        public AssociatedEntitiesRepository associatedEntitiesRepository = new AssociatedEntitiesRepository();

        public ApplibValuedEntity ave1, ave2; 
        public JdkValuedEntity jve1, jve2;
        public PrimitiveValuedEntity pve1, pve2;
        public WrapperValuedEntity wve1, wve2;
        
        public SimpleEntity smpl1, smpl2, smpl3, smpl4, smpl5, smpl6;
        public ReferencingEntity rfcg1, rfcg2, rfcg3, rfcg4, rfcg5, rfcg6;
        public ParentEntity prnt1, prnt2, prnt3, prnt4, prnt5, prnt6;
        public AggregatedEntity rfcg1_a1, rfcg1_a2, rfcg1_a3, prnt1_a1, prnt1_a2, prnt1_a3;

        private void init(DomainObjectContainer container) {
            ave1 = container.newTransientInstance(ApplibValuedEntity.class);
            ave2 = container.newTransientInstance(ApplibValuedEntity.class);
            
            jve1 = container.newTransientInstance(JdkValuedEntity.class);
            jve2 = container.newTransientInstance(JdkValuedEntity.class);
            
            pve1 = container.newTransientInstance(PrimitiveValuedEntity.class);
            pve2 = container.newTransientInstance(PrimitiveValuedEntity.class);

            wve1 = container.newTransientInstance(WrapperValuedEntity.class);
            wve2 = container.newTransientInstance(WrapperValuedEntity.class);
            
            smpl1 = container.newTransientInstance(SimpleEntity.class);
            smpl2 = container.newTransientInstance(SimpleEntity.class);
            smpl3 = container.newTransientInstance(SimpleEntity.class);
            smpl4 = container.newTransientInstance(SimpleEntity.class);
            smpl5 = container.newTransientInstance(SimpleEntity.class);
            smpl6 = container.newTransientInstance(SimpleEntity.class);
            rfcg1 = container.newTransientInstance(ReferencingEntity.class);
            rfcg2 = container.newTransientInstance(ReferencingEntity.class);
            rfcg3 = container.newTransientInstance(ReferencingEntity.class);
            rfcg4 = container.newTransientInstance(ReferencingEntity.class);
            rfcg5 = container.newTransientInstance(ReferencingEntity.class);
            rfcg6 = container.newTransientInstance(ReferencingEntity.class);
            prnt1 = container.newTransientInstance(ParentEntity.class);
            prnt2 = container.newTransientInstance(ParentEntity.class);
            prnt3 = container.newTransientInstance(ParentEntity.class);
            prnt4 = container.newTransientInstance(ParentEntity.class);
            prnt5 = container.newTransientInstance(ParentEntity.class);
            prnt6 = container.newTransientInstance(ParentEntity.class);
            rfcg1_a1 = container.newAggregatedInstance(rfcg1, AggregatedEntity.class);
            rfcg1_a2 = container.newAggregatedInstance(rfcg1, AggregatedEntity.class);
            rfcg1_a3 = container.newAggregatedInstance(rfcg1, AggregatedEntity.class);
            prnt1_a1 = container.newAggregatedInstance(prnt1, AggregatedEntity.class);
            prnt1_a2 = container.newAggregatedInstance(prnt1, AggregatedEntity.class);
            prnt1_a3 = container.newAggregatedInstance(prnt1, AggregatedEntity.class);
        }
    }

    private IsisSystemDefault isisSystem;
    private AuthenticationSession authenticationSession;

    // public visibility just to reduce noise in tests
    public DomainObjectContainer container;
    // public visibility just to reduce noise in tests
    public final Fixtures fixtures;
    
    private Initialization fixturesInitialization;
    private final IsisConfiguration configuration;
    private final PersistenceMechanismInstaller persistenceMechanismInstaller;
    private final AuthenticationRequest authenticationRequest;
    private final List<Object> services;
    private List <Listener> listeners;
    private final MetaModelValidator metaModelValidator;
    private final ProgrammingModel programmingModel;

    
    ////////////////////////////////////////////////////////////
    // constructor
    ////////////////////////////////////////////////////////////

    public static class Builder {

        private AuthenticationRequest authenticationRequest = new AuthenticationRequestDefault("tester");
        
        private Initialization fixturesInitialization = Initialization.INIT;
        private IsisConfiguration configuration;
        private PersistenceMechanismInstaller persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        private MetaModelValidator metaModelValidator;
        private ProgrammingModel programmingModel;

        private final List <Listener> listeners = Lists.newArrayList();
        private Object[] services;


        public Builder with(IsisConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
        
        public Builder with(Initialization initialization) {
            this.fixturesInitialization = initialization;
            return this;
        }
        
        public Builder with(PersistenceMechanismInstaller persistenceMechanismInstaller) {
            this.persistenceMechanismInstaller = persistenceMechanismInstaller;
            return this;
        }
        
        public Builder with(AuthenticationRequest authenticationRequest) {
            this.authenticationRequest = authenticationRequest;
            return this;
        }

        public Builder withServices(Object... services) {
            this.services = services;
            return this;
        }
        
        public IsisSystemWithFixtures build() {
            final List<Object> servicesIfAny = services != null? Arrays.asList(services): null;
            return new IsisSystemWithFixtures(fixturesInitialization, configuration, programmingModel, metaModelValidator, persistenceMechanismInstaller, authenticationRequest, servicesIfAny, listeners);
        }

        public Builder with(Listener listener) {
            if(listener != null) {
                listeners.add(listener);
            }
            return this;
        }

        public Builder with(MetaModelValidator metaModelValidator) {
            this.metaModelValidator = metaModelValidator;
            return this;
        }

        public Builder with(ProgrammingModel programmingModel) {
            this.programmingModel = programmingModel;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private IsisSystemWithFixtures(Initialization fixturesInitialization, IsisConfiguration configuration, ProgrammingModel programmingModel, MetaModelValidator metaModelValidator, PersistenceMechanismInstaller persistenceMechanismInstaller, AuthenticationRequest authenticationRequest, List<Object> services, List<Listener> listeners) {
        this.fixturesInitialization = fixturesInitialization;
        this.configuration = configuration;
        this.programmingModel = programmingModel;
        this.metaModelValidator = metaModelValidator;
        this.persistenceMechanismInstaller = persistenceMechanismInstaller;
        this.authenticationRequest = authenticationRequest;
        this.fixtures = new Fixtures();
        if(services == null) {
            services = Arrays.asList((Object)fixtures.associatedEntitiesRepository);
        }
        this.services = services;
        this.listeners = listeners;
    }


    ////////////////////////////////////////////////////////////
    // setup, teardown
    ////////////////////////////////////////////////////////////
    

    /**
     * Intended to be called from a test's {@link Before} method.
     */
    public void setUpSystem() throws Exception {
        setUpSystem(FireListeners.FIRE);
    }

    private void setUpSystem(FireListeners fireListeners) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        boolean firstTime = isisSystem == null;
        if(fireListeners.shouldFire()) {
            fireInitAndPreSetupSystem(firstTime);
        }
        
        if(firstTime) {
            isisSystem = createIsisSystem(services);
            isisSystem.init();
            IsisContext.closeSession();
        }

        final AuthenticationManager authenticationManager = isisSystem.getSessionFactory().getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequest);

        IsisContext.openSession(authenticationSession);
        container = getContainer();
        if(firstTime && fixturesInitialization == Fixtures.Initialization.INIT) {
            fixtures.init(container);
        }
        if(fireListeners.shouldFire()) {
            firePostSetupSystem(firstTime);
        }
    }

    private enum FireListeners {
        FIRE,
        DONT_FIRE;
        public boolean shouldFire() {
            return this == FIRE;
        }
    }
    
    private DomainObjectContainer getContainer() {
        return IsisContext.getPersistenceSession().getServicesInjector().getContainer();
    }

    /**
     * Intended to be called from a test's {@link After} method.
     */
    public void tearDownSystem() throws Exception {
        tearDownSystem(FireListeners.FIRE);
    }

    private void tearDownSystem(final FireListeners fireListeners) throws Exception {
        if(fireListeners.shouldFire()) {
            firePreTeardownSystem();
        }
        IsisContext.closeSession();
        if(fireListeners.shouldFire()) {
            firePostTeardownSystem();
        }
    }

    public void bounceSystem() throws Exception {
        firePreBounceSystem();
        tearDownSystem(FireListeners.DONT_FIRE);
        setUpSystem(FireListeners.DONT_FIRE);
        firePostBounceSystem();
    }


    private IsisSystemDefault createIsisSystem(List<Object> services) {
        final IsisSystemDefault system = new IsisSystemDefault(DeploymentType.UNIT_TESTING, services) {
            @Override
            public IsisConfiguration getConfiguration() {
                if(IsisSystemWithFixtures.this.configuration != null) {
                    return IsisSystemWithFixtures.this.configuration;
                } else {
                    return super.getConfiguration();
                }
            }
            @Override
            protected ProgrammingModel obtainReflectorProgrammingModel() {
                if(IsisSystemWithFixtures.this.programmingModel != null) {
                    return IsisSystemWithFixtures.this.programmingModel;
                } else {
                    return super.obtainReflectorProgrammingModel();
                }
            }
            @Override
            protected MetaModelValidator obtainReflectorMetaModelValidator() {
                if(IsisSystemWithFixtures.this.metaModelValidator != null) {
                    return IsisSystemWithFixtures.this.metaModelValidator;
                } else {
                    return super.obtainReflectorMetaModelValidator();
                }
            }
            @Override
            protected PersistenceMechanismInstaller obtainPersistenceMechanismInstaller(IsisConfiguration configuration) {
                final PersistenceMechanismInstaller installer = IsisSystemWithFixtures.this.persistenceMechanismInstaller;
                configuration.injectInto(installer);
                return installer;
            }
        };
        return system;
    }



    ////////////////////////////////////////////////////////////
    // listeners
    ////////////////////////////////////////////////////////////

    private void fireInitAndPreSetupSystem(boolean firstTime) throws Exception {
        if(firstTime) {
            for(Listener listener: listeners) {
                listener.init(configuration);
            }
        }
        for(Listener listener: listeners) {
            listener.preSetupSystem(firstTime);
        }
    }

    private void firePostSetupSystem(boolean firstTime) throws Exception {
        for(Listener listener: listeners) {
            listener.postSetupSystem(firstTime);
        }
    }

    private void firePreTeardownSystem() throws Exception {
        for(Listener listener: listeners) {
            listener.preTeardownSystem();
        }
    }

    private void firePostTeardownSystem() throws Exception {
        for(Listener listener: listeners) {
            listener.postTeardownSystem();
        }
    }

    private void firePreBounceSystem() throws Exception {
        for(Listener listener: listeners) {
            listener.preBounceSystem();
        }
    }

    private void firePostBounceSystem() throws Exception {
        for(Listener listener: listeners) {
            listener.postBounceSystem();
        }
    }

    
    ////////////////////////////////////////////////////////////
    // properties
    ////////////////////////////////////////////////////////////

    /**
     * The {@link IsisSystemDefault} created during {@link #setUpSystem()}.
     * 
     * <p>
     * Can fine-tune the actual implementation using the hook {@link #createIsisSystem()}.
     */
    public IsisSystemDefault getIsisSystem() {
        return isisSystem;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
     * 
     * <p>
     * Can fine-tune before hand using {@link #createAuthenticationRequest()}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }



    ////////////////////////////////////////////////////////////
    // Convenience for tests
    ////////////////////////////////////////////////////////////

    public ObjectSpecification loadSpecification(Class<?> cls) {
        return getIsisSystem().getSessionFactory().getSpecificationLoader().loadSpecification(cls);
    }

    public ObjectAdapter persist(Object domainObject) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(domainObject);
        container.persist(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter destroy(Object domainObject ) {
        ensureSessionInProgress();
        ensureObjectIsPersistent(domainObject);
        container.remove(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter adapterFor(Object domainObject) {
        ensureSessionInProgress();
        return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(domainObject);
    }

    public ObjectAdapter reload(RootOid oid) {
        ensureSessionInProgress();
        final PersistenceSession persistenceSession = IsisContext.getPersistenceSession();
        return persistenceSession.loadObject(oid);
    }

    public ObjectAdapter recreateAdapter(RootOid oid) {
        ensureSessionInProgress();
        return IsisContext.getPersistenceSession().recreateAdapter(oid);
    }

    public ObjectAdapter remapAsPersistent(Object pojo, RootOid persistentOid) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(pojo);
        final ObjectAdapter adapter = adapterFor(pojo);
        getAdapterManagerPersist().remapAsPersistent(adapter, persistentOid);
        return adapter;
    }

    @SuppressWarnings("unchecked")
    public <T extends ObjectStore> T getObjectStore(Class<T> cls) {
        final PersistenceSession persistenceSession = IsisContext.getPersistenceSession();
        return (T) persistenceSession.getObjectStore();
    }

    private static void ensureSessionInProgress() {
        if(!IsisContext.inSession()) {
            throw new IllegalStateException("Session must be in progress");
        }
    }

    private void ensureObjectIsNotPersistent(Object domainObject) {
        if(container.isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is already persistent");
        }
    }

    private void ensureObjectIsPersistent(Object domainObject) {
        if(!container.isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is not persistent");
        }
    }

    ////////////////////////////////////////////////////////////
    // JUnit @Rule integration
    ////////////////////////////////////////////////////////////

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUpSystem();
                try {
                    base.evaluate();
                    tearDownSystem();
                } catch(Throwable ex) {
                    try {
                        tearDownSystem();
                    } catch(Exception ex2) {
                        // ignore, since already one pending
                    }
                    throw ex;
                }
            }
        };
    }


    
    private AdapterManagerPersist getAdapterManagerPersist() {
        return (AdapterManagerPersist)IsisContext.getPersistenceSession().getAdapterManager();
    }

    public void beginTran() {
        final IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();

        if(transaction == null) {
            transactionManager.startTransaction();
            return;
        } 

        final State state = transaction.getState();
        switch(state) {
            case COMMITTED:
            case ABORTED:
                transactionManager.startTransaction();
                break;
            case IN_PROGRESS:
                // nothing to do
                break;
            case MUST_ABORT:
                Assert.fail("Transaction is in state of '" + state + "'");
                break;
            default:
                Assert.fail("Unknown transaction state '" + state + "'");
        }
        
    }

    public void commitTran() {
        final IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();
        if(transaction == null) {
            Assert.fail("No transaction exists");
            return;
        } 
        final State state = transaction.getState();
        switch(state) {
            case COMMITTED:
            case ABORTED:
            case MUST_ABORT:
                Assert.fail("Transaction is in state of '" + state + "'");
                break;
            case IN_PROGRESS:
                transactionManager.endTransaction();
                break;
            default:
                Assert.fail("Unknown transaction state '" + state + "'");
        }
    }

    public void abortTran() {
        final IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();
        if(transaction == null) {
            Assert.fail("No transaction exists");
            return;
        } 
        final State state = transaction.getState();
        switch(state) {
            case ABORTED:
                break;
            case COMMITTED:
                Assert.fail("Transaction is in state of '" + state + "'");
                break;
            case MUST_ABORT:
            case IN_PROGRESS:
                transactionManager.abortTransaction();
                break;
            default:
                Assert.fail("Unknown transaction state '" + state + "'");
        }
    }

    
}
