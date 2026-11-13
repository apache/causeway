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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.springframework.transaction.TransactionDefinition;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayableCommandMappingTest {

    @Test
    void replayRemapsCopyAndNotifiesResultInsideExecutionTransaction() {
        var transactionService = new ImmediateTransactionService();
        var interactionId = UUID.randomUUID();
        var recordedTarget = bookmark("demo.Customer", "1");
        var actualTarget = bookmark("demo.Customer", "2");
        var recordedResult = bookmark("demo.Invoice", "1");
        var actualResult = bookmark("demo.Invoice", "2");
        var notifications = new AtomicInteger();
        var listener = new CommandReplayMappingListener() {
            @Override
            public Optional<Bookmark> lookup(final Bookmark bookmark) {
                return bookmark.equals(recordedTarget) ? Optional.of(actualTarget) : Optional.empty();
            }

            @Override
            public void onReplayResult(
                    final Bookmark recorded,
                    final Bookmark actual,
                    final UUID commandInteractionId) {
                assertThat(transactionService.isActive()).isTrue();
                assertThat(recorded).isEqualTo(recordedResult);
                assertThat(actual).isEqualTo(actualResult);
                assertThat(commandInteractionId).isEqualTo(interactionId);
                notifications.incrementAndGet();
            }
        };
        var fixture = fixture(transactionService, interactionId, recordedTarget, recordedResult, listener, actualResult);

        var result = fixture.replayableCommand().tryReplayOrRetry();

        assertThat(result.isSuccess()).isTrue();
        assertThat(notifications).hasValue(1);
        verify(fixture.commandExecutorService()).executeCommand(
                org.mockito.ArgumentMatchers.eq(InteractionContextPolicy.SWITCH_USER_AND_TIME),
                org.mockito.ArgumentMatchers.<CommandDto>argThat(
                        dto -> dto != fixture.recordedDto() && target(dto).equals(actualTarget)));
        assertThat(target(fixture.recordedDto())).isEqualTo(recordedTarget);
    }

    @Test
    void everyRetryStartsFromRecordedDto() {
        var transactionService = new ImmediateTransactionService();
        var interactionId = UUID.randomUUID();
        var recordedTarget = bookmark("demo.Customer", "1");
        var actualTarget = bookmark("demo.Customer", "2");
        var lookups = new AtomicInteger();
        var listener = new CommandReplayMappingListener() {
            @Override
            public Optional<Bookmark> lookup(final Bookmark bookmark) {
                lookups.incrementAndGet();
                return Optional.of(actualTarget);
            }
        };
        var fixture = fixture(transactionService, interactionId, recordedTarget, null, listener, null);

        fixture.replayableCommand().tryReplayOrRetry();
        fixture.replayableCommand().tryReplayOrRetry();

        assertThat(lookups).hasValue(2);
        verify(fixture.commandExecutorService(), times(2)).executeCommand(
                org.mockito.ArgumentMatchers.eq(InteractionContextPolicy.SWITCH_USER_AND_TIME),
                org.mockito.ArgumentMatchers.<CommandDto>argThat(
                        dto -> dto != fixture.recordedDto() && target(dto).equals(actualTarget)));
        assertThat(target(fixture.recordedDto())).isEqualTo(recordedTarget);
    }

    @Test
    void missingRecordedOrActualResultDoesNotNotify() {
        var listener = mock(CommandReplayMappingListener.class);
        var first = fixture(
                new ImmediateTransactionService(), UUID.randomUUID(), bookmark("demo.Customer", "1"),
                null, listener, bookmark("demo.Invoice", "2"));
        var second = fixture(
                new ImmediateTransactionService(), UUID.randomUUID(), bookmark("demo.Customer", "1"),
                bookmark("demo.Invoice", "1"), listener, null);

        first.replayableCommand().tryReplayOrRetry();
        second.replayableCommand().tryReplayOrRetry();

        verify(listener, never()).onReplayResult(any(), any(), any());
    }

    @Test
    void failedCommandExecutionRecordsAnalysisAndMapsToSuccess() {
        var listener = mock(CommandReplayMappingListener.class);
        var fixture = fixture(
            new ImmediateTransactionService(), UUID.randomUUID(), bookmark("demo.Customer", "1"),
            bookmark("demo.Invoice", "1"), listener, bookmark("demo.Invoice", "2"));
        when(fixture.commandExecutorService().executeCommand(
            org.mockito.ArgumentMatchers.eq(InteractionContextPolicy.SWITCH_USER_AND_TIME),
            any(CommandDto.class)))
            .thenReturn(Try.failure(new IllegalStateException("execution failed")));

        var result = fixture.replayableCommand().tryReplayOrRetry();

        // a handled replay failure is mapped to success so a batch continues; the failure is recorded
        assertThat(result.isSuccess()).isTrue();
        verify(fixture.commandLogEntry()).saveAnalysis("execution failed");
        verify(listener, never()).onReplayResult(any(), any(), any());
    }

    @Test
    void notificationFailureRecordsAnalysisAndMapsToSuccess() {
        var listener = mock(CommandReplayMappingListener.class);
        var recordedResult = bookmark("demo.Invoice", "1");
        var actualResult = bookmark("demo.Invoice", "2");
        org.mockito.Mockito.doThrow(new IllegalStateException("mapping rejected"))
                .when(listener).onReplayResult(
                        org.mockito.ArgumentMatchers.eq(recordedResult),
                        org.mockito.ArgumentMatchers.eq(actualResult),
                        org.mockito.ArgumentMatchers.any());
        var fixture = fixture(
                new ImmediateTransactionService(), UUID.randomUUID(), bookmark("demo.Customer", "1"),
                recordedResult, listener, actualResult);

        var result = fixture.replayableCommand().tryReplayOrRetry();

        // the recorded failure no longer halts the batch: the outcome is a success, with the analysis recorded
        assertThat(result.isSuccess()).isTrue();
        verify(fixture.commandLogEntry()).saveAnalysis("mapping rejected");
    }

    @Test
    void disabledFailureIsRecordedWithTypedPrefix() {
        var listener = mock(CommandReplayMappingListener.class);
        var fixture = fixture(
            new ImmediateTransactionService(), UUID.randomUUID(), bookmark("demo.Customer", "1"),
            bookmark("demo.Invoice", "1"), listener, bookmark("demo.Invoice", "2"));
        var disabled = mock(DisabledException.class);
        when(disabled.getMessage()).thenReturn("not allowed");
        when(fixture.commandExecutorService().executeCommand(
            org.mockito.ArgumentMatchers.eq(InteractionContextPolicy.SWITCH_USER_AND_TIME),
            any(CommandDto.class)))
            .thenReturn(Try.failure(disabled));

        var result = fixture.replayableCommand().tryReplayOrRetry();

        // a hidden/disabled advisor failure is classified with a typed prefix in the recorded analysis
        assertThat(result.isSuccess()).isTrue();
        verify(fixture.commandLogEntry()).saveAnalysis("Disabled: not allowed");
    }

    private static Fixture fixture(
            final ImmediateTransactionService transactionService,
            final UUID interactionId,
            final Bookmark recordedTarget,
            final Bookmark recordedResult,
            final CommandReplayMappingListener listener,
            final Bookmark actualResult) {
        var recordedDto = command(recordedTarget);
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(recordedDto);
        when(entry.getReplayState()).thenReturn(ReplayState.PENDING);
        when(entry.getResult()).thenReturn(recordedResult);

        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(interactionId)).thenReturn(Optional.of(entry));

        var executor = mock(CommandExecutorService.class);
        when(executor.executeCommand(
                org.mockito.ArgumentMatchers.eq(InteractionContextPolicy.SWITCH_USER_AND_TIME),
                any(CommandDto.class)))
                .thenReturn(Try.success(actualResult));

        var remappingService = new ResultRemappingService(List.of(listener));
        var context = new ReplayContext(
                null, null, transactionService, repository, executor, null, remappingService);
        return new Fixture(new ReplayableCommand(interactionId, context), recordedDto, entry, executor);
    }

    private static CommandDto command(final Bookmark target) {
        var dto = new CommandDto();
        var targets = new OidsDto();
        var targetOid = new OidDto();
        targetOid.setType(target.logicalTypeName());
        targetOid.setId(target.identifier());
        targets.getOid().add(targetOid);
        dto.setTargets(targets);
        return dto;
    }

    private static Bookmark target(final CommandDto dto) {
        return Bookmark.forOidDto(dto.getTargets().getOid().get(0));
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
    }

    private record Fixture(
            ReplayableCommand replayableCommand,
            CommandDto recordedDto,
            CommandLogEntry commandLogEntry,
            CommandExecutorService commandExecutorService) {
    }

    private static final class ImmediateTransactionService implements TransactionService {
        private boolean active;

        @Override
        public <T> Try<T> callTransactional(
                final TransactionDefinition definition,
                final Callable<T> callable) {
            active = true;
            try {
                return Try.call(callable);
            } finally {
                active = false;
            }
        }

        boolean isActive() {
            return active;
        }

        @Override
        public Optional<TransactionId> currentTransactionId() {
            return Optional.empty();
        }

        @Override
        public TransactionState currentTransactionState() {
            return active ? TransactionState.IN_PROGRESS : TransactionState.NONE;
        }

        @Override
        public void flushTransaction() {
        }
    }
}
