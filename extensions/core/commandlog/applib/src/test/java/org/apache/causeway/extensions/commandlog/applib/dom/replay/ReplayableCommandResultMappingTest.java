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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.ReplayResultMappingListener;
import org.apache.causeway.schema.cmd.v2.CommandDto;

class ReplayableCommandResultMappingTest {

    @Test
    void notifies_listener_when_recorded_and_actual_results_differ() {
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        ReplayResultMappingListener listener = mock(ReplayResultMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, actualResult);

        verify(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
    }

    @Test
    void notifies_listener_when_recorded_and_actual_results_are_equal() {
        Bookmark result = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(result);
        ReplayResultMappingListener listener = mock(ReplayResultMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, result);

        verify(listener).onReplayResultMapped(result, result, commandLogEntry);
    }

    @Test
    void does_not_notify_listener_when_recorded_result_is_missing() {
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(null);
        ReplayResultMappingListener listener = mock(ReplayResultMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, actualResult);

        verify(listener, never()).onReplayResultMapped(any(), any(), any());
    }

    @Test
    void does_not_notify_listener_when_actual_result_is_missing() {
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        ReplayResultMappingListener listener = mock(ReplayResultMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, null);

        verify(listener, never()).onReplayResultMapped(any(), any(), any());
    }

    @Test
    void does_not_notify_listener_when_replay_fails() throws Exception {
        UUID interactionId = UUID.randomUUID();
        CommandDto commandDto = new CommandDto();
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);

        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));

        TransactionService transactionService = mock(TransactionService.class);
        AtomicInteger transactionCall = new AtomicInteger();
        when(transactionService.callTransactional(any(Propagation.class), any(Callable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<Object> callable = invocation.getArgument(1);
                    if (transactionCall.getAndIncrement() == 0) {
                        return Try.failure(new RuntimeException("replay failed"));
                    }
                    return Try.call(callable);
                });

        ReplayResultMappingListener listener = mock(ReplayResultMappingListener.class);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, null, null, List.of(listener));

        new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        verify(listener, never()).onReplayResultMapped(any(), any(), any());
    }

    private static ReplayableCommand replayableCommand(final ReplayResultMappingListener listener) {
        ReplayContext replayContext = new ReplayContext(null, null, null, null, null, null, List.of(listener));
        return new ReplayableCommand(UUID.randomUUID(), replayContext);
    }

    private static CommandLogEntry commandLogEntryWithRecordedResult(final Bookmark recordedResult) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getResult()).thenReturn(recordedResult);
        return commandLogEntry;
    }
}
