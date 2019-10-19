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

import java.util.HashMap;
import java.util.Map;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.Getter;
import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class, 
                PublisherServiceTest.PublisherServiceProbe.class
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                "logging.level.org.apache.isis.incubator.IsisPlatformTransactionManagerForJdo=DEBUG",
                // "isis.reflector.introspector.parallelize=false",
                // "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
        })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublisherServiceTest {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private PublisherServiceProbe publisherService;
    @Inject private PlatformTransactionManager txMan; 

    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test @Order(1)
    void publisherServiceShouldBeAwareOfInventoryChanges() {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        publisherService.clearHistory();

        // when - running within its own transactional boundary
        val transactionTemplate = new TransactionTemplate(txMan);
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertEquals("{}", publisherService.getHistory().toString());

            return null;
        });

        // then - after the commit
        val history = publisherService.getHistory();
        assertEquals(0, history.get("created"));
        assertEquals(0, history.get("deleted"));
        assertEquals(0, history.get("loaded"));
        assertEquals(1, history.get("updated"));
        assertEquals(1, history.get("modified"));

    }

    @Test @Order(2)
    void publisherServiceShouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() 
            throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        publisherService.clearHistory();

        // when - running within its own background task
        val future = wrapper.async(book, ExecutionMode.SKIP_RULES) //TODO why do we fail when not skipping rules?
                .run(Book::setName, "Book #2");

        future.get(1000, TimeUnit.SECONDS);

        // then - after the commit
        val history = publisherService.getHistory();
        assertEquals(0, history.get("created"));
        assertEquals(1, history.get("deleted"));
        //assertEquals(0, history.get("loaded"));
        assertEquals(2, history.get("updated"));
        assertEquals(1, history.get("modified"));

    }

    // -- HELPER

    @Service
    public static class PublisherServiceProbe implements PublisherService {

        @Getter
        private final Map<String, Integer> history = new HashMap<>();
        
        void clearHistory() {
            history.clear();
        }


        @Override
        public void publish(Execution<?, ?> execution) {
            history.put("execution", 999);
        }

        @Override
        public void publish(PublishedObjects publishedObjects) {
            history.put("created", publishedObjects.getNumberCreated());
            history.put("deleted", publishedObjects.getNumberDeleted());
            history.put("loaded", publishedObjects.getNumberLoaded());
            history.put("updated", publishedObjects.getNumberUpdated());
            history.put("modified", publishedObjects.getNumberPropertiesModified());
        }

    }

}
