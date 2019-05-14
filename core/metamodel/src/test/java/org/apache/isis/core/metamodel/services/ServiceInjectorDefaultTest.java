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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

//import org.jmock.Expectations;
//import org.jmock.auto.Mock;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(classes = {
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorDefaultTest.Producers.class,
})
class ServiceInjectorDefaultTest {

    // -- SPRING SETUP
    
    @Configuration
    @Profile("test")
    static class Producers {
        
        @Bean
        IsisConfiguration getConfiguration() {
            return _Config.getConfiguration();
        }
        
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

    public static interface SomeDomainObject {
        public void setRepositoryService(RepositoryService container);
        public void setMixinService(MixinService mixin);
        public void setService1(Service1 service);
        public void setService2(Service2 service);
    }
    
    @MockBean SomeDomainObject mockDomainObject;
    
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

        injector.injectServicesInto(mockDomainObject);
        
        verify(mockDomainObject, times(1)).setRepositoryService(any(RepositoryService.class));
        //FIXME[2112] does not get injected ... 
        //verify(mockDomainObject, times(1)).setMixinService(any(MixinService.class));
        verify(mockDomainObject, times(1)).setService1(any(Service1.class));
        verify(mockDomainObject, times(1)).setService2(any(Service2.class));
        
    }
    
    @Test
    void shouldStreamRegisteredServices() {
        long registeredServiceCount = registry.streamRegisteredBeans()
                .count();
        assertTrue(registeredServiceCount>=3);
    }

}
