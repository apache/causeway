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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorDefaultTest.Producers.class,
        ServiceInjectorDefaultTest_validateServices_happy.DomainServiceWithSomeId.class,
        ServiceInjectorDefaultTest_validateServices_happy.DomainServiceWithDifferentId.class
})
class ServiceInjectorDefaultTest_validateServices_happy {
    
    // -- SCENARIO

    @DomainService @Component("someId")
    public static class DomainServiceWithSomeId {
     
    }

    @DomainService @Component("otherId")
    public static class DomainServiceWithDifferentId {
     
    }

    // -- TESTS
    
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private ApplicationContext applicationContext;
    
    @BeforeEach
    void setup() {
        _Context.clear();
        _Spring.init(applicationContext);    
    }

    @Test
    public void validate_DomainServicesWithoutDuplicateIds() {
        
        // ensure we actually test a ServiceRegistryDefault 
        assertEquals(ServiceRegistryDefault.class, serviceRegistry.getClass());
        
        // nothing else to check
    }
    
    
}
