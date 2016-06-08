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
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction.State;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.security.authentication.AuthenticationRequestNameOnly;
import org.apache.isis.core.specsupport.scenarios.DomainServiceProvider;

import static org.junit.Assert.fail;

/**
 * Wraps a plain {@link IsisSystem}, and provides a number of features to assist with testing.
 */
public class IsisSystemForTest implements org.junit.rules.TestRule, DomainServiceProvider {

    //region > Listener, ListenerAdapter

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

    //endregion

    //region > getElseNull, get, set

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
    //endregion

    //region > Builder


    public static class Builder {

        private AuthenticationRequest authenticationRequest = new AuthenticationRequestNameOnly("tester");

        private IsisConfigurationDefault configuration = new IsisConfigurationDefault();

        private AppManifest appManifestIfAny;

        private final List<Object> services = Lists.newArrayList();
        private final List<InstallableFixture> fixtures = Lists.newArrayList();

        private final List <Listener> listeners = Lists.newArrayList();

        private org.apache.log4j.Level level;

        public Builder with(IsisConfiguration configuration) {
            this.configuration = (IsisConfigurationDefault) configuration;
            return this;
        }

        public Builder with(AuthenticationRequest authenticationRequest) {
            this.authenticationRequest = authenticationRequest;
            return this;
        }

        public Builder with(AppManifest appManifest) {
            this.appManifestIfAny = appManifest;
            return this;
        }

        /**
         * @deprecated - prefer to use {@link org.apache.isis.applib.fixturescripts.FixtureScript}s API instead.
         */
        @Deprecated
        public Builder withFixtures(InstallableFixture... fixtures) {
            if(appManifestIfAny != null) {
                throw new IllegalStateException("An appManifest has already been provided");
            }
            this.fixtures.addAll(Arrays.asList(fixtures));
            return this;
        }

        public Builder withLoggingAt(org.apache.log4j.Level level) {
            this.level = level;
            return this;
        }

