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
package org.apache.isis.testdomain.entitychangemetrics;

import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.auditing.AuditerServiceForTesting;
import org.apache.isis.testdomain.auditing.Configuration_usingAuditing;
import org.apache.isis.testdomain.commons.InteractionBoundaryProbe;
import org.apache.isis.testdomain.commons.InteractionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryManager;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingAuditing.class,
                InteractionBoundaryProbe.class,
        }, 
        properties = {
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG"
        })
@TestPropertySource({
    IsisPresets.SilenceWicket
    ,IsisPresets.UseLog4j2Test
})
class ChangedObjectsTest extends InteractionTestAbstract {
    
    @Inject protected FixtureScripts fixtureScripts;

    @BeforeEach
    void setUp() {
        
        System.err.println("===BEFORE SETUP");

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);
        
        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

        // each test runs in its own interaction context (check)
        val testRunNr = kvStoreForTesting.incrementCounter(ChangedObjectsTest.class, "test-run");
        assertEquals(testRunNr, InteractionBoundaryProbe.totalInteractionsStarted(kvStoreForTesting));
        
        assertJdoBookCreateAudits();
        
        System.err.println("===AFTER SETUP");
    }
    
    @Test
    void wrapperInvocation_shouldSpawnSingleTransaction() {

        // given
        val book = getBookSample();
        val inventoryManager = factoryService.create(JdoInventoryManager.class);
        
        System.err.println("=1==BEFORE  TX");
        
        // spawns its own transactional boundary (check)
        val product = assertTransactional( 
                ()->wrapper.wrap(inventoryManager).updateProductPrice(book, 12.));
        
        System.err.println("=1==AFTER  TX");
        
        assertEquals(12., product.getPrice(), 1E-3);
        
        // previous transaction has committed, so ChangedObjectsService should have cleared its data  
        assertTrue(getChangedObjectsService().getChangedObjectProperties().isEmpty());
        
        assertNoChangedObjectsPending();
        assertJdoBookPriceChangeAudit();
        // dumpAuditsAndClear(); // debug
    }

    @Test 
    void actionInteraction_shouldSpawnSingleTransaction() {
        
        // given
        val book = getBookSample();

        System.err.println("=2==BEFORE  TX");
        
        // spawns its own transactional boundary (check) 
        val product = (JdoProduct) assertTransactional( 
                ()->invokeAction(JdoInventoryManager.class, "updateProductPrice", _Lists.of(book, 12.)));
        
        System.err.println("=2==AFTER  TX");
        
        assertEquals(12., product.getPrice(), 1E-3);                
        
        assertNoChangedObjectsPending();
        assertJdoBookPriceChangeAudit();
        // dumpAuditsAndClear(); // debug
    }
    
    // -- HELPER
    
    private JdoBook getBookSample() {
        
        System.err.println("===BEFORE BOOK");
        
        // spawns its own transactional boundary (check)
        val books = assertTransactional(()->repositoryService.allInstances(JdoBook.class));
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        
        assertEmptyAudits(); // query only, no entity changes expected
        
        System.err.println("===AFTER BOOK");
        return book;
    }
    
    @SuppressWarnings("unused")
    private void dumpAuditsAndClear() {
        val audits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting);
        System.err.println("==AUDITS==");
        audits.forEach(System.err::println);
        System.err.println("==========");
        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);
    }

    private void assertEmptyAudits() {
        val audits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting);
        assertTrue(audits.isEmpty());
    }
    
    private void assertNoChangedObjectsPending() {
        // previous transaction has committed, so ChangedObjectsService should have cleared its data
        // however, this call has side-effects, it locks current transaction's capacity of further enlisting changes
        //assertTrue(getChangedObjectsService().getChangedObjectProperties().isEmpty());
    }
    
    private void assertJdoBookCreateAudits() {
        
        val expectedAudits = _Sets.ofSorted(
        "Jdo Book/author: '[NEW]' -> 'Sample Author'",
        "Jdo Book/price: '[NEW]' -> '99.0'",
        "Jdo Book/publisher: '[NEW]' -> 'Sample Publisher'",
        "Jdo Book/isbn: '[NEW]' -> 'Sample ISBN'",
        "Jdo Book/description: '[NEW]' -> 'A sample book for testing.'",
        "Jdo Book/name: '[NEW]' -> 'Sample Book'",
        "Jdo Inventory/name: '[NEW]' -> 'Sample Inventory'");
        
        val actualAudits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));
        
        assertEquals(expectedAudits, actualAudits);
        
        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);
        
    }
    
    private void assertJdoBookPriceChangeAudit() {
        
        val expectedAudits = _Sets.ofSorted(
        "Jdo Book/price: '99.0' -> '12.0'");
        
        val actualAudits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));
        
        assertEquals(expectedAudits, actualAudits);
        
        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);
    }
    
}



