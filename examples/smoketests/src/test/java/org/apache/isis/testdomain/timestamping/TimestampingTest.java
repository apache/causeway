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
package org.apache.isis.testdomain.timestamping;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.Product;
import org.apache.isis.testdomain.jdo.ProductComment;

import static org.junit.Assert.assertNotNull;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class
        }, 
        properties = {
//                "logging.config=log4j2-debug-persistence.xml",
//                IsisPresets.DebugPersistence,
        })
@Transactional
class TimestampingTest {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;

    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test
    void updatedByAt_shouldBeFilledInByTheTimestampingService() {
        
        val product = repository.allInstances(Product.class).listIterator().next();
        assertNotNull(product);

        val comment = new ProductComment();
        comment.setProduct(product);
        comment.setComment("Awesome Book!");

        repository.persist(comment);
            
        assertNotNull(comment.getUpdatedAt());
        assertNotNull(comment.getUpdatedBy());
        
    }

}
