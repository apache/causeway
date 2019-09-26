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

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.config.IsisPresets;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These tests use the {@code @Transactional} annotation as provided by Spring.
 * <p> 
 * We test whether JUnit Tests are automatically rolled back by Spring. 
 */
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                IsisPresets.DebugPersistence,
        })
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionRollbackTest_withTransactional {
    
    @Inject FixtureScripts fixtureScripts;
    @Inject TransactionService transactionService;
    @Inject RepositoryService repository;
    
    @Test @Order(1)
    void happyCaseTx_shouldCommit() {
        
        // cleanup just in case
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        
        // expected pre condition
        assertEquals(0, repository.allInstances(Book.class).size());
            
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
        
        // expected post condition
        assertEquals(1, repository.allInstances(Book.class).size());
        
    }
    
    @Test @Order(2)
    void previousTest_shouldHaveBeenRolledBack() {
        
        
        
        // expected condition
        assertEquals(0, repository.allInstances(Book.class).size());
    }

}
