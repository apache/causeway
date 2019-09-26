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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.config.IsisPresets;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class, 
                AuditerServiceTest.AuditerServiceProbe.class
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
        })
@TestPropertySource({
    IsisPresets.DebugPersistence
})
//@Transactional //XXX this test is non transactional
class AuditerServiceTest {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private BackgroundService backgroundService;
    @Inject private AuditerServiceProbe auditerService;

    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        
        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

    }

    @Test
    void auditerServiceShouldBeAwareOfInventoryChanges() {

        // given
        val books = repository.allInstances(Book.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        auditerService.clearHistory();

        // when - running within its own transactional boundary
        val transactionTemplate = IsisContext.createTransactionTemplate();
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertEquals("", auditerService.getHistory());

            return null;
        });

        // then - after the commit
        assertEquals("targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                auditerService.getHistory());
    }

    @Test
    void auditerServiceShouldBeAwareOfInventoryChanges_whenUsingBackgroundService() throws InterruptedException {

        // given
        val books = repository.allInstances(Book.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        auditerService.clearHistory();

        // when - running within its own background task
        backgroundService.execute(book)
        .setName("Book #2");

        Thread.sleep(1000); //TODO fragile test, find another way to sync on the background task

        // then - after the commit
        assertEquals("targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                auditerService.getHistory());
    }

    // -- HELPER

    @Singleton @Log4j2
    public static class AuditerServiceProbe implements AuditerService {

        private StringBuilder history = new StringBuilder();

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void audit(UUID interactionId, int sequence, String targetClassName, Bookmark target,
                String memberIdentifier, String propertyName, String preValue, String postValue, String user,
                Timestamp timestamp) {

            history.append("targetClassName=").append(targetClassName).append(",").append("propertyName=")
            .append(propertyName).append(",").append("preValue=").append(preValue).append(",")
            .append("postValue=").append(postValue).append(";");

            log.debug("audit {}", history.toString());
        }

        void clearHistory() {
            log.debug("clearing");
            history = new StringBuilder();
            log.debug("cleared");
        }

        String getHistory() {
            log.debug("get {}", history.toString());
            return history.toString();
        }

    }

}
