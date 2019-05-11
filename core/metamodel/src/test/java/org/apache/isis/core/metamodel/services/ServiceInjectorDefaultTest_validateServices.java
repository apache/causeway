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

import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;

@SpringBootTest(classes = {
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorDefaultTest.Producers.class,
})
class ServiceInjectorDefaultTest_validateServices {

    // -- SCENARIO

    @DomainService
    public static class DomainServiceWithSomeId {
        public String getId() { return "someId"; }
    }

    @DomainService
    public static class DomainServiceWithDuplicateId {
        public String getId() { return "someId"; }
    }

    // -- TESTS

    @Inject private ServiceRegistry serviceRegistry;

    @Test
    public void validate_DomainServicesWithDuplicateIds() {
        Assertions.assertThrows(NoSuchElementException.class, serviceRegistry::validateServices);
    }

}
