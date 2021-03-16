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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedConsumer;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.util.XrayUtil;
import org.apache.isis.core.transaction.changetracking.EntityChangeTrackerDefault;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import io.smallrye.common.constraint.Assert;

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
    private final InteractionFactory interactionFactory;
    private final InteractionTracker interactionTracker;
    private final Provider<EntityChangeTrackerDefault> entityChangeTrackerProvider;
    
    @Named("transaction-aware-pmf-proxy")
    private final PersistenceManagerFactory pmf;
    
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
                
                dynamicTest("close interaction session stack (if any)", interactionFactory::closeSessionStack),
                
                interactionTest("Programmatic Execution", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION_WHEN_PROGRAMMATIC, 
                        this::programmaticExecution),
                interactionTest("Interaction Api Execution", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION, 
                        this::interactionApiExecution),
                interactionTest("Wrapper Sync Execution w/o Rules", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION, 
                        this::wrapperSyncExecutionNoRules),
                interactionTest("Wrapper Sync Execution w/ Rules (expected to fail w/ DisabledException)", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION, 
                        this::wrapperSyncExecutionWithFailure),
                interactionTest("Wrapper Async Execution w/o Rules", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION, 
                        this::wrapperAsyncExecutionNoRules),
                interactionTest("Wrapper Async Execution w/ Rules (expected to fail w/ DisabledException)", 
                        given, verifier, 
                        VerificationStage.POST_INTERACTION, 
                        this::wrapperAsyncExecutionWithFailure),
                
                dynamicTest("wait for xray viewer (if any)", XrayUi::waitForShutdown)
                
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
            final VerificationStage onSuccess,
            final InteractionTestRunner interactionTestRunner) {
        
        return dynamicTest(displayName, ()->{

            xrayAddTest(displayName);
            
            Assert.assertFalse(interactionFactory.isInInteractionSession());
            assert_no_initial_tx_context();
            
            final boolean isSuccesfulRun = interactionFactory.callAnonymous(()->{
                val currentInteraction = interactionTracker.currentInteraction();
                xrayEnterInteraction(currentInteraction);
                val result = interactionTestRunner.run(given, verifier);
                xrayExitInteraction();
                return result;
            });
            
            interactionFactory.closeSessionStack();
            
            if(isSuccesfulRun) {
                verifier.accept(onSuccess);
            }
                        
        });
    }
    

    // -- TESTS - ENSURE TESTS ARE CORRECTLY INVOKED 

    void assert_no_initial_tx_context() {
        val txState = transactionService.currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    // -- TESTS - WRAPPER SYNC

    private boolean programmaticExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {
        
        // given
        setupBookForJdo();
        
        preCommitListener.setVerifier(verifier);
        
        withBookDoTransactional(book->{
        
            given.run();

            // when - direct change (circumventing the framework)
            book.setName("Book #2");
            repository.persist(book);
            
            // trigger publishing of entity changes (flush queue)
            entityChangeTrackerProvider.get().onPreCommit(null);
            
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
        setupBookForJdo();
        
        // when
        withBookDoTransactional(book->{
            
            given.run();

            preCommitListener.setVerifier(verifier);
            
            // when
            val bookAdapter = objectManager.adapt(book);
            val propertyInteraction = PropertyInteraction.start(bookAdapter, "name", Where.OBJECT_FORMS);
            val managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
            val propertyModel = managedProperty.startNegotiation();
            val propertySpec = managedProperty.getSpecification();
            propertyModel.getValue().setValue(ManagedObject.of(propertySpec, "Book #2"));
            propertyModel.submit();    
            
        });
        
        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);
        
        return true;
    }

    // -- TESTS - WRAPPER SYNC

    private boolean wrapperSyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();
        
        // when
        withBookDoTransactional(book->{

            given.run();
            
            preCommitListener.setVerifier(verifier);

            // when - running synchronous
            val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
            wrapper.wrap(book, syncControl).setName("Book #2");
            
            preCommitListener.setVerifier(null);
            
        });

        // then
        verifier.accept(VerificationStage.POST_COMMIT);
        
        return true;
    }

    private boolean wrapperSyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();
        
        // when
        withBookDoTransactional(book->{
            
            given.run();

            preCommitListener.setVerifier(verifier);
            
            // when - running synchronous
            val syncControl = SyncControl.control().withCheckRules(); // enforce rules 

            assertThrows(DisabledException.class, ()->{
                wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
            });
            
            preCommitListener.setVerifier(null);
            
        });
        

        // then
        verifier.accept(VerificationStage.FAILURE_CASE);
        
        return false;
    }

    // -- TESTS - WRAPPER ASYNC

    private boolean wrapperAsyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier) throws InterruptedException, ExecutionException, TimeoutException {

        // given
        setupBookForJdo();
        val asyncControl = returningVoid().withSkipRules(); // don't enforce rules
        
        // when
        
        withBookDoTransactional(book->{

            given.run();
            
            preCommitListener.setVerifier(verifier);

            // when - running asynchronous
            wrapper.asyncWrap(book, asyncControl)
            .setName("Book #2");
            
        });
        
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
        setupBookForJdo();
        
        // when
        withBookDoTransactional(book->{

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
            
        });

        // then
        verifier.accept(VerificationStage.FAILURE_CASE);
     
        return false;
    }
    
    // -- TEST SETUP
    
    private void setupBookForJdo() {
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            val pm = pmf.getPersistenceManager();
            
            // cleanup
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
            
            // given Inventory with 1 Book
            
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
            
            //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
            
            pm.flush();
            
            // trigger publishing of entity changes (flush queue)
            entityChangeTrackerProvider.get().onPreCommit(null);
            
        });
    }
    
    private void withBookDoTransactional(CheckedConsumer<JdoBook> transactionalBookConsumer) {
        
        xrayEnterTansaction(Propagation.REQUIRES_NEW);
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            val book = repository.allInstances(JdoBook.class).listIterator().next();
            transactionalBookConsumer.accept(book);

            //FIXME ... should not be required explicitly here
            // trigger publishing of entity changes (flush queue)
            entityChangeTrackerProvider.get().onPreCommit(null);
        })
        .optionalElseFail();
        
        xrayExitTansaction();
    }
    
    // -- XRAY
    
    private void xrayAddTest(String name) {
        
        val threadId = XrayUtil.currentThreadId();
        
        XrayUi.updateModel(model->{
            model.addContainerNode(
                    model.getThreadNode(threadId), 
                    String.format("Test: %s", name));
                
        });  
        
    }
    
    private void xrayEnterTansaction(Propagation propagation) {
    }
    
    private void xrayExitTansaction() {
    }
    
    private void xrayEnterInteraction(Optional<Interaction> currentInteraction) {
    }
    
    private void xrayExitInteraction() {
    }

}
