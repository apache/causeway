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


package org.apache.isis.runtime.system;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;

import java.util.List;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.fixturesinstaller.FixturesInstaller;
import org.apache.isis.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.runtime.imageloader.awt.TemplateImageLoaderAwt;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.session.IsisSessionFactory;
import org.apache.isis.runtime.userprofile.UserProfileStore;


/**
 * A simple implementation of {@link IsisSystem}, intended
 * for use by Spring (dependency injection) or for testing.
 * 
 * <p>
 * Constructor injection is used for non-defaulted, mandatory components.  
 * Setter-based injection can be used for components that will otherwise by 
 * defaulted or are optional.
 */
public abstract class IsisSystemSimple extends IsisSystemAbstract {
    
    private final IsisConfiguration configuration;
    
    private TemplateImageLoader templateImageLoader;
    private ObjectReflector reflector;
    private FixturesInstaller fixturesInstaller;

    private AuthenticationManager authenticationManager;

    private PersistenceSessionFactory persistenceSessionFactory;
    private UserProfileStore userProfileStore;

	private List<Object> serviceList;

    /////////////////////////////////////////////
    // Constructor
    /////////////////////////////////////////////
    

    /**
     * Dependency injection of {@link IsisConfiguration}.
     * 
     * All other components are either optional or defaulted.
     */
    public IsisSystemSimple(
            final DeploymentType deploymentType, 
            final IsisConfiguration configuration) {
        super(deploymentType);
        ensureThatArg(configuration, is(not(nullValue())), "configuration may not be null");
        this.configuration = configuration;
    }


    /////////////////////////////////////////////
    // doCreateExecutionContextFactory
    /////////////////////////////////////////////

    @Override
    protected abstract IsisSessionFactory doCreateSessionFactory(DeploymentType deploymentType) throws IsisSystemException;


    /////////////////////////////////////////////
    // Configuration
    /////////////////////////////////////////////

    /**
     * As specified in the constructor.
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    
    /////////////////////////////////////////////
    // Authentication Manager
    /////////////////////////////////////////////

    /**
     * The {@link AuthenticationManager}, if any.
     */
    @Override
    protected AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) {
        return authenticationManager;
    }

    
    /**
     * Optionally specify the {@link AuthenticationManager}.
     * 
     * <p>
     * It will otherwise be <tt>null</tt>.
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    
    /////////////////////////////////////////////
    // FixturesInstaller
    /////////////////////////////////////////////

    /**
     * The {@link FixturesInstaller}, if any.
     */
    @Override
    protected FixturesInstaller obtainFixturesInstaller() {
        return fixturesInstaller;
    }

    /**
     * Optionally specify the {@link FixturesInstaller}.
     * 
     * <p>
     * It will otherwise be <tt>null</tt>.
     */
    public void setFixturesInstaller(final FixturesInstaller installer) {
        this.fixturesInstaller = installer;
    }
    


    /////////////////////////////////////////////
    // TemplateImageLoader
    /////////////////////////////////////////////

    /**
     * The {@link TemplateImageLoader}, if any.
     */
    protected TemplateImageLoader obtainTemplateImageLoader() {
        return templateImageLoader != null? templateImageLoader: new TemplateImageLoaderAwt(getConfiguration());
    }

    /**
     * Optionally specify the {@link TemplateImageLoader}.
     * 
     * <p>
     * It will otherwise be {@link IsisSystemDefault#obtainTemplateImageLoader(DeploymentType) defaulted}.
     */
    public void setTemplateImageLoader(final TemplateImageLoader templateImageLoader) {
        ensureThatArg(templateImageLoader, is(notNullValue()), "template Image Loader may not be set to null");
        this.templateImageLoader = templateImageLoader; 
    }


    public TemplateImageLoader getTemplateImageLoader() {
        return templateImageLoader;
    }

    
    /////////////////////////////////////////////
    // Reflector
    /////////////////////////////////////////////

    /**
     * The injected {@link ObjectReflector}.
     * 
     * @see #setReflector(ObjectReflector)
     */
    protected ObjectReflector obtainReflector(DeploymentType deploymentType) throws IsisSystemException {
        return reflector;
    }
    
    public void setReflector(final ObjectReflector reflector) {
        ensureThatArg(reflector, is(notNullValue()), "reflector may not be set to null");
        this.reflector = reflector;
    }

    public ObjectReflector getReflector() {
        return reflector;
    }
    

    /////////////////////////////////////////////
    // PersistenceSessionFactory
    /////////////////////////////////////////////

    /**
     * The injected {@link PersistenceSessionFactory}.
     * 
     * @see #setPersistenceSessionFactory(PersistenceSessionFactory)
     */
    @Override
    protected PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType) throws IsisSystemException {
        return persistenceSessionFactory;
    }


    public void setPersistenceSessionFactory(PersistenceSessionFactory persistenceSessionFactory) {
        this.persistenceSessionFactory = persistenceSessionFactory;
    }


    
    ///////////////////////////////////////////////
    // UserProfileStore
    ///////////////////////////////////////////////
    
    public void setUserProfileStore(UserProfileStore userProfileStore) {
		this.userProfileStore = userProfileStore;
	}
    
    
	@Override
	protected UserProfileStore obtainUserProfileStore() {
		return userProfileStore;
	}

	
    ///////////////////////////////////////////////
    // Services
    ///////////////////////////////////////////////

	
	public void setServiceList(List<Object> serviceList) {
		this.serviceList = serviceList;
	}
	
	@Override
	protected List<Object> obtainServices() {
		return serviceList;
	}



}
