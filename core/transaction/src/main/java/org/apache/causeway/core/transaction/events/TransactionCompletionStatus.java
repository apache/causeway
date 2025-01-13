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
package org.apache.causeway.core.transaction.events;

import jakarta.transaction.Status;

/**
 * @since 2.0 {@index}
 * @see Status
 */
public enum TransactionCompletionStatus {

    /** Completion status in case of proper commit. */
    COMMITTED,

    /** Completion status in case of proper rollback. */
    ROLLED_BACK,

    /** Completion status in case of heuristic mixed completion or system errors. */
    UNKNOWN,
    ;

    /**
     * @param status field from {@link Status}.
     */
    public static TransactionCompletionStatus forStatus(final int status) {
        switch (status) {
            case 3:
                // int STATUS_COMMITTED = 3;
                return COMMITTED;
            case 4:
                // int STATUS_ROLLEDBACK = 4;
                return ROLLED_BACK;
            default:
                // int STATUS_ACTIVE = 0;
                // int STATUS_MARKED_ROLLBACK = 1;
                // int STATUS_PREPARED = 2;
                // int STATUS_UNKNOWN = 5;
                // int STATUS_NO_TRANSACTION = 6;
                // int STATUS_PREPARING = 7;
                // int STATUS_COMMITTING = 8;
                // int STATUS_ROLLING_BACK = 9;
                return UNKNOWN;
        }
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
