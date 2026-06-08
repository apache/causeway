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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

class CommandExportManagerCommandsTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));

    @Test
    void commands_collection_includes_mixed_replay_states_since_baseline() {
        final var undefined = entry(ReplayState.UNDEFINED);
        final var exported = entry(ReplayState.EXPORTED);
        final var repository = repositoryReturning(List.of(undefined, exported), List.of());
        final var manager = manager(repository);

        final var commands = manager.getCommands();

        assertThat(interactionIds(commands))
                .containsExactly(undefined.getInteractionId(), exported.getInteractionId());
    }

    @Test
    void previous_page_uses_unified_unfiltered_query() {
        final var excluded = entry(ReplayState.EXCLUDED);
        final var exported = entry(ReplayState.EXPORTED);
        final var repository = repositoryReturning(List.of(), List.of(exported, excluded));
        final var manager = manager(repository);

        final var commands = manager.commands(CommandExportManager.Direction.PREVIOUS);

        assertThat(interactionIds(commands))
                .containsExactly(exported.getInteractionId(), excluded.getInteractionId());
    }

    private static CommandExportManager manager(final CommandLogEntryRepository repository) {
        final var replayContext = new ReplayContext(null, null, null, repository, null, null, List.of());
        return new CommandExportManager(new CommandExportManager.State(BASELINE, 50), replayContext);
    }

    private static CommandLogEntryRepository repositoryReturning(
            final List<CommandLogEntry> next,
            final List<CommandLogEntry> previous) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(next);
        when(repository.findForegroundBeforeTimestamp(BASELINE, 50)).thenReturn(previous);
        java.util.stream.Stream.concat(next.stream(), previous.stream())
                .forEach(entry -> when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry)));
        return repository;
    }

    private static CommandLogEntry entry(final ReplayState replayState) {
        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    private static List<UUID> interactionIds(final List<ReplayableCommand> commands) {
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toList());
    }
}
