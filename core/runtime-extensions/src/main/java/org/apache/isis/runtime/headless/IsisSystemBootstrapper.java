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
package org.apache.isis.runtime.headless;

import javax.jdo.PersistenceManagerFactory;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.assertions._Ensure;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.runtime.headless.logging.LeveledLogger;
import org.apache.isis.runtime.headless.logging.LogConfig;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class IsisSystemBootstrapper {

    private final IsisConfiguration isisConfiguration;
    private final LeveledLogger logger;
    
    public static IsisSystemBootstrapper of(LogConfig logConfig, IsisConfiguration isisConfiguration) {
        return new IsisSystemBootstrapper(logConfig, isisConfiguration);
    }
    
    private IsisSystemBootstrapper(
            final LogConfig logConfig,
            final IsisConfiguration isisConfiguration) {
        
        _Ensure.ensure("Should have an IsisConfiguration!", isisConfiguration!=null);

        this.isisConfiguration = isisConfiguration;
        this.logger = new LeveledLogger(log, logConfig.getTestLoggingLevel());
    }

    public IsisSystem bootstrapIfRequired() {
        //FIXME[2112]
        _Exceptions.throwNotImplemented();
        //bootstrapUsingConfig();
        return IsisSystem.get();
    }

    /**
     * Expects a transaction to have been started
     */
    public void setupModuleRefData() {
        MetaModelService metaModelService = lookupService(MetaModelService.class);
        //FIXME[2112]
        _Exceptions.throwNotImplemented();
        //FixtureScript refDataSetupFixture = metaModelService.getAppManifest2().getRefDataSetupFixture();
        //runFixtureScript(refDataSetupFixture);
    }


//    private void bootstrapUsingConfig() {
//
//        final SystemState systemState = null;//FIXME[2112] determineSystemState(isisConfiguration.getAppManifest());
//        switch (systemState) {
//
//        case BOOTSTRAPPED_SAME_MODULES:
//            // nothing to do
//            break;
//        case BOOTSTRAPPED_DIFFERENT_MODULES:
//            // TODO: this doesn't work correctly yet; not tearing down HSQLDB correctly.
//            if(false) {
//                teardownSystem();
//            } else {
//                throw new RuntimeException("Bootstrapping different modules is not yet supported");
//            }
//            // fall through
//        case NOT_BOOTSTRAPPED:
//
//            long t0 = System.currentTimeMillis();
//            setupSystem(isisConfiguration);
//            long t1 = System.currentTimeMillis();
//
//            log("##########################################################################");
//            log("# Bootstrapped in " + (t1- t0) + " millis");
//            log("##########################################################################");
//
//            TickingFixtureClock.replaceExisting();
//
//            break;
//        }
//    }
//
//    private static SystemState determineSystemState(final AppManifest appManifest) {
//        IsisSystem isft = IsisSystem.getElseNull();
//        if (isft == null)
//            return SystemState.NOT_BOOTSTRAPPED;
//
//        final AppManifest appManifestFromPreviously = isftAppManifest.get();
//        return haveSameModules(appManifest, appManifestFromPreviously)
//                ? SystemState.BOOTSTRAPPED_SAME_MODULES
//                        : SystemState.BOOTSTRAPPED_DIFFERENT_MODULES;
//    }
//
//    static boolean haveSameModules(
//            final AppManifest m1,
//            final AppManifest m2) {
//        final List<Class<?>> m1Modules = m1.getModules();
//        final List<Class<?>> m2Modules = m2.getModules();
//        return m1Modules.containsAll(m2Modules) && m2Modules.containsAll(m1Modules);
//    }
//
//    private static IsisSystem setupSystem(IsisConfiguration isisConfiguration) {
//
//        final IsisSystem isft =
//                IsisSystem.ofConfiguration(isisConfiguration);
//
//        isft.setUpSystem();
//
//        // save both the system and the manifest
//        // used to bootstrap the system onto thread-local
//        IsisSystem.set(isft);
//        isftAppManifest.set(isisConfiguration.getAppManifest());
//
//        return isft;
//    }

    public void injectServicesInto(final Object object) {
        lookupService(ServiceInjector.class).injectServicesInto(object);
    }

    enum SystemState {
        NOT_BOOTSTRAPPED,
        BOOTSTRAPPED_SAME_MODULES,
        BOOTSTRAPPED_DIFFERENT_MODULES
    }

    private static void teardownSystem() {
        final IsisSessionFactory isisSessionFactory = lookupService(IsisSessionFactory.class);

        final IsisJdoSupport isisJdoSupport = lookupService(IsisJdoSupport.class);
        final PersistenceManagerFactory pmf =
                isisJdoSupport.getJdoPersistenceManager().getPersistenceManagerFactory();
        isisSessionFactory.destroyServicesAndShutdown();
        pmf.close();

        IsisContext.clear();
    }

    public void tearDownAllModules() {
        final MetaModelService metaModelService4 = lookupService(MetaModelService.class);

        //FIXME[2112]
        _Exceptions.throwNotImplemented();
//        FixtureScript fixtureScript = metaModelService4.getAppManifest2().getTeardownFixture();
//        runFixtureScript(fixtureScript);
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
        logger.log(message);
    }

}