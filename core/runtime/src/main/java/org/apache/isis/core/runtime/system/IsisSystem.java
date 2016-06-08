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

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
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
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProviderDefault2;

public class IsisSystem implements ApplicationScopedComponent {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystem.class);

    public static final String MSG_ARE_YOU_SURE = "Are you sure?";
    public static final String MSG_CONFIRM = "Confirm";
    public static final String MSG_CANCEL = "Cancel";


    private boolean initialized = false;

    private ServiceInitializer serviceInitializer;
    private IsisSessionFactory sessionFactory;

    //region > constructors, fields

    private final IsisComponentProvider componentProvider;
    private final DeploymentCategory deploymentCategory;

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;

    public IsisSystem(final AppManifest manifest) {
        this(new IsisComponentProviderDefault2(
                manifest, null, null, null, null), DeploymentCategory.PRODUCTION);
    }

    public IsisSystem(final IsisComponentProvider componentProvider, final DeploymentCategory deploymentCategory) {

        this.componentProvider = componentProvider;
        this.deploymentCategory = deploymentCategory;

        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
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
        }
        initialized = true;


        LOG.info("initialising Isis System");
        LOG.info("working directory: " + new File(".").getAbsolutePath());

        final IsisConfigurationDefault configuration = componentProvider.getConfiguration();
        LOG.info("resource stream source: " + configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (!getDeploymentCategory().isProduction() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        try {

            // services
            ServicesInjector servicesInjector = componentProvider.provideServiceInjector(configuration);
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
                    componentProvider.provideSpecificationLoader(deploymentCategory, servicesInjector, metaModelRefiners);
            servicesInjector.addFallbackIfRequired(SpecificationLoader.class, specificationLoader);

            // persistenceSessionFactory
            final PersistenceSessionFactory persistenceSessionFactory =
                    componentProvider.providePersistenceSessionFactory(deploymentCategory, servicesInjector);
            servicesInjector.addFallbackIfRequired(PersistenceSessionFactory.class, persistenceSessionFactory);

            // wire up components and components into services...
            for (Object service : servicesInjector.getRegisteredServices()) {
                // inject itself into each service (if implements ServiceInjectorAware).
                servicesInjector.injectInto(service);
            }

            // instantiate
            sessionFactory = new IsisSessionFactory(deploymentCategory, servicesInjector);
            IsisContext.set(sessionFactory);

            try {
                // first, initial metamodel (may throw exception if invalid)
                specificationLoader.init();
                specificationLoader.validateAndAssert();


                //
                // remaining functionality only done if metamodel is valid.
                //
                authenticationManager.init();
                authorizationManager.init();

                persistenceSessionFactory.init();

                // do postConstruct.  We store the initializer to do preDestroy on shutdown
                serviceInitializer = new ServiceInitializer(configuration, servicesInjector.getRegisteredServices());
                serviceInitializer.validate();

                postConstructInSession(serviceInitializer);

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

    private void postConstructInSession(final ServiceInitializer serviceInitializer) {
        IsisContext.openSession(new InitialisationSession());
        try {
            IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();
            transactionManager.startTransaction();
            try {
                serviceInitializer.postConstruct();
            } catch(RuntimeException ex) {
                transactionManager.getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
    }

    private void installFixturesIfRequired() throws IsisSystemException {

        final FixturesInstaller fixtureInstaller = componentProvider.provideFixturesInstaller();

        IsisContext.openSession(new InitialisationSession());
        fixtureInstaller.installFixtures();
        try {

            // only allow logon fixtures if not in production mode.
            if (!getDeploymentCategory().isProduction()) {
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
        final ServicesInjector servicesInjector = getServicesInjector();

        IsisContext.openSession(new InitialisationSession());
        try {
            final List<Object> services = servicesInjector.getRegisteredServices();
            final TitleService titleService = servicesInjector.lookupServiceElseFail(TitleService.class);
            for (Object service : services) {
                final String unused = titleService.titleOf(service);
            }
            for (final ObjectSpecification objSpec : servicesInjector.getSpecificationLoader().allSpecifications()) {
                final Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass.isEnum()) {
                    final Object[] enumConstants = correspondingClass.getEnumConstants();
                    for (Object enumConstant : enumConstants) {
                        final String unused = titleService.titleOf(enumConstant);
                    }
                }
            }

            // as used by the Wicket UI
            final TranslationService translationService = servicesInjector.lookupServiceElseFail(TranslationService.class);

            final String context = IsisSystem.class.getName();
            translationService.translate(context, MSG_ARE_YOU_SURE);
            translationService.translate(context, MSG_CONFIRM);
            translationService.translate(context, MSG_CANCEL);

        } finally {
            IsisContext.closeSession();
        }
    }

    private ServicesInjector getServicesInjector() {
        return sessionFactory.getServicesInjector();
    }

    //endregion

    //region > shutdown

    public void shutdown() {
        LOG.info("shutting down system");

        preDestroyInSession(this.serviceInitializer);

    }

    private void preDestroyInSession(final ServiceInitializer serviceInitializer) {

        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PreDestroy (in a session)
        IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();

        IsisContext.openSession(new InitialisationSession());
        try {
            transactionManager.startTransaction();
            try {

                serviceInitializer.preDestroy();

            } catch (RuntimeException ex) {
                transactionManager.getTransaction().setAbortCause(
                        new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }
        } finally {
            IsisContext.closeSession();
        }
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

    IsisTransactionManager getCurrentSessionTransactionManager() {
        return sessionFactory.getCurrentSession().getPersistenceSession().getTransactionManager();
    }

    //endregion

}
