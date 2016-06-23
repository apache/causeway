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

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction.State;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.security.authentication.AuthenticationRequestNameOnly;
import org.apache.isis.core.specsupport.scenarios.DomainServiceProvider;

import static org.junit.Assert.fail;

/**
 * Wraps a plain {@link IsisSessionFactoryBuilder}, and provides a number of features to assist with testing.
 */
public class IsisSystemForTest implements org.junit.rules.TestRule, DomainServiceProvider {

    //region > Listener, ListenerAdapter
    public interface Listener {

        void init(IsisConfiguration configuration) throws Exception;
        
        void preOpenSession(boolean firstTime) throws Exception;
        void postOpenSession(boolean firstTime) throws Exception;
        
        void preNextSession() throws Exception;
        void postNextSession() throws Exception;

        void preCloseSession() throws Exception;
        void postCloseSession() throws Exception;
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
        public void preOpenSession(boolean firstTime) throws Exception {
        }

        @Override
        public void postOpenSession(boolean firstTime) throws Exception {
        }

        @Override
        public void preNextSession() throws Exception {
        }

        @Override
        public void postNextSession() throws Exception {
        }

        @Override
        public void preCloseSession() throws Exception {
        }

        @Override
        public void postCloseSession() throws Exception {
        }
    }

    //endregion

    //region > getElseNull, get, set

    private static ThreadLocal<IsisSystemForTest> ISFT = new ThreadLocal<>();

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

        public Builder withLoggingAt(org.apache.log4j.Level level) {
            this.level = level;
            return this;
        }

