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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @Inject private BackgroundService backgroundService;
    @Inject private PublisherServiceProbe publisherService;

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
        val transactionTemplate = IsisContext.createTransactionTemplate();
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertEquals("", publisherService.getHistory());

            return null;
        });

        // then - after the commit
        assertEquals("publishedObjects=created=0,deleted=0,loaded=0,updated=1,modified=1,",
                publisherService.getHistory());

    }

    @Test @Order(2)
    void publisherServiceShouldBeAwareOfInventoryChanges_whenUsingBackgroundService() throws InterruptedException {

        // given
        val book = repository.allInstances(Book.class).listIterator().next();
        publisherService.clearHistory();

        // when - running within its own background task
        backgroundService.execute(book)
        .setName("Book #2");

        Thread.sleep(1000); //TODO fragile test, find another way to sync on the background task

        // then - after the commit
        assertEquals("publishedObjects=created=0,deleted=1,loaded=0,updated=2,modified=1,",
                publisherService.getHistory());

    }

    // -- HELPER

    @Service
    public static class PublisherServiceProbe implements PublisherService {

        private StringBuilder history = new StringBuilder();

        void clearHistory() {
            history = new StringBuilder();
        }

        String getHistory() {
            return history.toString();
        }

        @Override
        public void publish(Execution<?, ?> execution) {
            history.append("execution=").append(execution).append(",");
        }

        @Override
        public void publish(PublishedObjects publishedObjects) {
            history.append("publishedObjects=")
            .append("created=").append(publishedObjects.getNumberCreated()).append(",")
            .append("deleted=").append(publishedObjects.getNumberDeleted()).append(",")
            .append("loaded=").append(publishedObjects.getNumberLoaded()).append(",")
            .append("updated=").append(publishedObjects.getNumberUpdated()).append(",")
            .append("modified=").append(publishedObjects.getNumberPropertiesModified()).append(",")
            ;
        }

    }

}
