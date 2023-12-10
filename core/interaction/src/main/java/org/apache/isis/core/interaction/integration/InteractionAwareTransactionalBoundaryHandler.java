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
package org.apache.isis.core.interaction.integration;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.core.interaction.IsisModuleCoreInteraction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.interaction.session.IsisInteraction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(InteractionAwareTransactionalBoundaryHandler.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class InteractionAwareTransactionalBoundaryHandler {

    public static final String LOGICAL_TYPE_NAME = IsisModuleCoreInteraction.NAMESPACE + ".InteractionAwareTransactionalBoundaryHandler";

    private final Can<PlatformTransactionManager> txManagers;

    @Inject
    public InteractionAwareTransactionalBoundaryHandler(final List<PlatformTransactionManager> txManagers) {
        this.txManagers = Can.ofCollection(txManagers);
    }

    // -- OPEN


    // -- CLOSE

    public void onClose(final @NonNull IsisInteraction interaction) {

        if (log.isDebugEnabled()) {
            log.debug("closing on {}", _Probe.currentThreadId());
        }

        if(txManagers.isEmpty()) {
            return; // nothing to do
        }

        Optional.ofNullable(interaction.getAttribute(OnCloseHandle.class))
                .ifPresent(OnCloseHandle::runOnCloseTasks);

    }


    // -- HELPER


    @Value
    public static class CloseTask {
        private final @NonNull TransactionStatus txStatus;
        private final @NonNull String onErrorInfo;
        private final @NonNull ThrowingRunnable runnable;
    }

    @RequiredArgsConstructor
    public static class OnCloseHandle {
        private final @NonNull List<CloseTask> onCloseTasks;
        public void requestRollback() {
            onCloseTasks.forEach(onCloseTask->{
                onCloseTask.txStatus.setRollbackOnly();
            });
        }
        public void runOnCloseTasks() {
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
