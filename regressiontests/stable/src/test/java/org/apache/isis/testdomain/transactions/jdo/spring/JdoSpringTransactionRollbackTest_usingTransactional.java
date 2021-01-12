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

import java.util.HashSet;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdoSpring;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;

import lombok.val;

/**
 * These tests use the {@code @Transactional} annotation as provided by Spring.
 * <p> 
 * We test whether JUnit Tests are automatically rolled back by Spring. 
 */
@SpringBootTest(
        classes = { 
                Configuration_usingJdoSpring.class,
        },
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.*=DEBUG",
                "logging.level.org.springframework.test.context.transaction.*=DEBUG"
        })
@Transactional
@TestPropertySource(IsisPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoSpringTransactionRollbackTest_usingTransactional 
//extends IsisIntegrationTestAbstract 
{

    @Inject private RepositoryService repository;

    @Test @Order(1) @Commit
    void cleanup_justInCase() {
        // cleanup just in case
        repository.removeAll(JdoProduct.class);
        repository.removeAll(JdoInventory.class);
    }

    @Test @Order(2)
    void happyCaseTx_shouldCommit() {

        System.err.println("== ENTER TEST");

        // expected pre condition
        assertEquals(0, repository.allInstances(JdoBook.class).size());

        val products = new HashSet<JdoProduct>();

        products.add(JdoBook.of(
                "Sample Book", "A sample book for testing.", 99.,
                "Sample Author", "Sample ISBN", "Sample Publisher"));

        val inventory = JdoInventory.of("Sample Inventory", products);

        System.err.println("== ENTER PERSIST");

        repository.persist(inventory);

        System.err.println("== EXIT PERSIST");

        // expected post condition
        assertEquals(1, repository.allInstances(JdoBook.class).size());

        System.err.println("== EXIT TEST");

    }

    @Test @Order(3)
    void previousTest_shouldHaveBeenRolledBack() {

        // expected condition
        assertEquals(0, repository.allInstances(JdoBook.class).size());
    }

}
