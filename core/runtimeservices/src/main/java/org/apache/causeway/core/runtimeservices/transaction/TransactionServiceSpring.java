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
package org.apache.causeway.core.runtimeservices.transaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.interaction.session.CausewayInteraction;
import org.apache.causeway.core.runtime.flushmgmt.FlushMgmt;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.transaction.events.TransactionCompletionStatus;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link TransactionService}, which delegates to Spring's own transaction management
 * framework, such as {@link PlatformTransactionManager}.
 *
 * @implNote This implementation does not yet support more than one {@link PlatformTransactionManager}
 * on the same Spring context. If more than one are discovered, some methods will fail
 * with {@link IllegalStateException}s.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".TransactionServiceSpring")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Spring")
@Log4j2
public class TransactionServiceSpring
implements
    TransactionService {

    private final Can<PlatformTransactionManager> platformTransactionManagers;
    private final Provider<InteractionLayerTracker> interactionLayerTrackerProvider;
    private final Can<PersistenceExceptionTranslator> persistenceExceptionTranslators;
    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Inject
    public TransactionServiceSpring(
            final List<PlatformTransactionManager> platformTransactionManagers,
            final List<PersistenceExceptionTranslator> persistenceExceptionTranslators,
            final Provider<InteractionLayerTracker> interactionLayerTrackerProvider,
            final ConfigurableListableBeanFactory configurableListableBeanFactory
    ) {

        this.platformTransactionManagers = Can.ofCollection(platformTransactionManagers);
        log.info("PlatformTransactionManagers: {}", platformTransactionManagers);

        this.configurableListableBeanFactory = configurableListableBeanFactory;

        this.persistenceExceptionTranslators = Can.ofCollection(persistenceExceptionTranslators);
        log.info("PersistenceExceptionTranslators: {}", persistenceExceptionTranslators);

        this.interactionLayerTrackerProvider = interactionLayerTrackerProvider;
    }

    // -- API

    @Override
    public <T> Try<T> callTransactional(final TransactionDefinition def, final Callable<T> callable) {

        var platformTransactionManager = transactionManagerForElseFail(def); // always throws if configuration is wrong

        Try<T> result = null;

        try {
            TransactionStatus txStatus = platformTransactionManager.getTransaction(def);
            registerTransactionSynchronizations(txStatus);

            result = Try.call(() -> {
                        final T callResult = callable.call();

                        if(!FlushMgmt.isAutoFlushSuppressed()) {
                            // we flush here to ensure that the result captures any exception, eg from a declarative constraint violation
                            txStatus.flush();
                        }

                        return callResult;
                    })
                    .mapFailure(ex->translateExceptionIfPossible(ex, platformTransactionManager));

            if(result.isFailure()) {
                // if this is a nested transaction, then the javadoc says it will actually be just a call to
                // setRollbackOnly.
                platformTransactionManager.rollback(txStatus);
            } else {
                platformTransactionManager.commit(txStatus);
            }
        } catch (Exception ex) {

            // return the original failure cause (originating from calling the callable)
            // (so we don't shadow the original failure)
            // return the failure we just caught
            if (result != null && result.isFailure()) {
                return result;
            }

            // otherwise, we thought we had a success, but now we have an exception thrown by either ,
            // the call to rollback or commit above.  We don't need to do anything though; if either of
            // rollback or commit encountered an exception, they will have implicitly called setRollbackOnly (if nested)
            // or just rolled-back if top-level.

            return Try.failure(translateExceptionIfPossible(ex, platformTransactionManager));
        }

        return result;
    }

    private void registerTransactionSynchronizations(final TransactionStatus txStatus) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            configurableListableBeanFactory.getBeansOfType(TransactionSynchronization.class)
                    .values()
                    .stream().filter(AopUtils::isAopProxy)  // only the proxies
                    .forEach(TransactionSynchronizationManager::registerSynchronization);
        }
    }

//    @Override
//    public void nextTransaction() {
//
//        var txManager = singletonTransactionManagerElseFail();
//
//        try {
//
//            var txTemplate = new TransactionTemplate(txManager);
//            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//            // either reuse existing or create new
//            var txStatus = txManager.getTransaction(txTemplate);
//            if(txStatus.isNewTransaction()) {
//                // we have created a new transaction, so we are done
//                return;
//            }
//            // we are reusing an exiting transaction, so end it and create a new one afterwards
//            if(txStatus.isRollbackOnly()) {
//                txManager.rollback(txStatus);
//            } else {
//                //XXX we removed the entire method, because of following subtlety
                  // If the transaction wasn't a new one, omit the commit for proper participation in
