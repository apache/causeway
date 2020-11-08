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
package org.apache.isis.testdomain.publishing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.publishing.Configuration_usingPublishing;
import org.apache.isis.testdomain.publishing.PublisherServiceForTesting;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingPublishing.class
        }, 
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublisherServiceTest extends IsisIntegrationTestAbstract {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private PlatformTransactionManager txMan; 
    @Inject private KVStoreForTesting kvStore;
    @Inject private TransactionService transactionService;
    
    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test @Order(0)
    void no_initial_tx_context() {
        val txState = transactionService.currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }
    
    @Test @Order(1)
    void publisherService_shouldBeAwareOfInventoryChanges() {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        PublisherServiceForTesting.clearPublishedEntries(kvStore);
        
        _Probe.errOut("(1) BEFORE BOOK UPDATED");
        
        // when - running within its own transactional boundary
        val transactionTemplate = new TransactionTemplate(txMan);
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertEquals(0, kvStore.countEntries(PublisherServiceForTesting.class));
            
            return null;
        });
        
        _Probe.errOut("(1) AFTER BOOK UPDATED");
        
        // this test does not trigger publishing 
        // because invocation happens directly rather than through the means of
        // of an ActionInvocationFacet, which is required to create a CommandDto 
        // for each execution  
        
        // then - after the commit
        assertEquals(0, getCreated());
        assertEquals(0, getDeleted());
        //assertEquals(1, getLoaded()); // not reproducible
        assertEquals(1, getUpdated());
        assertEquals(1, getModified());
    }
    
    @Test @Order(2)
    void publisherService_shouldBeAwareOfInventoryChanges_whenUsingSyncExecution() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        PublisherServiceForTesting.clearPublishedEntries(kvStore);

        _Probe.errOut("(2) BEFORE BOOK UPDATED");
        
        // when - running synchronous
        val syncControl = SyncControl.control().withSkipRules();
        wrapper.wrap(book, syncControl).setName("Book #2"); // don't enforce rules for this test

        _Probe.errOut("(2) AFTER BOOK UPDATED");

        
        // then - after the commit
        assertEquals(0, getCreated());
        assertEquals(0, getDeleted());
        //assertEquals(1, getLoaded()); // not reproducible
        assertEquals(1, getUpdated());
        assertEquals(1, getModified());

    }

    @Test @Order(3)
    void publisherService_shouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        PublisherServiceForTesting.clearPublishedEntries(kvStore);
        val latch = kvStore.latch(PublisherServiceForTesting.class);

        _Probe.errOut("(3) BEFORE BOOK UPDATED");
        
        // when - running within its own background task
        AsyncControl<Void> control = returningVoid().withSkipRules();
        wrapper.asyncWrap(book, control).setName("Book #2"); // don't enforce rules for this test

        control.getFuture().get(10, TimeUnit.SECONDS);

        latch.await(2, TimeUnit.SECONDS);
        
        _Probe.errOut("(3) AFTER BOOK UPDATED");

        // then - after the commit
        assertEquals(0, getCreated());
        assertEquals(0, getDeleted());
        //assertEquals(1, getLoaded()); // not reproducible
        assertEquals(1, getUpdated());
        assertEquals(1, getModified());

    }
    
    
    @Test @Order(4)
    void publisherService_shouldNotBeAwareOfInventoryChanges_whenUsingAsyncExecutionFails() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        PublisherServiceForTesting.clearPublishedEntries(kvStore);

        // when - running within its own background task
        assertThrows(DisabledException.class, ()->{

            wrapper.asyncWrap(book, returningVoid()).setName("Book #2");
            
            returningVoid().getFuture().get(10, TimeUnit.SECONDS);
            
        });

        // then - after the commit
        assertEquals(0, getCreated());
        assertEquals(0, getDeleted());
        assertEquals(0, getLoaded());
        assertEquals(0, getUpdated());
        assertEquals(0, getModified());

    }

    // -- HELPER
    
    private int getCreated() {
        val publishedObjects = PublisherServiceForTesting.getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberCreated).sum();
    }
    
    private int getDeleted() {
        val publishedObjects = PublisherServiceForTesting.getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberDeleted).sum();
    }
    
    private int getLoaded() {
        val publishedObjects = PublisherServiceForTesting.getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberLoaded).sum();
    }
    
    private int getUpdated() {
        val publishedObjects = PublisherServiceForTesting.getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberUpdated).sum();
    }
    
    private int getModified() {
        val publishedObjects = PublisherServiceForTesting.getPublishedObjects(kvStore);
        return publishedObjects.stream().mapToInt(PublishedObjects::getNumberPropertiesModified).sum();
    }


}
