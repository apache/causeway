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
package org.apache.causeway.core.interaction.integration;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.core.interaction.session.CausewayInteraction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("causeway.interaction.InteractionAwareTransactionalBoundaryHandler")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class InteractionAwareTransactionalBoundaryHandler {

    private final Can<PlatformTransactionManager> txManagers;

    @Inject
    public InteractionAwareTransactionalBoundaryHandler(final List<PlatformTransactionManager> txManagers) {
        this.txManagers = Can.ofCollection(txManagers);
    }

    // -- OPEN

    public void onOpen(final @NonNull CausewayInteraction interaction) {

        if (log.isDebugEnabled()) {
            log.debug("opening on {}", _Probe.currentThreadId());
        }

        if(txManagers.isEmpty()) {
            return; // nothing to do
        }

        val onCloseTasks = _Lists.<CloseTask>newArrayList(txManagers.size());
        interaction.putAttribute(OnCloseHandle.class, new OnCloseHandle(onCloseTasks));

        txManagers.forEach(txManager->newTransactionOrParticipateInExisting(txManager, onCloseTasks::add));

    }

    // -- CLOSE

    public void onClose(final @NonNull CausewayInteraction interaction) {

        if (log.isDebugEnabled()) {
            log.debug("closing on {}", _Probe.currentThreadId());
        }

        if(txManagers.isEmpty()) {
            return; // nothing to do
        }

        Optional.ofNullable(interaction.getAttribute(OnCloseHandle.class))
                .ifPresent(OnCloseHandle::runOnCloseTasks);

    }

    public void requestRollback(final @NonNull CausewayInteraction interaction) {
        Optional.ofNullable(interaction.getAttribute(OnCloseHandle.class))
                .ifPresent(OnCloseHandle::requestRollback);
    }

    // -- HELPER

    private void newTransactionOrParticipateInExisting(
            final PlatformTransactionManager txManager,
            final Consumer<CloseTask> onNewCloseTask) {

        val txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // either participate in existing or create new transaction
        val txStatus = txManager.getTransaction(txTemplate);
        if(txStatus==null // in support of JUnit testing (TransactionManagers might be mocked or hollow stubs)
                || !txStatus.isNewTransaction()) {
            // we are participating in an exiting transaction (or testing), nothing to do
            return;
        }

        // we have created a new transaction, so need to provide a CloseTask

        onNewCloseTask.accept(
            new CloseTask(
                    txStatus,
                    txManager.getClass().getName(), // info to be used for display in case of errors
                    ()->{

                        if(txStatus.isRollbackOnly()) {
                            txManager.rollback(txStatus);
                        } else {
                            txManager.commit(txStatus);
                        }

                    }));
    }

    @Value
    private static class CloseTask {
        private final @NonNull TransactionStatus txStatus;
        private final @NonNull String onErrorInfo;
        private final @NonNull ThrowingRunnable runnable;
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
