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

public interface TransactionService {

	public enum Policy {
        UNLESS_MARKED_FOR_ABORT,
        ALWAYS
    }
	
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
    void flushTransaction();

    /**
     * Generally this is equivalent to using {@link #currentTransaction()} and {@link Transaction#getTransactionState()}.
     * However, if there is no current transaction, then this will return {@link TransactionState#NONE}.
     */
    TransactionState currentTransactionState();

    /**
     * Return a latch, that allows threads to wait on the current transaction to complete.
     */
    CountDownLatch currentTransactionLatch();
    
    void executeWithinTransaction(Runnable task);
    <T> T executeWithinTransaction(Supplier<T> task);

}
