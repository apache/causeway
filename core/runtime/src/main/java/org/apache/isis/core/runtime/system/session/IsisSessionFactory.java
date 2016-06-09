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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProviderUsingInstallers;

/**
 * Analogous (and in essence a wrapper for) a JDO <code>PersistenceManagerFactory</code>
 * 
 * Creates an implementation of
 * {@link IsisSessionFactory#openSession(AuthenticationSession)} to create an
 * {@link IsisSession}, but delegates to subclasses to actually obtain the
 * components that make up that {@link IsisSession}.
 *
 * <p>
 * The idea is that one subclass can use the {@link IsisComponentProviderUsingInstallers.InstallerLookup} design to
 * lookup installers for components (and hence create the components
 * themselves), whereas another subclass might simply use Spring (or another DI
 * container) to inject in the components according to some Spring-configured
 * application context.
 */

public class IsisSessionFactory implements ApplicationScopedComponent {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(IsisSessionFactory.class);

    //region > constructor, fields, shutdown

    private final DeploymentCategory deploymentCategory;
    private final IsisConfiguration configuration;
    private final SpecificationLoader specificationLoader;
    private final ServicesInjector servicesInjector;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final OidMarshaller oidMarshaller;

    public IsisSessionFactory(
            final DeploymentCategory deploymentCategory,
            final ServicesInjector servicesInjector) {

        this.servicesInjector = servicesInjector;
        this.deploymentCategory = deploymentCategory;

        this.configuration = servicesInjector.getConfigurationServiceInternal();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
        this.authenticationManager = servicesInjector.getAuthenticationManager();
        this.authorizationManager = servicesInjector.getAuthorizationManager();
        this.persistenceSessionFactory = servicesInjector.lookupServiceElseFail(PersistenceSessionFactory.class);

        this.oidMarshaller = new OidMarshaller();
    }

    @Programmatic
    public void shutdown() {
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoader.shutdown();
    }

    //endregion

    //region > openSession, closeSession, currentSession, inSession
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
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param runnable The piece of code to run.
     */
    @Programmatic
    public void doInSession(final Runnable runnable) {
        doInSession(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        });
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param callable The piece of code to run.
     * @return The result of the code execution.
     */
    @Programmatic
    public <R> R doInSession(final Callable<R> callable) {
        final IsisSessionFactory sessionFactory = this;
        boolean noSession = !sessionFactory.inSession();
        try {
            if (noSession) {
                sessionFactory.openSession(new InitialisationSession());
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

    //endregion

    //region > component accessors
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

    /**
     * The {@link OidMarshaller} to use for marshalling and unmarshalling {@link Oid}s
     * into strings.
     */
    @Programmatic
    public OidMarshaller getOidMarshaller() {
        return oidMarshaller;
    }

    //endregion

}
