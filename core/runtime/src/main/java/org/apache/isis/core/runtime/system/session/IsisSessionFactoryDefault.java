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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.*;

/**
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
public class IsisSessionFactoryDefault implements IsisSessionFactory {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(IsisSessionFactoryDefault.class);
    
    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;
    private final SpecificationLoaderSpi specificationLoaderSpi;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final OidMarshaller oidMarshaller;

    public IsisSessionFactoryDefault(
            final DeploymentType deploymentType,
            final IsisConfiguration configuration,
            final SpecificationLoaderSpi specificationLoader,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager,
            final PersistenceSessionFactory persistenceSessionFactory,
            final OidMarshaller oidMarshaller) {

        ensureThatArg(deploymentType, is(not(nullValue())));
        ensureThatArg(configuration, is(not(nullValue())));
        ensureThatArg(specificationLoader, is(not(nullValue())));
        ensureThatArg(authenticationManager, is(not(nullValue())));
        ensureThatArg(authorizationManager, is(not(nullValue())));
        ensureThatArg(persistenceSessionFactory, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = configuration;
        this.specificationLoaderSpi = specificationLoader;
        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
        this.persistenceSessionFactory = persistenceSessionFactory;
        this.oidMarshaller = oidMarshaller;
    }



    // ///////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////

    /**
     * Wires components as necessary, and then
     * {@link ApplicationScopedComponent#init() init}ializes all.
     */
    @Override
    public void init() {
        final ServicesInjectorSpi servicesInjector = persistenceSessionFactory.getServicesInjector();
        specificationLoaderSpi.setServiceInjector(servicesInjector);

        specificationLoaderSpi.init();

        // must come after init of spec loader.
        specificationLoaderSpi.injectInto(persistenceSessionFactory);

        authenticationManager.init();
        authorizationManager.init();
        persistenceSessionFactory.init();
        
    }


    
    @Override
    public void shutdown() {
        
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoaderSpi.shutdown();
    }


    @Override
    public IsisSession openSession(final AuthenticationSession authenticationSession) {
        final PersistenceSession persistenceSession = persistenceSessionFactory.createPersistenceSession();
        ensureThatArg(persistenceSession, is(not(nullValue())));

        // inject into persistenceSession any/all application-scoped components
        // that it requires
        getSpecificationLoader().injectInto(persistenceSession);

        final IsisSessionDefault isisSessionDefault = newIsisSessionDefault(authenticationSession, persistenceSession);
        return isisSessionDefault;
    }

    protected IsisSessionDefault newIsisSessionDefault(
            final AuthenticationSession authenticationSession,
            final PersistenceSession persistenceSession) {
        return new IsisSessionDefault(this, authenticationSession, persistenceSession);
    }

    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    @Override
    public SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoaderSpi;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    @Override
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    @Override
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }

    @Override
    public List<Object> getServices() {
        return getPersistenceSessionFactory().getServicesInjector().getRegisteredServices();
    }
    
    @Override
    public OidMarshaller getOidMarshaller() {
    	return oidMarshaller;
    }
}
