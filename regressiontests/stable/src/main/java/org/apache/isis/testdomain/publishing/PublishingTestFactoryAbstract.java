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
import java.util.function.BiConsumer;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.security.util.XrayUtil;
import org.apache.isis.core.transaction.events.TransactionAfterCompletionEvent;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public abstract class PublishingTestFactoryAbstract {

    public static enum VerificationStage {
        PRE_COMMIT,
        POST_COMMIT,
        FAILURE_CASE,
        POST_INTERACTION,
    }

    /** what kind of entity change is under test */
    public static enum ChangeScenario {
        ENTITY_CREATION,
        ENTITY_UPDATE,
        ENTITY_REMOVAL
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

        private final @NonNull String displayName;
        private final @NonNull ChangeScenario scenario;
        private final @NonNull Runnable given;
        private final @NonNull BiConsumer<ChangeScenario, VerificationStage> verifier;
        private final @NonNull VerificationStage transactionCompletionStage;

        private final TraceLog traceLog = new TraceLog();
        private final List<Throwable> verificationErrors = _Lists.newConcurrentList();

        public void runGiven() {
            traceLog.log("2.1 about to run given");
            given.run();
            traceLog.log("2.2 given has run");
        }

        public void runVerify(final VerificationStage verificationStage) {
            traceLog.log("4.? verify %s", verificationStage);
            try {
                verifier.accept(scenario, verificationStage);
            } catch (Throwable e) {
                verificationErrors.add(e);
            }
        }

        /**
         * @param preCommitListener - shared instance
         */
        public void bind(final CommitListener preCommitListener) {
            traceLog.log("1. bind to commit events");
            preCommitListener.bind(this);
        }

        public void changeProperty(final Runnable runnable) {
            traceLog.log("2.3 about to change book's name");
            runnable.run();
            traceLog.log("2.4 book's name has changed");
        }

        public void runPostCommitVerify() {
            runVerify(transactionCompletionStage);
        }

    }

    @FunctionalInterface
    private static interface PublishingTestRunner {
        boolean run(PublishingTestContext context) throws Exception;
    }

    /**
     * For each test we setup a {@link PublishingTestContext test-context},
     * which this singleton CommitListener instance will be temporarily bound
     * to, until the end of the test's exclusive transaction.
     */
    @Service
    public static class CommitListener {

        private PublishingTestContext testContext;

        /** transaction end boundary (pre) */
        @EventListener(TransactionBeforeCompletionEvent.class)
        public void onPreCommit(final TransactionBeforeCompletionEvent event) {
            _Probe.errOut("=== TRANSACTION before completion");
            if(testContext!=null) {
                testContext.getTraceLog().log("3. pre-commit event is occurring");
                testContext.runVerify(VerificationStage.PRE_COMMIT);
            }
        }

        /** transaction end boundary (post) */
        @EventListener(TransactionAfterCompletionEvent.class)
        public void onPreCommit(final TransactionAfterCompletionEvent event) {
            _Probe.errOut("=== TRANSACTION after completion");
            if(testContext!=null) {
                try {
                    testContext.runPostCommitVerify();
                } finally {
                    unbind(testContext);
                }
            }
        }

        public void bind(final PublishingTestContext testContext) {
            _Assert.assertNull(this.testContext, "PreCommitListener is already bound to a testContext.");
            this.testContext = testContext;
        }

        public void unbind(final PublishingTestContext testContext) {
            _Assert.assertEquals(this.testContext, testContext, "PreCommitListener is not bound to the testContext, "
                    + "which it receives a request to unbind from.");
            this.testContext.getTraceLog().log("4.? unbind from commit events");
            this.testContext = null;
        }

    }

    // -- DEPENDENCIES

    protected abstract InteractionService getInteractionService();
    protected abstract TransactionService getTransactionService();

    // -- CREATE DYNAMIC TESTS

    public final List<DynamicTest> generateTestsIncludeProgrammatic(
            final Runnable given,
            final BiConsumer<ChangeScenario, VerificationStage> verifier) {
        return generateTests(true, given, verifier);
    }

    public final List<DynamicTest> generateTests(
            final Runnable given,
            final BiConsumer<ChangeScenario, VerificationStage> verifier) {
        return generateTests(false, given, verifier);
    }

    private final List<DynamicTest> generateTests(
            final boolean includeProgrammatic,
            final Runnable given,
            final BiConsumer<ChangeScenario, VerificationStage> verifier) {

        var dynamicTests = Can.<DynamicTest>empty();

        for(val changeScenario : ChangeScenario.values()) {

            if(includeProgrammatic) {
                dynamicTests = dynamicTests
                    .add(publishingTest(
                            PublishingTestContext.of("Programmatic",
                                    changeScenario,
                                    given, verifier, VerificationStage.POST_COMMIT),
                            VerificationStage.POST_INTERACTION,
                            this::programmaticExecution));
            }
        }

        val changeScenario = ChangeScenario.ENTITY_UPDATE;

        dynamicTests = dynamicTests
                .add(publishingTest(
                        PublishingTestContext.of("Interaction Api",
                                changeScenario,
                                given, verifier, VerificationStage.POST_COMMIT),
                        VerificationStage.POST_INTERACTION,
                        this::interactionApiExecution))
                .add(publishingTest(
                        PublishingTestContext.of("Wrapper Sync w/o Rules",
                                changeScenario,
                                given, verifier, VerificationStage.POST_COMMIT),
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionNoRules))
//                    .add(publishingTest(
//                            PublishingTestContext.of("Wrapper Async w/o Rules",
//                                  changeScenario,
//                                  given, verifier, VerificationStage.POST_COMMIT),
//                            VerificationStage.POST_INTERACTION,
//                            this::wrapperAsyncExecutionNoRules))
                .add(publishingTest(
                        PublishingTestContext.of("Wrapper Sync w/ Rules (expected to fail w/ DisabledException)",
                                changeScenario,
                                given, verifier, VerificationStage.FAILURE_CASE),
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionWithFailure))
//                    .add(publishingTest(
//                            PublishingTestContext.of("Wrapper Async w/ Rules (expected to fail w/ DisabledException)",
//                                    changeScenario,
//                                    given, verifier, VerificationStage.FAILURE_CASE),
//                            VerificationStage.POST_INTERACTION,
//                            this::wrapperAsyncExecutionWithFailure))
                    ;

        return XrayUi.isXrayEnabled()
                ? dynamicTests
                        .add(dynamicTest("wait for xray viewer", XrayUi::waitForShutdown))
                        .toList()
                : dynamicTests
                        .toList();

    }

    /** to setup the test - method is embedded in its own interaction and transaction */
    protected abstract void setupEntity(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract boolean programmaticExecution(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract boolean interactionApiExecution(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract boolean wrapperSyncExecutionNoRules(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    @Deprecated // not deprecated - but we don't know yet how to test
    //XXX requires the AsyncExecution to run in its own interaction and transaction,
    // ideally to be enforced by running on a different thread, yet we are using the fork-join pool
    // which provides no such guarantee
    protected abstract boolean wrapperAsyncExecutionNoRules(PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException;

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract boolean wrapperSyncExecutionWithFailure(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract boolean wrapperAsyncExecutionWithFailure(PublishingTestContext context);


    // -- HELPER

    private final DynamicTest publishingTest(
            final PublishingTestContext testContext,
            final VerificationStage onSuccess,
            final PublishingTestRunner testRunner) {

        val displayName = testContext.getScenario().name() + ": " + testContext.getDisplayName();

        return dynamicTest(displayName, ()->{

            val traceLog = testContext.getTraceLog();

            xrayAddTest(displayName);

            traceLog.log("0. enter test %s", displayName);

            try {

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                getInteractionService().runAnonymous(()->{
                    val currentInteraction = getInteractionService().currentInteraction();
                    xrayEnterInteraction(currentInteraction);
                    setupEntity(testContext);
                    xrayExitInteraction();
                });

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                final boolean isExpectedToRunSuccesful = getInteractionService().callAnonymous(()->{
                    val currentInteraction = getInteractionService().currentInteraction();
                    xrayEnterInteraction(currentInteraction);
                    val result = testRunner.run(testContext);
                    xrayExitInteraction();
                    return result;
                });

                getInteractionService().closeInteractionLayers();

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                failWhenContextHasErrors(testContext);

                if(isExpectedToRunSuccesful) {
                    testContext.runVerify(onSuccess);
                    failWhenContextHasErrors(testContext);
                }

            } finally {
                traceLog.log("5. exit test %s", displayName);
            }

        });
    }

    private final void assert_no_initial_tx_context() {
        val txState = getTransactionService().currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    private final void failWhenContextHasErrors(final PublishingTestContext testContext) {
        if(!testContext.getVerificationErrors().isEmpty()) {
            fail(testContext.getVerificationErrors().get(0));
        }
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
