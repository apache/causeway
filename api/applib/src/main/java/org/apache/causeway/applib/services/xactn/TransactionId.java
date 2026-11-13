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

import java.util.UUID;

import org.apache.causeway.applib.mixins.system.HasInteractionIdAndSequence;

/**
 * Value type used to identify a transaction within the context of an
 * outer {@link org.apache.causeway.applib.services.iactn.Interaction}.
 * <p>
 * The transaction and
 * {@link org.apache.causeway.applib.services.iactn.Interaction} are associated
 * by the {@link #getInteractionId() uniqueId}.
 * <p>
 * Obtainable from {@link TransactionService#currentTransactionId()}.
 *
 * @since 2.0 {@index}
 */
public record TransactionId(
    /**
     * The unique identifier of the outer
     * {@link org.apache.causeway.applib.services.iactn.Interaction}.
     * <p>
     * Together with {@link #getSequence()}, this makes up the
     * implementation of {@link HasInteractionIdAndSequence}
     */
    UUID interactionId,

    /**
     * Identifies the transaction (there could be multiple) within the
     * {@link org.apache.causeway.applib.services.iactn.Interaction}.
     * <p>
     * Together with {@link #getInteractionId()}, this makes up the
     * implementation of {@link HasInteractionIdAndSequence}
     */
    int sequence,

    /**
     * Identifies the persistence context that this {@link TransactionId} was
     * created for.
     * <p>
     * Useful when there are multiple persistence contexts configured.
     * There are no constraints to format of this String, it is  left for the
     * implementation to ensure that the string is a unique identifier to
     * the context.
     */
    String context
    ) implements HasInteractionIdAndSequence {

    // -- EMPTY

    private static final TransactionId EMPTY =
            new TransactionId(UUID.fromString("0000-00-00-00-000000"), 0, "");

    /**
     * Factory method that returns a nominally &quot;empty&quot; transaction
     * identifier, used as a placeholder.
     */
    public static TransactionId empty() {
        return EMPTY;
    }
    
    @Override public UUID getInteractionId() { return interactionId; }
    @Override public int getSequence() { return sequence; }

}
