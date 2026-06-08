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
package org.apache.causeway.extensions.commandlog.applib.subscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

class CommandSubscriberForCommandLogTest {

    @Test
    void on_started_preserves_recorded_command_data_for_replay_entries() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.PENDING);
        final var fixture = subscriberFixture(command, commandLogEntry, true, false);

        fixture.subscriber.onStarted(command);

        verify(commandLogEntry).sync(command);
    }

    @Test
    void on_completed_preserves_recorded_command_data_for_replay_entries() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.FAILED);
        final var fixture = subscriberFixture(command, commandLogEntry, true, false);

        fixture.subscriber.onCompleted(command);

        verify(commandLogEntry).sync(command);
    }

    @Test
    void on_started_keeps_full_sync_for_exportable_entries() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);
        final var fixture = subscriberFixture(command, commandLogEntry, true, false);

        fixture.subscriber.onStarted(command);

        verify(commandLogEntry).sync(command);
    }

    @Test
    void on_completed_keeps_full_sync_for_exportable_entries() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);
        final var fixture = subscriberFixture(command, commandLogEntry, true, false);

        fixture.subscriber.onCompleted(command);

        verify(commandLogEntry).sync(command);
    }

    @Test
    void on_started_does_not_lookup_entry_when_disabled() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);
        final var fixture = subscriberFixture(command, commandLogEntry, false, false);

        fixture.subscriber.onStarted(command);

        verify(fixture.commandLogEntryRepository, never()).findByInteractionId(command.getInteractionId());
        verify(commandLogEntry, never()).sync(command);
    }

    @Test
    void on_completed_does_not_lookup_entry_when_paused() {
        final var command = new Command(UUID.randomUUID());
        final var commandLogEntry = commandLogEntry(ReplayState.UNDEFINED);
        final var fixture = subscriberFixture(command, commandLogEntry, true, true);

        fixture.subscriber.onCompleted(command);

        verify(fixture.commandLogEntryRepository, never()).findByInteractionId(command.getInteractionId());
        verify(commandLogEntry, never()).sync(command);
    }

    private static CommandLogEntry commandLogEntry(final ReplayState replayState) {
        final var commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getReplayState()).thenReturn(replayState);
        return commandLogEntry;
    }

    private static SubscriberFixture subscriberFixture(
            final Command command,
            final CommandLogEntry commandLogEntry,
            final boolean enabled,
            final boolean paused) {
        final var commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(command.getInteractionId()))
                .thenReturn(Optional.of(commandLogEntry));
        final var commandLogPauseState = mock(CommandLogPauseState.class);
        when(commandLogPauseState.isPaused()).thenReturn(paused);
        final var subscriber = new CommandSubscriberForCommandLog(
                commandLogEntryRepository,
                null,
                null,
                null,
                commandLogPauseState) {
            @Override
            public boolean isEnabled() {
                return enabled;
            }
        };
        return new SubscriberFixture(subscriber, commandLogEntryRepository);
    }

    private static class SubscriberFixture {

        private final CommandSubscriberForCommandLog subscriber;
        private final CommandLogEntryRepository commandLogEntryRepository;

        private SubscriberFixture(
                final CommandSubscriberForCommandLog subscriber,
                final CommandLogEntryRepository commandLogEntryRepository) {
            this.subscriber = subscriber;
            this.commandLogEntryRepository = commandLogEntryRepository;
        }
    }
}
