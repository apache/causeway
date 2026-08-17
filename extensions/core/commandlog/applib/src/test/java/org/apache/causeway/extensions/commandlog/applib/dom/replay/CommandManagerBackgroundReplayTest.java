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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;

class CommandManagerBackgroundReplayTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-08-06T10:00:00Z"));

    @Test
    void limitsExposeMaintenanceBoundsAndDefaultToTen() {
        assertThat(CommandManager_replayOrRetryMultiple.Limit.values())
                .extracting(CommandManager_replayOrRetryMultiple.Limit::limit)
                .containsExactly(5L, 10L, 20L, 40L, 80L, 160L, 320L, (long) Integer.MAX_VALUE);
        assertThat(CommandManager_replayOrRetryMultiple.Limit.values())
                .extracting(CommandManager_replayOrRetryMultiple.Limit::title)
                .containsExactly("5", "10", "20", "40", "80", "160", "320", "All");
        assertThat(new CommandManager_replayOrRetryMultiple(fixture(1, true).manager()).defaultLimit())
                .isEqualTo(CommandManager_replayOrRetryMultiple.Limit.TEN);
    }

    @Test
    void emptyManagerDisablesBothReplayActions() {
        var fixture = fixture(0, true);

        assertThat(new CommandManager_replayOrRetryNext(fixture.manager()).disableAct())
                .isEqualTo("No commands to execute");
        assertThat(new CommandManager_replayOrRetryMultiple(fixture.manager()).disableAct())
                .isEqualTo("No commands in collection");
    }

    @Test
    void replayNextExecutesOldestKnownCommandAndRetainsManagerState() {
        var fixture = fixture(2, true);
        var action = new CommandManager_replayOrRetryNext(fixture.manager());
        var memento = fixture.manager().viewModelMemento();

        assertThat(action.disableAct()).isNull();
        assertThat(action.act()).isSameAs(fixture.manager());

        assertThat(fixture.executions()).hasValue(1);
        assertThat(fixture.entries().get(0).state()).hasValue(ReplayState.OK);
        assertThat(fixture.entries().get(1).state()).hasValue(ReplayState.PENDING);
        assertThat(fixture.manager().viewModelMemento()).isEqualTo(memento);
    }

    @Test
    void replayNextRejectsUnknownParticipants() {
        var fixture = fixture(1, false);

        assertThat(new CommandManager_replayOrRetryNext(fixture.manager()).disableAct())
                .isEqualTo("Unknown participants (target and/or action args)");
    }

    @Test
    void boundedReplayUsesManagerOrderAndSelectedLimitWithoutKnownParticipantGate() {
        var fixture = fixture(6, false);
        var action = new CommandManager_replayOrRetryMultiple(fixture.manager());

        assertThat(action.act(CommandManager_replayOrRetryMultiple.Limit.FIVE))
                .isSameAs(fixture.manager());

        assertThat(fixture.executions()).hasValue(5);
        assertThat(fixture.entries()).extracting(EntryFixture::state)
                .extracting(AtomicReference::get)
                .containsExactly(
                        ReplayState.OK, ReplayState.OK, ReplayState.OK,
                        ReplayState.OK, ReplayState.OK, ReplayState.PENDING);
    }

    @Test
    void boundedReplayStopsForNewBackgroundWorkAndContinuesAfterCompletion() {
        var fixture = fixture(2, true);
        fixture.createBackgroundAfterNextExecution().set(true);
        var action = new CommandManager_replayOrRetryMultiple(fixture.manager());

        action.act(CommandManager_replayOrRetryMultiple.Limit.ALL);

        assertThat(fixture.executions()).hasValue(1);
        assertThat(fixture.entries().get(0).state()).hasValue(ReplayState.OK);
        assertThat(fixture.entries().get(1).state()).hasValue(ReplayState.PENDING);
        assertThat(action.disableAct()).isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);

        fixture.pendingBackground().set(false);
        fixture.createBackgroundAfterNextExecution().set(false);
        action.act(CommandManager_replayOrRetryMultiple.Limit.ALL);

        assertThat(fixture.executions()).hasValue(2);
        assertThat(fixture.entries().get(1).state()).hasValue(ReplayState.OK);
    }

    @Test
    void boundedReplayContinuesAfterRecordedFailure() {
        var fixture = fixture(2, true);
        fixture.failNextExecution().set(true);

        new CommandManager_replayOrRetryMultiple(fixture.manager())
                .act(CommandManager_replayOrRetryMultiple.Limit.ALL);

        // the first command fails, but the failure is recorded and mapped to success, so the batch
        // continues to the second command instead of halting on the first failure.
        assertThat(fixture.executions()).hasValue(2);
        verify(fixture.entries().get(0).entry()).saveAnalysis("replay failed");
        assertThat(fixture.entries().get(1).state()).hasValue(ReplayState.OK);
    }

    @Test
    void pendingBackgroundWorkDisablesAndDirectlyGuardsUnifiedActions() {
        var fixture = fixture(1, true);
        fixture.pendingBackground().set(true);
        var next = new CommandManager_replayOrRetryNext(fixture.manager());
        var multiple = new CommandManager_replayOrRetryMultiple(fixture.manager());

        assertThat(next.disableAct()).isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
        assertThat(multiple.disableAct()).isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
        assertThat(next.act()).isSameAs(fixture.manager());
        assertThat(multiple.act(CommandManager_replayOrRetryMultiple.Limit.ALL))
                .isSameAs(fixture.manager());
        verify(fixture.executor(), never()).executeCommand(
                any(InteractionContextPolicy.class), any(CommandDto.class));
    }

    @Test
    void legacyNextAndSelectedReplayUseSameGateAndResumeAfterCompletion() {
        var fixture = fixture(2, true);
        var legacy = new CommandReplayManager(BASELINE, fixture.context());
        var next = legacy.new replayOrRetryNext();
        var selected = legacy.new replayOrRetrySelected();
        fixture.pendingBackground().set(true);

        assertThat(next.disableAct()).isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
        assertThat(selected.disableAct()).isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
        next.act();
        selected.act(legacy.getPendingOrFailed());
        assertThat(fixture.executions()).hasValue(0);

        fixture.pendingBackground().set(false);
        assertThat(next.disableAct()).isNull();
        next.act();
        assertThat(fixture.executions()).hasValue(1);

        fixture.createBackgroundAfterNextExecution().set(true);
        selected.act(legacy.getPendingOrFailed());
        assertThat(fixture.executions()).hasValue(2);
        assertThat(fixture.pendingBackground()).isTrue();
    }

    private static Fixture fixture(final int count, final boolean knownParticipants) {
        var entries = java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> entry(index, knownParticipants))
                .toList();
        var repository = mock(CommandLogEntryRepository.class);
        var pendingBackground = new AtomicBoolean();
        when(repository.findBackgroundAndNotYetStarted()).thenAnswer(__ -> pendingBackground.get()
                ? List.of(mock(CommandLogEntry.class))
                : List.of());
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE))
                .thenAnswer(__ -> entries.stream()
                        .filter(entry -> entry.state().get().isPendingOrFailed())
                        .map(EntryFixture::entry)
                        .toList());
        when(repository.findForegroundSinceTimestamp(BASELINE, 50))
                .thenAnswer(__ -> entries.stream().map(EntryFixture::entry).toList());
        entries.forEach(entry -> when(repository.findByInteractionId(entry.entry().getInteractionId()))
                .thenReturn(Optional.of(entry.entry())));

        var executions = new AtomicInteger();
        var createBackgroundAfterNextExecution = new AtomicBoolean();
        var failNextExecution = new AtomicBoolean();
        var executor = mock(CommandExecutorService.class);
        when(executor.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenAnswer(invocation -> {
                    executions.incrementAndGet();
                    if (failNextExecution.getAndSet(false))
						return Try.failure(new IllegalStateException("replay failed"));
                    var command = invocation.<CommandDto>getArgument(1);
                    entries.stream()
                            .filter(entry -> entry.entry().getInteractionId().toString()
                                    .equals(command.getInteractionId()))
                            .findFirst()
                            .ifPresent(entry -> entry.state().set(ReplayState.OK));
                    if (createBackgroundAfterNextExecution.get()) {
                        pendingBackground.set(true);
                    }
                    return Try.success(null);
                });

        var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(RecordingSupport.ENABLED);
        var specificationLoader = mock(SpecificationLoader.class);
        if (knownParticipants) {
            var serviceSpecification = mock(ObjectSpecification.class);
            when(serviceSpecification.isDomainService()).thenReturn(true);
            when(specificationLoader.specForLogicalTypeName("demo.Service"))
                    .thenReturn(Optional.of(serviceSpecification));
        } else {
            when(specificationLoader.specForLogicalTypeName("demo.Unknown"))
                    .thenReturn(Optional.empty());
        }
        var context = new ReplayContext(
                null, null, new ImmediateTransactionService(), repository, executor, null,
                new ResultRemappingService(List.of()), null, null,
                configuration, specificationLoader, List.of());
        return new Fixture(
                new CommandManager(BASELINE, 50, context), context, entries, executor,
                executions, pendingBackground, createBackgroundAfterNextExecution, failNextExecution);
    }

    private static EntryFixture entry(final int index, final boolean knownParticipants) {
        var interactionId = UUID.randomUUID();
        var state = new AtomicReference<>(ReplayState.PENDING);
        var commandDto = new CommandDto();
        commandDto.setInteractionId(interactionId.toString());
        commandDto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(
                Timestamp.from(Instant.parse("2026-08-06T10:00:00Z").plusSeconds(index))));
        var target = new OidDto();
        target.setType(knownParticipants ? "demo.Service" : "demo.Unknown");
        target.setId("1");
        var targets = new OidsDto();
        targets.getOid().add(target);
        commandDto.setTargets(targets);
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(Timestamp.from(
                Instant.parse("2026-08-06T10:00:00Z").plusSeconds(index)));
        when(entry.getReplayState()).thenAnswer(__ -> state.get());
        when(entry.getCommandDto()).thenReturn(commandDto);
        return new EntryFixture(entry, state);
    }

    private record EntryFixture(CommandLogEntry entry, AtomicReference<ReplayState> state) { }

    private record Fixture(
            CommandManager manager,
            ReplayContext context,
            List<EntryFixture> entries,
            CommandExecutorService executor,
            AtomicInteger executions,
            AtomicBoolean pendingBackground,
            AtomicBoolean createBackgroundAfterNextExecution,
            AtomicBoolean failNextExecution) { }

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
