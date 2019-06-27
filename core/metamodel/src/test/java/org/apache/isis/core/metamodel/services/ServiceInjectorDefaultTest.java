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

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@ActiveProfiles("test")
@SpringBootTest(classes = {
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorDefaultTest.Producers.class,
    },
    properties = {
            "isis.services.injector.setPrefix=true"
    })
class ServiceInjectorDefaultTest {

    // -- SPRING SETUP
    
    @Configuration
    @Profile("test")
    static class Producers {
        
        @Bean
        InjectorMethodEvaluator getInjectorMethodEvaluator() {
            return new InjectorMethodEvaluatorDefault();
        }
        
        @Bean
        SomeDomainObject mockDomainObject() {
            return Mockito.mock(SomeDomainObject.class);
        }
        
        @Bean
        RepositoryService mockRepositoryService() {
            return Mockito.mock(RepositoryServiceExtended.class);
        }
        
        @Bean
        MixinService mockMixin() {
            return Mockito.mock(MixinService.class);
        }
        
        @Bean
        Service1 mockService1() {
            return Mockito.mock(Service1.class);
        }
        
        @Bean
        Service2 mockService2() {
            return Mockito.mock(Service2.class);
        }
        
    }

    // -- SCENARIO
    
    public static interface Service1 {
    }

    public static interface Service2 {
    }

    public static interface MixinService {
    }

    public static interface RepositoryServiceExtended extends RepositoryService, MixinService {
    }

    @Getter @Setter
    public static class SomeDomainObject {
        private RepositoryService a;
        private MixinService b;
        private Service1 c;
        private Service2 d;
    }
    
    // -- TESTS
    
    @Inject private ServiceInjector injector;
    @Inject private ServiceRegistry registry;
    @Inject private ApplicationContext applicationContext;
    
    @BeforeEach
    void setup() {
        if(!_Spring.isContextAvailable()) {
            _Spring.init(applicationContext);    
        }
    }

    @Test
    void shouldInject_RepositoryService() {

        val mockDomainObject = new SomeDomainObject();
        
        injector.injectServicesInto(mockDomainObject, onNotResolvable->{
            // ignore, checked below
        });

        
        assertNotNull(mockDomainObject.getA());
        //FIXME[2112] does not get injected ... 
        //assertNotNull(mockDomainObject.getB());
        assertNotNull(mockDomainObject.getC());
        assertNotNull(mockDomainObject.getD());
        
    }
    
    @Test
    void shouldStreamRegisteredServices() {
        long registeredServiceCount = registry.streamRegisteredBeans()
                .count();
        assertTrue(registeredServiceCount>=3);
    }

}
