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

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@Component
@Import({
    ApplicationLayerTestFactory.PreCommitListener.class
})
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationLayerTestFactory {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final TransactionService transactionService;
    private final ObjectManager objectManager;
    private final FixtureScripts fixtureScripts;
    private final PreCommitListener preCommitListener;
    private final InteractionFactory isisInteractionFactory;
    
    public static enum VerificationStage {
        PRE_COMMIT,
        POST_COMMIT,
        POST_COMMIT_WHEN_PROGRAMMATIC,
        FAILURE_CASE, 
        POST_INTERACTION, 
        POST_INTERACTION_WHEN_PROGRAMMATIC, 
    }
    
    @Service
    public static class PreCommitListener {
        
        @Setter private Consumer<VerificationStage> verifier;
        
        /** TRANSACTION END BOUNDARY */
        @EventListener(TransactionBeforeCompletionEvent.class)
        public void onPreCommit(TransactionBeforeCompletionEvent event) {
            if(verifier!=null) {
                verifier.accept(VerificationStage.PRE_COMMIT);
            }
        }
    }

    public List<DynamicTest> generateTests(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {
        return _Lists.of(
//                dynamicTest("No initial Transaction with Test Execution", 
//                        this::no_initial_tx_context),
                programmaticTest("Programmatic Execution", 
                        given, verifier, this::programmaticExecution),
                interactionTest("Interaction Api Execution", 
                        given, verifier, this::interactionApiExecution),
                interactionTest("Wrapper Sync Execution w/o Rules", 
                        given, verifier, this::wrapperSyncExecution),
                interactionTest("Wrapper Sync Execution w/ Rules (expected to fail w/ DisabledException)", 
                        given, verifier, this::wrapperSyncExecutionWithFailure),
                interactionTest("Wrapper Async Execution w/o Rules", 
                        given, verifier, this::wrapperAsyncExecution),
                interactionTest("Wrapper Async Execution w/ Rules (expected to fail w/ DisabledException)", 
                        given, verifier, this::wrapperAsyncExecutionWithFailure)
                );
    }
    
    // -- INTERACTION TEST FACTORY
    
    @FunctionalInterface
    private static interface InteractionTestRunner {
        boolean run(Runnable given, Consumer<VerificationStage> verifier) throws Exception;
    }
    
    private DynamicTest interactionTest(
            final String displayName,
            final Runnable given,
            final Consumer<VerificationStage> verifier,
            final InteractionTestRunner interactionTestRunner) {
        return dynamicTest(displayName, ()->{
            
            val isSuccesfulRun = isisInteractionFactory.callAnonymous(()->
                    interactionTestRunner.run(given, verifier));
            
            isisInteractionFactory.closeSessionStack();
            
            if(isSuccesfulRun) {
                verifier.accept(VerificationStage.POST_INTERACTION);
            }
                        
        });
    }

    private DynamicTest programmaticTest(
            final String displayName,
            final Runnable given,
            final Consumer<VerificationStage> verifier,
            final InteractionTestRunner interactionTestRunner) {
        return dynamicTest(displayName, ()->{
            
            val isSuccesfulRun = isisInteractionFactory.callAnonymous(()->
                        interactionTestRunner.run(given, verifier));
                    
            isisInteractionFactory.closeSessionStack();
            
            if(isSuccesfulRun) {
                verifier.accept(VerificationStage.POST_INTERACTION_WHEN_PROGRAMMATIC);
            }

        });
    }
    

    // -- TESTS - ENSURE TESTS ARE CORRECTLY INVOKED 

//    boolean no_initial_tx_context() {
//        val txState = transactionService.currentTransactionState();
//        assertEquals(TransactionState.NONE, txState);
//        return true;
//    }

    // -- TESTS - WRAPPER SYNC

    private boolean programmaticExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {
        
        // given
        val book = setupForJdo();
        given.run();

        preCommitListener.setVerifier(verifier);
        
        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // when - direct change (circumventing the framework)
            book.setName("Book #2");
            repository.persist(book);
            
        });
        
        preCommitListener.setVerifier(null);

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.

        // then
        verifier.accept(VerificationStage.POST_COMMIT_WHEN_PROGRAMMATIC);
        
        return true;
    }
    
    // -- TESTS - INTERACTION API

    private boolean interactionApiExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        val book = setupForJdo();
        given.run();

        preCommitListener.setVerifier(verifier);
        
        // when
        val managedObject = objectManager.adapt(book);
        val propertyInteraction = PropertyInteraction.start(managedObject, "name", Where.OBJECT_FORMS);
        val managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
        val propertyModel = managedProperty.startNegotiation();
        val propertySpec = managedProperty.getSpecification();
        propertyModel.getValue().setValue(ManagedObject.of(propertySpec, "Book #2"));
        propertyModel.submit();
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);
        
        return true;
    }

    // -- TESTS - WRAPPER SYNC

    private boolean wrapperSyncExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        val book = setupForJdo();
        given.run();
        
        preCommitListener.setVerifier(verifier);

        // when - running synchronous
        val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
        wrapper.wrap(book, syncControl).setName("Book #2");
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);
        
        return true;
    }

    private boolean wrapperSyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        val book = setupForJdo();
        given.run();

        preCommitListener.setVerifier(verifier);
        
        // when - running synchronous
        val syncControl = SyncControl.control().withCheckRules(); // enforce rules 

        assertThrows(DisabledException.class, ()->{
            wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
        });
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.FAILURE_CASE);
        
        return false;
    }

    // -- TESTS - WRAPPER ASYNC

    private boolean wrapperAsyncExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) throws InterruptedException, ExecutionException, TimeoutException {

        // given
        val book = setupForJdo();
        given.run();
        
        preCommitListener.setVerifier(verifier);

        // when - running asynchronous
        val asyncControl = returningVoid().withSkipRules(); // don't enforce rules
        wrapper.asyncWrap(book, asyncControl).setName("Book #2");

        asyncControl.getFuture().get(10, TimeUnit.SECONDS);
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);
        
        return true;
    }

    private boolean wrapperAsyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {
        
        // given
        val book = setupForJdo();
        given.run();
        
        preCommitListener.setVerifier(verifier);

        // when - running synchronous
        val asyncControl = returningVoid().withCheckRules(); // enforce rules 

        assertThrows(DisabledException.class, ()->{
            // should fail with DisabledException (synchronous) within the calling Thread
            wrapper.asyncWrap(book, asyncControl).setName("Book #2"); 

            fail("unexpected code reach");
        });
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.FAILURE_CASE);
     
        return false;
    }
    
    // -- TEST SETUP

    private JdoBook setupForJdo() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        
        // given Inventory with 1 Book
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
        
        return repository.allInstances(JdoBook.class).listIterator().next();
    }

}
