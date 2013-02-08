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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.util.InvokeUtils;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.log4j.Logger;

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
public abstract class IsisSessionFactoryAbstract implements IsisSessionFactory {

    private final static Logger LOG = Logger.getLogger(IsisSessionFactoryAbstract.class);
    
    private final DeploymentType deploymentType;
    private final IsisConfiguration configuration;
    private final TemplateImageLoader templateImageLoader;
    private final SpecificationLoaderSpi specificationLoaderSpi;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final UserProfileLoader userProfileLoader;
    private final List<Object> serviceList;
    private final OidMarshaller oidMarshaller;

    public IsisSessionFactoryAbstract(final DeploymentType deploymentType, final IsisConfiguration configuration, final SpecificationLoaderSpi specificationLoader, final TemplateImageLoader templateImageLoader, final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager, final UserProfileLoader userProfileLoader, final PersistenceSessionFactory persistenceSessionFactory, final List<Object> serviceList, OidMarshaller oidMarshaller) {

        ensureThatArg(deploymentType, is(not(nullValue())));
        ensureThatArg(configuration, is(not(nullValue())));
        ensureThatArg(specificationLoader, is(not(nullValue())));
        ensureThatArg(templateImageLoader, is(not(nullValue())));
        ensureThatArg(authenticationManager, is(not(nullValue())));
        ensureThatArg(authorizationManager, is(not(nullValue())));
        ensureThatArg(userProfileLoader, is(not(nullValue())));
        ensureThatArg(persistenceSessionFactory, is(not(nullValue())));
        ensureThatArg(serviceList, is(not(nullValue())));

        this.deploymentType = deploymentType;
        this.configuration = configuration;
        this.templateImageLoader = templateImageLoader;
        this.specificationLoaderSpi = specificationLoader;
        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
        this.userProfileLoader = userProfileLoader;
        this.persistenceSessionFactory = persistenceSessionFactory;
        this.serviceList = serviceList;
        this.oidMarshaller = oidMarshaller;
        
        validateServices(serviceList);
    }

    /**
     * Validate domain services lifecycle events.
     * 
     * <p>
     * Specifically:
     * <ul>
     * <li>All {@link PostConstruct} methods must either take no arguments or take a {@link Properties} object.</li>
     * <li>All {@link PreDestroy} methods must take no arguments.</li>
     * </ul>
     * 
     * <p>
     * If this isn't the case, then we fail fast.
     */
    private void validateServices(List<Object> serviceList) {
        for (Object service : serviceList) {
            final Method[] methods = service.getClass().getMethods();
            for (Method method : methods) {
                validatePostConstructMethods(service, method);
                validatePreDestroyMethods(service, method);
            }
        }
    }

    private void validatePostConstructMethods(Object service, Method method) {
        PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
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
        PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
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
        templateImageLoader.init();

        specificationLoaderSpi.setServiceClasses(JavaClassUtils.toClasses(serviceList));

        specificationLoaderSpi.init();

        // must come after init of spec loader.
        specificationLoaderSpi.injectInto(persistenceSessionFactory);
        persistenceSessionFactory.setServices(serviceList);
        userProfileLoader.setServices(serviceList);

        authenticationManager.init();
        authorizationManager.init();
        persistenceSessionFactory.init();
        
        initServices(getConfiguration());
    }

    
    @Override
    public void shutdown() {
        
        shutdownServices();
        
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoaderSpi.shutdown();
        templateImageLoader.shutdown();
        userProfileLoader.shutdown();
    }

    protected void initServices(IsisConfiguration configuration) {
        final List<Object> services = getServices();
        Map<String, String> props = configuration.asMap();
        for (Object service : services) {
            callPostConstructIfExists(service, props);
        }
    }

    private void callPostConstructIfExists(Object service, Map<String, String> props) {
        LOG.info("calling @PostConstruct on all domain services");
        Method[] methods = service.getClass().getMethods();
        for (Method method : methods) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            if(postConstruct == null) {
                continue;
            }
            LOG.info("calling @PostConstruct method: " + service.getClass().getName() + ": " + method.getName());

            final int numParams = method.getParameterTypes().length;
            
            // unlike shutdown, we don't swallow exceptions; would rather fail early
            if(numParams == 0) {
                InvokeUtils.invoke(method, service);
            } else {
                InvokeUtils.invoke(method, service, new Object[]{props});
            }
        }
    }


    protected void shutdownServices() {
        final List<Object> services = getServices();
        for (Object service : services) {
            callPreDestroyIfExists(service);
        }
    }

    private void callPreDestroyIfExists(Object service) {
        LOG.info("calling @PreDestroy on all domain services");
        final Method[] methods = service.getClass().getMethods();
        for (Method method : methods) {
            PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
            if(preDestroy == null) {
                continue;
            }
            LOG.info("calling @PreDestroy method: " + service.getClass().getName() + ": " + method.getName());
            try {
                InvokeUtils.invoke(method, service);
            } catch(Exception ex) {
                // do nothing
                LOG.warn("@PreDestroy method threw exception - continuing anyway", ex);
            }
        }
    }


    @Override
    public IsisSession openSession(final AuthenticationSession authenticationSession) {
        final PersistenceSession persistenceSession = persistenceSessionFactory.createPersistenceSession();
        ensureThatArg(persistenceSession, is(not(nullValue())));

        final UserProfile userProfile = userProfileLoader.getProfile(authenticationSession);
        ensureThatArg(userProfile, is(not(nullValue())));

        // inject into persistenceSession any/all application-scoped components
        // that it requires
        getSpecificationLoader().injectInto(persistenceSession);

        final IsisSessionDefault isisSessionDefault = new IsisSessionDefault(this, authenticationSession, persistenceSession, userProfile);

        return isisSessionDefault;
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
    public TemplateImageLoader getTemplateImageLoader() {
        return templateImageLoader;
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
    public UserProfileLoader getUserProfileLoader() {
        return userProfileLoader;
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
