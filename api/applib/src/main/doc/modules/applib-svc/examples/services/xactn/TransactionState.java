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

public enum TransactionState {

    /**
     * No transaction exists.
     */
    NONE,
    /**
     * Started, still in progress.
     * <p/>
     * <p/>
     * May flush, commit or abort.
     */
    IN_PROGRESS,
    /**
     * Started, but has hit an exception.
     * <p/>
     * <p/>
     * May not flush or commit (will throw an {@link IllegalStateException}),
     * can only abort.
     * <p/>
     * <p/>
     * Similar to <tt>setRollbackOnly</tt> in EJBs.
     */
    MUST_ABORT,
    /**
     * Completed, having successfully committed.
     * <p/>
     * <p/>
     * May not flush or abort or commit (will throw {@link IllegalStateException}).
     */
    COMMITTED,
    /**
     * Completed, having aborted.
     * <p/>
     * <p/>
     * May not flush, commit or abort (will throw {@link IllegalStateException}).
     */
    ABORTED;

    private TransactionState() {
    }

    /**
     * Whether it is valid to flush the transaction.
     */
    public boolean canFlush() {
        return this == IN_PROGRESS;
    }

    /**
     * Whether it is valid to commit the transaction.
     */
    public boolean canCommit() {
        return this == IN_PROGRESS;
    }

    /**
     * Whether it is valid to mark as aborted this transaction}.
     */
    public boolean canAbort() {
        return this == IN_PROGRESS || this == MUST_ABORT;
    }

    /**
     * Whether the transaction is complete (and so a new one can be started).
     */
    public boolean isComplete() {
        return this == COMMITTED || this == ABORTED;
    }

    public boolean mustAbort() {
        return this == MUST_ABORT;
    }
}
