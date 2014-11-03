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

package org.apache.isis.core.integtestsupport;

import java.util.Arrays;
import java.util.List;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.objectstore.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.ObjectStore;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction.State;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationRequestNameOnly;
import org.apache.isis.core.specsupport.scenarios.DomainServiceProvider;

/**
 * Wraps a plain {@link IsisSystemDefault}, and provides a number of features to assist with testing.
 */
public class IsisSystemForTest implements org.junit.rules.TestRule, DomainServiceProvider {

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

    // //////////////////////////////////////

    private static ThreadLocal<IsisSystemForTest> ISFT = new ThreadLocal<IsisSystemForTest>();

    public static IsisSystemForTest getElseNull() {
        return ISFT.get();
    }
    
    public static IsisSystemForTest get() {
        final IsisSystemForTest isft = ISFT.get();
        if(isft == null) {
            throw new IllegalStateException("No IsisSystemForTest available on thread; call #set(IsisSystemForTest) first");
        }
        return isft;
    }

    public static void set(IsisSystemForTest isft) {
        ISFT.set(isft);
    }

    // //////////////////////////////////////


    private IsisSystemDefault isisSystem;
    private AuthenticationSession authenticationSession;

    private final IsisConfiguration configuration;
    private final PersistenceMechanismInstaller persistenceMechanismInstaller;
    private final AuthenticationRequest authenticationRequest;
    private final List<Object> services;
    private final List<InstallableFixture> fixtures;
    private List <Listener> listeners;
    
    private org.apache.log4j.Level level = org.apache.log4j.Level.INFO;
    
    private final MetaModelValidator metaModelValidator;
    private final ProgrammingModel programmingModel;
    
    private DomainObjectContainer container;

    
    ////////////////////////////////////////////////////////////
    // constructor
    ////////////////////////////////////////////////////////////

    public static class Builder {

        private AuthenticationRequest authenticationRequest = new AuthenticationRequestNameOnly("tester");
        
        private IsisConfiguration configuration;
        private PersistenceMechanismInstaller persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        private MetaModelValidator metaModelValidator;
        private ProgrammingModel programmingModel;

        private final List<Object> services = Lists.newArrayList();
        private final List<InstallableFixture> fixtures = Lists.newArrayList();
        
        private final List <Listener> listeners = Lists.newArrayList();

        private org.apache.log4j.Level level;

