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

/**
 * Represents the state of the current transaction.
 *
 * <p>
 *     Obtainable from {@link TransactionService#currentTransactionState()}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public enum TransactionState {

    /**
     * No transaction exists.
     */
    NONE,
    /**
     * Started, still in progress.
     *
     * <p>
     * May flush, commit or abort.
     * </p>
     */
    IN_PROGRESS,
    /**
     * Started, but has hit an exception.
     *
     * <p>
     * May not flush or commit (will throw an {@link IllegalStateException}),
     * can only abort.
     * </p>
     */
    MUST_ABORT,
    /**
     * Completed, having successfully committed.
     *
     * <p>
     * May not flush or abort or commit (will throw {@link IllegalStateException}).
     * </p>
     */
    COMMITTED,
    /**
     * Completed, having aborted.
     *
     * <p>
     * May not flush, commit or abort (will throw {@link IllegalStateException}).
     * </p>
     */
    ABORTED
    ;

    /**
     * Whether it is valid to flush the transaction (specifically if the
     * transaction is {@link #IN_PROGRESS in progress}.
     */
    public boolean canFlush() {
        return this == IN_PROGRESS;
    }

    /**
     * Whether it is valid to commit the transaction (specifically if the
     * transaction is {@link #IN_PROGRESS in progress}.
     */
    public boolean canCommit() {
        return this == IN_PROGRESS;
    }

    /**
     * Whether it is valid to mark as aborted this transaction.
     *
     * <p>
     *     This is the case if the transaction is either currently
     *     {@link #IN_PROGRESS in progress} or has already been marked as
     *     {@link #MUST_ABORT must abort}.
     * </p>
     */
    public boolean canAbort() {
        return this == IN_PROGRESS || this == MUST_ABORT;
    }

    /**
     * Whether the transaction is complete (that is, is either
     * {@link #COMMITTED committed} or {@link #ABORTED aborted}), and so a new
     * transaction can be started.
     */
    public boolean isComplete() {
        return this == COMMITTED || this == ABORTED;
    }

    /**
     * Whether the transaction is {@link #IN_PROGRESS in progress}.
     * @return
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * Whether the transaction {@link #MUST_ABORT must abort}.
     */
    public boolean mustAbort() {
        return this == MUST_ABORT;
    }

}
