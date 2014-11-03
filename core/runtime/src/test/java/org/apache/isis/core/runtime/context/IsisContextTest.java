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

package org.apache.isis.core.runtime.context;

import java.util.Collections;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.*;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.IsisContextStatic;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class IsisContextTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    

    private IsisConfiguration configuration;
    
    @Mock
    private PersistenceSession mockPersistenceSession;
    
    @Mock
    private SpecificationLoaderSpi mockSpecificationLoader;

    @Mock
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    protected AuthenticationManager mockAuthenticationManager;
    @Mock
    protected AuthorizationManager mockAuthorizationManager;

    @Mock
    protected DomainObjectContainer mockContainer;
    
    protected OidMarshaller oidMarshaller;

    private List<Object> servicesList;


    private AuthenticationSession authSession;


    private IsisSessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        IsisContext.testReset();

        servicesList = Collections.emptyList();

        configuration = new IsisConfigurationDefault();
        
        oidMarshaller = new OidMarshaller();
        
        context.checking(new Expectations() {
            {
                allowing(mockPersistenceSessionFactory).createPersistenceSession();
                will(returnValue(mockPersistenceSession));
                
                ignoring(mockPersistenceSession);
                ignoring(mockSpecificationLoader);
                ignoring(mockPersistenceSessionFactory);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);

                ignoring(mockContainer);
            }
        });

        sessionFactory = new IsisSessionFactoryDefault(DeploymentType.UNIT_TESTING, configuration, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockPersistenceSessionFactory, servicesList, oidMarshaller);
        authSession = new SimpleSession("tester", Collections.<String>emptyList());
        
        IsisContext.setConfiguration(configuration);
    }
    
    @After
    public void tearDown() throws Exception {
        if(IsisContext.inSession()) {
            IsisContext.closeSession();
        }
    }
    
    @Test
    public void getConfiguration() {
        IsisContextStatic.createRelaxedInstance(sessionFactory);
        Assert.assertEquals(configuration, IsisContext.getConfiguration());
    }

    @Test
    public void openSession_getSpecificationLoader() {
        IsisContextStatic.createRelaxedInstance(sessionFactory);
        IsisContext.openSession(authSession);

        Assert.assertEquals(mockSpecificationLoader, IsisContext.getSpecificationLoader());
    }

    @Test
    public void openSession_getAuthenticationLoader() {
        IsisContextStatic.createRelaxedInstance(sessionFactory);
        IsisContext.openSession(authSession);

        Assert.assertEquals(authSession, IsisContext.getAuthenticationSession());
    }
    
    @Test
    public void openSession_getPersistenceSession() {
        IsisContextStatic.createRelaxedInstance(sessionFactory);
        IsisContext.openSession(authSession);

        Assert.assertSame(mockPersistenceSession, IsisContext.getPersistenceSession());
    }


}
