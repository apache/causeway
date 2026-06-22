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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.springframework.transaction.annotation.Propagation;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

class CommandManager_pendingOrFailed_Test {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Bookmark RESULT = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void pending_or_failed_includes_safe_action_with_single_result() {
        final var safeAction = safeActionEntry(ReplayState.PENDING, RESULT);
        final var repository = repositoryReturningPendingOrFailed(List.of(safeAction));
        final var manager = manager(repository, safeActionSpecificationLoader());

        final var commands = manager.getPendingOrFailed();

        assertThat(commands)
                .extracting(ReplayableCommand::interactionId)
                .containsExactly(safeAction.getInteractionId());
    }

    @Test
    void pending_or_failed_returns_safe_action_without_result() {
        final var safeAction = safeActionEntry(ReplayState.PENDING, null);
        final var repository = repositoryReturningPendingOrFailed(List.of(safeAction));
        final var manager = manager(repository, safeActionSpecificationLoader());

        final var commands = manager.getPendingOrFailed();

        assertThat(commands).hasSize(1);
        assertThat(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE)).containsExactly(safeAction);
    }

    @Test
    void pending_or_failed_keeps_state_changing_command_without_result() {
        final var command = entry(ReplayState.PENDING);
        final var repository = repositoryReturningPendingOrFailed(List.of(command));
        final var manager = manager(repository, safeActionSpecificationLoader());

        final var commands = manager.getPendingOrFailed();

        assertThat(commands)
                .extracting(ReplayableCommand::interactionId)
                .containsExactly(command.getInteractionId());
    }

    @Test
    void selected_replay_stops_after_command_creates_pending_background_work() {
        final var first = entryWithCommandDto(ReplayState.PENDING);
        final var second = entryWithCommandDto(ReplayState.PENDING);
        final var repository = repositoryReturningPendingOrFailed(List.of(first, second));
        final var transactionService = transactionServiceExecutingCallable();
        final var pendingBackgroundCommands = new AtomicBoolean(false);
        when(repository.findBackgroundAndNotYetStarted()).thenAnswer(__ -> pendingBackgroundCommands.get()
                ? List.of(mock(CommandLogEntry.class))
                : List.of());
        final var commandExecutorService = commandExecutorSettingPendingBackground(pendingBackgroundCommands);
        final var manager = manager(repository, transactionService, commandExecutorService, safeActionSpecificationLoader());

        new CommandManager_replayOrRetrySelected(manager).act(manager.getPendingOrFailed());

        verify(commandExecutorService, times(1)).executeCommand(
                eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class));
    }

    @Test
    void selected_replay_continues_when_no_background_work_is_pending() {
        final var first = entryWithCommandDto(ReplayState.PENDING);
        final var second = entryWithCommandDto(ReplayState.PENDING);
        final var repository = repositoryReturningPendingOrFailed(List.of(first, second));
        final var transactionService = transactionServiceExecutingCallable();
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of());
        final var commandExecutorService = commandExecutorReturningSuccess();
        final var manager = manager(repository, transactionService, commandExecutorService, safeActionSpecificationLoader());

        new CommandManager_replayOrRetrySelected(manager).act(manager.getPendingOrFailed());

        verify(commandExecutorService, times(2)).executeCommand(
                eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class));
    }

    @Test
    void selected_replay_is_disabled_while_background_work_is_pending() {
        final var command = entry(ReplayState.PENDING);
        final var repository = repositoryReturningPendingOrFailed(List.of(command));
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of(mock(CommandLogEntry.class)));
        final var manager = manager(repository, safeActionSpecificationLoader());

        assertThat(new CommandManager_replayOrRetrySelected(manager).disableAct())
                .isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
        assertThat(new CommandManager_replayOrRetryNext(manager).disableAct())
                .isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
    }

    @Test
    void replay_next_does_not_report_background_wait_after_background_work_completes() {
        final var command = entry(ReplayState.PENDING);
        final var repository = repositoryReturningPendingOrFailed(List.of(command));
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of());
        final var manager = manager(repository, safeActionSpecificationLoader());

        assertThat(new CommandManager_replayOrRetryNext(manager).disableAct())
                .isNotEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
    }

    private static CommandManager manager(
            final CommandLogEntryRepository repository,
            final SpecificationLoader specificationLoader) {
        return manager(repository, null, null, specificationLoader);
    }

    private static CommandManager manager(
            final CommandLogEntryRepository repository,
            final TransactionService transactionService,
            final CommandExecutorService commandExecutorService,
            final SpecificationLoader specificationLoader) {
        final var replayContext = ReplayContext.builder()
                .transactionService(transactionService)
                .commandLogEntryRepository(repository)
                .commandExecutorService(commandExecutorService)
                .specificationLoader(specificationLoader)
                .resultRemappingService(ResultRemappingService.builder().build())
                .build();
        return new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);
    }

    private static TransactionService transactionServiceExecutingCallable() {
        final var transactionService = mock(TransactionService.class);
        when(transactionService.callTransactional(any(Propagation.class), any(Callable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<Object> callable = invocation.getArgument(1);
                    return Try.call(callable);
                });
        return transactionService;
    }

    private static CommandExecutorService commandExecutorReturningSuccess() {
        final var commandExecutorService = mock(CommandExecutorService.class);
        when(commandExecutorService.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenReturn(Try.success(null));
        return commandExecutorService;
    }

    private static CommandExecutorService commandExecutorSettingPendingBackground(final AtomicBoolean pendingBackgroundCommands) {
        final var commandExecutorService = mock(CommandExecutorService.class);
        when(commandExecutorService.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenAnswer(__ -> {
                    pendingBackgroundCommands.set(true);
                    return Try.success(null);
                });
        return commandExecutorService;
    }

    private static CommandLogEntryRepository repositoryReturningPendingOrFailed(final List<CommandLogEntry> entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE)).thenReturn(entries);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(entries);
        entries.forEach(entry -> when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry)));
        return repository;
    }

    private static CommandLogEntry entry(final ReplayState replayState) {
        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(Timestamp.from(Instant.now()));
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    private static CommandLogEntry entryWithCommandDto(final ReplayState replayState) {
        final var entry = entry(replayState);
        when(entry.getCommandDto()).thenReturn(commandWithTargetOnly("demo.Customer", "1"));
        return entry;
    }

    private static CommandLogEntry safeActionEntry(
            final ReplayState replayState,
            final Bookmark result) {
        final var actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        actionDto.setLogicalMemberIdentifier("demo.Customer#find");
        final var commandDto = new CommandDto();
        commandDto.setMember(actionDto);
        final var entry = entry(replayState);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(actionDto.getLogicalMemberIdentifier());
        when(entry.getResult()).thenReturn(result);
        return entry;
    }

    private static CommandDto commandWithTargetOnly(
            final String targetType,
            final String targetId) {
        final var commandDto = new CommandDto();
        commandDto.setInteractionId(UUID.randomUUID().toString());
        commandDto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(Timestamp.from(Instant.now())));

        final var target = new OidDto();
        target.setType(targetType);
        target.setId(targetId);
        final var targets = new OidsDto();
        targets.getOid().add(target);
        commandDto.setTargets(targets);
        return commandDto;
    }

    private static SpecificationLoader safeActionSpecificationLoader() {
        final var specificationLoader = mock(SpecificationLoader.class);
        final var objectSpecification = mock(ObjectSpecification.class);
        final var objectAction = mock(ObjectAction.class);
        doReturn(Customer.class).when(objectSpecification).getCorrespondingClass();
        when(objectAction.getSemantics()).thenReturn(SemanticsOf.SAFE);
        when(specificationLoader.specForLogicalTypeNameElseFail("demo.Customer")).thenReturn(objectSpecification);
        when(specificationLoader.loadFeature(any(Identifier.class))).thenReturn(Optional.of(objectAction));
        return specificationLoader;
    }

    private static class Customer {
    }
}
