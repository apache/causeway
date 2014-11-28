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
    private final List<Object> serviceList;
    private final OidMarshaller oidMarshaller;

    public IsisSessionFactoryDefault(
            final DeploymentType deploymentType,
            final IsisConfiguration configuration,
            final SpecificationLoaderSpi specificationLoader,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager,
            final PersistenceSessionFactory persistenceSessionFactory,
            final List<Object> serviceList,
            final OidMarshaller oidMarshaller) {

        ensureThatArg(deploymentType, is(not(nullValue())));
        ensureThatArg(configuration, is(not(nullValue())));
        ensureThatArg(specificationLoader, is(not(nullValue())));
        ensureThatArg(authenticationManager, is(not(nullValue())));
        ensureThatArg(authorizationManager, is(not(nullValue())));
        ensureThatArg(persistenceSessionFactory, is(not(nullValue())));
        ensureThatArg(serviceList, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = configuration;
        this.specificationLoaderSpi = specificationLoader;
        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
        this.persistenceSessionFactory = persistenceSessionFactory;
        this.serviceList = serviceList;
        this.oidMarshaller = oidMarshaller;
        
        validateServices(serviceList);
    }

    /**
     * Validate domain service Ids are unique, and that the {@link PostConstruct} method, if present, must either 
     * take no arguments or take a {@link Map} object), and that the {@link PreDestroy} method, if present, must take 
     * no arguments.
     * 
     * <p>
     * TODO: there seems to be some duplication/overlap with {@link ServiceInitializer}.
     */
    private void validateServices(List<Object> serviceList) {
        for (Object service : serviceList) {
            final Method[] methods = service.getClass().getMethods();
            for (Method method : methods) {
                validatePostConstructMethods(service, method);
                validatePreDestroyMethods(service, method);
            }
        }
        
        ListMultimap<String, Object> servicesById = ArrayListMultimap.create();
        for (Object service : serviceList) {
            String id = ServiceUtil.id(service);
            servicesById.put(id, service);
        }
        for (Entry<String, Collection<Object>> servicesForId : servicesById.asMap().entrySet()) {
            String serviceId = servicesForId.getKey();
            Collection<Object> services = servicesForId.getValue();
            if(services.size() > 1) {
                throw new IllegalStateException("Service ids must be unique; serviceId '" + serviceId + "' is declared by domain services " + classNamesFor(services)); 
            }
        }
    }

    private static String classNamesFor(Collection<Object> services) {
        StringBuilder buf = new StringBuilder();
        for (Object service : services) {
            if(buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(service.getClass().getName());
        }
        return buf.toString();
    }

    private void validatePostConstructMethods(Object service, Method method) {
        final PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
        if(postConstruct == null) {
            return;
        }
        final int numParams = method.getParameterTypes().length;
        if(numParams == 0) {
            return;
        }
        if(numParams == 1 && method.getParameterTypes()[0].isAssignableFrom(Map.class)) {
            return;
        }
        throw new IllegalStateException("Domain service " + service.getClass().getName() + " has @PostConstruct method " + method.getName() + "; such methods must take either no argument or 1 argument of type Map<String,String>"); 
    }

    private void validatePreDestroyMethods(Object service, Method method) {
        final PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
        if(preDestroy == null) {
            return;
        }
        final int numParams = method.getParameterTypes().length;
        if(numParams == 0) {
            return;
        }
        throw new IllegalStateException("Domain service " + service.getClass().getName() + " has @PreDestroy method " + method.getName() + "; such methods must take no arguments"); 
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
        servicesInjector.setServices(serviceList);
        specificationLoaderSpi.setServiceInjector(servicesInjector);

        specificationLoaderSpi.init();

        // must come after init of spec loader.
        specificationLoaderSpi.injectInto(persistenceSessionFactory);
        persistenceSessionFactory.setServices(serviceList);

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
        return serviceList;
    }
    
    @Override
    public OidMarshaller getOidMarshaller() {
    	return oidMarshaller;
    }
}
