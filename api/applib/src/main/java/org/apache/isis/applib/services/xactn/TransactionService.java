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

package org.apache.isis.applib.services.xactn;

import java.util.function.Supplier;

/**
 * 
 * @since 2.0 {@index}
 */
public interface TransactionService {

    /**
     * When called within an existing transactional boundary returns the unique identifier to the transaction,
     * {@code null} otherwise.
     *
     * @return nullable
     */
    TransactionId currentTransactionId();

    /**
     * @return - the state of the current transaction.  If there is no current transaction, then returns {@link TransactionState#NONE}.
     */
    TransactionState currentTransactionState();

    /**
     * Flush all changes to the object store.
     *
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query.
     * </p>
     */
    void flushTransaction();


    /**
     * Commits the current transaction (if there is one), and begins a new one.
     *
     * If there is no current transaction, then is a no-op.
     */
    void nextTransaction();

    /**
     * Runs given {@code task} within an existing transactional boundary, or in the absence of such a
     * boundary creates a new one.
     *
     * @param task
     */
    void executeWithinTransaction(Runnable task);

    /**
     * Runs given {@code task} within an existing transactional boundary, or in the absence of such a
     * boundary creates a new one.
     *
     * @param task
     */
    <T> T executeWithinTransaction(Supplier<T> task);


}
