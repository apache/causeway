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
package org.apache.isis.testdomain.applayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationLayerTestFactory {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final TransactionService transactionService;
    private final FixtureScripts fixtureScripts;

    public List<DynamicTest> generateTests(
            final Runnable given,
            final Runnable thenHappyCase,
            final Runnable thenFailureCase) {
        return _Lists.of(
                dynamicTest("No initial Transaction with Test Execution", this::no_initial_tx_context),
                programmaticExecution(given, thenFailureCase),
                wrapperSyncExecution(given, thenHappyCase),
                wrapperSyncExecutionWithFailure(given, thenFailureCase),
                wrapperAsyncExecution(given, thenHappyCase),
                wrapperAsyncExecutionWithFailure(given, thenFailureCase)
                );
    }

    // -- TEST SETUP

    private JdoBook setupForJdo() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

        return repository.allInstances(JdoBook.class).listIterator().next();
    }

    // -- TESTS - ENSURE TESTS ARE CORRECTLY INVOKED 

    void no_initial_tx_context() {
        val txState = transactionService.currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    // -- TESTS - WRAPPER SYNC

    private DynamicTest programmaticExecution(
            final Runnable given,
            final Runnable then) {
        return dynamicTest("Programmatic Execution", ()->{
            // given
            val book = setupForJdo();
            given.run();

            // when - direct change (circumventing the framework)
            book.setName("Book #2");
            repository.persist(book);

            // this test does not trigger publishing 
            // because invocation happens directly rather than through the means of
            // an implementation of ObjectMemberAbstract, which has the meta-data
            // required to generate the associated CommandDTO

            //TODO however, we should still be able to receive valid metrics 
            // even though Commands don't have CommandDTOs

            // then
            then.run();
        });
    }

    // -- TESTS - WRAPPER SYNC

    private DynamicTest wrapperSyncExecution(
            final Runnable given,
            final Runnable then) {
        return dynamicTest("Wrapper Sync Execution w/o Rules", ()->{
            // given
            val book = setupForJdo();
            given.run();

            // when - running synchronous
            val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
            wrapper.wrap(book, syncControl).setName("Book #2"); 

            // then
            then.run();
        });
    }

    private DynamicTest wrapperSyncExecutionWithFailure(
            final Runnable given,
            final Runnable then) {
        return dynamicTest("Wrapper Sync Execution w/ Rules (expected to fail w/ DisabledException)", ()->{
            // given
            val book = setupForJdo();
            given.run();

            // when - running synchronous
            val syncControl = SyncControl.control().withCheckRules(); // enforce rules 

            assertThrows(DisabledException.class, ()->{
                wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
            });

            // then
            then.run();
        });
    }

    // -- TESTS - WRAPPER ASYNC

    private DynamicTest wrapperAsyncExecution(
            final Runnable given,
            final Runnable then) {
        return dynamicTest("Wrapper Async Execution w/o Rules", ()->{
            // given
            val book = setupForJdo();
            given.run();

            // when - running asynchronous
            val asyncControl = returningVoid().withSkipRules(); // don't enforce rules
            wrapper.asyncWrap(book, asyncControl).setName("Book #2");

            asyncControl.getFuture().get(10, TimeUnit.SECONDS);

            // then
            then.run();
        });
    }

    private DynamicTest wrapperAsyncExecutionWithFailure(
            final Runnable given,
            final Runnable then) {
        return dynamicTest("Wrapper Async Execution w/ Rules (expected to fail w/ DisabledException)", ()->{
            // given
            val book = setupForJdo();
            given.run();

            // when - running synchronous
            val asyncControl = returningVoid().withCheckRules(); // enforce rules 

            assertThrows(DisabledException.class, ()->{
                wrapper.asyncWrap(book, asyncControl).setName("Book #2"); // should fail with DisabledException

                asyncControl.getFuture().get(10, TimeUnit.SECONDS);
            });

            // then
            then.run();
        });
    }

}
