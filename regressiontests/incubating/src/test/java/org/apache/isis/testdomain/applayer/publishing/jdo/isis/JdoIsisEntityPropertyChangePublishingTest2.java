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
package org.apache.isis.testdomain.applayer.publishing.jdo.isis;

import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.transaction.changetracking.EntityChangeTrackerDefault;
import org.apache.isis.testdomain.applayer.publishing.EntityPropertyChangeSubscriberForTesting;
import org.apache.isis.testdomain.applayer.publishing.conf.Configuration_usingEntityPropertyChangePublishing;
import org.apache.isis.testdomain.conf.Configuration_usingJdoIsis;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdoIsis.class,
                Configuration_usingEntityPropertyChangePublishing.class,
        }, 
        properties = {
                "logging.level.org.apache.isis.applib.services.publishing.log.*=DEBUG",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.integration.changetracking.JdoLifecycleListener=DEBUG",
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoIsisEntityPropertyChangePublishingTest2 extends IsisIntegrationTestAbstract {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private KVStoreForTesting kvStore;
    @Inject private InteractionFactory interactionFactory;
    @Inject Provider<EntityChangeTrackerDefault> entityChangeTrackerProvider;
    
    @Named("transaction-aware-pmf-proxy")
    @Inject private PersistenceManagerFactory pmf;
    
    @BeforeEach
    void setup() {
        
    }
    
    
    @Test
    void test() {

        // given
        val book = setupForJdo();
        
        _Probe.errOut("BOOK STATE: %s", JDOHelper.getObjectState(book));
        
        // trigger publishing of entity changes
        entityChangeTrackerProvider.get().onPreCommit(null);
        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStore);
        _Probe.errOut("BEFORE BOOK UPDATE");
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW,
                ()->{

            // when - direct change (circumventing the framework)
            book.setName("Book #2");
            repositoryService.persist(book);
            
            entityChangeTrackerProvider.get().onPreCommit(null);
        });
        
        _Probe.errOut("AFTER BOOK UPDATE");

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.
        
        assertHasPropertyChangeEntries(Can.of(
                "Jdo Book/name: 'Sample Book' -> 'Book #2'"));

    }

    // -- HELPER

    private JdoBook setupForJdo() {
        

        // cleanup
        //fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given Inventory with 1 Book
        //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
        
            val pm = pmf.getPersistenceManager();
            
            val products = new HashSet<JdoProduct>();
    
            products.add(JdoBook.of(
                    "Sample Book", "A sample book for testing.", 99.,
                    "Sample Author", "Sample ISBN", "Sample Publisher"));
    
            val inventory = JdoInventory.of("Sample Inventory", products);
            pm.makePersistent(inventory);
            
            inventory.getProducts().forEach(product->{
                val prod = pm.makePersistent(product);
                
                _Probe.errOut("PROD ID: %s", JDOHelper.getObjectId(prod));
                
            });
            
            
            pm.flush();
        
        });
        
        return transactionService.callWithinCurrentTransactionElseCreateNew(()->{
            val pm = pmf.getPersistenceManager();
            return pm.getObjectById(JdoBook.class, -1L);
        })
        .orElseFail();
        
        //return repositoryService.allInstances(JdoBook.class).listIterator().next();
    }
    
    private void assertHasPropertyChangeEntries(Can<String> expectedAuditEntries) {
        val actualAuditEntries = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
