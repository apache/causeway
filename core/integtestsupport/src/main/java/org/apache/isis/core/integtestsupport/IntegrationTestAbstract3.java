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

import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

import org.apache.log4j.PropertyConfigurator;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.clock.TickingFixtureClock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.metamodel.MetaModelService4;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.integtestsupport.logging.LogConfig;
import org.apache.isis.core.integtestsupport.logging.LogStream;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.objectstore.jdo.datanucleus.IsisConfigurationForJdoIntegTests;

/**
 * Reworked base class for integration tests, uses a {@link Module} to bootstrap, rather than an {@link AppManifest}.
 */
public abstract class IntegrationTestAbstract3 {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestAbstract3.class);
    private final LogConfig logConfig;

    protected static PrintStream logPrintStream() {
        return logPrintStream(Level.DEBUG);
    }
    protected static PrintStream logPrintStream(Level level) {
        return LogStream.logPrintStream(LOG, level);
    }

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    /**
     * this is asymmetric - handles only the teardown of the transaction afterwards, not the initial set up
     * (which is done instead by the @Before, so that can also bootstrap system the very first time)
     */
    @Rule
    public IntegrationTestAbstract3.IsisTransactionRule isisTransactionRule = new IntegrationTestAbstract3.IsisTransactionRule();

    private final static ThreadLocal<Boolean> setupLogging = new ThreadLocal<Boolean>() {{
        set(false);
    }};

    protected final Module module;
    private final Class[] additionalModuleClasses;

    public IntegrationTestAbstract3(final Module module, final Class... additionalModuleClasses) {
        this(new LogConfig(Level.INFO), module, additionalModuleClasses);
    }

    private Long t0;
    public IntegrationTestAbstract3(
            final LogConfig logConfig,
            final Module module, final Class... additionalModuleClasses) {
        this.logConfig = logConfig;
        final boolean firstTime = !setupLogging.get();
        if(firstTime) {
            PropertyConfigurator.configure(logConfig.getLoggingPropertyFile());
            System.setOut(logConfig.getFixtureTracing());
            setupLogging.set(true);
            t0 = System.currentTimeMillis();
        }

        final String moduleFqcn = System.getProperty("isis.integTest.module");

        if(!Strings.isNullOrEmpty(moduleFqcn)) {
            this.module = InstanceUtil.createInstance(moduleFqcn, Module.class);
            this.additionalModuleClasses = new Class<?>[] { };
        } else {
            this.module = module;
            this.additionalModuleClasses = additionalModuleClasses;
        }
    }

    private void log(final String message) {
        switch (logConfig.getTestLoggingLevel()) {
        case ERROR:
            LOG.error(message);
            break;
        case WARN:
            LOG.warn(message);
            break;
        case INFO:
            LOG.info(message);
            break;
        case DEBUG:
            LOG.debug(message);
            break;
        case TRACE:
            LOG.trace(message);
            break;
        }
    }

    private LocalDate timeBeforeTest;

    @Before
    public void bootstrapAndSetupIfRequired() {

        System.setProperty("isis.integTest", "true");

        bootstrapIfRequired();

        if(t0 != null) {
            long t1 = System.currentTimeMillis();
            log("##########################################################################");
            log("# Bootstrapped in " + (t1- t0) + " millis");
            log("##########################################################################");
        }
        log("### TEST: " + this.getClass().getCanonicalName());

        beginTransaction();

        timeBeforeTest = Clock.getTimeAsLocalDate();

        setupModuleRefData();
    }

    private void bootstrapIfRequired() {

        final AppManifestAbstract2.Builder2 builder =
                AppManifestAbstract2.Builder2.forModule(module);
        builder.withAdditionalModules(additionalModuleClasses); // eg fake module, as passed into constructor

        final AppManifestAbstract2 appManifest = (AppManifestAbstract2) builder.build();

        bootstrapUsing(appManifest);
    }

    /**
     * The {@link AppManifest} used to bootstrap the {@link IsisSystemForTest} (on the thread-local)
     */
    private static ThreadLocal<AppManifest> isftAppManifest = new ThreadLocal<>();

    private void bootstrapUsing(AppManifest appManifest) {

        final SystemState systemState = determineSystemState(appManifest);
        switch (systemState) {

        case BOOTSTRAPPED_SAME_MODULES:
            // nothing to do
            break;
        case BOOTSTRAPPED_DIFFERENT_MODULES:
            // TODO: this doesn't work correctly yet;
            teardownSystem();
            setupSystem(appManifest);
            break;
        case NOT_BOOTSTRAPPED:
            setupSystem(appManifest);
            TickingFixtureClock.replaceExisting();
            break;
        }
    }

    private static void teardownSystem() {
        final IsisSessionFactory isisSessionFactory = IsisSystemForTest.get().getService(IsisSessionFactory.class);

        // TODO: this ought to be part of isisSessionFactory's responsibilities
        final IsisJdoSupport isisJdoSupport = isisSessionFactory.getServicesInjector()
                .lookupService(IsisJdoSupport.class);
        final PersistenceManagerFactory pmf =
                isisJdoSupport.getJdoPersistenceManager().getPersistenceManagerFactory();
        isisSessionFactory.destroyServicesAndShutdown();
        pmf.close();

        IsisContext.testReset();
    }

    private static void setupSystem(final AppManifest appManifest) {

        final IsisConfigurationForJdoIntegTests configuration = new IsisConfigurationForJdoIntegTests();
        configuration.putDataNucleusProperty("javax.jdo.option.ConnectionURL","jdbc:hsqldb:mem:test-" + UUID.randomUUID().toString());
        final IsisSystemForTest.Builder isftBuilder =
                new IsisSystemForTest.Builder()
                        .withLoggingAt(org.apache.log4j.Level.INFO)
                        .with(appManifest)
                        .with(configuration);

        IsisSystemForTest isft = isftBuilder.build();
        isft.setUpSystem();

        // save both the system and the manifest
        // used to bootstrap the system onto thread-loca
        IsisSystemForTest.set(isft);
        isftAppManifest.set(appManifest);
    }

    enum SystemState {
        NOT_BOOTSTRAPPED,
        BOOTSTRAPPED_SAME_MODULES,
        BOOTSTRAPPED_DIFFERENT_MODULES
    }

    private static SystemState determineSystemState(final AppManifest appManifest) {
        IsisSystemForTest isft = IsisSystemForTest.getElseNull();
        if (isft == null)
            return SystemState.NOT_BOOTSTRAPPED;

        final AppManifest appManifestFromPreviously = isftAppManifest.get();
        return haveSameModules(appManifest, appManifestFromPreviously)
                ? SystemState.BOOTSTRAPPED_SAME_MODULES
                : SystemState.BOOTSTRAPPED_DIFFERENT_MODULES;
    }

    static boolean haveSameModules(
            final AppManifest m1,
            final AppManifest m2) {
        final List<Class<?>> m1Modules = m1.getModules();
        final List<Class<?>> m2Modules = m2.getModules();
        return m1Modules.containsAll(m2Modules) && m2Modules.containsAll(m1Modules);
    }

    private static class IsisTransactionRule implements MethodRule {

        @Override
        public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {

            return new Statement() {
                @Override
                public void evaluate() throws Throwable {

                    // we don't set up the ISFT, because the very first time it won't be there.
                    // Instead we expect it to be bootstrapped via @Before
                    try {
                        base.evaluate();
                        final IsisSystemForTest isft = IsisSystemForTest.get();
                        isft.endTran();
                    } catch(final Throwable e) {
                        // determine if underlying cause is an applib-defined exception,
                        final RecoverableException recoverableException =
                                determineIfRecoverableException(e);
                        final NonRecoverableException nonRecoverableException =
                                determineIfNonRecoverableException(e);

                        if(recoverableException != null) {
                            try {
                                final IsisSystemForTest isft = IsisSystemForTest.get();
                                isft.getContainer().flush(); // don't care if npe
                                isft.getService(IsisJdoSupport.class).getJdoPersistenceManager().flush();
                            } catch (Exception ignore) {
                                // ignore
                            }
                        }
                        // attempt to close this
                        try {
                            final IsisSystemForTest isft = IsisSystemForTest.getElseNull();
                            isft.closeSession(); // don't care if npe
                        } catch(Exception ignore) {
                            // ignore
                        }

                        // attempt to start another
                        try {
                            final IsisSystemForTest isft = IsisSystemForTest.getElseNull();
                            isft.openSession(); // don't care if npe
                        } catch(Exception ignore) {
                            // ignore
                        }


                        // if underlying cause is an applib-defined, then
                        // throw that rather than Isis' wrapper exception
                        if(recoverableException != null) {
                            throw recoverableException;
                        }
                        if(nonRecoverableException != null) {
                            throw nonRecoverableException;
                        }

                        // report on the error that caused
                        // a problem for *this* test
                        throw e;
                    }
                }

                NonRecoverableException determineIfNonRecoverableException(final Throwable e) {
                    NonRecoverableException nonRecoverableException = null;
                    final List<Throwable> causalChain2 = Throwables.getCausalChain(e);
                    for (final Throwable cause : causalChain2) {
                        if(cause instanceof NonRecoverableException) {
                            nonRecoverableException = (NonRecoverableException) cause;
                            break;
                        }
                    }
                    return nonRecoverableException;
                }

                RecoverableException determineIfRecoverableException(final Throwable e) {
                    RecoverableException recoverableException = null;
                    final List<Throwable> causalChain = Throwables.getCausalChain(e);
                    for (final Throwable cause : causalChain) {
                        if(cause instanceof RecoverableException) {
                            recoverableException = (RecoverableException) cause;
                            break;
                        }
                    }
                    return recoverableException;
                }
            };
        }
    }

    private void beginTransaction() {
        final IsisSystemForTest isft = IsisSystemForTest.get();

        isft.getContainer().injectServicesInto(this);
        isft.beginTran();
    }

    @Inject
    MetaModelService4 metaModelService4;

    protected void setupModuleRefData() {
        FixtureScript refDataSetupFixture = metaModelService4.getAppManifest2().getRefDataSetupFixture();
        runFixtureScript(refDataSetupFixture);
    }

    @After
    public void tearDownAllModules() {

        final boolean testHealthy = transactionService != null;
        if(!testHealthy) {
            // avoid throwing an NPE here if something unexpected has occurred...
            return;
        }

        transactionService.nextTransaction();

        FixtureScript fixtureScript = metaModelService4.getAppManifest2().getTeardownFixture();
        runFixtureScript(fixtureScript);

        // reinstate clock
        setFixtureClockDate(timeBeforeTest);
    }

    protected void runFixtureScript(final FixtureScript... fixtureScriptList) {
        if (fixtureScriptList.length == 1) {
            this.fixtureScripts.runFixtureScript(fixtureScriptList[0], null);
        } else {
            this.fixtureScripts.runFixtureScript(new FixtureScript() {
                protected void execute(ExecutionContext executionContext) {
                    FixtureScript[] fixtureScripts = fixtureScriptList;
                    for (FixtureScript fixtureScript : fixtureScripts) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }, null);
        }

        transactionService.nextTransaction();
    }


    protected <T,F extends BuilderScriptAbstract<T,F>> T runBuilderScript(final F fixture) {

        serviceRegistry.injectServicesInto(fixture);

        fixture.run(null);


        final T object = fixture.getObject();
        transactionService.nextTransaction();

        return object;
    }


    private static Class[] asClasses(final List<Module> dependencies) {
        final List<? extends Class<? extends Module>> dependenciesAsClasses =
                FluentIterable.from(dependencies).transform(new Function<Module, Class<? extends Module>>() {
                    @Nullable @Override public Class apply(@Nullable final Module module) {
                        return module.getClass();
                    }
                }).toList();
        return dependenciesAsClasses.toArray(new Class[] {});
    }

    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T wrap(final T obj) {
        return wrapperFactory.wrap(obj);
    }

    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }


    /**
     * To use instead of {@link #getFixtureClock()}'s {@link FixtureClock#setDate(int, int, int)} ()}.
     */
    protected void setFixtureClockDate(final LocalDate date) {
        setFixtureClockDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    /**
     * To use instead of {@link #getFixtureClock()}'s {@link FixtureClock#setDate(int, int, int)} ()}.
     */
    protected void setFixtureClockDate(final int year, final int month, final int day) {
        final Clock instance = Clock.getInstance();

        if(instance instanceof TickingFixtureClock) {
            TickingFixtureClock.reinstateExisting();
            getFixtureClock().setDate(year, month, day);
            TickingFixtureClock.replaceExisting();
        }

        if(instance instanceof FixtureClock) {
            getFixtureClock().setDate(year, month, day);
        }
    }

    /**
     * If just require the current time, use {@link ClockService}
     */
    private FixtureClock getFixtureClock() {
        return ((FixtureClock)FixtureClock.getInstance());
    }



    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T unwrap(final T obj) {
        return wrapperFactory.unwrap(obj);
    }

    @Inject
    protected FixtureScripts fixtureScripts;
    @Inject
    protected FactoryService factoryService;
    @Inject
    protected ServiceRegistry2 serviceRegistry;
    @Inject
    protected RepositoryService repositoryService;
    @Inject
    protected UserService userService;
    @Inject
    protected WrapperFactory wrapperFactory;
    @Inject
    protected TransactionService transactionService;
    @Inject
    protected SessionManagementService sessionManagementService;

}