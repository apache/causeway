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

import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

//@EnableWeld
//TODO[2112] migrate to spring
class ServicesInjectorDefaultTest_validateServices_happy {
    
    // -- SCENARIO

    @DomainService
    public static class DomainServiceWithSomeId {
        public String getId() { return "someId"; }
    }

    @DomainService
    public static class DomainServiceWithDifferentId {
        public String getId() { return "otherId"; }
    }

//    @WeldSetup
//    public WeldInitiator weld = WeldInitiator.from(
//
//            BeansForTesting.builder()
//            .injector()
//            .add(DomainServiceWithSomeId.class)
//            .add(DomainServiceWithDifferentId.class)
//            .build()
//
//            )
//    .build();

    @Inject private ServiceRegistry serviceRegistry;

    @Test
    public void validate_DomainServicesWithoutDuplicateIds() {
        serviceRegistry.validateServices();
    }
    
    
}
