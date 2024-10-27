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
package org.apache.causeway.testdomain.util.interaction;

import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;

import org.apache.causeway.applib.annotation.TransactionScope;
import org.apache.causeway.core.transaction.events.TransactionCompletionStatus;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.extern.log4j.Log4j2;

@Service
@TransactionScope
@Log4j2
public class InteractionBoundaryProbe implements TransactionSynchronization {

    @Inject private KVStoreForTesting kvStoreForTesting;

    @Override
    public void beforeCompletion() {
        TransactionSynchronization.super.beforeCompletion();

        log.debug("txStarted");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txEnding");
    }

    @Override
    public void afterCompletion(final int status) {
        TransactionCompletionStatus transactionCompletionStatus = TransactionCompletionStatus.forStatus(status);

        if(transactionCompletionStatus.isRolledBack()) {
            kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txRolledBack");
        } else if(transactionCompletionStatus.isCommitted()) {
            kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txCommitted");
        }
    }

    // -- ACCESS TO COUNTERS

    public static long totalInteractionsStarted(final KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaStarted");
    }

    public static long totalInteractionsEnded(final KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaEnded");
    }

    public static long totalTransactionsEnding(final KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txEnding");
    }

    public static long totalTransactionsCommitted(final KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txCommitted");
    }

    public static long totalTransactionsRolledBack(final KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txRolledBack");
    }

    // -- ASSERTIONS (INTERACTIONAL)

    public static void assertInteractional(final KVStoreForTesting kvStoreForTesting, final Runnable runnable) {
        assertInteractional(kvStoreForTesting, ()->{ runnable.run(); return null; });
    }

    public static <T> T assertInteractional(final KVStoreForTesting kvStoreForTesting, final Supplier<T> supplier) {

        final long iaStartCountBefore = totalInteractionsStarted(kvStoreForTesting);
        final long iaEndCountBefore = totalInteractionsEnded(kvStoreForTesting);
        var result = supplier.get();
        final long iaStartCountAfter = totalInteractionsStarted(kvStoreForTesting);
        final long iaEndCountAfter = totalInteractionsEnded(kvStoreForTesting);

        Assertions.assertEquals(1, iaStartCountAfter - iaStartCountBefore);
        Assertions.assertEquals(1, iaEndCountAfter - iaEndCountBefore);

        return result;
    }

    // -- ASSERTIONS (TRANSACTIONAL)

    public static void assertTransactional(final KVStoreForTesting kvStoreForTesting, final Runnable runnable) {
        assertTransactional(kvStoreForTesting, ()->{ runnable.run(); return null; });
    }

    public static <T> T assertTransactional(final KVStoreForTesting kvStoreForTesting, final Supplier<T> supplier) {

        final long txEndCountBefore = totalTransactionsEnding(kvStoreForTesting);
        var result = supplier.get();
        final long txEndCountAfter = totalTransactionsEnding(kvStoreForTesting);

        Assertions.assertEquals(1, txEndCountAfter - txEndCountBefore);

        return result;
    }

}
