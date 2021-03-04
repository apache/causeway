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
package org.apache.isis.applib.mixins.system;

import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.services.iactn.SequenceType;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;

/**
 * Extends {@link HasInteractionId} to add a strictly monotonically increasing
 * sequence number so that each transaction within the overall
 * {@link org.apache.isis.applib.services.iactn.Interaction} has its own
 * unique identity.
 *
 * <p>
 *     In the vast majority of cases there will only be a single transaction
 *     per {@link org.apache.isis.applib.services.iactn.Interaction}, but this
 *     isn't <i>always</i> the case as domain objects may on occasion need to
 *     explicitly manage transaction boundaries using
 *     {@link org.apache.isis.applib.services.xactn.TransactionService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface HasTransactionId extends HasInteractionId {

    /**
     * Holds the sequence number uniquely identifying the transaction number
     * within the overall
     * {@link org.apache.isis.applib.services.iactn.Interaction}.
     *
     * <p>
     *     The values in this sequence are ultimately obtained from the non-API
     *     method
     *     {@link org.apache.isis.applib.services.iactn.Interaction#next(SequenceType)},
     *     with a {@link SequenceType} of {@link SequenceType#TRANSACTION}.
     * </p>
     *
     * @return
     */
    int getSequence();
}
