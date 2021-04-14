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
package org.apache.isis.testdomain.transactions.jdo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.interaction.InteractionBoundaryProbe;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                InteractionBoundaryProbe.class
        })
//@Transactional
@TestPropertySource(IsisPresets.UseLog4j2Test)
/**
 * With this test we manage IsisInteractions ourselves. (not sub-classing IsisIntegrationTestAbstract)
 */
class JdoTransactionScopeListenerTest {
    
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
     * then we have 1 InteractionScope with 3 expected Transactions within the test method
     *  
     */
    
    @BeforeEach
    void setUp() {
        
        // new InteractionScope with a new transaction (#1)
        isisInteractionFactory.runAnonymous(()->{
        
            // cleanup
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
            
        });
        
    }
    
    @Test
    void sessionScopedProbe_shouldBeReused_andBeAwareofTransactionBoundaries() {
        
        // new InteractionScope with a new transaction (#2)
        isisInteractionFactory.runAnonymous(()->{
            
            // expected pre condition
            // reuse transaction (#2)
            assertEquals(0, repository.allInstances(JdoBook.class).size());
        
            // reuse transaction (#2)
            transactionService.runWithinCurrentTransactionElseCreateNew(()->{
                
                fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
                
            });
            
            // expected post condition
            // reuse transaction (#2)
            assertEquals(1, repository.allInstances(JdoBook.class).size());
            
        });
        
        assertEquals(2, InteractionBoundaryProbe.totalInteractionsStarted(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalInteractionsEnded(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalTransactionsEnding(kvStoreForTesting));
        assertEquals(2, InteractionBoundaryProbe.totalTransactionsCommitted(kvStoreForTesting));

    }
    

}
