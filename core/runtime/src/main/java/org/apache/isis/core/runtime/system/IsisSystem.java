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
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;

public class IsisSystem implements DebugSelection, ApplicationScopedComponent {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystem.class);

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;
    private final DeploymentType deploymentType;

    private boolean initialized = false;

    private ServiceInitializer serviceInitializer;
    private FixturesInstaller fixtureInstaller;
    private IsisSessionFactory sessionFactory;

    //region > constructors

    private final IsisComponentProvider isisComponentProvider;

    public IsisSystem(IsisComponentProvider isisComponentProvider) {
        this.deploymentType = isisComponentProvider.getDeploymentType();
        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();

        this.isisComponentProvider = isisComponentProvider;
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


    @Override
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

        try {
            sessionFactory = createSessionFactory(deploymentType);

            // temporarily make a configuration available
            // REVIEW: would rather inject this, or perhaps even the
            // ConfigurationBuilder
            IsisContext.setConfiguration(getConfiguration());

            initContext(sessionFactory);
            sessionFactory.init();

            // validate here after all entities have been registered in the persistence session factory
            final SpecificationLoaderSpi specificationLoader = sessionFactory.getSpecificationLoader();
            specificationLoader.validateAndAssert();

            serviceInitializer = initializeServices();

            installFixturesIfRequired();

            translateServicesAndEnumConstants();

        } catch (final IsisSystemException ex) {
            LOG.error("failed to initialise", ex);
            throw new RuntimeException(ex);
        }
    }


    private IsisSessionFactory createSessionFactory(final DeploymentType deploymentType) throws IsisSystemException {

        final IsisConfiguration configuration = isisComponentProvider.getConfiguration();
        final List<Object> services = isisComponentProvider.provideServices();

        ServicesInjectorSpi servicesInjectorSpi = new ServicesInjectorDefault(services);
        servicesInjectorSpi.addFallbackIfRequired(FixtureScripts.class, new FixtureScriptsDefault());
        servicesInjectorSpi.validateServices();

        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession(configuration);

        final PersistenceSessionFactory persistenceSessionFactory =
                isisComponentProvider.providePersistenceSessionFactory(deploymentType, servicesInjectorSpi, runtimeContext);

        final AuthenticationManager authenticationManager =
                isisComponentProvider.provideAuthenticationManager(deploymentType);
        final AuthorizationManager authorizationManager =
                isisComponentProvider.provideAuthorizationManager(deploymentType);

        final Collection<MetaModelRefiner> metaModelRefiners =
                refiners(authenticationManager, authorizationManager, persistenceSessionFactory);
        final SpecificationLoaderSpi specificationLoader =
                isisComponentProvider.provideSpecificationLoaderSpi(metaModelRefiners);

        // bind metamodel to the (runtime) framework
        runtimeContext.injectInto(specificationLoader);

        return new IsisSessionFactory (
                deploymentType, configuration, specificationLoader,
                authenticationManager, authorizationManager,
                persistenceSessionFactory);
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }


    private void initContext(final IsisSessionFactory sessionFactory) {
        getDeploymentType().initContext(sessionFactory);
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

        fixtureInstaller = isisComponentProvider.provideFixturesInstaller();
        if (isNoop(fixtureInstaller)) {
            return;
        }

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

    private boolean isNoop(final FixturesInstaller candidate) {
        return candidate == null || (fixtureInstaller instanceof Noop);
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
        } finally {
            IsisContext.closeSession();
        }

    }

    private <T> T lookupService(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }

    private ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    Collection<ObjectSpecification> allSpecifications() {
        return IsisContext.getSpecificationLoader().allSpecifications();
    }

    //endregion

    //region > shutdown

    @Override
    public void shutdown() {
        LOG.info("shutting down system");

        shutdownServices(this.serviceInitializer);

        IsisContext.closeAllSessions();
    }

    /**
     * @see #initializeServices()
     */
    private void shutdownServices(final ServiceInitializer serviceInitializer) {

        // call @PostDestroy (in a session)
        IsisContext.openSession(new InitialisationSession());
        try {
            getTransactionManager().startTransaction();
            try {
                serviceInitializer.preDestroy();

            } catch(RuntimeException ex) {
                getTransactionManager().getTransaction().setAbortCause(new IsisTransactionManagerException(ex));
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
        return isisComponentProvider.getConfiguration();
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

    @Override
    public DebuggableWithTitle debugSection(final String selectionName) {
        // DebugInfo deb;
        if (selectionName.equals("Configuration")) {
            return getConfiguration();
        }
        return null;
    }

    @Override
    public String[] debugSectionNames() {
        final String[] general = new String[] { "Overview", "Authenticator", "Configuration", "Reflector", "Requests", "Contexts" };
        final String[] contextIds = IsisContext.getInstance().allSessionIds();
        final String[] combined = new String[general.length + contextIds.length];
        System.arraycopy(general, 0, combined, 0, general.length);
        System.arraycopy(contextIds, 0, combined, general.length, contextIds.length);
        return combined;
    }

    IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion

}