        public Builder with(IsisConfiguration configuration) {
            this.configuration = configuration;
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

        public Builder withServicesIn(String... packagePrefixes ) {
            if(packagePrefixes.length == 0) {
                throw new IllegalArgumentException("Specify packagePrefixes to search for @DomainService-annotated services");
            }
            final ServicesInstallerFromAnnotation installer = new ServicesInstallerFromAnnotation();
            installer.setConfiguration(configuration);
            installer.withPackagePrefixes(packagePrefixes);
            final List<Object> serviceList = installer.getServices(null);
            this.services.addAll(serviceList);

            installer.init();
            return this;
        }

        public Builder withServices(Object... services) {
            this.services.addAll(Arrays.asList(services));
            return this;
        }

        /**
         * @deprecated - prefer to use {@link org.apache.isis.applib.fixturescripts.FixtureScript}s API instead.
         */
        @Deprecated
        public Builder withFixtures(InstallableFixture... fixtures) {
            this.fixtures.addAll(Arrays.asList(fixtures));
            return this;
        }
        
        public Builder withLoggingAt(org.apache.log4j.Level level) {
            this.level = level;
            return this;
        }

        public IsisSystemForTest build() {
            final IsisSystemForTest isisSystem =
                    new IsisSystemForTest(
                            configuration,
                            programmingModel,
                            metaModelValidator,
                            persistenceMechanismInstaller,
                            authenticationRequest,
                            services,
                            fixtures,
                            listeners);
            if(level != null) {
                isisSystem.setLevel(level);
            }
            return isisSystem;
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

    private IsisSystemForTest(
            final IsisConfiguration configuration, 
            final ProgrammingModel programmingModel, 
            final MetaModelValidator metaModelValidator, 
            final PersistenceMechanismInstaller persistenceMechanismInstaller, 
            final AuthenticationRequest authenticationRequest, 
            final List<Object> services, 
            final List<InstallableFixture> fixtures, 
            final List<Listener> listeners) {
        this.configuration = configuration;
        this.programmingModel = programmingModel;
        this.metaModelValidator = metaModelValidator;
        this.persistenceMechanismInstaller = persistenceMechanismInstaller;
        this.authenticationRequest = authenticationRequest;
        this.services = services;
        this.fixtures = fixtures;
        this.listeners = listeners;
    }

    ////////////////////////////////////////////////////////////
    // level
    ////////////////////////////////////////////////////////////

    /**
     * The level to use for the root logger if fallback (ie a <tt>logging.properties</tt> file cannot be found).
     */
    public org.apache.log4j.Level getLevel() {
        return level;
    }
    
    public void setLevel(org.apache.log4j.Level level) {
        this.level = level;
    }

    ////////////////////////////////////////////////////////////
    // setup, teardown
    ////////////////////////////////////////////////////////////
    

    /**
     * Intended to be called from a test's {@link Before} method.
     */
    public IsisSystemForTest setUpSystem() throws RuntimeException {
        try {
            setUpSystem(FireListeners.FIRE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void setUpSystem(FireListeners fireListeners) throws Exception {

        boolean firstTime = isisSystem == null;
        if(fireListeners.shouldFire()) {
            fireInitAndPreSetupSystem(firstTime);
        }
        
        if(firstTime) {
            IsisLoggingConfigurer isisLoggingConfigurer = new IsisLoggingConfigurer(getLevel());
            isisLoggingConfigurer.configureLogging(".", new String[]{});

            isisSystem = createIsisSystem(services);

            // ensures that a FixtureClock is installed as the singleton underpinning the ClockService
            FixtureClock.initialize();

            isisSystem.init();
            IsisContext.closeSession();
        }

        final AuthenticationManager authenticationManager = isisSystem.getSessionFactory().getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequest);

        setContainer(getContainer());

        openSession();

        wireAndInstallFixtures();
        if(fireListeners.shouldFire()) {
            firePostSetupSystem(firstTime);
        }
    }

    private void wireAndInstallFixtures() {
        FixturesInstallerDelegate fid = new FixturesInstallerDelegate(getPersistenceSession());
        fid.addFixture(fixtures);
        fid.installFixtures();
    }

    private enum FireListeners {
        FIRE,
        DONT_FIRE;
        public boolean shouldFire() {
            return this == FIRE;
        }
    }

    public DomainObjectContainer getContainer() {
        for (Object service : services) {
            if(service instanceof DomainObjectContainer) {
                return (DomainObjectContainer) service;
            }
        }
        throw new IllegalStateException("Could not locate DomainObjectContainer");
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
        closeSession();
        openSession();
        firePostBounceSystem();
    }

    public void openSession() throws Exception {
        openSession(authenticationSession);

    }

    public void openSession(AuthenticationSession authenticationSession) throws Exception {
        IsisContext.openSession(authenticationSession);
    }

    public void closeSession() throws Exception {
        IsisContext.closeSession();
    }

    private IsisSystemDefault createIsisSystem(List<Object> services) {

        final IsisSystemDefault system = new IsisSystemDefault(DeploymentType.UNIT_TESTING, services) {
            @Override
            public IsisConfiguration getConfiguration() {
                if(IsisSystemForTest.this.configuration != null) {
                    return IsisSystemForTest.this.configuration;
                } else {
                    return super.getConfiguration();
                }
            }
            @Override
            protected ProgrammingModel obtainReflectorProgrammingModel() {
                if(IsisSystemForTest.this.programmingModel != null) {
                    return IsisSystemForTest.this.programmingModel;
                } else {
                    return super.obtainReflectorProgrammingModel();
                }
            }
            @Override
            protected MetaModelValidator obtainReflectorMetaModelValidator() {
                if(IsisSystemForTest.this.metaModelValidator != null) {
                    return IsisSystemForTest.this.metaModelValidator;
                } else {
                    return super.obtainReflectorMetaModelValidator();
                }
            }
            @Override
            protected PersistenceMechanismInstaller obtainPersistenceMechanismInstaller(IsisConfiguration configuration) {
                final PersistenceMechanismInstaller installer = IsisSystemForTest.this.persistenceMechanismInstaller;
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
     * Can fine-tune the actual implementation using the hook {@link #createIsisSystem(List)}.
     */
    public IsisSystemDefault getIsisSystem() {
        return isisSystem;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
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
        getContainer().persist(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter destroy(Object domainObject ) {
        ensureSessionInProgress();
        ensureObjectIsPersistent(domainObject);
        getContainer().remove(domainObject);
        return adapterFor(domainObject);
    }

    public ObjectAdapter adapterFor(Object domainObject) {
        ensureSessionInProgress();
        return getAdapterManager().adapterFor(domainObject);
    }

    public ObjectAdapter reload(RootOid oid) {
        ensureSessionInProgress();
        final PersistenceSession persistenceSession = getPersistenceSession();
        return persistenceSession.loadObject(oid);
    }

    public ObjectAdapter recreateAdapter(RootOid oid) {
        ensureSessionInProgress();
        return getAdapterManager().adapterFor(oid);
    }

    public ObjectAdapter remapAsPersistent(Object pojo, RootOid persistentOid) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(pojo);
        final ObjectAdapter adapter = adapterFor(pojo);
        getPersistenceSession().getAdapterManager().remapAsPersistent(adapter, persistentOid);
        return adapter;
    }

    @SuppressWarnings("unchecked")
    public <T extends ObjectStore> T getObjectStore(Class<T> cls) {
        final PersistenceSession persistenceSession = getPersistenceSession();
        return (T) persistenceSession.getObjectStore();
    }

    private static void ensureSessionInProgress() {
        if(!IsisContext.inSession()) {
            throw new IllegalStateException("Session must be in progress");
        }
    }

    private void ensureObjectIsNotPersistent(Object domainObject) {
        if(getContainer().isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is already persistent");
        }
    }

    private void ensureObjectIsPersistent(Object domainObject) {
        if(!getContainer().isPersistent(domainObject)) {
            throw new IllegalArgumentException("domain object is not persistent");
        }
    }

    ////////////////////////////////////////////////////////////
    // JUnit @Rule integration
    ////////////////////////////////////////////////////////////

    @Override
    public Statement apply(final Statement base, final Description description) {
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


    
    public void beginTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();

        if(transaction == null) {
            startTransactionForUser(transactionManager);
            return;
        } 

        final State state = transaction.getState();
        switch(state) {
            case COMMITTED:
            case ABORTED:
                startTransactionForUser(transactionManager);
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

    private void startTransactionForUser(IsisTransactionManager transactionManager) {
        transactionManager.startTransaction();

        // specify that this command (if any) is being executed by a 'USER'
        final CommandContext commandContext = getService(CommandContext.class);
        Command command;
        if (commandContext != null) {
            command = commandContext.getCommand();
            command.setExecutor(Command.Executor.USER);
        }
    }

    /**
     * Either commits or aborts the transaction, depending on the Transaction's {@link org.apache.isis.core.runtime.system.transaction.IsisTransaction#getState()}
     */
    public void endTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();
        if(transaction == null) {
            Assert.fail("No transaction exists");
            return;
        }

        transactionManager.endTransaction();

        final State state = transaction.getState();
        switch(state) {
            case COMMITTED:
                break;
            case ABORTED:
                break;
            case IN_PROGRESS:
                Assert.fail("Transaction is still in state of '" + state + "'");
                break;
            case MUST_ABORT:
                Assert.fail("Transaction is still in state of '" + state + "'");
                break;
            default:
                Assert.fail("Unknown transaction state '" + state + "'");
        }
    }

    /**
     * Commits the transaction.
     * 
     * @deprecated - typically use just {@link #endTran()}
     */
    @Deprecated
    public void commitTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
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

    /**
     * Commits the transaction.
     *
     * @deprecated - typically use just {@link #abortTran()}
     */
    @Deprecated
    public void abortTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
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


    /* (non-Javadoc)
     * @see org.apache.isis.core.integtestsupport.ServiceProvider#getService(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if(serviceClass == DomainObjectContainer.class) {
            return (T) getContainer();
        }
        final ServicesInjectorSpi servicesInjector = getPersistenceSession().getServicesInjector();
        final T service = servicesInjector.lookupService(serviceClass);
        if(service == null) {
            throw new RuntimeException("Could not find a service of type: " + serviceClass.getName());
        }
        return service;
    }

    @Override
    public <T> void replaceService(final T originalService, final T replacementService) {
        final ServicesInjectorSpi servicesInjector = getPersistenceSession().getServicesInjector();
        servicesInjector.replaceService(originalService, replacementService);
    }


    ////////////////////////////////////////////////////////////
    // Fixture management 
    // (for each test, rather than at bootstrap)
    ////////////////////////////////////////////////////////////

    
    public void installFixtures(final InstallableFixture... fixtures) {
        final FixturesInstallerDelegate fid = new FixturesInstallerDelegate(getPersistenceSession());
        for (InstallableFixture fixture : fixtures) {
            fid.addFixture(fixture);
        }
        fid.installFixtures();

        // ensure that tests are performed in separate xactn to any fixture setup.
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();
        final State transactionState = transaction.getState();
        if(transactionState.canCommit()) {
            commitTran();
            try {
                bounceSystem();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            beginTran();
        }
    }


    ////////////////////////////////////////////////////////////
    // Dependencies
    ////////////////////////////////////////////////////////////
    
    protected IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
    
    public PersistenceSession getPersistor() {
    	return getPersistenceSession();
    }
    
    public AdapterManager getAdapterManager() {
        return getPersistor().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * @param container the container to set
     */
    public void setContainer(DomainObjectContainer container) {
        this.container = container;
    }

    
}
