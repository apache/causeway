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

import java.util.UUID;

import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.mixins.system.HasTransactionId;

import lombok.Value;

/**
 * Value type used to identify a transaction within the context of an
 * outer {@link org.apache.isis.applib.services.iactn.Interaction}.
 *
 * <p>
 *     The transaction and
 *     {@link org.apache.isis.applib.services.iactn.Interaction} are associated
 *     by the {@link #getInteractionId() uniqueId}.
 * </p>
 *
 * <p>
 *     Obtainable from {@link TransactionService#currentTransactionId()}.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Value(staticConstructor = "of")
public class TransactionId implements HasTransactionId {

    /**
     * The unique identifier of the outer
     * {@link org.apache.isis.applib.services.iactn.Interaction}.
     *
     * <p>
     *     Together with {@link #getSequence()}, this makes up the
     *     implementation of {@link org.apache.isis.applib.mixins.system.HasTransactionId}
     * </p>
     */
    UUID interactionId;

    /**
     * Identifies the transaction (there could be multiple) within the
     * {@link org.apache.isis.applib.services.iactn.Interaction}.
     *
     * <p>
     *     Together with {@link #getInteractionId()}, this makes up the
     *     implementation of {@link org.apache.isis.applib.mixins.system.HasTransactionId}
     * </p>
     */
    int sequence;

    /**
     * Identifies the persistence context that this {@link TransactionId} was
     * created for.
     *
     * <p>
     * Useful when there are multiple persistence contexts configured.
     * There are no constraints to format of this String, it is  left for the
     * implementation to ensure that the string is a uniqie identifier to
     * the context.
     * </p>
     */
    String context;

    // -- EMPTY

    private static final TransactionId EMPTY =
            TransactionId
            .of(UUID.fromString("0000-00-00-00-000000"), 0, "");

    /**
     * Factory method that returns a nominally &quot;empty&quot; transaction
     * identifier, used as a placeholder.
     */
    public static TransactionId empty() {
        return EMPTY;
    }

}
