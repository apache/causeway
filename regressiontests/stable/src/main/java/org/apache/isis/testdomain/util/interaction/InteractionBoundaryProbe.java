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
package org.apache.isis.testdomain.util.interaction;

import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.core.interaction.scope.InteractionScopeAware;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.transaction.events.TransactionAfterCompletionEvent;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InteractionBoundaryProbe implements InteractionScopeAware {

    @Inject private KVStoreForTesting kvStoreForTesting;
    
    /** INTERACTION BEGIN BOUNDARY */
    @Override
    public void beforeEnteringTransactionalBoundary(InteractionSession interactionSession) {
        log.debug("iaStarted");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iaStarted");
    }
    
    /** INTERACTION END BOUNDARY */
    @Override
    public void afterLeavingTransactionalBoundary(InteractionSession interactionSession) {
        log.debug("iaEnded");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iaEnded");
    }

    /** TRANSACTION BEGIN BOUNDARY */
    @EventListener(TransactionBeforeCompletionEvent.class)
    public void onTransactionEnding(TransactionBeforeCompletionEvent event) {
        log.debug("txStarted");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txEnding");
    }

    /** TRANSACTION END BOUNDARY */
    @EventListener(TransactionAfterCompletionEvent.class)
    public void onTransactionEnded(TransactionAfterCompletionEvent event) {
        if(event.isRolledBack()) {
            kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txRolledBack");
        } else if(event.isCommitted()) {
            kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txCommitted");
        }
    }
    
    // -- ACCESS TO COUNTERS
    
    public static long totalInteractionsStarted(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaStarted");
    }
    
    public static long totalInteractionsEnded(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaEnded");
    }

    public static long totalTransactionsEnding(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txEnding");
    }
    
    public static long totalTransactionsCommitted(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txCommitted");
    }
    
    public static long totalTransactionsRolledBack(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txRolledBack");
    }

    // -- ASSERTIONS (INTERACTIONAL)
    
    public static void assertInteractional(KVStoreForTesting kvStoreForTesting, Runnable runnable) {
        assertInteractional(kvStoreForTesting, ()->{ runnable.run(); return null; });
    }
    
    public static <T> T assertInteractional(KVStoreForTesting kvStoreForTesting, Supplier<T> supplier) {

        final long iaStartCountBefore = totalInteractionsStarted(kvStoreForTesting);
        final long iaEndCountBefore = totalInteractionsEnded(kvStoreForTesting);
        val result = supplier.get();
        final long iaStartCountAfter = totalInteractionsStarted(kvStoreForTesting);
        final long iaEndCountAfter = totalInteractionsEnded(kvStoreForTesting);

        Assertions.assertEquals(1, iaStartCountAfter - iaStartCountBefore);
        Assertions.assertEquals(1, iaEndCountAfter - iaEndCountBefore);
        
        return result;
    }
    
    // -- ASSERTIONS (TRANSACTIONAL)
    
    public static void assertTransactional(KVStoreForTesting kvStoreForTesting, Runnable runnable) {
        assertTransactional(kvStoreForTesting, ()->{ runnable.run(); return null; });
    }
    
    public static <T> T assertTransactional(KVStoreForTesting kvStoreForTesting, Supplier<T> supplier) {

        final long txEndCountBefore = totalTransactionsEnding(kvStoreForTesting);
        val result = supplier.get();
        final long txEndCountAfter = totalTransactionsEnding(kvStoreForTesting);

        Assertions.assertEquals(1, txEndCountAfter - txEndCountBefore);
        
        return result;
    }
    

}
