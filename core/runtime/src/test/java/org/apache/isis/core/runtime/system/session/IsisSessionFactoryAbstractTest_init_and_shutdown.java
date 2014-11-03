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
import com.google.common.collect.Lists;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class IsisSessionFactoryAbstractTest_init_and_shutdown {

    public static class DomainServiceWithSomeId {
        public String getId() { return "someId"; }
    }

    public static class DomainServiceWithDuplicateId {
        public String getId() { return "someId"; }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private DeploymentType mockDeploymentType;
    @Mock
    private SpecificationLoaderSpi mockSpecificationLoader;
    @Mock
    private AuthenticationManager mockAuthenticationManager;
    @Mock
    private AuthorizationManager mockAuthorizationManager;
    @Mock
    private PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    private OidMarshaller mockOidMarshaller;
    
    @Mock
    private DomainObjectContainer mockContainer;
    
    private IsisConfigurationDefault configuration;
    private List<Object> serviceList;

    private IsisSessionFactory isf;
    
    @Before
    public void setUp() throws Exception {
        configuration = new IsisConfigurationDefault();
        configuration.add("foo", "bar");
        
        serviceList = Lists.newArrayList();
        context.ignoring(mockDeploymentType, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockContainer, mockPersistenceSessionFactory, mockOidMarshaller);
    }
    

    @Test(expected=IllegalStateException.class)
    public void validate_DomainServicesWithDuplicateIds() {
        serviceList.add(new DomainServiceWithSomeId());
        serviceList.add(new DomainServiceWithDuplicateId());
        isf = new IsisSessionFactoryDefault(mockDeploymentType, configuration, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockPersistenceSessionFactory, serviceList, mockOidMarshaller) {
        };
    }
}
