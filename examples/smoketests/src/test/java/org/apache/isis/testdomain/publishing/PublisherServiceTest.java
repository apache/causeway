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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingPublishing.class
        }, 
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG"
        })
@TestPropertySource({
    IsisPresets.SilenceWicket, // just to have any config properties at all
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext // because of the temporary installed PublisherServiceProbe
@Incubating("fails when run with surefire")
class PublisherServiceTest {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private PlatformTransactionManager txMan; 
    @Inject private KVStoreForTesting kvStore;
    @Inject private PublisherDispatchService publisherDispatchService;
    //@Inject private List<PublisherService> publisherServices;
    
    @Configuration
    public class Config {
        // so that we get a new ApplicationContext.
    }
    
    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test @Order(1) @Tag("Incubating")
    void publisherService_shouldBeAwareOfInventoryChanges() {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        kvStore.clear(PublisherServiceForTesting.class);

        val latch = kvStore.latch(PublisherServiceForTesting.class);
        
        
        // when - running within its own transactional boundary
        val transactionTemplate = new TransactionTemplate(txMan);
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertEquals(0, kvStore.count(PublisherServiceForTesting.class));
            
            return null;
        });
        
        latch.await(2, TimeUnit.SECONDS);
        
        System.err.println("!!! after sync writes");
        
        
        // then - after the commit
        assertEquals(0, getValue("created"));
        assertEquals(0, getValue("deleted"));
        assertEquals(0, getValue("loaded"));
        assertEquals(1, getValue("updated"));
        assertEquals(1, getValue("modified"));

    }

    @Test @Order(2) @Tag("Incubating")
    void publisherService_shouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        kvStore.clear(PublisherServiceForTesting.class);
        val latch = kvStore.latch(PublisherServiceForTesting.class);

        // when - running within its own background task
        val future = wrapper.async(book, ExecutionMode.SKIP_RULES) // don't enforce rules for this test
                .run(Book::setName, "Book #2");

        future.get(10, TimeUnit.SECONDS);

        latch.await(2, TimeUnit.SECONDS);
        
        System.err.println("!!! after wait");

        // then - after the commit
        assertEquals(0, getValue("created"));
        assertEquals(1, getValue("deleted"));
        //assertEquals(0, getValue("loaded"));
        assertEquals(2, getValue("updated"));
        assertEquals(1, getValue("modified"));

    }
    
    
    @Test @Order(3) @Tag("Incubating")
    void publisherService_shouldNotBeAwareOfInventoryChanges_whenUsingAsyncExecutionFails() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        kvStore.clear(PublisherServiceForTesting.class);

        // when - running within its own background task
        assertThrows(DisabledException.class, ()->{
            
            val future = wrapper.async(book, ExecutionMode.EXECUTE) 
                .run(Book::setName, "Book #2");
            
            future.get(10, TimeUnit.SECONDS);
            
        });

        // then - after the commit
        assertEquals(null, getValue("created"));
        assertEquals(null, getValue("deleted"));
        assertEquals(null, getValue("loaded"));
        assertEquals(null, getValue("updated"));
        assertEquals(null, getValue("modified"));

    }

    // -- HELPER
    
    private Object getValue(String keyStr) {
        return kvStore.get(PublisherServiceForTesting.class, keyStr).orElse(null);
    }

}
