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
import java.util.UUID;

import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.AppManifestAbstract;
import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.clock.TickingFixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.metamodel.MetaModelService4;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.core.integtestsupport.logging.LogConfig;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.objectstore.jdo.datanucleus.IsisConfigurationForJdoIntegTests;

public class IsisSystemBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(IsisSystemBootstrapper.class);

    /**
     * The {@link AppManifest2} used to bootstrap the {@link IsisSystem} (on the thread-local)
     */
    private static ThreadLocal<AppManifest2> isftAppManifest = new ThreadLocal<>();


    private final LogConfig logConfig;
    private final Module module;
    private final Class[] additionalModuleClasses;

    public IsisSystemBootstrapper(
            final LogConfig logConfig,
            final Module module,
            final Class... additionalModuleClasses) {

        this.logConfig = logConfig;
        this.module = module;
        this.additionalModuleClasses = additionalModuleClasses;
    }

    public void bootstrapIfRequired(final Long t0) {

        final AppManifestAbstract.Builder builder =
                AppManifestAbstract2.Builder.forModule(module);
        builder.withAdditionalModules(additionalModuleClasses); // eg fake module, as passed into constructor

        final AppManifestAbstract2 appManifest = (AppManifestAbstract2) builder.build();

        bootstrapUsing(appManifest, t0);
    }

    /**
     * Expects a transaction to have been started
     */
    public void setupModuleRefData() {
        MetaModelService4 metaModelService4 = lookupService(MetaModelService4.class);
        FixtureScript refDataSetupFixture = metaModelService4.getAppManifest2().getRefDataSetupFixture();
        runFixtureScript(refDataSetupFixture);
    }


    private void bootstrapUsing(AppManifest2 appManifest2, Long t0) {

        final SystemState systemState = determineSystemState(appManifest2);
        switch (systemState) {

        case BOOTSTRAPPED_SAME_MODULES:
            // nothing to do
            break;
        case BOOTSTRAPPED_DIFFERENT_MODULES:
            // TODO: this doesn't work correctly yet; not tearing down HSQLDB correctly.
            teardownSystem();
            // fall through
        case NOT_BOOTSTRAPPED:
            setupSystem(appManifest2);
            TickingFixtureClock.replaceExisting();

            if(t0 != null) {
                long t1 = System.currentTimeMillis();
                log("##########################################################################");
                log("# Bootstrapped in " + (t1- t0) + " millis");
                log("##########################################################################");
            }

            break;
        }
    }

    private static SystemState determineSystemState(final AppManifest appManifest) {
        IsisSystem isft = IsisSystem.getElseNull();
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

    private static IsisSystem setupSystem(final AppManifest2 appManifest2) {

        final IsisConfigurationForJdoIntegTests configuration = new IsisConfigurationForJdoIntegTests();
        configuration.putDataNucleusProperty("javax.jdo.option.ConnectionURL","jdbc:hsqldb:mem:test-" + UUID.randomUUID().toString());
        final IsisSystem.Builder isftBuilder =
                new IsisSystem.Builder()
                        .withLoggingAt(org.apache.log4j.Level.INFO)
                        .with(appManifest2)
                        .with(configuration);

        IsisSystem isft = isftBuilder.build();
        isft.setUpSystem();

        // save both the system and the manifest
        // used to bootstrap the system onto thread-loca
        IsisSystem.set(isft);
        isftAppManifest.set(appManifest2);

        return isft;
    }

    public void injectServicesInto(final Object object) {
        lookupService(ServiceRegistry2.class).injectServicesInto(object);
    }

    enum SystemState {
        NOT_BOOTSTRAPPED,
        BOOTSTRAPPED_SAME_MODULES,
        BOOTSTRAPPED_DIFFERENT_MODULES
    }

    private static void teardownSystem() {
        final IsisSessionFactory isisSessionFactory = lookupService(IsisSessionFactory.class);

        // TODO: this ought to be part of isisSessionFactory's responsibilities
        final IsisJdoSupport isisJdoSupport = lookupService(IsisJdoSupport.class);
        final PersistenceManagerFactory pmf =
                isisJdoSupport.getJdoPersistenceManager().getPersistenceManagerFactory();
        isisSessionFactory.destroyServicesAndShutdown();
        pmf.close();

        IsisContext.testReset();
    }

    public void tearDownAllModules() {
        final MetaModelService4 metaModelService4 = lookupService(MetaModelService4.class);

        FixtureScript fixtureScript = metaModelService4.getAppManifest2().getTeardownFixture();
        runFixtureScript(fixtureScript);
    }


    private void runFixtureScript(final FixtureScript... fixtureScriptList) {
        final FixtureScripts fixtureScripts = lookupService(FixtureScripts.class);
        fixtureScripts.runFixtureScript(fixtureScriptList);
    }


    private static IsisSystem getIsisSystem() {
        return IsisSystem.get();
    }

    private static <T> T lookupService(Class<T> serviceClass) {
        return getIsisSystem().getService(serviceClass);
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

}