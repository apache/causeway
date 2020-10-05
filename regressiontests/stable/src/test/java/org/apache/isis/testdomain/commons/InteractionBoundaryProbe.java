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
package org.apache.isis.testdomain.commons;

import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.services.TransactionScopeListener;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

@Component
@IsisInteractionScope
public class InteractionBoundaryProbe implements TransactionScopeListener {

    @Inject private KVStoreForTesting kvStoreForTesting;

    /** INTERACTION BEGIN BOUNDARY */
    @PostConstruct
    public void init() {
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iactnStarted");
    }

    /** INTERACTION END BOUNDARY */
    @PreDestroy
    public void destroy() {
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "iactnEnded");
    }

    /** TRANSACTION BEGIN BOUNDARY */
    @Override
    public void onTransactionStarted() {
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txStarted");
    }

    /** TRANSACTION END BOUNDARY */
    @Override
    public void onTransactionEnding() {
        kvStoreForTesting.incrementCounter(InteractionBoundaryProbe.class, "txEnding");
    }
    
    // -- ACCESS TO COUNTERS
    
    public static long totalInteractionsStarted(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iactnStarted");
    }
    
    public static long totalInteractionsEnded(KVStoreForTesting kvStoreForTesting) {
        return kvStoreForTesting.getCounter(InteractionBoundaryProbe.class, "iactnEnded");
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
