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

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures.Lock;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;

/**
 * These tests use the {@code @Transactional} annotation as provided by Spring.
 * <p>
 * We test whether JUnit Tests are automatically rolled back by Spring.
 */
@SpringBootTest(
        classes = {
                Configuration_usingJdo.class
        },
        properties = {
                "logging.level.org.apache.causeway.persistence.jdo.*=DEBUG",
                "logging.level.org.springframework.test.context.transaction.*=DEBUG",
                "logging.level.org.datanucleus.*=DEBUG",
                "logging.config=log4j2-debug-persistence.xml"

        })
@Transactional
//@TestPropertySource(CausewayPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoTransactionRollbackTest_usingTransactional {

    @Inject private JdoTestFixtures jdoTestFixtures;
    @Inject private RepositoryService repository;
    @Inject private InteractionService interactionService;
    private static Lock lock;

    @Test @Order(1) @Commit
    void clearRepository() {
        // clear repository
        lock = jdoTestFixtures.clearAndAquireLock();
    }

    @Test @Order(2)
    void happyCaseTx_shouldCommit() {

        _Probe.errOut("before interaction");

        interactionService.runAnonymous(()->{

            // expected pre condition
            assertEquals(0, repository.allInstances(JdoBook.class).size());

            _Probe.errOut("before fixture");

            jdoTestFixtures.install(lock);
            //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

            _Probe.errOut("after fixture");

            // expected post condition
            assertEquals(3, repository.allInstances(JdoBook.class).size());


        });

        _Probe.errOut("after interaction");

    }

    @Test @Order(3)
    void previousTest_shouldHaveBeenRolledBack() {

        interactionService.runAnonymous(()->{

            // expected condition
            assertEquals(0, repository.allInstances(JdoBook.class).size());

        });

    }

    @Test @Order(4) @Commit
    void restoreDefaultCondition() {
        lock.release();
    }

}
