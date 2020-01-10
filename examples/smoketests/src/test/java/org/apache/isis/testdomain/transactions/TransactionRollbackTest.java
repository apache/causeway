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

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
class TransactionRollbackTest {
    
    @Inject FixtureScripts fixtureScripts;
    @Inject TransactionService transactionService;
    @Inject RepositoryService repository;
    
    @BeforeEach
    void setUp() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

    }
    
    @Test
    void happyCaseTx_shouldCommit() {
        
        // expected pre condition
        assertEquals(0, repository.allInstances(Book.class).size());
        
        transactionService.executeWithinTransaction(()->{
            
            fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
            
        });
        
        // expected post condition
        assertEquals(1, repository.allInstances(Book.class).size());
        
    }
    
    @Test
    void whenExceptionWithinTx_shouldRollback() {
        
        // expected pre condition
        assertEquals(0, repository.allInstances(Book.class).size());
        
        assertThrows(RuntimeException.class, ()->{
            
            transactionService.executeWithinTransaction(()->{
                
                fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

                throw _Exceptions.unrecoverable("Test: force current tx to rollback");            
                
            });    
            
        });
        
        // expected post condition
        assertEquals(0, repository.allInstances(Book.class).size());
        
    }
    

}
