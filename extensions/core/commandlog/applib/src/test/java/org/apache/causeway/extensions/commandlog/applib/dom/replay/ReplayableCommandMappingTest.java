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
import org.apache.causeway.applib.services.bookmark.BookmarkService;
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
        when(listener.lookup(
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
        when(listener.lookup(
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
        verify(listener, never()).lookup(
                any(), eq(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3")));
    }

    @Test
    void target_remapping_participant_uses_lookup_and_keeps_unresolved_bookmarks_visible() {
        UUID interactionId = UUID.randomUUID();
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.lookup(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2")));

        List<ReplayableCommandParticipant> remappings = replayableCommand(interactionId, commandLogEntry, listener).getParticipants();

        assertThat(remappings).hasSize(2);
        ReplayableCommandParticipant participant = remappings.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.TARGET)
                .findFirst()
                .orElseThrow();
        assertThat(participant.getOwningInteractionId()).isEqualTo(interactionId);
        assertThat(participant.getRole()).isEqualTo(ReplayableCommandParticipant.Role.TARGET);
        assertThat(participant.getParameterName()).isNull();
        assertThat(participant.getRecordedBookmark()).isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1"));
        assertThat(participant.getActualBookmark()).isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2"));
        assertThat(participant.getTarget()).isNull();
    }

    @Test
    void successful_replay_populates_unchanged_actual_bookmarks_and_objects() {
        UUID interactionId = UUID.randomUUID();
        Object targetObject = new Object();
        Object parameterObject = new Object();
        Bookmark targetBookmark = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1");
        Bookmark parameterBookmark = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3");
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.OK);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        when(bookmarkService.lookup(targetBookmark)).thenReturn(Optional.of(targetObject));
        when(bookmarkService.lookup(parameterBookmark)).thenReturn(Optional.of(parameterObject));
        ReplayableCommand replayableCommand = replayableCommand(interactionId, commandLogEntry);
        replayableCommand.bookmarkService = bookmarkService;

        List<ReplayableCommandParticipant> participants = replayableCommand.getParticipants();

        ReplayableCommandParticipant targetParticipant = participants.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.TARGET)
                .findFirst()
                .orElseThrow();
        ReplayableCommandParticipant parameterParticipant = participants.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.PARAMETER)
                .findFirst()
                .orElseThrow();
        assertThat(targetParticipant.getRecordedBookmark()).isEqualTo(targetBookmark);
        assertThat(targetParticipant.getActualBookmark()).isEqualTo(targetBookmark);
        assertThat(targetParticipant.getTarget()).isSameAs(targetObject);
        assertThat(parameterParticipant.getRecordedBookmark()).isEqualTo(parameterBookmark);
        assertThat(parameterParticipant.getActualBookmark()).isEqualTo(parameterBookmark);
        assertThat(parameterParticipant.getArgument()).isSameAs(parameterObject);
    }

    @Test
    void pending_replay_leaves_unmapped_target_and_parameter_actual_bookmarks_empty() {
        UUID interactionId = UUID.randomUUID();
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);

        List<ReplayableCommandParticipant> participants = replayableCommand(interactionId, commandLogEntry).getParticipants();

        ReplayableCommandParticipant targetParticipant = participants.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.TARGET)
                .findFirst()
                .orElseThrow();
        ReplayableCommandParticipant parameterParticipant = participants.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.PARAMETER)
                .findFirst()
                .orElseThrow();
        assertThat(targetParticipant.getActualBookmark()).isNull();
        assertThat(parameterParticipant.getActualBookmark()).isNull();
    }

    @Test
    void participant_resolves_actual_object_best_effort() {
        Object actualObject = new Object();
        ReplayableCommandParticipant participant = new ReplayableCommandParticipant(
                UUID.randomUUID(),
                ReplayableCommandParticipant.Role.TARGET,
                null,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1"),
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2"));
        BookmarkService bookmarkService = mock(BookmarkService.class);
        when(bookmarkService.lookup(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2")))
                .thenReturn(Optional.of(actualObject));
        participant.bookmarkService = bookmarkService;

        assertThat(participant.getTarget()).isSameAs(actualObject);
        assertThat(participant.getArgument()).isNull();
        assertThat(participant.getResult()).isNull();
    }

    @Test
    void reference_parameter_participant_uses_parameter_name_and_omits_non_reference_parameters() {
        UUID interactionId = UUID.randomUUID();
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        ParamDto stringParameter = new ParamDto();
        stringParameter.setName("description");
        stringParameter.setType(ValueType.STRING);
        stringParameter.setString("unchanged");
        ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().add(stringParameter);
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.lookup(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "4")));

        List<ReplayableCommandParticipant> remappings = replayableCommand(interactionId, commandLogEntry, listener).getParticipants();

        assertThat(remappings).hasSize(2);
        ReplayableCommandParticipant participant = remappings.stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.PARAMETER)
                .findFirst()
                .orElseThrow();
        assertThat(participant.getRole()).isEqualTo(ReplayableCommandParticipant.Role.PARAMETER);
        assertThat(participant.getParameterName()).isEqualTo("simpleObject");
        assertThat(participant.getRecordedBookmark()).isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3"));
        assertThat(participant.getActualBookmark()).isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "4"));
        verify(listener, never()).lookup(
                any(), eq(Bookmark.forLogicalTypeNameAndIdentifier("description", "unchanged")));
    }

    @Test
    void result_participant_is_only_visible_after_successful_replay() {
        UUID interactionId = UUID.randomUUID();
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(new CommandDto(), recordedResult);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.OK);
        Object actualResultObject = new Object();
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.lookup(commandLogEntry, recordedResult)).thenReturn(Optional.of(actualResult));
        BookmarkService bookmarkService = mock(BookmarkService.class);
        when(bookmarkService.lookup(actualResult)).thenReturn(Optional.of(actualResultObject));
        ReplayableCommand replayableCommand = replayableCommand(interactionId, commandLogEntry, listener);
        replayableCommand.bookmarkService = bookmarkService;

        List<ReplayableCommandParticipant> remappings = replayableCommand.getParticipants();

        assertThat(remappings).hasSize(1);
        ReplayableCommandParticipant participant = remappings.get(0);
        assertThat(participant.getRole()).isEqualTo(ReplayableCommandParticipant.Role.RESULT);
        assertThat(participant.getRecordedBookmark()).isEqualTo(recordedResult);
        assertThat(participant.getActualBookmark()).isEqualTo(actualResult);
        assertThat(participant.getResult()).isSameAs(actualResultObject);

        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);
        List<ReplayableCommandParticipant> pendingParticipants = replayableCommand(interactionId, commandLogEntry, listener).getParticipants();
        assertThat(pendingParticipants).hasSize(1);
        assertThat(pendingParticipants.get(0).getRole()).isEqualTo(ReplayableCommandParticipant.Role.RESULT);
        assertThat(pendingParticipants.get(0).getRecordedBookmark()).isEqualTo(recordedResult);
        assertThat(pendingParticipants.get(0).getActualBookmark()).isNull();
    }

    @Test
    void participant_derivation_tolerates_listener_lookup_failure() {
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        doThrow(new IllegalStateException("lookup failed"))
                .when(listener).lookup(commandLogEntry, Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1"));

        List<ReplayableCommandParticipant> participants = replayableCommand(UUID.randomUUID(), commandLogEntry, listener).getParticipants();

        assertThat(participants).hasSize(2);
        assertThat(participants).allSatisfy(participant -> assertThat(participant.getActualBookmark()).isNull());
        assertThat(recordedCommandDto.getTargets().getOid().get(0).getId()).isEqualTo("1");
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
        when(listener.lookup(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1")))
                .thenReturn(Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2")));
        when(listener.lookup(
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
        verify(listener).lookup(
                commandLogEntry,
                Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1"));
        verify(listener).lookup(
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

        replayableCommand(listener).notifyReplayResult(commandLogEntry, actualResult);

        verify(listener).onReplayResult(recordedResult, actualResult, commandLogEntry);
    }

    @Test
    void notifies_listener_when_recorded_and_actual_results_are_equal() {
        Bookmark result = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(result);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResult(commandLogEntry, result);

        verify(listener).onReplayResult(result, result, commandLogEntry);
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
        }).when(listener).onReplayResult(recordedResult, actualResult, commandLogEntry);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, commandExecutorService, null, List.of(listener));

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isSuccess()).isTrue();
        verify(listener).onReplayResult(recordedResult, actualResult, commandLogEntry);
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
                .when(listener).onReplayResult(recordedResult, actualResult, commandLogEntry);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, commandExecutorService, null, List.of(listener));

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isFailure()).isTrue();
        verify(listener).onReplayResult(recordedResult, actualResult, commandLogEntry);
        verify(commandLogEntry).saveAnalysis("java.lang.IllegalStateException: conflicting result mapping");
    }

    @Test
    void does_not_notify_listener_when_recorded_result_is_missing() {
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(null);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResult(commandLogEntry, actualResult);

        verify(listener, never()).onReplayResult(any(), any(), any());
    }

    @Test
    void does_not_notify_listener_when_actual_result_is_missing() {
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        CommandLogEntry commandLogEntry = commandLogEntryWithRecordedResult(recordedResult);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);

        replayableCommand(listener).notifyReplayResult(commandLogEntry, null);

        verify(listener, never()).onReplayResult(any(), any(), any());
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

        verify(listener, never()).onReplayResult(any(), any(), any());
    }

    private static ReplayableCommand replayableCommand(final CommandReplayMappingListener listener) {
        ReplayContext replayContext = new ReplayContext(null, null, null, null, null, null, List.of(listener));
        return new ReplayableCommand(UUID.randomUUID(), replayContext);
    }

    private static ReplayableCommand replayableCommand(final CommandLogEntry commandLogEntry) {
        return replayableCommand(UUID.randomUUID(), commandLogEntry);
    }

    private static ReplayableCommand replayableCommand(
            final UUID interactionId,
            final CommandLogEntry commandLogEntry,
            final CommandReplayMappingListener... listeners) {
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of(listeners));
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
        CommandDto commandDto = commandWithTargetOnly(targetType, targetId);
        ParamsDto parameters = new ParamsDto();
        parameters.getParameter().add(referenceParameter("simpleObject", parameterType, parameterId));
        ActionDto action = new ActionDto();
        action.setLogicalMemberIdentifier("simple.SimpleObject#sameAs");
        action.setInteractionType(InteractionType.ACTION_INVOCATION);
        action.setParameters(parameters);
        commandDto.setMember(action);
        return commandDto;
    }

    private static CommandDto commandWithTargetOnly(
            final String targetType,
            final String targetId) {
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
        return commandDto;
    }

    private static ParamDto referenceParameter(
            final String parameterName,
            final String parameterType,
            final String parameterId) {
        OidDto reference = new OidDto();
        reference.setType(parameterType);
        reference.setId(parameterId);
        ParamDto parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(reference);
        return parameter;
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
