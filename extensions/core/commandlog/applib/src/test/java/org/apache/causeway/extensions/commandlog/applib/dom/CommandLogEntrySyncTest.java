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
package org.apache.causeway.extensions.commandlog.applib.dom;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.functional.Try;

class CommandLogEntrySyncTest {

    private static final Timestamp STARTED_AT = Timestamp.from(Instant.parse("2026-06-08T10:00:00Z"));
    private static final Timestamp COMPLETED_AT = Timestamp.from(Instant.parse("2026-06-08T10:00:01Z"));
    private static final Bookmark RESULT = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void sync_execution_metadata_preserves_recorded_outcome_for_replay_entries() {
        final var command = commandWithExecutionMetadataAndResult();
        final var commandLogEntry = commandLogEntry(ReplayState.OK);

        commandLogEntry.syncExecutionMetadata(command);

        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verify(commandLogEntry, never()).setResult(RESULT);
        verify(commandLogEntry, never()).setException((Throwable) null);
    }

    @Test
    void sync_execution_metadata_updates_outcome_for_non_replay_entries() {
        final var command = commandWithExecutionMetadataAndResult();
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);

        commandLogEntry.syncExecutionMetadata(command);

        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verify(commandLogEntry).setResult(RESULT);
        verify(commandLogEntry).setException((Throwable) null);
    }

    private static Command commandWithExecutionMetadataAndResult() {
        final var command = new Command(UUID.randomUUID());
        command.updater().setStartedAt(STARTED_AT);
        command.updater().setCompletedAt(COMPLETED_AT);
        command.updater().setResult(Try.success(RESULT));
        return command;
    }

    private static CommandLogEntry commandLogEntry(final ReplayState replayState) {
        final var commandLogEntry = mock(CommandLogEntry.class, CALLS_REAL_METHODS);
        when(commandLogEntry.getReplayState()).thenReturn(replayState);
        return commandLogEntry;
    }
}
