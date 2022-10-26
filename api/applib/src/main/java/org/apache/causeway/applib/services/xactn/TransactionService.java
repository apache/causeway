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

import java.util.Optional;

/**
 * Provides utilities to access active transactions associated with the
 * current thread.
 *
 * <p>
 *     This is a low-level service that domain objects will typically have
 *     little need to leverage; there will normally be a transaction started
 *     already by the framework at the beginning of an
 *     {@link org.apache.causeway.applib.services.iactn.Interaction} and committed
 *     at the end. On occasion though it can be useful to take
 *     explicit control over transaction boundaries, which is where the
 *     methods provided by this domain service can be useful.
 * </p>
 *
 * <p>
 *     NOTE: there is <i>no</i> method to close (which is to say to commit) an existing transaction, because of a
 *     subtlety with the JPA transaction manager: if a transaction wasn't a new one, then closing the transaction
 *     would actually mean to decrement the transaction counter (only when it hits zero should the transaction be
 *     committed); but if the previous transaction was instead suspended, the commit the new transaction and then
 *     resume the previous transaction.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface TransactionService extends TransactionalProcessor {

    /**
     * Optionally returns the unique identifier of the current thread's transaction,
     * based on whether there is an active transaction associated with the current thread.
     */
    Optional<TransactionId> currentTransactionId();

    /**
     * Returns the state of the current thread's transaction., or returns
     * {@link TransactionState#NONE}, if there is no active transaction associated with the
     * current thread.
     */
    TransactionState currentTransactionState();

    /**
     * Flushes all changes to the object store.
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query.
     * <p>
     * If there is no active transaction associated with the current thread, then does nothing.
     */
    void flushTransaction();

}
