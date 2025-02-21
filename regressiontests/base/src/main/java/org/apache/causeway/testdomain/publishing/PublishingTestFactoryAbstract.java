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
package org.apache.causeway.testdomain.publishing;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;

import org.apache.causeway.applib.annotation.TransactionScope;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.Stickiness;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.ThreadMemento;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.transaction.events.TransactionCompletionStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class PublishingTestFactoryAbstract {

    public static enum VerificationStage {
        PRE_COMMIT,
        POST_COMMIT,
        FAILURE_CASE,
        POST_INTERACTION,
    }

    /** what kind of entity change is under test */
    @RequiredArgsConstructor @Getter
    public static enum ChangeScenario {
        ENTITY_CREATION("creation via factory-service", true),
        ENTITY_PERSISTING("persisting", true),
        ENTITY_LOADING("loading", true),
        PROPERTY_UPDATE("property update", false),
        ACTION_INVOCATION("action invocation", false),
        ENTITY_REMOVAL("removal", true);
        final String displayName;
        final boolean supportsProgrammatic;
    }

    public record PublishingTestContext(
            @NonNull String displayName,
            @NonNull ChangeScenario scenario,
            @NonNull Optional<Class<? extends Throwable>> expectedException,
            @NonNull Runnable given,
            @NonNull BiConsumer<ChangeScenario, VerificationStage> verifier,

            TraceLog traceLog,
            List<Throwable> verificationErrors) {

        public static PublishingTestContext of(
                final String displayName, final ChangeScenario scenario, final Optional<Class<? extends Throwable>> expectedException,
                final Runnable given, final BiConsumer<ChangeScenario, VerificationStage> verifier) {
            return new PublishingTestContext(displayName, scenario, expectedException, given, verifier,
                    new TraceLog(), _Lists.newConcurrentList());
        }

        public static class TraceLog {
            private final StringBuilder buffer = new StringBuilder();
            @Getter private boolean debug = true;

            public TraceLog log(final String format, final Object...args) {
                var msg = String.format(format, args);
                buffer.append(msg).append("\n");
                if(debug) {
                    System.err.println(msg);
                }
                return this;
            }

            public void header() {
                log("---------------------------------------------------------");
            }

            public void footer() {
                log("---------------------------------------------------------");
            }
        }

        public void runGiven() {
            traceLog.log("2.1 about to run given");
            given.run();
            traceLog.log("2.2 given has run");
        }

        public void runVerify(final VerificationStage verificationStage) {
            try {
                verifier.accept(scenario, verificationStage);
                traceLog.log("v. verify %s -> %s", verificationStage, "success");
            } catch (Throwable e) {
                verificationErrors.add(e);
                traceLog.log("v. verify %s -> %s", verificationStage, "failure");
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

        public void executeAction(final Runnable runnable) {
            traceLog.log("2.3 about to double book's price");
            runnable.run();
            traceLog.log("2.4 book's price has been doubled");
        }

    }

    @FunctionalInterface
    private static interface PublishingTestRunner {
        void run(PublishingTestContext context) throws Exception;
    }

    /**
     * For each test we setup a {@link PublishingTestContext test-context},
     * which this singleton CommitListener instance will be temporarily bound
     * to, until the end of the test's exclusive transaction.
     */
    @Service
    @TransactionScope
    public static class CommitListener implements TransactionSynchronization {

        private PublishingTestContext testContext;

//        /** transaction end boundary (pre) */
//      @EventListener(TransactionBeforeCompletionEvent.class)
//      public void onPreCompletion(final TransactionBeforeCompletionEvent event) {

        @Override
        public void beforeCompletion() {
            _Probe.errOut("=== TRANSACTION before completion");
            if(testContext!=null) {
                testContext.traceLog().log("3.1 pre-commit event is occurring");
                testContext.runVerify(VerificationStage.PRE_COMMIT);
            }
        }

//        /** transaction end boundary (post) */
//        @EventListener(TransactionAfterCompletionEvent.class)
//        public void onPostCompletion(final TransactionAfterCompletionEvent event) {

        @Override
        public void afterCompletion(final int status) {

            TransactionCompletionStatus transactionCompletionStatus = TransactionCompletionStatus.forStatus(status);
            _Probe.errOut("=== TRANSACTION after completion");
            if(testContext!=null) {
                try {
                    if(transactionCompletionStatus.isCommitted()) {
                        testContext.traceLog().log("3.2 post-commit event is occurring");
                        testContext.runVerify(VerificationStage.POST_COMMIT);
                    } else {
                        testContext.traceLog().log("3.2 rollback event is occurring");
                        testContext.runVerify(VerificationStage.FAILURE_CASE);
                    }

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
            this.testContext.traceLog().log("4.? unbind from commit events");
            this.testContext = null;
        }

    }

    // -- DEPENDENCIES

    protected abstract InteractionService getInteractionService();
    protected abstract TransactionService getTransactionService();

    // -- CREATE DYNAMIC TESTS

    public final List<DynamicTest> generateTests(
            final ChangeScenario changeScenario,
            final boolean includeProgrammatic,
            final Runnable given,
            final BiConsumer<ChangeScenario, VerificationStage> verifier) {

        Can<DynamicTest> dynamicTests = Can.<DynamicTest>empty();

        if(includeProgrammatic) {
            dynamicTests = dynamicTests
                .add(publishingTest(
                        PublishingTestContext.of("Programmatic",
                                changeScenario,
                                Optional.empty(),
                                given, verifier),
                        this::programmaticExecution));
        }

        if(changeScenario == ChangeScenario.PROPERTY_UPDATE
                || changeScenario == ChangeScenario.ACTION_INVOCATION) {

            dynamicTests = dynamicTests
                .add(publishingTest(
                        PublishingTestContext.of("Interaction Api",
                                changeScenario,
                                Optional.empty(),
                                given, verifier),
                        this::interactionApiExecution))
                .add(publishingTest(
                        PublishingTestContext.of("Wrapper Sync w/o Rules",
                                changeScenario,
                                Optional.empty(),
                                given, verifier),
                        this::wrapperSyncExecutionNoRules))
//                    .add(publishingTest(
//                            PublishingTestContext.of("Wrapper Async w/o Rules",
//                                  changeScenario,
//                                  given, verifier),
//                            this::wrapperAsyncExecutionNoRules))
                .add(publishingTest(
                        PublishingTestContext.of("Wrapper Sync w/ Rules (expected to fail w/ DisabledException)",
                                changeScenario,
                                Optional.of(DisabledException.class),
                                given, verifier),
                        this::wrapperSyncExecutionWithRules))
//                    .add(publishingTest(
//                            PublishingTestContext.of("Wrapper Async w/ Rules (expected to fail w/ DisabledException)",
//                                    changeScenario,
//                                    Optional.of(DisabledException.class),
//                                    given, verifier),
//                            this::wrapperAsyncExecutionWithRules))
                ;

        };

        return XrayUi.isXrayEnabled()
                ? dynamicTests
                        .add(dynamicTest("wait for xray viewer", XrayUi::waitForShutdown))
                        .toList()
                : dynamicTests
                        .toList();

    }

    /** to setup the test - method is embedded in its own interaction and transaction */
    protected abstract void setupEntity(PublishingTestContext context);

    protected abstract void releaseContext(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract void programmaticExecution(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract void interactionApiExecution(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract void wrapperSyncExecutionNoRules(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    @Deprecated // not deprecated - but we don't know yet how to test
    //XXX requires the AsyncExecution to run in its own interaction and transaction,
    // ideally to be enforced by running on a different thread, yet we are using the fork-join pool
    // which provides no such guarantee
    protected abstract void wrapperAsyncExecutionNoRules(PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException;

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract void wrapperSyncExecutionWithRules(PublishingTestContext context);

    /** a test - method is embedded in its own interaction and transaction */
    protected abstract void wrapperAsyncExecutionWithRules(PublishingTestContext context);

    // -- HELPER

    private final DynamicTest publishingTest(
            final PublishingTestContext testContext,
            final PublishingTestRunner testRunner) {

        var displayName = String.format("%s (%s)",
                testContext.displayName(),
                testContext.scenario().getDisplayName());

        var onSuccess = VerificationStage.POST_INTERACTION;
        var onFailure = VerificationStage.FAILURE_CASE;

        return dynamicTest(displayName, ()->{

            var traceLog = testContext.traceLog();

            xrayAddTest(displayName);

            traceLog.header();
            traceLog.log("0. enter test %s", displayName);

            try {

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                getInteractionService().runAnonymous(()->{
                    var currentInteraction = getInteractionService().currentInteraction();
                    xrayEnterInteraction(currentInteraction);
                    setupEntity(testContext);
                    xrayExitInteraction();
                });

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                var result = getInteractionService().runAnonymousAndCatch(()->{
                    var currentInteraction = getInteractionService().currentInteraction();
                    xrayEnterInteraction(currentInteraction);

                    try {
                        testRunner.run(testContext); // is allowed to throw
                    } finally {
                        xrayExitInteraction();
                    }

                });

                traceLog.log("entering post test-runner phase");

                assertFalse(getInteractionService().isInInteraction());
                assert_no_initial_tx_context();

                if(testContext.expectedException().isPresent()) {
                    var expectedException = testContext.expectedException().get();
                    var actualException = result.getFailure().map(Throwable::getClass).orElse(null);
                    assertEquals(expectedException, actualException);
                    testContext.runVerify(onFailure);
                    failWhenContextHasErrors(testContext);
                    return;
                }

                if(result.isFailure()) {
                    // unexpected failure
                    fail("unexpeted exception during test: ", result.getFailure().get());
                }

                failWhenContextHasErrors(testContext);
                testContext.runVerify(onSuccess);
                failWhenContextHasErrors(testContext);

            } finally {

                releaseContext(testContext);
                traceLog.log("5. exit test %s", displayName);
                traceLog.footer();
            }

        });
    }

    private final void assert_no_initial_tx_context() {
        var txState = getTransactionService().currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    private final void failWhenContextHasErrors(final PublishingTestContext testContext) {
        if(!testContext.verificationErrors().isEmpty()) {
            fail(testContext.verificationErrors().get(0));
        }
    }

    // -- XRAY

    private final void xrayAddTest(final String name) {

        var threadId = ThreadMemento.fromCurrentThread();

        XrayUi.updateModel(model->{
            model.addContainerNode(
                    model.getThreadNode(threadId),
                    String.format("Test: %s", name),
                    Stickiness.CAN_DELETE_NODE);

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
