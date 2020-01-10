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

package org.apache.isis.metamodel.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.IsisModuleCoreConfig;
import org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.isis.metamodel.services.registry.ServiceRegistryDefault;

import lombok.Getter;

@ActiveProfiles("test")
@SpringBootTest(classes = {
        IsisBeanFactoryPostProcessorForSpring.class,
        IsisModuleCoreConfig.class,
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorLegacyTest.Producers.class,
        ServiceInjectorDefaultTest_usingFields.Producers.class,
},
properties = {
        "isis.services.injector.setPrefix=true"
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class ServiceInjectorDefaultTest_usingFields {

    @Configuration
    static class Producers {

        private final int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

        @Inject AutowireCapableBeanFactory beanFactory;


        @Bean @Singleton
        A mockA() {
            return (A) beanFactory.autowire(A.class, autowireMode, true);
        }

        @Bean @Singleton
        B mockB() {
            return (B) beanFactory.autowire(B.class, autowireMode, true);
        }

        @Bean @Singleton
        C mockC() {
            return (C) beanFactory.autowire(C.class, autowireMode, true);
        }

    }

    // -- SCENARIO

    private D serviceD = new D();

    // managed
    static class C { }

    // managed
    static class A {
        @Inject @Getter private B_Abstract someB;
    }

    // managed
    static abstract class B_Abstract {
        @Inject @Getter private RepositoryService repositoryService;
    }

    // managed
    static class B extends B_Abstract {
        @Inject @Getter private C someC;
    }

    // not-managed
    static class D { 
        @Inject @Getter private A someA;    
        @Inject @Getter private B someB;
        @Inject @Getter private C someC;
    }

    // -- TESTS

    @Inject private ServiceInjector injector;

    @BeforeEach
    void setup() {

    }

    @Inject private A serviceA;
    @Inject private B serviceB;

    @Test
    void shouldInject_RepositoryService() {

        // managed
        assertThat(serviceA.getSomeB(), any(B_Abstract.class));
        assertThat(serviceB.getSomeC(), any(C.class));


        // not-managed
        injector.injectServicesInto(serviceD);

        assertThat(serviceD.getSomeA(), any(A.class));
        assertThat(serviceD.getSomeB(), any(B.class));
        assertThat(serviceD.getSomeC(), any(C.class));

    }

}
