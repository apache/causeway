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

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.appmanifest.AppManifestProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.MessageRegistry;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;

/**
 * Is the factory of {@link IsisSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class can in considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 * <p>
 *     The class is only instantiated once; it is also registered with {@link ServicesInjector}, meaning that
 *     it can be {@link Inject}'d into other domain services.
 * </p>
 */
public class IsisSessionFactory
implements ApplicationScopedComponent, AppManifestProvider {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(IsisSessionFactory.class);

    // -- constructor, fields, accessors

    private final DeploymentCategory deploymentCategory;
    private final IsisConfiguration configuration;
    private final SpecificationLoader specificationLoader;
    private final ServicesInjector servicesInjector;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final AppManifest appManifest;

    public IsisSessionFactory(
            final DeploymentCategory deploymentCategory,
            final ServicesInjector servicesInjector,
            final AppManifest appManifest) {

        this.servicesInjector = servicesInjector;
        this.deploymentCategory = deploymentCategory;

        this.configuration = servicesInjector.getConfigurationServiceInternal();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
        this.authenticationManager = servicesInjector.getAuthenticationManager();
        this.authorizationManager = servicesInjector.getAuthorizationManager();
        this.persistenceSessionFactory = servicesInjector.lookupServiceElseFail(PersistenceSessionFactory.class);
        this.appManifest = appManifest;
    }

    @Override
    @Programmatic
    public AppManifest getAppManifest() {
        return appManifest;
    }



    // -- constructServices, destroyServicesAndShutdown

    private ServiceInitializer serviceInitializer;

    @Programmatic
    public void constructServices() {

        // do postConstruct.  We store the initializer to do preDestroy on shutdown
        serviceInitializer = new ServiceInitializer(configuration, servicesInjector.getRegisteredServices());
        serviceInitializer.validate();

        openSession(new InitialisationSession());

        try {
            //
            // postConstructInSession
            //

            IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();
            transactionManager.startTransaction();
            try {
                serviceInitializer.postConstruct();
            } catch(RuntimeException ex) {
                transactionManager.getCurrentTransaction().setAbortCause(new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }


            //
            // installFixturesIfRequired
            //
            final FixturesInstallerFromConfiguration fixtureInstaller =
                    new FixturesInstallerFromConfiguration(this);
            fixtureInstaller.installFixtures();

            // only allow logon fixtures if not in production mode.
            if (!deploymentCategory.isProduction()) {
                logonFixture = fixtureInstaller.getLogonFixture();
            }

            //
            // translateServicesAndEnumConstants
            //

            final List<Object> services = servicesInjector.getRegisteredServices();
            // take a copy of all services to avoid occasional concurrent modification exceptions
            // that can sometimes occur in the loop
            final List<Object> copyOfServices = _Lists.newArrayList(services);
            final TitleService titleService = servicesInjector.lookupServiceElseFail(TitleService.class);
            for (Object service : copyOfServices) {
                @SuppressWarnings("unused")
                final String unused = titleService.titleOf(service);
            }

            // (previously we took a protective copy to avoid a concurrent modification exception,
            // but this is now done by SpecificationLoader itself)
            for (final ObjectSpecification objSpec : servicesInjector.getSpecificationLoader().allSpecifications()) {
                final Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass.isEnum()) {
                    final Object[] enumConstants = correspondingClass.getEnumConstants();
                    for (Object enumConstant : enumConstants) {
                        @SuppressWarnings("unused")
                        final String unused = titleService.titleOf(enumConstant);
                    }
                }
            }

            // as used by the Wicket UI
            final TranslationService translationService = servicesInjector.lookupServiceElseFail(TranslationService.class);

            final String context = IsisSessionFactoryBuilder.class.getName();
            final MessageRegistry messageRegistry = new MessageRegistry();
            final List<String> messages = messageRegistry.listMessages();
            for (String message : messages) {
                translationService.translate(context, message);
            }

        } finally {
            closeSession();
        }
    }


    @Programmatic
    public void destroyServicesAndShutdown() {
        destroyServices();
        shutdown();
    }

    private void destroyServices() {
        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PreDestroy (in a session)
        openSession(new InitialisationSession());
        IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();
        try {
            transactionManager.startTransaction();
            try {

                serviceInitializer.preDestroy();

            } catch (RuntimeException ex) {
                transactionManager.getCurrentTransaction().setAbortCause(
                        new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }
        } finally {
            closeSession();
        }
    }

    private void shutdown() {
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoader.shutdown();
    }



    // -- logonFixture

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
    @Programmatic
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }

    // -- openSession, closeSession, currentSession, inSession
    private final ThreadLocal<IsisSession> currentSession = new ThreadLocal<>();

    /**
     * Creates and {@link IsisSession#open() open}s the {@link IsisSession}.
     */
    @Programmatic
    public IsisSession openSession(final AuthenticationSession authenticationSession) {

        closeSession();

        final PersistenceSession persistenceSession =
                persistenceSessionFactory.createPersistenceSession(servicesInjector, authenticationSession);
        IsisSession session = new IsisSession(authenticationSession, persistenceSession);
        currentSession.set(session);
        session.open();
        return session;
    }

    @Programmatic
    public void closeSession() {
        final IsisSession existingSessionIfAny = getCurrentSession();
        if (existingSessionIfAny == null) {
            return;
        }
        existingSessionIfAny.close();
        currentSession.set(null);
    }

    @Programmatic
    public IsisSession getCurrentSession() {
        return currentSession.get();
    }

    private IsisTransactionManager getCurrentSessionTransactionManager() {
        final IsisSession currentSession = getCurrentSession();
        return currentSession.getPersistenceSession().getTransactionManager();
    }

    @Programmatic
    public boolean inSession() {
        return getCurrentSession() != null;
    }

    @Programmatic
    public boolean inTransaction() {
        if (inSession()) {
            if (getCurrentSession().getCurrentTransaction() != null) {
                if (!getCurrentSession().getCurrentTransaction().getState().isComplete()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * As per {@link #doInSession(Runnable, AuthenticationSession)}, using a default {@link InitialisationSession}.
     * @param runnable
     */
    @Programmatic
    public void doInSession(final Runnable runnable) {
        doInSession(runnable, new InitialisationSession());
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param runnable The piece of code to run.
     * @param authenticationSession
     */
    @Programmatic
    public void doInSession(final Runnable runnable, final AuthenticationSession authenticationSession) {
        doInSession(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        }, authenticationSession);
    }

    /**
     * As per {@link #doInSession(Callable), AuthenticationSession}, using a default {@link InitialisationSession}.
     */
    @Programmatic
    public <R> R doInSession(final Callable<R> callable) {
        return doInSession(callable, new InitialisationSession());
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param callable The piece of code to run.
     * @param authenticationSession - the user to run under
     */
    @Programmatic
    public <R> R doInSession(final Callable<R> callable, final AuthenticationSession authenticationSession) {
        final IsisSessionFactory sessionFactory = this;
        boolean noSession = !sessionFactory.inSession();
        try {
            if (noSession) {
                sessionFactory.openSession(authenticationSession);
            }

            return callable.call();
        } catch (Exception x) {
            throw new RuntimeException(
                    String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"),
                    x);
        } finally {
            if (noSession) {
                sessionFactory.closeSession();
            }
        }
    }



    // -- component accessors
    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link DeploymentCategory}.
     */
    @Programmatic
    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link IsisConfiguration}.
     */
    @Programmatic
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped} {@link ServicesInjector}.
     */
    @Programmatic
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    /**
     * Derived from {@link #getServicesInjector()}.
     *
     * @deprecated - use {@link #getServicesInjector()} instead.
     */
    @Programmatic
    @Deprecated
    public List<Object> getServices() {
        return servicesInjector.getRegisteredServices();
    }


    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link SpecificationLoader}.
     */
    @Programmatic
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * The {@link AuthenticationManager} that will be used to authenticate and
     * create {@link AuthenticationSession}s
     * {@link IsisSession#getAuthenticationSession() within} the
     * {@link IsisSession}.
     */
    @Programmatic
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * The {@link AuthorizationManager} that will be used to authorize access to
     * domain objects.
     */
    @Programmatic
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    /**
     * The {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} that will be used to create
     * {@link PersistenceSession} {@link IsisSession#getPersistenceSession()
     * within} the {@link IsisSession}.
     */
    @Programmatic
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }




}
