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
package org.apache.isis.testdomain.transactions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                TransactionScopeListenerTest.IsisInteractionScopedProbe.class
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
/**
 * With this test we manage IsisInteractions ourselves. (not sub-classing IsisIntegrationTestAbstract)
 */
class TransactionScopeListenerTest {
    
    @Inject private FixtureScripts fixtureScripts;
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repository;
    @Inject private IsisInteractionFactory isisInteractionFactory;
    @Inject private KVStoreForTesting kvStoreForTesting;
    
    /* Expectations:
     * 1. for each IsisInteractionScope there should be a new IsisInteractionScopedProbe instance
     * 2. for each Transaction the current IsisInteractionScopedProbe should get notified
     * 
     * first we have 1 IsisInteractionScope with 1 expected Transaction during 'setUp'
     * then we have 1 IsisInteractionScope with 3 expected Transactions within the test method
     *  
     */
    
    @Service
    @IsisInteractionScope
    public static class IsisInteractionScopedProbe implements TransactionScopeListener {

        @Inject private KVStoreForTesting kvStoreForTesting;
        
        @PostConstruct
        public void init() {
            kvStoreForTesting.incrementCounter(IsisInteractionScopedProbe.class, "init");
        }
        
        @PreDestroy
        public void destroy() {
            kvStoreForTesting.incrementCounter(IsisInteractionScopedProbe.class, "destroy");
        }
        
        @Override
        public void onTransactionEnded() {
            kvStoreForTesting.incrementCounter(IsisInteractionScopedProbe.class, "tx");
        }
        
    }
    
    @BeforeEach
    void setUp() {
        
        // new IsisInteractionScope with a new transaction (#1)
        isisInteractionFactory.runAnonymous(()->{
        
            // cleanup
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
            
        });
        
        
    }
    
    @Test
    void sessionScopedProbe_shouldBeReused_andBeAwareofTransactionBoundaries() {
        
        // new IsisInteractionScope
        isisInteractionFactory.runAnonymous(()->{
            
            // expected pre condition
            // new transaction (#2)
            assertEquals(0, repository.allInstances(JdoBook.class).size());
        
            // new transaction (#3)
            transactionService.executeWithinTransaction(()->{
                
                fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
                
            });
            
            // expected post condition
            // new transaction (#4)
            assertEquals(1, repository.allInstances(JdoBook.class).size());
            
        });
        
        long totalTransactions = kvStoreForTesting.getCounter(IsisInteractionScopedProbe.class, "tx");
        long totalSessionScopesInitialized = kvStoreForTesting.getCounter(IsisInteractionScopedProbe.class, "init");
        long totalSessionScopesDestroyed = kvStoreForTesting.getCounter(IsisInteractionScopedProbe.class, "destroy");
        
        assertEquals(4, totalTransactions);
        assertEquals(2, totalSessionScopesInitialized);
        assertEquals(2, totalSessionScopesDestroyed);

    }
    

    

}
