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

package org.apache.isis.core.runtime.system;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactoryMetamodelRefiner;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionInternal;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;

public class IsisSystem implements ApplicationScopedComponent {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystem.class);

    public static final String MSG_ARE_YOU_SURE = "Are you sure?";
    public static final String MSG_CONFIRM = "Confirm";
    public static final String MSG_CANCEL = "Cancel";

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;
    private final DeploymentType deploymentType;

    private boolean initialized = false;

    private ServiceInitializer serviceInitializer;
    private FixturesInstaller fixtureInstaller;
    private IsisSessionFactory sessionFactory;

    //region > constructors

    private final IsisComponentProvider componentProvider;

    public IsisSystem(IsisComponentProvider componentProvider) {
        this.deploymentType = componentProvider.getDeploymentType();
        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();

        this.componentProvider = componentProvider;
    }

    //endregion

    //region > deploymentType
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }
    //endregion

    //region > sessionFactory

    /**
     * Populated after {@link #init()}.
     */
    public IsisSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    //endregion

    //region > init


    public void init() {

        if (initialized) {
            throw new IllegalStateException("Already initialized");
        } else {
            initialized = true;
        }

        LOG.info("initialising Isis System");
        LOG.info("working directory: " + new File(".").getAbsolutePath());
        LOG.info("resource stream source: " + getConfiguration().getResourceStreamSource());

        localeInitializer.initLocale(getConfiguration());
        timeZoneInitializer.initTimeZone(getConfiguration());

        // a bit of a workaround, but required if anything in the metamodel (for
        // example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock
        // singleton
        // we do this after loading the services to allow a service to prime a
        // different clock
        // implementation (eg to use an NTP time service).
        if (!deploymentType.isProduction() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        try {

            // configuration
            // TODO: HACKY
            final IsisConfigurationDefault configuration = componentProvider.getConfiguration();

            // services
            ServicesInjector servicesInjector = componentProvider.provideServiceInjector();
            servicesInjector.addFallbackIfRequired(FixtureScripts.class, new FixtureScriptsDefault());
            servicesInjector.addFallbackIfRequired(ConfigurationServiceInternal.class, configuration);
            servicesInjector.validateServices();

            // authentication
            final AuthenticationManager authenticationManager = componentProvider.provideAuthenticationManager();
            servicesInjector.addFallbackIfRequired(AuthenticationManager.class, authenticationManager);

            // authorization
            final AuthorizationManager authorizationManager = componentProvider.provideAuthorizationManager();
            servicesInjector.addFallbackIfRequired(AuthorizationManager.class, authorizationManager);

            // specificationLoader
            final Collection<MetaModelRefiner> metaModelRefiners =
                    refiners(authenticationManager, authorizationManager,
                            new PersistenceSessionFactoryMetamodelRefiner());
            final SpecificationLoader specificationLoader =
                    componentProvider.provideSpecificationLoader(deploymentType, servicesInjector, metaModelRefiners);
            servicesInjector.addFallbackIfRequired(SpecificationLoader.class, specificationLoader);

            // persistenceSessionFactory
            final PersistenceSessionFactory persistenceSessionFactory =
                    componentProvider.providePersistenceSessionFactory(
                            deploymentType, servicesInjector);
            servicesInjector.addFallbackIfRequired(PersistenceSessionFactory.class, persistenceSessionFactory);

            // runtimeContext
            final RuntimeContext runtimeContext = new RuntimeContext(servicesInjector);

            // wire up components and components into services...
            runtimeContext.injectInto(specificationLoader);

            for (Object service : servicesInjector.getRegisteredServices()) {
                runtimeContext.injectInto(service);
            }

            // instantiate
            sessionFactory = new IsisSessionFactory(
                    deploymentType, configuration, servicesInjector, specificationLoader,
                    authenticationManager, authorizationManager, persistenceSessionFactory);

            // temporarily make a configuration available
            // TODO: REVIEW: would rather inject this, or perhaps even the ConfigurationBuilder
            IsisContext.setConfiguration(configuration);

            // set up the "appropriate" IsisContext (usually IsisContextThreadLocal) to hold
            // a reference to the sessionFactory just created
            deploymentType.initContext(sessionFactory);

            specificationLoader.init(runtimeContext);

            try {
                // validate here after all entities have been registered in the persistence session factory
                specificationLoader.validateAndAssert();


                //
                // remaining functionality only done if metamodel is valid.
                //
                authenticationManager.init();
                authorizationManager.init();

                persistenceSessionFactory.init();

                // do postConstruct.  We store the initializer to do preDestroy on shutdown
                this.serviceInitializer = initializeServices();

                installFixturesIfRequired();

                translateServicesAndEnumConstants();

            } catch (final MetaModelInvalidException ex) {
                // no need to use a higher level, such as error(...); the calling code will expose any metamodel
                // validation errors in their own particular way.
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Meta model invalid", ex);
                }
                IsisContext.setMetaModelInvalidException(ex);
            }


        } catch (final IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            throw new RuntimeException(ex);
        }
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }

    /**
     * @see #shutdownServices(ServiceInitializer)
     */
    private ServiceInitializer initializeServices() {

        final List<Object> services = sessionFactory.getServices();

        // validate
        final ServiceInitializer serviceInitializer = new ServiceInitializer();
        serviceInitializer.validate(getConfiguration(), services);

        // call @PostConstruct (in a session)
        IsisContext.openSession(new InitialisationSession());
        try {
            getTransactionManager().startTransaction();
            try {
                serviceInitializer.postConstruct();

                return serviceInitializer;
            } catch(RuntimeException ex) {
                getTransactionManager().getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
                return serviceInitializer;
            } finally {
                // will commit or abort
                getTransactionManager().endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    private void installFixturesIfRequired() throws IsisSystemException {

        fixtureInstaller = componentProvider.provideFixturesInstaller();

        IsisContext.openSession(new InitialisationSession());
        fixtureInstaller.installFixtures();
        try {

            // only allow logon fixtures if not in production mode.
            if (!getDeploymentType().isProduction()) {
                logonFixture = fixtureInstaller.getLogonFixture();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    /**
     * The act of invoking titleOf(...) will cause translations to be requested.
     */
    private void translateServicesAndEnumConstants() {
        IsisContext.openSession(new InitialisationSession());
        try {
            final List<Object> services = sessionFactory.getServices();
            final DomainObjectContainer container = lookupService(DomainObjectContainer.class);
            for (Object service : services) {
                final String unused = container.titleOf(service);
            }
            for (final ObjectSpecification objSpec : allSpecifications()) {
                final Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass.isEnum()) {
                    final Object[] enumConstants = correspondingClass.getEnumConstants();
                    for (Object enumConstant : enumConstants) {
                        final String unused = container.titleOf(enumConstant);
                    }
                }
            }

            // as used by the Wicket UI
            final TranslationService translationService = lookupService(TranslationService.class);
            final String context = IsisSystem.class.getName();
            translationService.translate(context, MSG_ARE_YOU_SURE);
            translationService.translate(context, MSG_CONFIRM);
            translationService.translate(context, MSG_CANCEL);

        } finally {
            IsisContext.closeSession();
        }

    }

    private <T> T lookupService(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }

    private ServicesInjector getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    private PersistenceSessionInternal getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    Collection<ObjectSpecification> allSpecifications() {
        return IsisContext.getSpecificationLoader().allSpecifications();
    }

    //endregion

    //region > shutdown

    public void shutdown() {
        LOG.info("shutting down system");

        shutdownServices(this.serviceInitializer);

        IsisContext.closeAllSessions();
    }

    /**
     * @see #initializeServices()
     */
    private void shutdownServices(final ServiceInitializer serviceInitializer) {

        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PostDestroy (in a session)
        IsisContext.openSession(new InitialisationSession());
        try {
            getTransactionManager().startTransaction();
            try {

                serviceInitializer.preDestroy();

            } catch (RuntimeException ex) {
                getTransactionManager().getTransaction().setAbortCause(
                        new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                getTransactionManager().endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    //endregion

    //region > configuration

    /**
     * Returns a <i>snapshot</i> of the {@link IsisConfiguration configuration}
     * (although once the {@link IsisSystem} is completely initialized, will
     * effectively be immutable).
     */
    public IsisConfiguration getConfiguration() {
        return componentProvider.getConfiguration();
    }
    //endregion

    // region > metaModel validity
    public boolean isMetaModelValid() {
        return IsisContext.getMetaModelInvalidExceptionIfAny() == null;
    }
    //endregion

    //region > logonFixture
    private LogonFixture logonFixture;

    /**
     * The {@link LogonFixture}, if any, obtained by running fixtures.
     *
     * <p>
     * Intended to be used when for {@link DeploymentType#SERVER_EXPLORATION
     * exploration} (instead of an {@link ExplorationSession}) or
     * {@link DeploymentType#SERVER_PROTOTYPE prototype} deployments (saves logging
     * in). Should be <i>ignored</i> in other {@link DeploymentType}s.
     */
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }
    //endregion

    //region > debugging

    IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion

}
