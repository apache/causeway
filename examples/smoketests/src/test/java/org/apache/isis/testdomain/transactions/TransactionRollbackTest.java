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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = { 
                JdoTestDomainModule.class,
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                "logging.level.org.apache.isis.jdo.persistence.IsisPlatformTransactionManagerForJdo=DEBUG",
                "logging.level.org.apache.isis.jdo.persistence.PersistenceSession5=DEBUG",
                "logging.level.org.apache.isis.jdo.persistence.IsisTransactionJdo=DEBUG",
        })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionRollbackTest {
    
    @Inject FixtureScripts fixtureScripts;
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repository;
    
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
        
        System.out.println("!!! ROLLBACK TEST TX IN");
        
        transactionService.executeWithinTransaction(()->{
        
            System.out.println("!!! PRE FIXTURE");
            
            fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
            
            System.out.println("!!! POST FIXTURE");
            
            throw _Exceptions.unrecoverable("Test: force current tx to rollback");            
            
        });
        
        System.out.println("!!! ROLLBACK TEST TX OUT");
        
        // expected post condition
        assertEquals(0, repository.allInstances(Book.class).size());
        
    }
    

}
