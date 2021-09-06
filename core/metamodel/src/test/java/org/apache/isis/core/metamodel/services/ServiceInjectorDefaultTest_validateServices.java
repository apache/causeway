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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("test")
@SpringBootTest(classes = {
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorLegacyTest.Producers.class,
        ServiceInjectorDefaultTest_validateServices.DomainServiceWithSomeId.class,
        ServiceInjectorDefaultTest_validateServices.DomainServiceWithDuplicateId.class
})
@Disabled("Spring takes care of ambiguity and should not boot up this test.")
class ServiceInjectorDefaultTest_validateServices {

    // -- SCENARIO

    @DomainService @Component("someId") @Profile("test")
    public static class DomainServiceWithSomeId {

    }

    @DomainService @Component("someId") @Profile("test")
    public static class DomainServiceWithDuplicateId {

    }

    // -- TESTS

    @Inject private ServiceRegistry serviceRegistry;

    @BeforeEach
    void setup() {
        _Context.clear();
    }

    @Test 
    public void validate_DomainServicesWithDuplicateIds() {

        // ensure we actually test a ServiceRegistryDefault 
        assertEquals(ServiceRegistryDefault.class, serviceRegistry.getClass());

        fail("Spring Boot should not provision the given scenario."); 
    }

}
