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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Analogous (and in essence a wrapper for) a JDO <code>PersistenceManagerFactory</code>
 * 
 * Creates an implementation of
 * {@link IsisSessionFactory#openSession(AuthenticationSession)} to create an
 * {@link IsisSession}, but delegates to subclasses to actually obtain the
 * components that make up that {@link IsisSession}.
 *
 * <p>
 * The idea is that one subclass can use the {@link InstallerLookup} design to
 * lookup installers for components (and hence create the components
 * themselves), whereas another subclass might simply use Spring (or another DI
 * container) to inject in the components according to some Spring-configured
 * application context.
 */

public class IsisSessionFactory implements ApplicationScopedComponent {


    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(IsisSessionFactory.class);

    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;
    private final SpecificationLoader specificationLoaderSpi;
    private final ServicesInjector servicesInjector;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final OidMarshaller oidMarshaller;

    public IsisSessionFactory(
            final DeploymentType deploymentType,
            final IsisConfiguration configuration,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager,
            final PersistenceSessionFactory persistenceSessionFactory) {

        ensureThatArg(deploymentType, is(not(nullValue())));
        ensureThatArg(configuration, is(not(nullValue())));
        ensureThatArg(specificationLoader, is(not(nullValue())));
        ensureThatArg(servicesInjector, is(not(nullValue())));
        ensureThatArg(authenticationManager, is(not(nullValue())));
        ensureThatArg(authorizationManager, is(not(nullValue())));
        ensureThatArg(persistenceSessionFactory, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = configuration;
        this.specificationLoaderSpi = specificationLoader;
        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
        this.servicesInjector = servicesInjector;
        this.persistenceSessionFactory = persistenceSessionFactory;
        this.oidMarshaller = new OidMarshaller();
    }


    public void shutdown() {
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoaderSpi.shutdown();
    }



    /**
     * Creates and {@link IsisSession#open() open}s the {@link IsisSession}.
     */
    public IsisSession openSession(final AuthenticationSession authenticationSession) {
        final PersistenceSession persistenceSession =
                persistenceSessionFactory.createPersistenceSession(
                        servicesInjector, getSpecificationLoader(), authenticationSession);
        ensureThatArg(persistenceSession, is(not(nullValue())));

        return newIsisSession(authenticationSession, persistenceSession);
    }

    protected IsisSession newIsisSession(
            final AuthenticationSession authenticationSession,
            final PersistenceSession persistenceSession) {
        return new IsisSession(this, authenticationSession, persistenceSession);
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link DeploymentType}.
     */
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link IsisConfiguration}.
     */
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped} {@link ServicesInjector}.
     */
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    /**
     * Derived from {@link #getServicesInjector()}.
     * 
     * @deprecated - use {@link #getServicesInjector()} instead.
     */
    @Deprecated
    public List<Object> getServices() {
        return servicesInjector.getRegisteredServices();
    }


    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link SpecificationLoader}.
     */
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoaderSpi;
    }

    /**
     * The {@link AuthenticationManager} that will be used to authenticate and
     * create {@link AuthenticationSession}s
     * {@link IsisSession#getAuthenticationSession() within} the
     * {@link IsisSession}.
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * The {@link AuthorizationManager} that will be used to authorize access to
     * domain objects.
     */
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    /**
     * The {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} that will be used to create
     * {@link PersistenceSession} {@link IsisSession#getPersistenceSession()
     * within} the {@link IsisSession}.
     */
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }

    /**
     * The {@link OidMarshaller} to use for marshalling and unmarshalling {@link Oid}s
     * into strings.
     */
    public OidMarshaller getOidMarshaller() {
        return oidMarshaller;
    }

}
