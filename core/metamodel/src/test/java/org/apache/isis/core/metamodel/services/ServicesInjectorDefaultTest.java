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

package org.apache.isis.core.metamodel.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ServicesInjectorDefaultTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private RepositoryServiceExtended mockRepository;
    @Mock
    private Service1 mockService1;
    @Mock
    private Service2 mockService2;
    @Mock
    private SomeDomainObject mockDomainObject;

    private ServicesInjector injector;

    public static interface Service1 {
    }

    public static interface Service2 {
    }

    public static interface Mixin {
    }

    public static interface RepositoryServiceExtended extends RepositoryService, Mixin {
    }

    public static interface SomeDomainObject {
        public void setContainer(RepositoryService container);
        public void setMixin(Mixin mixin);
        public void setService1(Service1 service);
        public void setService2(Service2 service);
    }

    @Before
    public void setUp() throws Exception {
        final Object[] services = { mockRepository, mockService1, mockService2 };

        IsisConfigurationDefault stubConfiguration = new IsisConfigurationDefault();
        injector = ServicesInjector.forTesting(
        		Arrays.asList(services), stubConfiguration, new InjectorMethodEvaluatorDefault());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldInjectContainer() {

        context.checking(new Expectations() {
            {
                oneOf(mockDomainObject).setContainer(mockRepository);
                oneOf(mockDomainObject).setMixin(mockRepository);
                oneOf(mockDomainObject).setService1(mockService1);
                oneOf(mockDomainObject).setService2(mockService2);
            }
        });

        injector.injectServicesInto(mockDomainObject);
    }
    
    @Test
    public void shouldStreamRegisteredServices() {
        List<Class<?>> registeredServices = injector.streamServiceTypes().collect(Collectors.toList());
        Assert.assertEquals(3, registeredServices.size());
    }

}
