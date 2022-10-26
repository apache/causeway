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
package org.apache.causeway.testdomain.transactions.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures.Lock;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class JdoTransactionRollbackTest_usingTransactionService {

    @Inject private JdoTestFixtures jdoTestFixtures;
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repository;

    private Lock lock;

    @BeforeEach
    void setUp() {
        // clear repository
        lock = jdoTestFixtures.clearAndAquireLock();
    }

    @AfterEach
    void restore() {
        lock.release();
    }

    @Test
    void happyCaseTx_shouldCommit() {

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{
            // expected pre condition
            assertEquals(0, repository.allInstances(JdoBook.class).size());

            jdoTestFixtures.install(lock);

            // expected post condition
            assertEquals(1, repository.allInstances(JdoBook.class).size());
        });

    }

    @Test
    void whenExceptionWithinTx_shouldRollback() {

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{
            // expected pre condition
            assertEquals(0, repository.allInstances(JdoBook.class).size());
        });

        val result = transactionService.runWithinCurrentTransactionElseCreateNew(()->{
            //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
            jdoTestFixtures.install(lock);
            throw _Exceptions.unrecoverable("Test: force current tx to rollback");
        });

        assertTrue(result.isFailure());

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{
            // expected post condition
            assertEquals(0, repository.allInstances(JdoBook.class).size());
        });

    }


}
