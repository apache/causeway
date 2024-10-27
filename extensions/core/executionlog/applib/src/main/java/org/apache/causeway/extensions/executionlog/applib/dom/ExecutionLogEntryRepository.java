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
package org.apache.causeway.extensions.executionlog.applib.dom;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.Execution;

import lombok.Getter;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionLogEntry command} entities.
 *
 * @since 2.0 {@index}
 */
public interface ExecutionLogEntryRepository {

    class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Execution log entry not found");
            this.interactionId = interactionId;
        }
    }

    ExecutionLogEntry createEntryAndPersist(final Execution execution);

    List<ExecutionLogEntry> findByInteractionId(final UUID interactionId);

    Optional<ExecutionLogEntry> findByInteractionIdAndSequence(final UUID interactionId, final int sequence);

    List<ExecutionLogEntry> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to);

    List<ExecutionLogEntry> findMostRecent();

    List<ExecutionLogEntry> findMostRecent(final int limit);

    List<ExecutionLogEntry> findByTarget(final Bookmark target);

    List<ExecutionLogEntry> findByTargetAndTimestampAfter(final Bookmark target, final Timestamp timestamp);

    List<ExecutionLogEntry> findByTargetAndTimestampBefore(final Bookmark target, final Timestamp timestamp);

    List<ExecutionLogEntry> findByTargetAndTimestampBetween(final Bookmark target, final Timestamp timestampFrom, final Timestamp timestampTo);

    List<ExecutionLogEntry> findByTimestampAfter(final Timestamp timestamp);

    List<ExecutionLogEntry> findByTimestampBefore(final Timestamp timestamp);

    List<ExecutionLogEntry> findByTimestampBetween(final Timestamp timestampFrom, final Timestamp timestampTo);

    List<ExecutionLogEntry> findRecentByUsername(final String username);

    List<ExecutionLogEntry> findRecentByTarget(final Bookmark target);

    /**
     * intended for testing purposes only
     */
    List<ExecutionLogEntry> findAll();

    /**
     * intended for testing purposes only
     */
    void removeAll();

}
