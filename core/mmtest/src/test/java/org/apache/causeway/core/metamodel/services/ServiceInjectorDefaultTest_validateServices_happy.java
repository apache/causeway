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
package org.apache.causeway.core.metamodel.services;

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.services.inject.ServiceInjectorDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessor;
import org.apache.causeway.core.metamodel.services.registry.ServiceRegistryDefault;

@ActiveProfiles("test")
@SpringBootTest(classes = {
        CausewayModuleCoreConfig.class,
        ServiceInjectorDefault.class,
        ServiceRegistryDefault.class,
        ServiceInjectorLegacyTest.Producers.class,
        ServiceInjectorDefaultTest_validateServices_happy.DomainServiceWithSomeId.class,
        ServiceInjectorDefaultTest_validateServices_happy.DomainServiceWithDifferentId.class,

        CausewayBeanFactoryPostProcessor.class

},
properties = {
        "causeway.services.injector.setPrefix=true"
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class ServiceInjectorDefaultTest_validateServices_happy {

    // -- SCENARIO

    @DomainService @Component("someId") @Profile("test")
    public static class DomainServiceWithSomeId {

    }

    @DomainService @Component("otherId") @Profile("test")
    public static class DomainServiceWithDifferentId {

    }

    // -- TESTS

    @Inject private ServiceRegistry serviceRegistry;

    @Test
    void validate_DomainServicesWithoutDuplicateIds() {

        // ensure we actually test a ServiceRegistryDefault
        assertEquals(ServiceRegistryDefault.class, serviceRegistry.getClass());

        // nothing else to check
    }

}
