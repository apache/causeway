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
package org.apache.isis.testdomain.transactions.jdo.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.testdomain.conf.Configuration_usingJdoSpring;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.interaction.InteractionBoundaryProbe;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@SpringBootTest(
        classes = { 
                Configuration_usingJdoSpring.class,
                InteractionBoundaryProbe.class
        },
        properties = {
                "logging.level.org.apache.isis.testdomain.util.interaction.InteractionBoundaryProbe=DEBUG",
                "logging.level.org.apache.isis.core.interaction.scope.IsisInteractionScope=DEBUG",
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
//@Transactional
/**
 * With this test we manage IsisInteractions ourselves. (not sub-classing IsisIntegrationTestAbstract)
 */
class JdoSpringTransactionScopeListenerTest {
    
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repository;
    @Inject private InteractionFactory isisInteractionFactory;
    @Inject private KVStoreForTesting kvStoreForTesting;
    
    /* Expectations:
     * 1. for each InteractionScope there should be a new InteractionBoundaryProbe instance
     * 2. for each Transaction the current InteractionBoundaryProbe should get notified
     * 
     * first we have 1 InteractionScope with 1 expected Transaction during 'setUp'
     * then we have 1 InteractionScope with 1 expected Transaction within the test method
     *  
     */
    
    @BeforeEach
    void setUp() {
        
        // new IsisInteractionScope with a new transaction (#1)
        isisInteractionFactory.runAnonymous(()->{
            
            // request an InteractionBoundaryProbe for the current interaction
            serviceRegistry.lookupServiceElseFail(InteractionBoundaryProbe.class);
        
            // cleanup
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
            
        });
        
    }
    
    @Test
    void sessionScopedProbe_shouldBeReused_andBeAwareofTransactionBoundaries() {
        
        // new IsisInteractionScope with a new transaction (#2)
        isisInteractionFactory.runAnonymous(()->{
            
            // request an InteractionBoundaryProbe for the current interaction
            serviceRegistry.lookupServiceElseFail(InteractionBoundaryProbe.class);
            
            // expected pre condition
            // reuse existing transaction (#2)
            assertEquals(0, repository.allInstances(JdoBook.class).size());
        
            // reuse existing transaction (#2)
            transactionService.runWithinCurrentTransactionElseCreateNew(()->{
                
                fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
                
            });
            
            // expected post condition
            // reuse existing transaction (#2)
            assertEquals(1, repository.allInstances(JdoBook.class).size());
            
        });
        
        assertEquals(2, InteractionBoundaryProbe.totalInteractionsStarted(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalInteractionsEnded(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalTransactionsStarted(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalTransactionsEnded(kvStoreForTesting));

    }
    

}
