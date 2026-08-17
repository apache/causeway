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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;
import org.junit.jupiter.api.Test;

class CommandSubscriberForCommandLogTest {

    @Test
    void pausedSubscriberSkipsReadyStartedAndCompletedNotifications() {
        var command = new Command(UUID.randomUUID());
        var commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        var commandLogPauseState = new CommandLogPauseState();
        commandLogPauseState.pause();
        var subscriber = subscriber(commandLogEntryRepository, commandLogPauseState);

        subscriber.onReady(command);
        subscriber.onStarted(command);
        subscriber.onCompleted(command);

        verify(commandLogEntryRepository, never()).findByInteractionId(command.getInteractionId());
    }

    @Test
    void resumedSubscriberCreatesEntryForSubsequentReadyCommand() {
        var command = new Command(UUID.randomUUID());
        var commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(command.getInteractionId())).thenReturn(Optional.empty());
        var commandLogPauseState = new CommandLogPauseState();
        commandLogPauseState.pause();
        commandLogPauseState.resume();
        var subscriber = subscriber(commandLogEntryRepository, commandLogPauseState);

        subscriber.onReady(command);

        verify(commandLogEntryRepository)
            .createEntryAndPersist(command, command.getParentInteractionId(), ExecuteIn.FOREGROUND);
    }

    @Test
    void unpausedSubscriberSynchronizesStartedAndCompletedNotifications() {
        var command = new Command(UUID.randomUUID());
        var commandLogEntry = mock(CommandLogEntry.class);
        var commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(command.getInteractionId()))
            .thenReturn(Optional.of(commandLogEntry));
        var subscriber = subscriber(commandLogEntryRepository, new CommandLogPauseState());

        subscriber.onStarted(command);
        subscriber.onCompleted(command);

        verify(commandLogEntry, times(2)).sync(command);
    }

    @Test
    void recordingRejectsNewForegroundEntryWhileBackgroundWorkIsPending() {
        var command = new Command(UUID.randomUUID());
        var pending = mock(CommandLogEntry.class);
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(command.getInteractionId())).thenReturn(Optional.empty());
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of(pending));

        assertThatThrownBy(() -> subscriber(repository, new CommandLogPauseState()).onReady(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1 background command(s) are pending execution")
                .hasMessageContaining("executed and committed before continuing");
        verify(repository, never()).createEntryAndPersist(
                command, command.getParentInteractionId(), ExecuteIn.FOREGROUND);
    }

    @Test
    void schedulingForegroundAndPrePersistedBackgroundEntriesBypassNewForegroundGate() {
        var foreground = new Command(UUID.randomUUID());
        var background = new Command(UUID.randomUUID());
        var foregroundEntry = mock(CommandLogEntry.class);
        var backgroundEntry = mock(CommandLogEntry.class);
        when(foregroundEntry.getExecuteIn()).thenReturn(ExecuteIn.FOREGROUND);
        when(foregroundEntry.getCommandDto()).thenReturn(foreground.getCommandDto());
        when(backgroundEntry.getExecuteIn()).thenReturn(ExecuteIn.BACKGROUND);
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(foreground.getInteractionId()))
                .thenReturn(Optional.of(foregroundEntry));
        when(repository.findByInteractionId(background.getInteractionId()))
                .thenReturn(Optional.of(backgroundEntry));

        var subscriber = subscriber(repository, new CommandLogPauseState());
        subscriber.onReady(foreground);
        subscriber.onReady(background);

        verify(repository, never()).findBackgroundAndNotYetStarted();
        verify(repository, never()).createEntryAndPersist(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @SuppressWarnings("unchecked")
	@Test
    void recordingContinuesAfterBackgroundWorkCompletes() {
        var blocked = new Command(UUID.randomUUID());
        var accepted = new Command(UUID.randomUUID());
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(blocked.getInteractionId())).thenReturn(Optional.empty());
        when(repository.findByInteractionId(accepted.getInteractionId())).thenReturn(Optional.empty());
        when(repository.findBackgroundAndNotYetStarted())
                .thenReturn(List.of(mock(CommandLogEntry.class)), List.of());
        var subscriber = subscriber(repository, new CommandLogPauseState());

        assertThatThrownBy(() -> subscriber.onReady(blocked)).isInstanceOf(IllegalStateException.class);
        subscriber.onReady(accepted);

        verify(repository).createEntryAndPersist(
                accepted, accepted.getParentInteractionId(), ExecuteIn.FOREGROUND);
    }

    @Test
    void disabledRecordingSupportAllowsNewForegroundEntryWhileBackgroundWorkIsPending() {
        var command = new Command(UUID.randomUUID());
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(command.getInteractionId())).thenReturn(Optional.empty());
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of(mock(CommandLogEntry.class)));

        subscriber(repository, new CommandLogPauseState(), RecordingSupport.DISABLED, true).onReady(command);

        verify(repository, never()).findBackgroundAndNotYetStarted();
        verify(repository).createEntryAndPersist(
                command, command.getParentInteractionId(), ExecuteIn.FOREGROUND);
    }

    @Test
    void disabledCommandLogSkipsPendingBackgroundGate() {
        var command = new Command(UUID.randomUUID());
        var repository = mock(CommandLogEntryRepository.class);

        subscriber(repository, new CommandLogPauseState(), RecordingSupport.ENABLED, false).onReady(command);

        verify(repository, never()).findByInteractionId(command.getInteractionId());
        verify(repository, never()).findBackgroundAndNotYetStarted();
    }

    private CommandSubscriberForCommandLog subscriber(
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandLogPauseState commandLogPauseState) {
        return subscriber(commandLogEntryRepository, commandLogPauseState, RecordingSupport.ENABLED, true);
    }

    private CommandSubscriberForCommandLog subscriber(
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandLogPauseState commandLogPauseState,
            final RecordingSupport recordingSupport,
            final boolean enabled) {
        var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(recordingSupport);
        return new CommandSubscriberForCommandLog(
            commandLogEntryRepository,
            null,
            configuration,
            null,
            commandLogPauseState) {
            @Override
            public boolean isEnabled() {
                return enabled;
            }
        };
    }
}
