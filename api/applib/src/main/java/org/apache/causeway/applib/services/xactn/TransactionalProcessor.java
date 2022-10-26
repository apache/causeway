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
package org.apache.causeway.applib.services.xactn;

import java.util.concurrent.Callable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;

import lombok.val;

/**
 * Provides the mechanism to run a block of code within a transaction boundary,
 * using Spring Framework's transaction primitives (such as
 * {@link TransactionDefinition} and its annotation equivalent,
 * {@link Propagation}).
 *
 * @since 2.0 {@index}
 */
public interface TransactionalProcessor {

    // -- INTERFACE

    /**
     * Runs given {@code callable} with a transactional boundary, where the detailed transactional behavior
     * is governed by given {@link TransactionDefinition} {@code def}.
     * @return {@link Try} of calling given {@code callable}
     */
    <T> Try<T> callTransactional(TransactionDefinition def, Callable<T> callable);

    /**
     * Runs given {@code runnable} with a transactional boundary, where the detailed transactional behavior
     * is governed by given {@link TransactionDefinition} {@code def}.
     */
    default Try<Void> runTransactional(final TransactionDefinition def, final ThrowingRunnable runnable) {
        return callTransactional(def, ThrowingRunnable.toCallable(runnable));
    }

    // -- SHORTCUTS - WITH PROPAGATION CONTROL

    /**
     * Runs given {@code callable} with a transactional boundary, where the detailed transactional behavior
     * is governed by given {@link Propagation} {@code propagation}.
     * <p>
     * More fine grained control is given via {@link #callTransactional(TransactionDefinition, Callable)}
     * @return {@link Try} of calling given {@code callable}
     */
    default <T> Try<T> callTransactional(final Propagation propagation, final Callable<T> callable) {
        val def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(propagation.value());
        return callTransactional(def, callable);
    }

    /**
     * Runs given {@code runnable} with a transactional boundary, where the detailed transactional behavior
     * is governed by given {@link Propagation} {@code propagation}.
     * <p>
     * More fine grained control is given via
     * {@link #runTransactional(TransactionDefinition, ThrowingRunnable)}
     */
    default Try<Void> runTransactional(final Propagation propagation, final ThrowingRunnable runnable) {
        return callTransactional(propagation, ThrowingRunnable.toCallable(runnable));
    }


    // -- SHORTCUTS - MOST FREQUENT USAGE

    /**
     * Runs given {@code callable} within an existing transactional boundary, or in the absence of such a
     * boundary, creates a new one.
     * <p>
     * In other words, support a current transaction, create a new one if none exists.
     * @param <T>
     * @param callable
     * @return {@link Try} of calling given {@code callable}
     */
    default <T> Try<T> callWithinCurrentTransactionElseCreateNew(final Callable<T> callable) {
        val def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return callTransactional(def, callable);
    }

    /**
     * Runs given {@code runnable} within an existing transactional boundary, or in the absence of such a
     * boundary creates a new one.
     *
     * @param runnable
     */
    default Try<Void> runWithinCurrentTransactionElseCreateNew(final ThrowingRunnable runnable) {
        return callWithinCurrentTransactionElseCreateNew(ThrowingRunnable.toCallable(runnable));
    }

}