//                // the surrounding transaction. If a previous transaction has been suspended to be
//                // able to create a new one, resume the previous transaction after committing the new one.
//                txManager.commit(txStatus);
//           }
//
//            // begin a new transaction
//            txManager.getTransaction(txTemplate);
//
//        } catch (RuntimeException ex) {
//
//            var translatedEx = translateExceptionIfPossible(ex, txManager);
//
//            if(translatedEx instanceof RuntimeException) {
//                throw ex;
//            }
//
//            throw new RuntimeException(ex);
//
//        }
//
//    }

    @Override
    public void flushTransaction() {

        try {

            log.debug("about to flush tx");

            currentTransactionStatus()
                .ifPresent(TransactionStatus::flush);

        } catch (RuntimeException ex) {

            var txManager = singletonTransactionManagerElseFail();

            var translatedEx = translateExceptionIfPossible(ex, txManager);

            if(translatedEx instanceof RuntimeException) {
                throw ex;
            }

            throw new RuntimeException(ex);

        }
    }

    @Override
    public Optional<TransactionId> currentTransactionId() {
        return interactionLayerTrackerProvider.get().getInteractionId()
                .map(uuid->{
                    //XXX get current transaction's persistence context (once we support multiple contexts)
                    var persistenceContext = "";
                    return TransactionId.of(uuid, txCounter.get().intValue(), persistenceContext);
                });
    }

    @Override
    public TransactionState currentTransactionState() {

        return currentTransactionStatus()
        .map(txStatus->{

            if(txStatus.isCompleted()) {
                return txStatus.isRollbackOnly()
                        ? TransactionState.ABORTED
                        : TransactionState.COMMITTED;
            }

            return txStatus.isRollbackOnly()
                    ? TransactionState.MUST_ABORT
                    : TransactionState.IN_PROGRESS;

        })
        .orElse(TransactionState.NONE);
    }

    // -- TRANSACTION SEQUENCE TRACKING

    // TODO: this ThreadLocal (as with all thread-locals) should perhaps somehow be managed using
    //  TransactionSynchronizationManager; see its javadoc for more details and look at implementations of
    //  TransactionSynchronization
    private ThreadLocal<LongAdder> txCounter = ThreadLocal.withInitial(LongAdder::new);

    // -- SPRING INTEGRATION

    private PlatformTransactionManager transactionManagerForElseFail(final TransactionDefinition def) {
        if(def instanceof TransactionTemplate) {
            var txManager = ((TransactionTemplate)def).getTransactionManager();
            if(txManager!=null) {
                return txManager;
            }
        }
        return platformTransactionManagers.getSingleton()
                .orElseThrow(()->
                    platformTransactionManagers.getCardinality().isMultiple()
                        ? _Exceptions.illegalState(
                                "Multiple PlatformTransactionManagers are configured, cannot determine which one to use. "
                                + "Instead make sure a PlatformTransactionManager is provided explicitly by passing in a TransactionTemplate (implementation of TransactionDefinition).")
                        : _Exceptions.illegalState("Needs a PlatformTransactionManager."));
    }

    private PlatformTransactionManager singletonTransactionManagerElseFail() {
        return platformTransactionManagers.getSingleton()
                .orElseThrow(()->
                    platformTransactionManagers.getCardinality().isMultiple()
                        ? _Exceptions.illegalState("Multiple PlatformTransactionManagers are configured, "
                                + "cannot reason about which one to use.")
                        : _Exceptions.illegalState("Needs a PlatformTransactionManager."));
    }

    private Optional<TransactionStatus> currentTransactionStatus() {

        var txManager = singletonTransactionManagerElseFail();
        var txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);

        // not strictly required, but to prevent stack-trace creation later on
        if(!TransactionSynchronizationManager.isActualTransactionActive()) {
            return Optional.empty();
        }

        // get current transaction else throw an exception
        return Try.call(()->
                //XXX creating stack-traces is expensive
                txManager.getTransaction(txTemplate))
                .getValue();

    }

    private Throwable translateExceptionIfPossible(final Throwable ex, final PlatformTransactionManager txManager) {

        if(ex instanceof DataAccessException) {
            return ex; // nothing to do, already translated
        }

        if(ex instanceof RuntimeException) {

            var translatedEx = persistenceExceptionTranslators.stream()
            //debug .peek(translator->System.out.printf("%s%n", translator.getClass().getName()))
            .map(translator->translator.translateExceptionIfPossible((RuntimeException)ex))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .orElse(null);

            if(translatedEx!=null) {
                return translatedEx;
            }

        }

        return ex;
    }

    /**
     * For use only by {@link org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault}, sets up
     * the initial transaction automatically against all available {@link PlatformTransactionManager}s.
     *
     * @param interaction The {@link CausewayInteraction} object representing the current interaction.
     */
    public void onOpen(final @NonNull CausewayInteraction interaction) {

        txCounter.get().reset();

        if (log.isDebugEnabled()) {
            log.debug("opening on {}", _Probe.currentThreadId());
        }

        if (!platformTransactionManagers.isEmpty()) {
            var onCloseTasks = _Lists.<CloseTask>newArrayList(platformTransactionManagers.size());

            interaction.putAttribute(OnCloseHandle.class, new OnCloseHandle(onCloseTasks));

            platformTransactionManagers.forEach(txManager -> {

                var txDefn = new TransactionTemplate(txManager); // specify the txManager in question
                txDefn.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                // either participate in existing or create new transaction
                TransactionStatus txStatus = txManager.getTransaction(txDefn);

                if(!txStatus.isNewTransaction()) {
                    // we are participating in an exiting transaction (or testing), nothing to do
                    return;
                }
                registerTransactionSynchronizations(txStatus);

                // we have created a new transaction, so need to provide a CloseTask
                onCloseTasks.add(
                    new CloseTask(
                        txStatus,
                        txManager.getClass().getName(), // info to be used for display in case of errors
                        () -> {
                            _Xray.txBeforeCompletion(interactionLayerTrackerProvider.get(), "tx: beforeCompletion");
                            final TransactionCompletionStatus event;
                            if (txStatus.isRollbackOnly()) {
                                txManager.rollback(txStatus);
                                event = TransactionCompletionStatus.ROLLED_BACK;
                            } else {
                                txManager.commit(txStatus);
                                event = TransactionCompletionStatus.COMMITTED;
                            }
                            _Xray.txAfterCompletion(interactionLayerTrackerProvider.get(), String.format("tx: afterCompletion (%s)", event.name()));

                            txCounter.get().increment();
                        }
                    )
                );
            });
        }
    }

    /**
     * For use only by {@link org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault}, if
     * {@link org.apache.causeway.applib.services.iactnlayer.InteractionService#run(InteractionContext, ThrowingRunnable)}
     * or {@link org.apache.causeway.applib.services.iactnlayer.InteractionService#call(InteractionContext, Callable)}
     * (or their various overloads) result in an exception.
     *
     * @param interaction The {@link CausewayInteraction} object representing the current interaction.
     */
    public void requestRollback(final @NonNull CausewayInteraction interaction) {
        Optional.ofNullable(interaction.getAttribute(OnCloseHandle.class))
                .ifPresent(OnCloseHandle::requestRollback);
    }

    /**
     * For use only by {@link org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault}, to close the
     * transaction initially set up in {@link #onOpen(CausewayInteraction)} against all configured
     * {@link PlatformTransactionManager}s.
     *
     * @param interaction The {@link CausewayInteraction} object representing the current interaction.
     */
    public void onClose(final @NonNull CausewayInteraction interaction) {

        if (log.isDebugEnabled()) {
            log.debug("closing on {}", _Probe.currentThreadId());
        }

        if (!platformTransactionManagers.isEmpty()) {
            Optional.ofNullable(interaction.getAttribute(OnCloseHandle.class))
                    .ifPresent(OnCloseHandle::runOnCloseTasks);
        }

        txCounter.remove(); //XXX not tested yet: can we be certain that no txCounter.get() is called afterwards?
    }

    @Value
    private static class CloseTask {
        @NonNull TransactionStatus txStatus;
        @NonNull String onErrorInfo;
        @NonNull ThrowingRunnable runnable;
    }

    @RequiredArgsConstructor
    private static class OnCloseHandle {
        private final @NonNull List<CloseTask> onCloseTasks;
        void requestRollback() {
            onCloseTasks.forEach(onCloseTask->{
                onCloseTask.txStatus.setRollbackOnly();
            });
        }
        void runOnCloseTasks() {
            onCloseTasks.forEach(onCloseTask->{

                try {
                    onCloseTask.getRunnable().run();
                } catch(final Throwable ex) {
                    // ignore
                    log.error(
                            "failed to close transactional boundary using transaction-manager {}; "
                                    + "continuing to avoid memory leakage",
                            onCloseTask.getOnErrorInfo(),
                            ex);
                }
            });
        }
    }
}
