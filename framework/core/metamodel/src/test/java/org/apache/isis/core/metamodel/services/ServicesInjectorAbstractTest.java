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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.DomainObjectContainer;

@RunWith(JMock.class)
public class ServicesInjectorAbstractTest {

    private final Mockery mockery = new JUnit4Mockery();

    private ServicesInjectorAbstract injector;
    private DomainObjectContainerExtended mockContainer;
    private Service1 mockService1;
    private Service2 mockService2;

    private SomeDomainObject mockDomainObject;

    static interface Service1 {
    }

    static interface Service2 {
    }

    static interface Mixin {
    }

    static interface DomainObjectContainerExtended extends DomainObjectContainer, Mixin {
    }

    static interface SomeDomainObject {
        public void setContainer(DomainObjectContainer container);

        public void setMixin(Mixin mixin);

        public void setService1(Service1 service);

        public void setService2(Service2 service);
    }

    @Before
    public void setUp() throws Exception {
        mockDomainObject = mockery.mock(SomeDomainObject.class);
        mockContainer = mockery.mock(DomainObjectContainerExtended.class);
        mockService1 = mockery.mock(Service1.class);
        mockService2 = mockery.mock(Service2.class);
        injector = new ServicesInjectorAbstract() {
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldInjectContainer() {
        injector.setContainer(mockContainer);
        final Object[] services = { mockService1, mockService2 };
        injector.setServices(Arrays.asList(services));

        mockery.checking(new Expectations() {
            {
                one(mockDomainObject).setContainer(mockContainer);
                one(mockDomainObject).setMixin(mockContainer);
                one(mockDomainObject).setService1(mockService1);
                one(mockDomainObject).setService2(mockService2);
            }
        });

        injector.injectDependenciesInto(mockDomainObject);
    }

}
