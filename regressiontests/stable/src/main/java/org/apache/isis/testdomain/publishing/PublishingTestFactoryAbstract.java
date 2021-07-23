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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.security.util.XrayUtil;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.PublishingTestContext.TraceLog;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public abstract class PublishingTestFactoryAbstract {

    public static enum VerificationStage {
        PRE_COMMIT,
        POST_COMMIT,
        POST_COMMIT_WHEN_PROGRAMMATIC,
        FAILURE_CASE,
        POST_INTERACTION,
        POST_INTERACTION_WHEN_PROGRAMMATIC,
    }

    @Value(staticConstructor = "of")
    public static class PublishingTestContext {

        public static class TraceLog {
            private final StringBuilder buffer = new StringBuilder();
            @Getter private boolean debug = true;

            public TraceLog log(final String format, final Object...args) {
                val msg = String.format(format, args);
                buffer.append(msg).append("\n");
                if(debug) {
                    System.err.println(msg);
                }
                return this;
            }
        }

        private final @NonNull TraceLog traceLog;
        private final @NonNull Runnable given;
        private final @NonNull Consumer<VerificationStage> verifier;

        public void runGiven() {
            traceLog.log("2.1 about to run given");
            given.run();
            traceLog.log("2.2 given has run");
        }

        public void runVerify(final VerificationStage verificationStage) {
            verifier.accept(verificationStage);
        }

        /**
         * @param preCommitListener - shared instance
         */
        public void bind(final PreCommitListener preCommitListener) {
            traceLog.log("1. bind to pre-commit events");
            preCommitListener.bind(verifier);
        }

        /**
         * @param preCommitListener - shared instance
         */
        public void unbind(final PreCommitListener preCommitListener) {
            traceLog.log("3. unbind from pre-commit events");
            preCommitListener.unbind(verifier);
        }

        public void changeProperty(final Runnable runnable) {
            traceLog.log("2.3 about to change book's name");
            runnable.run();
            traceLog.log("2.4 book's name has changed");
        }

    }

    @FunctionalInterface
    private static interface PublishingTestRunner {
        boolean run(PublishingTestContext context) throws Exception;
    }

    @Service
    public static class PreCommitListener {

        private Consumer<VerificationStage> verifier;
//        @Setter private TraceLog traceLog;

        /** TRANSACTION END BOUNDARY */
        @EventListener(TransactionBeforeCompletionEvent.class)
        public void onPreCommit(final TransactionBeforeCompletionEvent event) {
//            if(traceLog!=null) {
//                traceLog.log("=== TRANSACTION END BOUNDARY");
//            }
            if(verifier!=null) {
                verifier.accept(VerificationStage.PRE_COMMIT);
            }
        }

        public void bind(final Consumer<VerificationStage> verifier) {
            _Assert.assertNull(this.verifier, "PreCommitListener is already bound to a verifier.");
            this.verifier = verifier;
        }

        public void unbind(final Consumer<VerificationStage> verifier) {
            _Assert.assertEquals(this.verifier, verifier, "PreCommitListener is not bound to the verifier, "
                    + "which it receives a request to unbind from.");
            this.verifier = null;
        }

    }

    // -- DEPENDENCIES

    protected abstract InteractionService getInteractionService();
    protected abstract TransactionService getTransactionService();

    // -- CREATE DYNAMIC TESTS

    public final List<DynamicTest> generateTests(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        val dynamicTests = Can.<DynamicTest>of(

                publishingTest("Programmatic Execution",
                        given, verifier,
                        VerificationStage.POST_INTERACTION_WHEN_PROGRAMMATIC,
                        this::programmaticExecution),
                publishingTest("Interaction Api Execution",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::interactionApiExecution),
                publishingTest("Wrapper Sync Execution w/o Rules",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionNoRules),
                publishingTest("Wrapper Sync Execution w/ Rules (expected to fail w/ DisabledException)",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionWithFailure),
                publishingTest("Wrapper Async Execution w/o Rules",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperAsyncExecutionNoRules),
                publishingTest("Wrapper Async Execution w/ Rules (expected to fail w/ DisabledException)",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperAsyncExecutionWithFailure)
                );

        return XrayUi.isXrayEnabled()
                ? dynamicTests
                        .add(dynamicTest("wait for xray viewer", XrayUi::waitForShutdown))
                        .toList()
                : dynamicTests
                        .toList();

    }

    protected abstract boolean programmaticExecution(PublishingTestContext context);

    protected abstract boolean interactionApiExecution(PublishingTestContext context);

    protected abstract boolean wrapperSyncExecutionNoRules(PublishingTestContext context);

    protected abstract boolean wrapperSyncExecutionWithFailure(PublishingTestContext context);

    protected abstract boolean wrapperAsyncExecutionNoRules(PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException;

    protected abstract boolean wrapperAsyncExecutionWithFailure(PublishingTestContext context);


    // -- HELPER

    private final DynamicTest publishingTest(
            final String displayName,
            final Runnable given,
            final Consumer<VerificationStage> verifier,
            final VerificationStage onSuccess,
            final PublishingTestRunner testRunner) {

        return dynamicTest(displayName, ()->{

            xrayAddTest(displayName);

            assertFalse(getInteractionService().isInInteraction());
            assert_no_initial_tx_context();

            val traceLog = new TraceLog();

            final boolean isSuccesfulRun = getInteractionService().callAnonymous(()->{
                val currentInteraction = getInteractionService().currentInteraction();
                xrayEnterInteraction(currentInteraction);
                val result = testRunner.run(PublishingTestContext.of(traceLog, given, verifier));
                xrayExitInteraction();
                return result;
            });

            getInteractionService().closeInteractionLayers();

            if(isSuccesfulRun) {
                verifier.accept(onSuccess);
            }

        });
    }

    private final void assert_no_initial_tx_context() {
        val txState = getTransactionService().currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    // -- XRAY

    private final void xrayAddTest(final String name) {

        val threadId = XrayUtil.currentThreadAsMemento();

        XrayUi.updateModel(model->{
            model.addContainerNode(
                    model.getThreadNode(threadId),
                    String.format("Test: %s", name));

        });

    }

    protected void xrayEnterTansaction(final Propagation propagation) {
    }

    protected void xrayExitTansaction() {
    }

    private void xrayEnterInteraction(final Optional<Interaction> currentInteraction) {
    }

    private void xrayExitInteraction() {
    }

}
