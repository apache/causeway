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

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.repository.RepositoryService;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertThat;

import lombok.Getter;

//@EnableWeld
//TODO[2112] migrate to spring
class ServicesInjectorDefaultTest_usingFields {


    static class Mocks {
        
        @Produces
        RepositoryService mockRepositoryService() {
            return Mockito.mock(RepositoryService.class);
        }
        
    }
    
//    @WeldSetup
//    public WeldInitiator weld = WeldInitiator.from(
//            
//            BeansForTesting.builder()
//            .injector()
//            .addAll(
//                    Mocks.class,
//                    A.class,
//                    B.class,
//                    C.class
//                    )
//            .build()
//            
//            )
//    .build();

    @Inject private ServiceInjector injector;

    // -- SCENARIO
    
    @Inject private A service1;
    @Inject private B service2;
    
    private D service4 = new D();

    // managed
    static class C { }

    // managed
    static class A {
        @Inject @Getter private B_Abstract someB;
    }

    // managed
    static abstract class B_Abstract {
        @Inject @Getter private RepositoryService container;
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
    

    @Test
    public void shouldInjectContainer() {

        // managed
        assertThat(service1.getSomeB(), any(B_Abstract.class));
        assertThat(service2.getSomeC(), any(C.class));
        
        
        // not-managed
        injector.injectServicesInto(service4);
        
        assertThat(service4.getSomeA(), any(A.class));
        assertThat(service4.getSomeB(), any(B.class));
        assertThat(service4.getSomeC(), any(C.class));
                
    }

}
