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
package org.apache.isis.core.transaction.events;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * @since 2.0 {@index}
 * @see TransactionSynchronization
 */
public enum TransactionAfterCompletionEvent {

    /** Completion status in case of proper commit. */
    COMMITTED,

    /** Completion status in case of proper rollback. */
    ROLLED_BACK,

    /** Completion status in case of heuristic mixed completion or system errors. */
    UNKNOWN,
    ;

    public static TransactionAfterCompletionEvent forStatus(int status) {
        return TransactionAfterCompletionEvent.values()[status];
    }

    public boolean isCommitted() {
        return this == COMMITTED;
    }

    public boolean isRolledBack() {
        return this == ROLLED_BACK;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

}
