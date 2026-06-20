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
import org.apache.causeway.schema.cmd.v2.CommandDto;

class CommandLogEntrySyncTest {

    private static final UUID INTERACTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USERNAME = "sven";
    private static final Timestamp TIMESTAMP = Timestamp.from(Instant.parse("2026-06-08T09:59:59Z"));
    private static final Timestamp STARTED_AT = Timestamp.from(Instant.parse("2026-06-08T10:00:00Z"));
    private static final Timestamp COMPLETED_AT = Timestamp.from(Instant.parse("2026-06-08T10:00:01Z"));
    private static final Bookmark TARGET = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "target");
    private static final String LOGICAL_MEMBER_IDENTIFIER = "demo.Customer#act()";
    private static final Bookmark RESULT = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void sync_execution_metadata_preserves_recorded_outcome_for_replay_entries() {
        final var command = commandWithExecutionMetadataAndResult();
        final var commandLogEntry = commandLogEntry(ReplayState.OK);

        commandLogEntry.sync(command);

        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verify(commandLogEntry).setResult(RESULT);
        verify(commandLogEntry).setException((Throwable) null);
    }

    @Test
    void sync_execution_metadata_preserves_recorded_command_data_for_replay_entries() {
        final var commandDto = mock(CommandDto.class);
        final var command = commandWithAllData(commandDto);
        final var commandLogEntry = commandLogEntry(ReplayState.OK);

        commandLogEntry.sync(command);

        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verifyRecordedCommandDataWasNotOverwritten(commandLogEntry, commandDto);
    }

    @Test
    void sync_execution_metadata_updates_outcome_for_non_replay_entries() {
        final var command = commandWithExecutionMetadataAndResult();
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);

        commandLogEntry.sync(command);

        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verify(commandLogEntry).setResult(RESULT);
        verify(commandLogEntry).setException((Throwable) null);
    }

    @Test
    void sync_keeps_full_synchronization_for_non_replay_entries() {
        final var commandDto = mock(CommandDto.class);
        final var command = commandWithAllData(commandDto);
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);

        commandLogEntry.sync(command);

        verifyFullSync(commandLogEntry, commandDto);
    }

    @Test
    void sync_keeps_full_synchronization_for_entries_with_unset_replay_state() {
        final var commandDto = mock(CommandDto.class);
        final var command = commandWithAllData(commandDto);
        final var commandLogEntry = commandLogEntry(null);

        commandLogEntry.sync(command);

        verifyFullSync(commandLogEntry, commandDto);
    }

    private static Command commandWithExecutionMetadataAndResult() {
        final var command = new Command(UUID.randomUUID());
        command.updater().setStartedAt(STARTED_AT);
        command.updater().setCompletedAt(COMPLETED_AT);
        command.updater().setResult(Try.success(RESULT));
        return command;
    }

    private static Command commandWithAllData(final CommandDto commandDto) {
        final var command = mock(Command.class);
        when(command.getInteractionId()).thenReturn(INTERACTION_ID);
        when(command.getUsername()).thenReturn(USERNAME);
        when(command.getTimestamp()).thenReturn(TIMESTAMP);
        when(command.getCommandDto()).thenReturn(commandDto);
        when(command.getTarget()).thenReturn(TARGET);
        when(command.getLogicalMemberIdentifier()).thenReturn(LOGICAL_MEMBER_IDENTIFIER);
        when(command.getStartedAt()).thenReturn(STARTED_AT);
        when(command.getCompletedAt()).thenReturn(COMPLETED_AT);
        when(command.getResult()).thenReturn(RESULT);
        when(command.getException()).thenReturn(null);
        return command;
    }

    private static CommandLogEntry commandLogEntry(final ReplayState replayState) {
        final var commandLogEntry = mock(CommandLogEntry.class, CALLS_REAL_METHODS);
        when(commandLogEntry.getReplayState()).thenReturn(replayState);
        return commandLogEntry;
    }

    private static void verifyRecordedCommandDataWasNotOverwritten(
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        verify(commandLogEntry, never()).setInteractionId(INTERACTION_ID);
        verify(commandLogEntry, never()).setUsername(USERNAME);
        verify(commandLogEntry, never()).setTimestamp(TIMESTAMP);
        verify(commandLogEntry, never()).setCommandDto(commandDto);
        verify(commandLogEntry, never()).setTarget(TARGET);
        verify(commandLogEntry, never()).setLogicalMemberIdentifier(LOGICAL_MEMBER_IDENTIFIER);
    }

    private static void verifyFullSync(
            final CommandLogEntry commandLogEntry,
            final CommandDto commandDto) {
        verify(commandLogEntry).setInteractionId(INTERACTION_ID);
        verify(commandLogEntry).setUsername(USERNAME);
        verify(commandLogEntry).setTimestamp(TIMESTAMP);
        verify(commandLogEntry).setCommandDto(commandDto);
        verify(commandLogEntry).setTarget(TARGET);
        verify(commandLogEntry).setLogicalMemberIdentifier(LOGICAL_MEMBER_IDENTIFIER);
        verify(commandLogEntry).setStartedAt(STARTED_AT);
        verify(commandLogEntry).setCompletedAt(COMPLETED_AT);
        verify(commandLogEntry).setResult(RESULT);
        verify(commandLogEntry).setException((Throwable) null);
    }
}
