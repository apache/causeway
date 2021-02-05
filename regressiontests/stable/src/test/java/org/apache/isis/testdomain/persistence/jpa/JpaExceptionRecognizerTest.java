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
package org.apache.isis.testdomain.persistence.jpa;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.testdomain.persistence.jpa._TestFixtures.assertInventoryHasBooks;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_usingJpa.class,
        }
        )
@TestPropertySource(IsisPresets.UseLog4j2Test)
//@Transactional
class JpaExceptionRecognizerTest extends IsisIntegrationTestAbstract {

    // @Inject private JpaSupportService jpaSupport;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Disabled("yet does not throw") //TODO 
    void booksUniqueByIsbn_whenViolated_shouldThrowRecognizedException() {

        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{

            _TestFixtures.setUp3Books(repositoryService);
            
        });
        
        // given
        
        val inventories = repositoryService.allInstances(JpaInventory.class);
        assertEquals(1, inventories.size());
        
        val inventory = inventories.get(0);
        assertNotNull(inventory);
        
        // when adding a book for which one with same ISBN already exists in the database,
        // we should expect to see a recognized Exception been thrown 
        
        assertThrows(Exception.class, ()->{
            
            transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
                
                // add a conflicting book (unique ISBN violation)
                _TestFixtures.addABookTo(inventory);
    
            });
            
        });
        
        // expected post condition: ONE inventory with 3 books

        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
    }
    
}
