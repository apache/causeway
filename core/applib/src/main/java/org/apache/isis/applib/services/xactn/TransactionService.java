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

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;

public interface TransactionService {

	TransactionId currentTransactionId();
	
    /**
     * Flush all changes to the object store.
     *
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query.
     * </p>
     *
     * <p>
     *     Equivalent to {@link Transaction#flush()} (with {@link Transaction} obtained using {@link #currentTransaction()}).
     * </p>
     */
    @Programmatic
    void flushTransaction();

    /**
     * See also {@link TransactionService#nextTransaction(TransactionService.Policy)} with a {@link TransactionService.Policy} of {@link TransactionService.Policy#UNLESS_MARKED_FOR_ABORT}.
     */
    @Programmatic
    default void nextTransaction() {
        nextTransaction((Command)null);
    }

//    /**
//     * Returns a representation of the current transaction.
//     */
//    @Programmatic
//    Transaction currentTransaction();

    /**
     * Generally this is equivalent to using {@link #currentTransaction()} and {@link Transaction#getTransactionState()}.
     * However, if there is no current transaction, then this will return {@link TransactionState#NONE}.
     */
    @Programmatic
    TransactionState currentTransactionState();

    /**
     * Return a latch, that allows threads to wait on the current transaction to complete.
     */
    @Programmatic
    CountDownLatch currentTransactionLatch();

    /**
     * Intended only for use by fixture scripts and integration tests.
     *
     * <p>
     *     The behaviour depends on the current state of the transaction, and the specified policy.
     *     <ul>
     *         <li>
     *              If the current transaction is in that it is still in progress, then commits and starts a new one.
     *         </li>
     *         <li>
     *              If the current transaction is complete, in that it is already committed or was rolled back, then simply starts a new one.
     *         </li>
     *         <li>
     *              If the current transaction is marked for abort, then depends on the provided policy:
     *              <ul>
     *                  <li>
     *                      If set to {@link Policy#ALWAYS always}, then rolls back and starts a new transaction
     *                  </li>
     *                  <li>
     *                      But if set to {@link Policy#UNLESS_MARKED_FOR_ABORT marked for abort}, then fails fast by throwing a runtime exception.
     *                  </li>
     *              </ul>
     *         </li>
     *
     *     </ul>
     *     If the current transaction has been marked for abort only, then depends on the provided rolls it back, and (again) starts a new one.
     * </p>
     *
     * <p>
     *     This is a refinement of the {@link TransactionService#nextTransaction()}, introduced in
     *     order to improve the error handling of that method in the case of an already must-abort transaction, and
     *     also to allow the caller to have more control on how to continue.
     * </p>
     */
    @Programmatic
    default void nextTransaction(Policy policy) {
        nextTransaction(policy, null);
    }
    
    /**
     * If the current transaction does not use the specified {@link Command} as its
     * {@link CommandContext#getCommand() command context}, then commit and start a new one.
     * @param command
     */
    default void nextTransaction(Command command) {
        nextTransaction(TransactionService.Policy.UNLESS_MARKED_FOR_ABORT, command);
    }

    /**
     * As per {@link #nextTransaction(Policy)} and {@link #nextTransaction(Command)}.
     */
    void nextTransaction(Policy policy, Command command);

    void executeWithinTransaction(Runnable task);
    <T> T executeWithinTransaction(Supplier<T> task);

    public enum Policy {
        UNLESS_MARKED_FOR_ABORT,
        ALWAYS
    }





}
