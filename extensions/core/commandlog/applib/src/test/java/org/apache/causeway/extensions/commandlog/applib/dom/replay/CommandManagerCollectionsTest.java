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

import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerCollectionsTest {

    private static final Timestamp BASELINE = Timestamp.valueOf("2026-08-06 10:00:00");

    @Test
    void collectionsPreserveStateMembershipOrderAndPendingException() {
        var repository = mock(CommandLogEntryRepository.class);
        var entries = entriesByState();
        var all = List.of(
                entries.get(ReplayState.UNDEFINED),
                entries.get(ReplayState.EXPORTED),
                entries.get(ReplayState.PENDING),
                entries.get(ReplayState.OK),
                entries.get(ReplayState.FAILED),
                entries.get(ReplayState.EXCLUDED));
        when(repository.findForegroundSinceTimestamp(BASELINE, 100)).thenReturn(all);
        when(repository.findForegroundSinceTimestampAndWithReplayExcluded(BASELINE))
                .thenReturn(List.of(entries.get(ReplayState.EXCLUDED)));
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE))
                .thenReturn(List.of(entries.get(ReplayState.PENDING), entries.get(ReplayState.FAILED)));
        when(repository.findForegroundSinceTimestampAndWithReplayRecordedOrReplayed(BASELINE))
                .thenReturn(List.of(
                        entries.get(ReplayState.UNDEFINED),
                        entries.get(ReplayState.EXPORTED),
                        entries.get(ReplayState.OK)));
        var manager = new CommandManager(BASELINE, 100, context(repository));

        assertThat(ids(manager.getCommandsInSequence()))
                .containsExactlyElementsOf(ids(all.subList(0, 5)));
        assertThat(ids(manager.getExcluded()))
                .containsExactly(entries.get(ReplayState.EXCLUDED).getInteractionId());
        assertThat(ids(manager.getPendingOrFailed())).containsExactly(
                entries.get(ReplayState.PENDING).getInteractionId(),
                entries.get(ReplayState.FAILED).getInteractionId());
        assertThat(ids(manager.getRecordedOrReplayed())).containsExactly(
                entries.get(ReplayState.UNDEFINED).getInteractionId(),
                entries.get(ReplayState.EXPORTED).getInteractionId(),
                entries.get(ReplayState.OK).getInteractionId());
        verify(repository).findForegroundSinceTimestamp(BASELINE, 100);
        all.forEach(CommandManagerCollectionsTest::verifyUnchanged);
    }

    @Test
    void eligibilityAppliesToGeneralCollectionsButPendingBypassesIt() {
        var repository = mock(CommandLogEntryRepository.class);
        var ineligibleSafe = CommandManagerEligibilityTest.entry(null);
        when(ineligibleSafe.getReplayState()).thenReturn(ReplayState.PENDING);
        when(repository.findForegroundSinceTimestamp(BASELINE, 5)).thenReturn(List.of(ineligibleSafe));
        when(repository.findForegroundSinceTimestampAndWithReplayExcluded(BASELINE)).thenReturn(List.of(ineligibleSafe));
        when(repository.findForegroundSinceTimestampAndWithReplayRecordedOrReplayed(BASELINE)).thenReturn(List.of(ineligibleSafe));
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE)).thenReturn(List.of(ineligibleSafe));
        var manager = new CommandManager(BASELINE, 5, CommandManagerEligibilityTest.context(repository));

        assertThat(manager.getCommandsInSequence()).isEmpty();
        assertThat(manager.getExcluded()).isEmpty();
        assertThat(manager.getRecordedOrReplayed()).isEmpty();
        assertThat(manager.getPendingOrFailed()).extracting(ReplayableCommand::interactionId)
                .containsExactly(ineligibleSafe.getInteractionId());
        verifyUnchanged(ineligibleSafe);
    }

    @Test
    void focusedCollectionsAreNotTruncatedBySequenceLimit() {
        var repository = mock(CommandLogEntryRepository.class);
        var first = entriesByState().get(ReplayState.PENDING);
        var second = entriesByState().get(ReplayState.FAILED);
        when(repository.findForegroundSinceTimestamp(BASELINE, 1)).thenReturn(List.of(first));
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE))
                .thenReturn(List.of(first, second));
        var manager = new CommandManager(BASELINE, 1, context(repository));

        assertThat(manager.getCommandsInSequence()).hasSize(1);
        assertThat(manager.getPendingOrFailed()).hasSize(2);
        verify(repository).findForegroundSinceTimestamp(BASELINE, 1);
    }

    private static ReplayContext context(final CommandLogEntryRepository repository) {
        return new ReplayContext(null, null, null, repository, null, null,
                new ResultRemappingService(List.of()), null, null);
    }

    private static Map<ReplayState, CommandLogEntry> entriesByState() {
        var entries = new EnumMap<ReplayState, CommandLogEntry>(ReplayState.class);
        for (var state : ReplayState.values()) {
            var entry = mock(CommandLogEntry.class);
            when(entry.getInteractionId()).thenReturn(UUID.randomUUID());
            when(entry.getReplayState()).thenReturn(state);
            when(entry.getCommandDto()).thenReturn(new CommandDto());
            entries.put(state, entry);
        }
        return entries;
    }

    private static List<UUID> ids(final List<? extends CommandLogEntry> entries) {
        return entries.stream().map(CommandLogEntry::getInteractionId).toList();
    }

    private static List<UUID> ids(final java.util.Collection<ReplayableCommand> commands) {
        return commands.stream().map(ReplayableCommand::interactionId).toList();
    }

    private static void verifyUnchanged(final CommandLogEntry entry) {
        verify(entry, never()).setReplayState(any());
        verify(entry, never()).setResult(any());
    }
}
