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

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.isis.core.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.defaults.progmodel.JavaReflectorInstaller;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.standard.SimpleSession;
import org.apache.isis.runtime.authorization.AuthorizationManager;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.context.IsisContextStatic;
import org.apache.isis.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.runtime.objectstore.inmemory.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtime.session.IsisSessionFactoryDefault;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.userprofile.UserProfileLoader;

/*
 * TODO allow to be created with specific requirements for components being set up rather than using mocks.
 */
public class TestProxySystemII {
    
    private PropertiesConfiguration configuration;
    private List<Object> servicesList;
    private Mockery mockery = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    public void init() {
        servicesList = Collections.emptyList();

        final TemplateImageLoader mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
   //     final SpecificationLoader mockSpecificationLoader = mockery.mock(SpecificationLoader.class);
//        final PersistenceSessionFactory mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        final UserProfileLoader mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        final AuthenticationManager mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        final AuthorizationManager mockAuthorizationManager = mockery.mock(AuthorizationManager.class);

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
            //    ignoring(mockSpecificationLoader);
             //   ignoring(mockPersistenceSessionFactory);
                ignoring(mockUserProfileLoader);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);
            }
        });

        configuration = new PropertiesConfiguration();

        SpecificationLoader mockSpecificationLoader;
        JavaReflectorInstaller javaReflectorInstaller = new JavaReflectorInstaller();
        javaReflectorInstaller.setConfiguration(configuration);
        mockSpecificationLoader = javaReflectorInstaller.createReflector();

        ((ObjectReflectorAbstract) mockSpecificationLoader).setRuntimeContext(new RuntimeContextFromSession());

        InMemoryPersistenceMechanismInstaller persistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        persistenceMechanismInstaller.setConfiguration(configuration);
        PersistenceSessionFactory mockPersistenceSessionFactory = persistenceMechanismInstaller.createPersistenceSessionFactory(DeploymentType.PROTOTYPE);
        
//        mockPersistenceSessionFactory.init();
        
        IsisSessionFactoryDefault sessionFactory = new IsisSessionFactoryDefault(
                DeploymentType.EXPLORATION,
                configuration, 
                mockTemplateImageLoader, 
                mockSpecificationLoader, 
                mockAuthenticationManager,
                mockAuthorizationManager, 
                mockUserProfileLoader, 
                mockPersistenceSessionFactory, 
                servicesList);
        IsisContext context = IsisContextStatic.createRelaxedInstance(sessionFactory);
        IsisContext.setConfiguration(sessionFactory.getConfiguration());
        sessionFactory.init();
        
        context.openSessionInstance(new SimpleSession("tester", new String[0], "001"));
    }
    
    public PropertiesConfiguration getConfiguration() {
        return configuration;
    }

    public void addToConfiguration(String key, String value) {
        configuration.add(key, value);
    }
}


