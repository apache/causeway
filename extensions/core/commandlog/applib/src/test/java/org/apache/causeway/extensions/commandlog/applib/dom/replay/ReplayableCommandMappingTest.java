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
import static org.mockito.Mockito.doThrow;
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

import org.mockito.ArgumentCaptor;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.cmd.v2.CommandDto;

class ReplayableCommandMappingTest {

    @Test
    void target_remapping_changes_replay_command_dto() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter("simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2")));

        CommandDto replayCommandDto = replayableCommand(listener).commandDtoPossiblyRemappedForReplay(commandLogEntry);

        assertThat(replayCommandDto.getTargets().getOid().get(0).getType()).isEqualTo("simple.SimpleObject");
        assertThat(replayCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("2");
        assertThat(recordedCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("1");
    }

    @Test
    void missing_target_remapping_leaves_replay_command_dto_unchanged() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter("simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        CommandDto replayCommandDto = replayableCommand(listener).commandDtoPossiblyRemappedForReplay(commandLogEntry);

        assertThat(replayCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("1");
        assertThat(recordedCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("1");
    }

    @Test
    void reference_parameter_remapping_changes_replay_command_dto() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter("simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "4")));

        CommandDto replayCommandDto = replayableCommand(listener).commandDtoPossiblyRemappedForReplay(commandLogEntry);
        ParamDto replayParameter = ((ActionDto) replayCommandDto.getMember()).getParameters().getParameter().get(0);
        ParamDto recordedParameter = ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().get(0);

        assertThat(replayParameter.getReference().getType()).isEqualTo("simple.SimpleObject");
        assertThat(replayParameter.getReference().getId()).isEqualTo("4");
        assertThat(recordedParameter.getReference().getId()).isEqualTo("3");
    }

    @Test
    void missing_reference_parameter_remapping_leaves_replay_command_dto_unchanged() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter("simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        CommandDto replayCommandDto = replayableCommand(listener).commandDtoPossiblyRemappedForReplay(commandLogEntry);
        ParamDto replayParameter = ((ActionDto) replayCommandDto.getMember()).getParameters().getParameter().get(0);
        ParamDto recordedParameter = ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().get(0);

        assertThat(replayParameter.getReference().getId()).isEqualTo("3");
        assertThat(recordedParameter.getReference().getId()).isEqualTo("3");
    }

    @Test
    void non_reference_parameter_is_not_remapped_by_reference_flow() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter("simple.SimpleObject", "1", "simple.SimpleObject", "3");
        ParamDto parameter = ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().get(0);
        parameter.setType(ValueType.STRING);
        parameter.setReference(null);
        parameter.setString("unchanged");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        CommandDto replayCommandDto = replayableCommand(listener).commandDtoPossiblyRemappedForReplay(commandLogEntry);
        ParamDto replayParameter = ((ActionDto) replayCommandDto.getMember()).getParameters().getParameter().get(0);

        assertThat(replayParameter.getString()).isEqualTo("unchanged");
        verify(listener, never()).remap(
                any(), eq(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3")));
    }

    @Test
    void replay_execution_receives_remapped_command_dto() throws Exception {
        UUID interactionId = UUID.randomUUID();
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);

        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));

        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.callTransactional(any(Propagation.class), any(Callable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<Object> callable = invocation.getArgument(1);
                    return Try.call(callable);
                });

        CommandExecutorService commandExecutorService = mock(CommandExecutorService.class);
        when(commandExecutorService.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenReturn(Try.success(null));

        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2")));
        when(listener.remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "4")));
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, commandExecutorService, null, List.of(listener));

        new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        ArgumentCaptor<CommandDto> commandDtoCaptor = ArgumentCaptor.forClass(CommandDto.class);
        verify(commandExecutorService).executeCommand(
                eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), commandDtoCaptor.capture());
        CommandDto replayCommandDto = commandDtoCaptor.getValue();
        ParamDto replayParameter = ((ActionDto) replayCommandDto.getMember()).getParameters().getParameter().get(0);
        ParamDto recordedParameter = ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().get(0);

        assertThat(replayCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("2");
        assertThat(replayParameter.getReference().getId()).isEqualTo("4");
        assertThat(recordedCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("1");
        assertThat(recordedParameter.getReference().getId()).isEqualTo("3");
        verify(listener).remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1"));
        verify(listener).remap(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3"));
    }

    @Test
    void displays_recorded_result_inside_command_export_dto() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(
                new CommandDto(),
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "1"));

        AsciiDoc dto = replayableCommand(commandLogEntry).getDto();

        assertThat(dto).isNotNull();
        assertThat(dto.getAdoc())
                .contains("command:")
                .contains("result:")
                .contains("type: \"demoCustomer\"")
                .contains("id: \"1\"")
                .doesNotContain("returnedObject");
    }

    @Test
    void omits_result_from_command_export_dto_when_missing() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(new CommandDto(), null);

        AsciiDoc dto = replayableCommand(commandLogEntry).getDto();

        assertThat(dto).isNotNull();
        assertThat(dto.getAdoc())
                .contains("command:")
                .doesNotContain("result:")
                .doesNotContain("returnedObject");
    }

    @Test
    void displays_unresolved_recorded_result_as_bookmark_metadata_inside_command_export_dto() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(
                new CommandDto(),
                Bookmark.forLogicalTypeNameAndIdentifier("unresolvable.Customer", "123"));

        AsciiDoc dto = replayableCommand(commandLogEntry).getDto();

        assertThat(dto).isNotNull();
        assertThat(dto.getAdoc())
                .contains("command:")
                .contains("result:")
                .contains("type: \"unresolvable.Customer\"")
                .contains("id: \"123\"");
    }

    @Test
    void notifies_listener_when_recorded_and_actual_results_differ() {
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, actualResult);

        verify(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
    }

    @Test
    void notifies_listener_when_recorded_and_actual_results_are_equal() {
        Bookmark result = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(result);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, result);

        verify(listener).onReplayResultMapped(result, result, commandLogEntry);
    }

    @Test
    void notifies_listener_in_same_transaction_as_command_execution() throws Exception {
        UUID interactionId = UUID.randomUUID();
        CommandDto commandDto = new CommandDto();
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);

        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));

        AtomicInteger transactionSequence = new AtomicInteger();
        AtomicInteger currentTransaction = new AtomicInteger(-1);
        AtomicInteger commandExecutionTransaction = new AtomicInteger(-1);
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.callTransactional(any(Propagation.class), any(Callable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<Object> callable = invocation.getArgument(1);
                    int transactionId = transactionSequence.incrementAndGet();
                    currentTransaction.set(transactionId);
                    Try<Object> result = Try.call(callable);
                    currentTransaction.set(-1);
                    return result;
                });

        CommandExecutorService commandExecutorService = mock(CommandExecutorService.class);
        when(commandExecutorService.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenAnswer(invocation -> {
                    commandExecutionTransaction.set(currentTransaction.get());
                    return Try.success(actualResult);
                });

        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            assertThat(currentTransaction.get()).isEqualTo(commandExecutionTransaction.get());
            return null;
        }).when(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, commandExecutorService, null, List.of(listener));

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isSuccess()).isTrue();
        verify(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
    }

    @Test
    void listener_exception_causes_replay_failure() throws Exception {
        UUID interactionId = UUID.randomUUID();
        CommandDto commandDto = new CommandDto();
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);

        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));

        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.callTransactional(any(Propagation.class), any(Callable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<Object> callable = invocation.getArgument(1);
                    return Try.call(callable);
                });

        CommandExecutorService commandExecutorService = mock(CommandExecutorService.class);
        when(commandExecutorService.executeCommand(eq(InteractionContextPolicy.SWITCH_USER_AND_TIME), any(CommandDto.class)))
                .thenReturn(Try.success(actualResult));

        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        doThrow(new IllegalStateException("conflicting result mapping"))
                .when(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, commandExecutorService, null, List.of(listener));

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isFailure()).isTrue();
        verify(listener).onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
        verify(commandLogEntry).saveAnalysis("java.lang.IllegalStateException: conflicting result mapping");
    }

    @Test
    void does_not_notify_listener_when_recorded_result_is_missing() {
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(null);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResultMapped(commandLogEntry, actualResult);

        verify(listener, never()).onReplayResultMapped(any(), any(), any());
    }

    @Test
    void does_not_notify_listener_when_actual_result_is_missing() {
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

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

        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, null, null, List.of(listener));

        new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        verify(listener, never()).onReplayResultMapped(any(), any(), any());
    }

    private static ReplayableCommand replayableCommand(final CommandReplayMappingListener listener) {
        ReplayContext replayContext = new ReplayContext(null, null, null, null, null, null, List.of(listener));
        return new ReplayableCommand(UUID.randomUUID(), replayContext);
    }

    private static ReplayableCommand replayableCommand(final CommandLogEntry commandLogEntry) {
        UUID interactionId = UUID.randomUUID();
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of());
        return new ReplayableCommand(interactionId, replayContext);
    }

    private static CommandLogEntry commandLogEntryWithCommandDto(final CommandDto commandDto) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        return commandLogEntry;
    }

    private static CommandDto commandWithTargetAndReferenceParameter(
            final String targetType,
            final String targetId,
            final String parameterType,
            final String parameterId) {
        CommandDto commandDto = new CommandDto();
        commandDto.setMajorVersion("2");
        commandDto.setMinorVersion("0");
        commandDto.setInteractionId(UUID.randomUUID().toString());

        OidDto target = new OidDto();
        target.setType(targetType);
        target.setId(targetId);
        OidsDto targets = new OidsDto();
        targets.getOid().add(target);
        commandDto.setTargets(targets);

        OidDto reference = new OidDto();
        reference.setType(parameterType);
        reference.setId(parameterId);
        ParamDto parameter = new ParamDto();
        parameter.setName("simpleObject");
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(reference);
        ParamsDto parameters = new ParamsDto();
        parameters.getParameter().add(parameter);
        ActionDto action = new ActionDto();
        action.setLogicalMemberIdentifier("simple.SimpleObject#sameAs");
        action.setInteractionType(InteractionType.ACTION_INVOCATION);
        action.setParameters(parameters);
        commandDto.setMember(action);
        return commandDto;
    }

    private static CommandLogEntry commandLogEntryWithRecordedResult(final Bookmark recordedResult) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getResult()).thenReturn(recordedResult);
        return commandLogEntry;
    }

    private static CommandLogEntry commandLogEntryWithCommandDtoAndRecordedResult(
            final CommandDto commandDto,
            final Bookmark recordedResult) {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(commandDto);
        when(commandLogEntry.getResult()).thenReturn(recordedResult);
        return commandLogEntry;
    }
}
