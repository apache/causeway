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
package org.apache.causeway.extensions.executionoutbox.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.extensions.executionoutbox.applib.CausewayModuleExtExecutionOutboxApplib;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.Getter;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionOutboxEntry command} entities.
 *
 * @since 2.0 {@index}
 */
public interface ExecutionOutboxEntryRepository {

    public final static String LOGICAL_TYPE_NAME = CausewayModuleExtExecutionOutboxApplib.NAMESPACE + ".ExecutionOutboxEntryRepository";

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Execution log entry not found");
            this.interactionId = interactionId;
        }
    }

    ExecutionOutboxEntry createEntryAndPersist(final Execution execution);

    Optional<ExecutionOutboxEntry> findByInteractionIdAndSequence(final UUID interactionId, final int sequence);

    List<ExecutionOutboxEntry> findOldest();

    ExecutionOutboxEntry upsert(
            final UUID interactionId,
            final int sequence,
            final ExecutionOutboxEntryType executionType,
            final Timestamp startedAt,
            final String username,
            final Bookmark target,
            final String logicalMemberIdentifier,
            final String xml);

    ExecutionOutboxEntry upsert(
            final UUID interactionId,
            final int sequence,
            final ExecutionOutboxEntryType executionType,
            final Timestamp startedAt,
            final String username,
            final Bookmark target,
            final String logicalMemberIdentifier,
            final InteractionDto interactionDto);

    boolean deleteByInteractionIdAndSequence(final UUID interactionId, final int sequence);

    /**
     * for testing purposes only
     */
    List<ExecutionOutboxEntry> findAll();

    /**
     * for testing purposes only
     */
    void removeAll();

}
