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
package org.apache.isis.testdomain.auditing;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.util.rest.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Smoketest
@SpringBootTest(
        classes = {
                AuditerServiceTest.AuditerServiceProbe.class,
                Configuration_usingJdo.class, 
                KVStoreForTesting.class
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG"
        })
@TestPropertySource({
    IsisPresets.SilenceWicket
    ,IsisPresets.UseLog4j2Test
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) // because of the temporary installed AuditerServiceProbe
class AuditerServiceTest {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private PlatformTransactionManager txMan; 
    @Inject private KVStoreForTesting kvStore;

    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        
        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

    }

    @Test @Tag("Incubating")
    void auditerService_shouldBeAwareOfInventoryChanges() {

        // given
        val books = repository.allInstances(Book.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        
        kvStore.clear(AuditerServiceProbe.class);

        // when - running within its own transactional boundary
        val transactionTemplate = new TransactionTemplate(txMan);
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertFalse(kvStore.get(AuditerServiceProbe.class, "audit").isPresent());

            return null;
        });

        // then - after the commit
        assertEquals("targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                kvStore.get(AuditerServiceProbe.class, "audit").orElse(null));
    }

    @Test @Tag("Incubating")
    void auditerService_shouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val books = repository.allInstances(Book.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        kvStore.clear(AuditerServiceProbe.class);

        // when - running within its own background task
        val future = wrapper.async(book, ExecutionMode.SKIP_RULES) // don't enforce rules for this test
        .run(Book::setName, "Book #2");

        future.get(1000, TimeUnit.SECONDS);
        
        // then - after the commit
        assertEquals("targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                kvStore.get(AuditerServiceProbe.class, "audit").orElse(null));
    }
    
    @Test @Tag("Incubating")
    void auditerService_shouldNotBeAwareOfInventoryChanges_whenUsingAsyncExecutionThatFails() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val books = repository.allInstances(Book.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        kvStore.clear(AuditerServiceProbe.class);

        // when - running within its own background task
        assertThrows(DisabledException.class, ()->{
        
            val future = wrapper.async(book, ExecutionMode.EXECUTE)
                    .run(Book::setName, "Book #2");

            future.get(1000, TimeUnit.SECONDS);
            
        });
        
        // then - after the exception
        assertFalse(kvStore.get(AuditerServiceProbe.class, "audit").isPresent());
    }

    // -- HELPER

    @Service @Log4j2
    public static class AuditerServiceProbe implements AuditerService {

        @Inject private KVStoreForTesting kvStore;
        
        @PostConstruct
        public void init() {
            log.info("about to initialize");
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void audit(UUID interactionId, int sequence, String targetClassName, Bookmark target,
                String memberIdentifier, String propertyName, String preValue, String postValue, String user,
                Timestamp timestamp) {

            val audit = new StringBuilder()
                    .append("targetClassName=").append(targetClassName).append(",").append("propertyName=")
                    .append(propertyName).append(",").append("preValue=").append(preValue).append(",")
                    .append("postValue=").append(postValue).append(";")
                    .toString();

            kvStore.put(this, "audit", audit);
            log.debug("audit {}", audit);
        }

    }

}
