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
package org.apache.causeway.testdomain.transactions.jpa;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.causeway.applib.annotation.TransactionScope;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.core.transaction.events.TransactionCompletionStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;

@Service
@TransactionScope
public class CommitListener implements TransactionSynchronization {

    @Value
    @RequiredArgsConstructor
    public static class TransactionCompletionStatusHolder {
        final TransactionCompletionStatus transactionCompletionStatus;
    }

    @Override
    public void afterCompletion(int status) {
        TransactionCompletionStatus transactionCompletionStatus = TransactionCompletionStatus.forStatus(status);
        TransactionCompletionStatusHolder event = new TransactionCompletionStatusHolder(transactionCompletionStatus);
        //_Probe.errOut("=== TRANSACTION after completion (%s)", event.name());
        Optional.ofNullable(listener)
                .ifPresent(li -> {
                    li.accept(event);
                    unbind();
                });
    }

    private Consumer<TransactionCompletionStatusHolder> listener;

    void bind(final @NonNull Consumer<TransactionCompletionStatusHolder> listener) {
        this.listener = listener;
    }

    void unbind() {
        this.listener = null;
    }

}
