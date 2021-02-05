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
package org.apache.isis.testdomain.persistence.jdo;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.jdo.JDOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
        })
@TestPropertySources({
    @TestPropertySource(IsisPresets.UseLog4j2Test)    
})
//@Transactional ... we manage transaction ourselves
class JdoExceptionRecognizerTest 
//extends IsisIntegrationTestAbstract ... we manage interactions ourselves
{

    // @Inject private JdoSupportService JdoSupport;
    
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repositoryService;
    @Inject private InteractionFactory interactionFactory;
    @Inject private JdoTransactionManager txManager;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Disabled("yet does not throw") //TODO 
    void booksUniqueByIsbn_whenViolated_shouldThrowRecognizedException() {

        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            
            interactionFactory.runAnonymous(()->{
            
                _TestFixtures.setUp3Books(repositoryService);
                
            });
            
            
        });
        
        // when adding a book for which one with same ISBN already exists in the database,
        // we should expect to see a Spring recognized DataAccessException been thrown 
        
        assertThrows(DataAccessException.class, ()->{
        
            try {
            
                //FIXME does not throw instead just shows warning
                // WARN 4664 --- [           main] D.D.Persist                              : Insert of object "org.apache.isis.testdomain.jdo.entities.JdoBook@224f90eb" using statement "INSERT INTO "testdomain"."JdoProduct" ("id","description","name","price","author","isbn","publisher","DISCRIMINATOR","products_id_OID") VALUES (?,?,?,?,?,?,?,?,?)" failed : Unique index or primary key violation: "testdomain.JdoBook_isbn_UNQ_INDEX_E ON testdomain.JdoProduct(isbn) VALUES -1"; SQL statement:
                // INSERT INTO "testdomain"."JdoProduct" ("id","description","name","price","author","isbn","publisher","DISCRIMINATOR","products_id_OID") VALUES (?,?,?,?,?,?,?,?,?) [23505-200]

                transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
                    
                    interactionFactory.runAnonymous(()->{
                    
                        // given
                        
                        val inventories = repositoryService.allInstances(JdoInventory.class);
                        assertEquals(1, inventories.size());
                        
                        val inventory = inventories.get(0);
                        assertNotNull(inventory);
                        
                        
                        // add a conflicting book (unique ISBN violation)
                        _TestFixtures.addABookTo(inventory);
                    
                    });
        
                });
                
            } catch (RuntimeException ex) {
                
                // TODO this catch and throw logic should be done by the TransactionService instead
                val translatedEx = 
                        _Exceptions.streamCausalChain(ex)
                        .filter(e->e instanceof JDOException)
                        .map(JDOException.class::cast)
                        // call Spring's exception translation mechanism (thats our fork)
                        .<RuntimeException>map(nextEx->txManager.getJdoDialect().translateException(nextEx))
                        .filter(nextEx -> nextEx instanceof DataAccessException)
                        .findFirst()
                        .orElse(ex);
                
                throw translatedEx;
                
            }
            
        });
        
        // expected post condition: ONE inventory with 3 books
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            
            interactionFactory.runAnonymous(()->{
            
                val inventories = repositoryService.allInstances(JdoInventory.class);
                assertEquals(1, inventories.size());
                
                val inventory = inventories.get(0);
                assertNotNull(inventory);
                
                assertNotNull(inventory);
                assertNotNull(inventory.getProducts());
                assertEquals(3, inventory.getProducts().size());

                _TestFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
                
            });
            
            
        });

        
    }    
}
