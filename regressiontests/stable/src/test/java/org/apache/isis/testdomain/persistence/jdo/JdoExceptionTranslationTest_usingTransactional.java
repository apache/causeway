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
import javax.inject.Provider;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;

import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.exceptions.NucleusException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.persistence.jdo.spring.integration.DefaultJdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryDao;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                JdoInventoryDao.class,
        })
@TestPropertySources({
    @TestPropertySource(IsisPresets.UseLog4j2Test)    
})
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoExceptionTranslationTest_usingTransactional
{

    //@Inject private JdoSupportService jdoSupport;
    //@Inject private TransactionService transactionService;
    @Inject private RepositoryService repositoryService;
    @Inject private InteractionFactory interactionFactory;
    @Inject private Provider<JdoInventoryDao> uniqueConstraintViolator;
    @Inject private JdoTransactionManager txManager;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }
    
    @Test @Order(1) @Rollback(false)
    void booksUniqueByIsbn_setupPhase() {
        interactionFactory.runAnonymous(()->{
            
            _TestFixtures.setUp3Books(repositoryService);
            
        });
    }

    @Test @Order(2) @Rollback(false)
    void booksUniqueByIsbn_whenViolated_shouldThrowTranslatedException() {

        // when adding a book for which one with same ISBN already exists in the database,
        // we expect to see a Spring recognized DataAccessException been thrown 
                
        assertThrows(DataIntegrityViolationException.class, ()->{
        
            interactionFactory.runAnonymous(()->{
            
                Result.ofVoid(()->uniqueConstraintViolator.get().addBookHavingIsbnA())
                
                .ifSuccess(__->fail("expected to fail, but did not"))
                
                //XXX seems like a bug in DN, why do we need to unwrap this?
                .mapFailure(ex->ex instanceof IllegalArgumentException
                        ? ((IllegalArgumentException)ex).getCause()
                        : ex)
                
                // asserts we have a NucleusException
                .ifFailure(ex->assertTrue(ex instanceof NucleusException))
                
                //XXX this part of the translation is not done by Spring!? 
                // (converts to JDOException)
                .mapFailure(ex->ex instanceof NucleusException
                        ? NucleusJDOHelper
                                .getJDOExceptionForNucleusException(((NucleusException)ex))
                        : ex)
                
                // asserts translation to JDO standard
                .ifFailure(ex->assertTrue(ex instanceof JDODataStoreException))
                
                //XXX this part of the translation is not done by Spring!?
                // (converts to DataAccessException)
                .mapFailure(ex->ex instanceof JDOException
                        ? txManager.getJdoDialect().translateException((JDOException)ex)
                        : ex)
                
                .ifFailure(ex->assertTrue(ex instanceof DataIntegrityViolationException))
                
                .optionalElseFail();
            
            });
        
        });
        
    }    
    
    @Test @Order(3) @Rollback(false)
    void booksUniqueByIsbn_verifyPhase() {

        // expected post condition: ONE inventory with 3 books
        
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
        
    }
    
    @Test @Order(4) @Rollback(false)
    void booksUniqueByIsbn_cleanupPhase() {

        interactionFactory.runAnonymous(()->{

            _TestFixtures.cleanUp(repositoryService);
            
        });
        
    }
    
    
}
