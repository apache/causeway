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

import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import lombok.Data;

@Disabled("legacy injector is no longer used")
class ServiceInjectorLegacyTest {

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

    @Data
    public static class SomeDomainObject {
        private RepositoryService a;
        private MixinService b;
        private Service1 c;
        private Service2 d;
    }


}
