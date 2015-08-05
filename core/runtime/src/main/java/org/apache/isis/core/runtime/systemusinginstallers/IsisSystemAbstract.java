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

package org.apache.isis.core.runtime.systemusinginstallers;

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
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;

/**
 * 
 */
public abstract class IsisSystemAbstract implements IsisSystem {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystemAbstract.class);


    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;
    private final DeploymentType deploymentType;

    private boolean initialized = false;

    private ServiceInitializer serviceInitializer;
    private FixturesInstaller fixtureInstaller;

    //region > constructors

    public IsisSystemAbstract(final DeploymentType deploymentType) {
        this(deploymentType, new IsisLocaleInitializer(), new IsisTimeZoneInitializer());
    }

    public IsisSystemAbstract(final DeploymentType deploymentType, final IsisLocaleInitializer localeInitializer, final IsisTimeZoneInitializer timeZoneInitializer) {
        this.deploymentType = deploymentType;
        this.localeInitializer = localeInitializer;
        this.timeZoneInitializer = timeZoneInitializer;
    }
    //endregion

    //region > deploymentType
    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
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

    //region > createSessionFactory

    private IsisSessionFactory sessionFactory;

    /**
     * Populated after {@link #init()}.
     */
    @Override
    public IsisSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private IsisSessionFactory createSessionFactory(final DeploymentType deploymentType) throws IsisSystemException {
        final List<Object> services = obtainServices();
        final PersistenceSessionFactory persistenceSessionFactory =
                obtainPersistenceSessionFactory(deploymentType, services);

        final IsisConfiguration configuration = getConfiguration();
        final AuthenticationManager authenticationManager = obtainAuthenticationManager(deploymentType);
        final AuthorizationManager authorizationManager = obtainAuthorizationManager(deploymentType);
        final OidMarshaller oidMarshaller = obtainOidMarshaller();

        final Collection<MetaModelRefiner> metaModelRefiners =
                refiners(authenticationManager, authorizationManager, persistenceSessionFactory);
        final SpecificationLoaderSpi reflector = obtainSpecificationLoaderSpi(deploymentType, metaModelRefiners);

        ServicesInjectorSpi servicesInjector = persistenceSessionFactory.getServicesInjector();
        servicesInjector.addFallbackIfRequired(FixtureScripts.class, new FixtureScriptsDefault());
        servicesInjector.validateServices();

        // bind metamodel to the (runtime) framework
        final RuntimeContextFromSession runtimeContext = obtainRuntimeContextFromSession();
        runtimeContext.injectInto(reflector);

        return new IsisSessionFactoryDefault(
                deploymentType, configuration, reflector,
                authenticationManager, authorizationManager,
                persistenceSessionFactory, oidMarshaller);
    }

    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
    }
    //endregion


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

        fixtureInstaller = obtainFixturesInstaller();
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

    //region > obtainXxx: specificationLoaderSpi, persistenceSessionFactory, oidMarshaller, runtimeContextFromSession, authenticationManager authorizationManager, services, fixturesInstaller

    protected abstract SpecificationLoaderSpi obtainSpecificationLoaderSpi(DeploymentType deploymentType, Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException;

    protected abstract PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType, final List<Object> services) throws IsisSystemException;

    protected OidMarshaller obtainOidMarshaller() {
        return new OidMarshaller();
    }

    protected RuntimeContextFromSession obtainRuntimeContextFromSession() {
        return new RuntimeContextFromSession();
    }

    protected abstract AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException;

    protected abstract AuthorizationManager obtainAuthorizationManager(final DeploymentType deploymentType);

    protected abstract List<Object> obtainServices();

    /**
     * This is the only {@link Installer} that is used by any (all) subclass
     * implementations, because it effectively <i>is</i> the component we need
     * (as opposed to a builder/factory of the component we need).
     *
     * <p>
     * The fact that the component <i>is</i> an installer (and therefore can be
     * {@link InstallerLookup} looked up} is at this level really just an
     * incidental implementation detail useful for the subclass that uses
     * {@link InstallerLookup} to create the other components.
     */
    protected abstract FixturesInstaller obtainFixturesInstaller() throws IsisSystemException;


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
    @Override
    public abstract IsisConfiguration getConfiguration();
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
    @Override
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }
    //endregion


    //region > debugging

    private void debug(final DebugBuilder debug, final Object object) {
        if (object instanceof DebuggableWithTitle) {
            final DebuggableWithTitle d = (DebuggableWithTitle) object;
            debug.appendTitle(d.debugTitle());
            d.debugData(debug);
        } else {
            debug.appendln(object.toString());
            debug.appendln("... no further debug information");
        }
    }

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
