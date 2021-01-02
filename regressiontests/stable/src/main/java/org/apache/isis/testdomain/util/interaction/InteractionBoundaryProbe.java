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

import org.apache.isis.core.interaction.events.InteractionLifecycleEvent;
import org.apache.isis.core.transaction.events.TransactionBeginEvent;
import org.apache.isis.core.transaction.events.TransactionEndingEvent;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InteractionBoundaryProbe {

    @Inject private KVStoreForTesting kvStoreForTesting;
    
    
    @EventListener(InteractionLifecycleEvent.class)
    public void onIsisInteractionLifecycleEvent(InteractionLifecycleEvent event) {
        switch(event.getEventType()) {
        case HAS_STARTED:
            onIaStarted();
            break;
        case IS_ENDING:
            onIaEnded();
            break;
        default:
            break;
        }
    }

    /** INTERACTION BEGIN BOUNDARY */
    public void onIaStarted() {
        log.debug("iaStarted");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iaStarted");
    }

    /** INTERACTION END BOUNDARY */
    public void onIaEnded() {
        log.debug("iaEnded");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iaEnded");
    }

    /** TRANSACTION BEGIN BOUNDARY */
    @EventListener(TransactionBeginEvent.class)
    public void onTransactionStarted(TransactionBeginEvent event) {
        log.debug("txStarted");
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txStarted");
    }

    /** TRANSACTION END BOUNDARY */
    @EventListener(TransactionEndingEvent.class)
    public void onTransactionEnding(TransactionEndingEvent event) {
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txEnding");
    }
    
    // -- ACCESS TO COUNTERS
    
    public static long totalInteractionsStarted(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaStarted");
    }
    
    public static long totalInteractionsEnded(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iaEnded");
    }

    public static long totalTransactionsStarted(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txStarted");
    }
    
    public static long totalTransactionsEnded(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "txEnding");
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

        final long txStartCountBefore = totalTransactionsStarted(kvStoreForTesting);
        final long txEndCountBefore = totalTransactionsEnded(kvStoreForTesting);
        val result = supplier.get();
        final long txStartCountAfter = totalTransactionsStarted(kvStoreForTesting);
        final long txEndCountAfter = totalTransactionsEnded(kvStoreForTesting);

        Assertions.assertEquals(1, txStartCountAfter - txStartCountBefore);
        Assertions.assertEquals(1, txEndCountAfter - txEndCountBefore);
        
        return result;
    }
    

}