        public IsisSystemForTest build() {
            final IsisSystemForTest isisSystemForTest =
                    new IsisSystemForTest(
                            appManifestIfAny,
                            configuration,
                            authenticationRequest,
                            listeners);
            if(level != null) {
                isisSystemForTest.setLevel(level);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public synchronized void run() {
                    try {
                        isisSystemForTest.closeSession();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if(isisSystemForTest.isisSessionFactory != null) {
                            isisSystemForTest.isisSessionFactory.destroyServicesAndShutdown();
                        }
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

    private final AuthenticationRequest authenticationRequestIfAny;
    private AuthenticationSession authenticationSession;


    private IsisSystemForTest(
            final AppManifest appManifestIfAny,
            final IsisConfiguration configurationOverride,
            final AuthenticationRequest authenticationRequestIfAny,
            final List<Listener> listeners) {
        this.appManifestIfAny = appManifestIfAny;
        this.configurationOverride = configurationOverride;
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
            initIfRequiredThenOpenSession(FireListeners.FIRE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void initIfRequiredThenOpenSession(FireListeners fireListeners) throws Exception {

        // exit as quickly as possible for this case...
        final MetaModelInvalidException mmie = IsisContext.getMetaModelInvalidExceptionIfAny();
        if(mmie != null) {
            final Set<String> validationErrors = mmie.getValidationErrors();
            final String validationMsg = Joiner.on("\n").join(validationErrors);
            fail(validationMsg);
            return;
        }

        boolean firstTime = isisSessionFactory == null;
        if(fireListeners.shouldFire()) {
            fireInitAndPreOpenSession(firstTime);
        }

        if(firstTime) {
            IsisLoggingConfigurer isisLoggingConfigurer = new IsisLoggingConfigurer(getLevel());
            isisLoggingConfigurer.configureLogging(".", new String[] {});

            componentProvider = new IsisComponentProviderDefault(
                    appManifestIfAny,
                    configurationOverride
            );

            final IsisSessionFactoryBuilder isisSessionFactoryBuilder = new IsisSessionFactoryBuilder(componentProvider, DeploymentCategory.PRODUCTION);

            // ensures that a FixtureClock is installed as the singleton underpinning the ClockService
            FixtureClock.initialize();

            isisSessionFactory = isisSessionFactoryBuilder.buildSessionFactory();
            // REVIEW: does no harm, but is this required?
            closeSession();

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

        final AuthenticationManager authenticationManager = isisSessionFactory.getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequestIfAny);

        openSession();

        if(fireListeners.shouldFire()) {
            firePostOpenSession(firstTime);
        }
    }

    public DomainObjectContainer getContainer() {
        return getService(DomainObjectContainer.class);
    }

    //endregion

    //region > isisSystem (populated during setup)
    private IsisSessionFactory isisSessionFactory;

    /**
     * The {@link IsisSessionFactory} created during {@link #setUpSystem()}.
     */
    public IsisSessionFactory getIsisSessionFactory() {
        return isisSessionFactory;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    //endregion

    //region > teardown

    private void closeSession(final FireListeners fireListeners) throws Exception {
        if(fireListeners.shouldFire()) {
            firePreCloseSession();
        }
        if(isisSessionFactory.inSession()) {
            isisSessionFactory.closeSession();
        }
        if(fireListeners.shouldFire()) {
            firePostCloseSession();
        }
    }

    public void nextSession() throws Exception {
        firePreNextSession();
        closeSession();
        openSession();
        firePostNextSession();
    }

    //endregion

    //region > openSession, closeSession
    public void openSession() throws Exception {
        openSession(authenticationSession);

    }

    public void openSession(AuthenticationSession authenticationSession) throws Exception {
        isisSessionFactory.openSession(authenticationSession);
    }

    public void closeSession() throws Exception {
        closeSession(FireListeners.FIRE);
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


    private void fireInitAndPreOpenSession(boolean firstTime) throws Exception {
        if(firstTime) {
            for(Listener listener: listeners) {
                listener.init(componentProvider.getConfiguration());
            }
        }
        for(Listener listener: listeners) {
            listener.preOpenSession(firstTime);
        }
    }

    private void firePostOpenSession(boolean firstTime) throws Exception {
        for(Listener listener: listeners) {
            listener.postOpenSession(firstTime);
        }
    }

    private void firePreCloseSession() throws Exception {
        for(Listener listener: listeners) {
            listener.preCloseSession();
        }
    }

    private void firePostCloseSession() throws Exception {
        for(Listener listener: listeners) {
            listener.postCloseSession();
        }
    }

    private void firePreNextSession() throws Exception {
        for(Listener listener: listeners) {
            listener.preNextSession();
        }
    }

    private void firePostNextSession() throws Exception {
        for(Listener listener: listeners) {
            listener.postNextSession();
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
                    closeSession();
                } catch(Throwable ex) {
                    try {
                        closeSession();
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

    /**
     * @deprecated - ought to be using regular domain services rather than reaching into the framework
     */
    @Deprecated
    public void beginTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();

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
     *
     * @deprecated - ought to be using regular domain services rather than reaching into the framework
     */
    @Deprecated
    public void endTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
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
     * @deprecated - ought to be using regular domain services rather than reaching into the framework
     */
    @Deprecated
    public void commitTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
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
     * Aborts the transaction.
     *
     * @deprecated - ought to be using regular domain services rather than reaching into the framework
     */
    @Deprecated
    public void abortTran() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
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
        final ServicesInjector servicesInjector = isisSessionFactory.getServicesInjector();
        return servicesInjector.lookupServiceElseFail(serviceClass);
    }

    @Override
    public <T> void replaceService(final T originalService, final T replacementService) {
        final ServicesInjector servicesInjector = isisSessionFactory.getServicesInjector();
        servicesInjector.replaceService(originalService, replacementService);
    }

    //endregion

    //region > Fixture management (for each test, rather than at bootstrap)

    /**
     * @deprecated - use {@link org.apache.isis.applib.fixturescripts.FixtureScripts} domain service instead.
     */
    @Deprecated
    public void installFixtures(final InstallableFixture... fixtures) {
        final FixturesInstallerDelegate fid = new FixturesInstallerDelegate(isisSessionFactory);
        for (final InstallableFixture fixture : fixtures) {
            fid.addFixture(fixture);
        }
        fid.installFixtures();

        // ensure that tests are performed in separate xactn to any fixture setup.
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
        final State transactionState = transaction.getState();
        if(transactionState.canCommit()) {
            commitTran();
            try {
                nextSession();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            beginTran();
        }
    }

    //endregion

    //region > Dependencies

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
    
    private PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    //endregion

}