        public IsisSystemForTest build() {
            final IsisSystemForTest isisSystemForTest =
                    new IsisSystemForTest(
                            appManifestIfAny,
                            configuration,
                            fixtures,
                            authenticationRequest,
                            listeners);
            if(level != null) {
                isisSystemForTest.setLevel(level);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public synchronized void run() {
                    try {
                        isisSystemForTest.tearDownSystem();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        isisSystemForTest.shutdown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        IsisContext.shutdown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            return isisSystemForTest;
        }


        public Builder with(Listener listener) {
            if(listener != null) {
                listeners.add(listener);
            }
            return this;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    //endregion

    //region > constructor, fields

    // these fields 'xxxForComponentProvider' are used to initialize the IsisComponentProvider, but shouldn't be used thereafter.
    private final AppManifest appManifestIfAny;
    private final IsisConfiguration configurationOverride;
    private final List<InstallableFixture> fixturesIfAny;

    private final AuthenticationRequest authenticationRequestIfAny;
    private AuthenticationSession authenticationSession;


    private IsisSystemForTest(
            final AppManifest appManifestIfAny,
            final IsisConfiguration configurationOverride,
            final List<InstallableFixture> fixturesIfAny,
            final AuthenticationRequest authenticationRequestIfAny,
            final List<Listener> listeners) {
        this.appManifestIfAny = appManifestIfAny;
        this.configurationOverride = configurationOverride;
        this.fixturesIfAny = fixturesIfAny;
        this.authenticationRequestIfAny = authenticationRequestIfAny;
        this.listeners = listeners;
    }

    //endregion

    //region > level
    private org.apache.log4j.Level level = org.apache.log4j.Level.INFO;

    /**
     * The level to use for the root logger if fallback (ie a <tt>logging.properties</tt> file cannot be found).
     */
    public org.apache.log4j.Level getLevel() {
        return level;
    }
    
    public void setLevel(org.apache.log4j.Level level) {
        this.level = level;
    }

    //endregion

    //region > setup (also componentProvider)

    // populated at #setupSystem
    private IsisComponentProvider componentProvider;

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

        // exit as quickly as possible for this case... (will already have been logged)
        if(IsisContext.getMetaModelInvalidExceptionIfAny() != null) {
            fail("Metamodel invalid");
            return;
        }

        boolean firstTime = isisSystem == null;
        if(fireListeners.shouldFire()) {
            fireInitAndPreSetupSystem(firstTime);
        }

        if(firstTime) {
            IsisLoggingConfigurer isisLoggingConfigurer = new IsisLoggingConfigurer(getLevel());
            isisLoggingConfigurer.configureLogging(".", new String[] {});

            componentProvider = new IsisComponentProviderDefault(
                    appManifestIfAny,
                    fixturesIfAny,
                    configurationOverride
            );

            isisSystem = new IsisSystem(componentProvider, DeploymentCategory.PRODUCTION);

            // ensures that a FixtureClock is installed as the singleton underpinning the ClockService
            FixtureClock.initialize();

            isisSystem.init();
            IsisContext.closeSession();

            // if the IsisSystem does not initialize properly, then - as a side effect - the resulting
            // MetaModelInvalidException will be pushed onto the IsisContext (as a static field).
            final MetaModelInvalidException ex = IsisContext.getMetaModelInvalidExceptionIfAny();
            if (ex != null) {

                // for subsequent tests; the attempt to bootstrap the framework will leave
                // the IsisContext singleton as set.
                IsisContext.testReset();

                final Set<String> validationErrors = ex.getValidationErrors();
                final StringBuilder buf = new StringBuilder();
                for (String validationError : validationErrors) {
                    buf.append(validationError).append("\n");
                }
                fail("Metamodel is invalid: \n" + buf.toString());
            }
        }

        final AuthenticationManager authenticationManager = IsisContext.getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequestIfAny);

        openSession();

        wireAndInstallFixtures();
        if(fireListeners.shouldFire()) {
            firePostSetupSystem(firstTime);
        }
    }

    private void wireAndInstallFixtures() {
        FixturesInstaller fixturesInstaller = componentProvider.provideFixturesInstaller();
        fixturesInstaller.installFixtures();
    }

    public DomainObjectContainer getContainer() {
        for (Object service : IsisContext.getSessionFactory().getServices()) {
            if(service instanceof DomainObjectContainer) {
                return (DomainObjectContainer) service;
            }
        }
        throw new IllegalStateException("Could not locate DomainObjectContainer");
    }

    //endregion

    //region > isisSystem (populated during setup)
    private IsisSystem isisSystem;

    /**
     * The {@link IsisSystem} created during {@link #setUpSystem()}.
     */
    public IsisSystem getIsisSystem() {
        return isisSystem;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    //endregion

    //region > teardown

    /**
     * Intended to be called from a test's {@link After} method.
     */
    public void tearDownSystem() throws Exception {
        closeSession(FireListeners.FIRE);
    }

    private void closeSession(final FireListeners fireListeners) throws Exception {
        if(fireListeners.shouldFire()) {
            firePreTeardownSystem();
        }
        if(IsisContext.inSession()) {
            IsisContext.closeSession();
        }
        if(fireListeners.shouldFire()) {
            firePostTeardownSystem();
        }
    }

    private void shutdown() {
        if(isisSystem != null) {
            isisSystem.shutdown();
        }
    }

    public void bounceSystem() throws Exception {
        firePreBounceSystem();
        closeSession();
        openSession();
        firePostBounceSystem();
    }

    //endregion

    //region > openSession, closeSession
    public void openSession() throws Exception {
        openSession(authenticationSession);

    }

    public void openSession(AuthenticationSession authenticationSession) throws Exception {
        IsisContext.openSession(authenticationSession);
    }

    public void closeSession() throws Exception {
        IsisContext.closeSession();
    }

    //endregion

    //region > listeners

    private List <Listener> listeners;

    private enum FireListeners {
        FIRE,
        DONT_FIRE;
        public boolean shouldFire() {
            return this == FIRE;
        }
    }


    private void fireInitAndPreSetupSystem(boolean firstTime) throws Exception {
        if(firstTime) {
            for(Listener listener: listeners) {
                listener.init(componentProvider.getConfiguration());
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
    //endregion

    //region > Convenience for tests

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

    /**
     * @deprecated - unused by framework
     */
    @Deprecated
    public ObjectAdapter reload(RootOid oid) {
        ensureSessionInProgress();
        final PersistenceSession persistenceSession = getPersistenceSession();
        return persistenceSession.loadObjectInTransaction(oid);
    }

    /**
     * @deprecated - unused by framework
     */
    @Deprecated
    public ObjectAdapter recreateAdapter(RootOid oid) {
        ensureSessionInProgress();
        return getPersistenceSession().adapterFor(oid);
    }

    /**
     * @deprecated - unused by framework
     */
    @Deprecated
    public ObjectAdapter remapAsPersistent(Object pojo, RootOid persistentOid) {
        ensureSessionInProgress();
        ensureObjectIsNotPersistent(pojo);
        final ObjectAdapter adapter = adapterFor(pojo);
        getPersistenceSession().remapAsPersistent(adapter, persistentOid);
        return adapter;
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

    //endregion

    //region > JUnit @Rule integration

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

    //endregion

    //region > beginTran, endTran, commitTran, abortTran

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
                fail("Transaction is in state of '" + state + "'");
                break;
            default:
                fail("Unknown transaction state '" + state + "'");
        }
        
    }

    private void startTransactionForUser(IsisTransactionManager transactionManager) {
        transactionManager.startTransaction();

        // specify that this command (if any) is being executed by a 'USER'
        final CommandContext commandContext = getService(CommandContext.class);
        Command command = commandContext.getCommand();
        command.setExecutor(Command.Executor.USER);
    }

    /**
     * Either commits or aborts the transaction, depending on the Transaction's {@link org.apache.isis.core.runtime.system.transaction.IsisTransaction#getState()}
     */
    public void endTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getTransaction();
        if(transaction == null) {
            fail("No transaction exists");
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
                fail("Transaction is still in state of '" + state + "'");
                break;
            case MUST_ABORT:
                fail("Transaction is still in state of '" + state + "'");
                break;
            default:
                fail("Unknown transaction state '" + state + "'");
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
            fail("No transaction exists");
            return;
        } 
        final State state = transaction.getState();
        switch(state) {
            case COMMITTED:
            case ABORTED:
            case MUST_ABORT:
                fail("Transaction is in state of '" + state + "'");
                break;
            case IN_PROGRESS:
                transactionManager.endTransaction();
                break;
            default:
                fail("Unknown transaction state '" + state + "'");
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
            fail("No transaction exists");
            return;
        } 
        final State state = transaction.getState();
        switch(state) {
            case ABORTED:
                break;
            case COMMITTED:
                fail("Transaction is in state of '" + state + "'");
                break;
            case MUST_ABORT:
            case IN_PROGRESS:
                transactionManager.abortTransaction();
                break;
            default:
                fail("Unknown transaction state '" + state + "'");
        }
    }

    //endregion

    //region > getService, replaceService

    /* (non-Javadoc)
     * @see org.apache.isis.core.integtestsupport.ServiceProvider#getService(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if(serviceClass == DomainObjectContainer.class) {
            return (T) getContainer();
        }
        final ServicesInjector servicesInjector = getPersistenceSession().getServicesInjector();
        final T service = servicesInjector.lookupService(serviceClass);
        if(service == null) {
            throw new RuntimeException("Could not find a service of type: " + serviceClass.getName());
        }
        return service;
    }

    @Override
    public <T> void replaceService(final T originalService, final T replacementService) {
        final ServicesInjector servicesInjector = getPersistenceSession().getServicesInjector();
        servicesInjector.replaceService(originalService, replacementService);
    }

    //endregion

    //region > Fixture management (for each test, rather than at bootstrap)

    /**
     * @deprecated - use {@link org.apache.isis.applib.fixturescripts.FixtureScripts} domain service instead.
     */
    @Deprecated
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

    //endregion

    //region > Dependencies

    protected IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
    
    public PersistenceSession getPersistor() {
    	return getPersistenceSession();
    }

    public AdapterManager getAdapterManager() {
        return getPersistor();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * @param container the container to set
     *
     * @deprecated
     */
    @Deprecated
    public void setContainer(DomainObjectContainer container) {
        // no-op
    }

    //endregion

}
