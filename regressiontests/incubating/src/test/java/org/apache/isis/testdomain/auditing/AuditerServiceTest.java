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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.ExceptionHandlerAbstract;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.internal.base._Blackhole;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.control;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingAuditing.class
        }, 
        properties = {
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG"
        })
@TestPropertySource({
    IsisPresets.SilenceWicket
    ,IsisPresets.UseLog4j2Test
})
@DirtiesContext // because of the temporary installed AuditerServiceProbe
@Incubating("possibly fails when run with surefire")
class AuditerServiceTest extends IsisIntegrationTestAbstract {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private KVStoreForTesting kvStore;
    
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

    @Test @Tag("Incubating")
    void auditerService_shouldBeAwareOfInventoryChanges() {

        // given
        val books = repository.allInstances(JdoBook.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        
        kvStore.clear(AuditerServiceForTesting.class);

        // when - running within its own transactional boundary
//        val transactionTemplate = new TransactionTemplate(txMan);
//        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertFalse(kvStore.get(AuditerServiceForTesting.class, "audit").isPresent());

//            return null;
//        });
        transactionService.nextTransaction();

        // then - after the commit
        assertEquals("targetClassName=Jdo Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                kvStore.get(AuditerServiceForTesting.class, "audit").orElse(null));
    }

    @Inject
    TransactionService transactionService;

    @Test @Tag("Incubating")
    void auditerService_shouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() throws ExecutionException, InterruptedException, TimeoutException {

        // given
        val books = repository.allInstances(JdoBook.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        kvStore.clear(AuditerServiceForTesting.class);

        // when - running within its own background task
        AsyncControl<Void> control = control().withSkipRules();
        wrapper.asyncWrap(book, control.with(new ExceptionHandlerAbstract() {
            @Override
            public Object handle(Exception ex) throws Exception {
                getLog().error(ex);
                return null;
            }
        })).setName("Book #2");

        Void await = control.getFuture().get(10, TimeUnit.SECONDS); // blocks the current thread
        _Blackhole.consume(await); // just use the value to suppress warnings

        // then - after the commit
        assertEquals("targetClassName=Jdo Book,propertyName=name,preValue=Sample Book,postValue=Book #2;",
                kvStore.get(AuditerServiceForTesting.class, "audit").orElse(null));
    }
    
    @Test @Tag("Incubating")
    void auditerService_shouldNotBeAwareOfInventoryChanges_whenUsingAsyncExecutionThatFails() {

        // given
        val books = repository.allInstances(JdoBook.class);
        assertEquals(1, books.size());
        val book = books.listIterator().next();
        kvStore.clear(AuditerServiceForTesting.class);

        // when - running within its own background task
        assertThrows(DisabledException.class, ()->{

            wrapper.asyncWrap(book, control()).setName("Book #2");

            control().getFuture().get(1000, TimeUnit.SECONDS);
            
        });
        
        // then - after the exception
        assertFalse(kvStore.get(AuditerServiceForTesting.class, "audit").isPresent());
    }


}
