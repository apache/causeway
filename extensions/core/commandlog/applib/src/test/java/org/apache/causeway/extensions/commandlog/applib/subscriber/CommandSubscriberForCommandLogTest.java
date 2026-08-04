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

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private CommandSubscriberForCommandLog subscriber(
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandLogPauseState commandLogPauseState) {
        return new CommandSubscriberForCommandLog(
            commandLogEntryRepository,
            null,
            null,
            null,
            commandLogPauseState) {
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }
}
