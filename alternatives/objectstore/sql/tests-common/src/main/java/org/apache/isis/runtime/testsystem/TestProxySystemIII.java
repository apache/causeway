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


package org.apache.isis.runtime.testsystem;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.core.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.context.IsisContextStatic;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStorePersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.defaults.progmodel.JavaReflectorInstaller;
import org.apache.isis.extensions.sql.objectstore.SqlPersistorInstaller;
import org.apache.isis.extensions.xml.objectstore.XmlPersistenceMechanismInstaller;
import org.apache.isis.runtime.objectstore.inmemory.InMemoryPersistenceMechanismInstaller;

/*
 * TODO allow to be created with specific requirements for components being set up rather than using mocks.
 */
public class TestProxySystemIII {
    
    private PropertiesConfiguration configuration;
    public void setConfiguration(PropertiesConfiguration configuration) {
		this.configuration = configuration;
	}
    public PropertiesConfiguration getConfiguration() {
        if (configuration == null){
        	configuration = new PropertiesConfiguration();
        }
        return configuration;
    }


	private List<Object> servicesList;
    private Mockery mockery = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
	private IsisSessionFactoryDefault sessionFactory = null;
	private ObjectStorePersistenceMechanismInstallerAbstract persistenceMechanismInstaller;
    
    
    
    public void init(AbstractFactoryAndRepository factory) {
        servicesList = new ArrayList<Object>();
        servicesList.add(factory);

        final TemplateImageLoader mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
        final UserProfileLoader mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        final AuthenticationManager mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        final AuthorizationManager mockAuthorizationManager = mockery.mock(AuthorizationManager.class);

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
                ignoring(mockUserProfileLoader);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);
            }
        });

        SpecificationLoader mockSpecificationLoader;
        JavaReflectorInstaller javaReflectorInstaller = new JavaReflectorInstaller();
        javaReflectorInstaller.setConfiguration(configuration);
        mockSpecificationLoader = javaReflectorInstaller.createReflector();

        ((ObjectReflectorAbstract) mockSpecificationLoader).setRuntimeContext(new RuntimeContextFromSession());

        if (configuration.getString("isis.persistence.sql.jdbc.driver") == null){
        	if (configuration.getString("isis.persistence") == "in-memory"){
                /*InMemoryPersistenceMechanismInstaller*/ //persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        		persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        	} else {
        		persistenceMechanismInstaller = new XmlPersistenceMechanismInstaller();
        	}
        } else {
            persistenceMechanismInstaller = new SqlPersistorInstaller(); 
        }
        
        persistenceMechanismInstaller.setConfiguration(configuration);
        PersistenceSessionFactory persistenceSessionFactory = persistenceMechanismInstaller.createPersistenceSessionFactory(DeploymentType.PROTOTYPE);
        
        sessionFactory = new IsisSessionFactoryDefault(
                DeploymentType.EXPLORATION,
                configuration, 
                mockTemplateImageLoader, 
                mockSpecificationLoader, 
                mockAuthenticationManager,
                mockAuthorizationManager, 
                mockUserProfileLoader, 
                persistenceSessionFactory, 
                servicesList);
        IsisContext context = IsisContextStatic.createRelaxedInstance(sessionFactory);
        IsisContext.setConfiguration(sessionFactory.getConfiguration());
        sessionFactory.init();
        
        context.openSessionInstance(new SimpleSession("tester", new String[0], "001"));
    }
    
    public void addToConfiguration(String key, String value) {
        configuration.add(key, value);
    }
    
    public void shutDown(){
    	if (sessionFactory != null){
    		sessionFactory.shutdown();
    	}
    	if (persistenceMechanismInstaller != null){
    		persistenceMechanismInstaller.shutdown();
    	}
    }
}


