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

package org.apache.isis.core.runtime.system.session;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.services.view.ConfigurationViewServiceDefault;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactoryMetamodelRefiner;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;
import org.apache.isis.schema.utils.ChangesDtoUtils;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;


public class IsisSessionFactoryBuilder {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSessionFactoryBuilder.class);

    private boolean initialized = false;

    // -- constructors, accessors

    private final IsisComponentProvider componentProvider;
    private final AppManifest appManifest;

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;

    public IsisSessionFactoryBuilder(final IsisComponentProvider componentProvider) {

        this.componentProvider = componentProvider;
        this.appManifest = componentProvider.getAppManifest();

        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();
    }

    public AppManifest getAppManifest() {
        return appManifest;
    }

    // -- buildSessionFactory

    public IsisSessionFactory buildSessionFactory() {

        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }
        initialized = true;


        LOG.info("initialising Isis System");
        LOG.info("working directory: {}", new File(".").getAbsolutePath());

        final IsisConfiguration configuration = _Config.getConfiguration();
        LOG.info("resource stream source: {}", configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (_Context.isPrototyping() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        IsisSessionFactory isisSessionFactory;
        try {

            // everything added to ServicesInjector will be able to @javax.inject.Inject'ed
            // the IsisSessionFactory will look up each of these components from the ServicesInjector

            final ServicesInjector servicesInjector = componentProvider.provideServiceInjector();

            // ConfigurationService
            servicesInjector.addFallbackIfRequired(ConfigurationViewService.class, new ConfigurationViewServiceDefault());
            
            // fixtureScripts
            servicesInjector.addFallbackIfRequired(FixtureScripts.class, new FixtureScriptsDefault());

            // authentication
            final AuthenticationManager authenticationManager = componentProvider.provideAuthenticationManager();
            servicesInjector.addFallbackIfRequired(AuthenticationManager.class, authenticationManager);

            // authorization
            final AuthorizationManager authorizationManager = componentProvider.provideAuthorizationManager();
            servicesInjector.addFallbackIfRequired(AuthorizationManager.class, authorizationManager);

            // specificationLoader
            final Collection<MetaModelRefiner> metaModelRefiners = refiners(
                    authenticationManager, authorizationManager, new PersistenceSessionFactoryMetamodelRefiner());
            final SpecificationLoader specificationLoader =
                    componentProvider.provideSpecificationLoader(servicesInjector, metaModelRefiners);
            servicesInjector.addFallbackIfRequired(SpecificationLoader.class, specificationLoader);

            // persistenceSessionFactory
            final PersistenceSessionFactory persistenceSessionFactory = PersistenceSessionFactory.get(/*configuration*/);
            servicesInjector.addFallbackIfRequired(PersistenceSessionFactory.class, persistenceSessionFactory);


            servicesInjector.validateServices();

            // instantiate the IsisSessionFactory
            isisSessionFactory = new IsisSessionFactory(servicesInjector, appManifest);

            // now, add the IsisSessionFactory itself into ServicesInjector, so it can be @javax.inject.Inject'd
            // into any internal domain services
            servicesInjector.addFallbackIfRequired(IsisSessionFactory.class, isisSessionFactory);



            // finally, wire up components and components into services...
            servicesInjector.autowire();

            // ... and make IsisSessionFactory available via the IsisContext static for those places where we cannot
            // yet inject.

            _Context.putSingleton(IsisSessionFactory.class, isisSessionFactory);

            // execute tasks using a threadpool
            final List<Future<Object>> futures = ThreadPoolSupport.getInstance().invokeAll(
                    new Callable<Object>() {
                        @Override
                        public Object call() {

                            // time to initialize...
                            specificationLoader.init();

                            // we need to do this before checking if the metamodel is valid.
                            //
                            // eg ActionChoicesForCollectionParameterFacetFactory metamodel validator requires a runtime...
                            // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionContributee.getServiceAdapter(ObjectActionContributee.java:287)
                            // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionContributee.determineParameters(ObjectActionContributee.java:138)
                            // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionDefault.getParameters(ObjectActionDefault.java:182)
                            // at o.a.i.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory$1.validate(ActionChoicesForCollectionParameterFacetFactory.java:85)
                            // at o.a.i.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory$1.visit(ActionChoicesForCollectionParameterFacetFactory.java:76)
                            // at o.a.i.core.metamodel.specloader.validator.MetaModelValidatorVisiting.validate(MetaModelValidatorVisiting.java:47)
                            //
                            // also, required so that can still call isisSessionFactory#doInSession
                            //
                            // eg todoapp has a custom UserSettingsThemeProvider that is called when rendering any page
                            // (including the metamodel invalid page)
                            // at o.a.i.core.runtime.system.session.IsisSessionFactory.doInSession(IsisSessionFactory.java:327)
                            // at todoapp.webapp.UserSettingsThemeProvider.getActiveTheme(UserSettingsThemeProvider.java:36)

                            authenticationManager.init();
                            authorizationManager.init();

                            return null;
                        }
                        public String toString() {
                            return "SpecificationLoader#init()";
                        }

                    },
                    new Callable<Object>() {
                        @Override public Object call() {
                            persistenceSessionFactory.init();
                            return null;
                        }
                        public String toString() {
                            return "persistenceSessionFactory#init()";
                        }
                    },
                    new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            ChangesDtoUtils.init();
                            return null;
                        }
                        public String toString() {
                            return "ChangesDtoUtils.init()";
                        }
                    },
                    new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            InteractionDtoUtils.init();
                            return null;
                        }
                        public String toString() {
                            return "InteractionDtoUtils.init()";
                        }
                    },
                    new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            CommandDtoUtils.init();
                            return null;
                        }
                        public String toString() {
                            return "CommandDtoUtils.init()";
                        }
                    }
                ); 


            // wait on this thread for tasks to complete
            ThreadPoolSupport.getInstance().joinGatherFailures(futures);
            specificationLoader.postProcess();

            persistenceSessionFactory.catalogNamedQueries(specificationLoader);

            isisSessionFactory.constructServices();

            isisSessionFactory.doInSession(
                    () -> {
                        try {
                            specificationLoader.validateAndAssert();

                        } catch (final MetaModelInvalidException ex) {
                            // no need to use a higher level, such as error(...); the calling code will expose any metamodel
                            // validation errors in their own particular way.
                            if(LOG.isDebugEnabled()) {
                                LOG.debug("Meta model invalid", ex);
                            }
                            _Context.putSingleton(MetaModelInvalidException.class, ex);
                        }
                    }
                    );


        } catch (final IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            throw new RuntimeException(ex);
        }

        return isisSessionFactory;
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }

    // region > metaModel validity
    public boolean isMetaModelValid() {
        return IsisContext.getMetaModelInvalidExceptionIfAny() == null;
    }

}
