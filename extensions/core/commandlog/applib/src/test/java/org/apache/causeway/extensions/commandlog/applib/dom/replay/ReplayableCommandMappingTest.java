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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.springframework.transaction.annotation.Propagation;

import org.mockito.ArgumentCaptor;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.applib.services.command.CommandExecutorService.InteractionContextPolicy;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
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
    void command_log_replay_helpers_suppress_command_recording() {
        assertThat(CommandRecordingSuppressed.class).isAssignableFrom(CommandManagerReplay.class);
        assertThat(CommandRecordingSuppressed.class).isAssignableFrom(CommandManagerExport.class);
        assertThat(CommandRecordingSuppressed.class).isAssignableFrom(ReplayableCommand.class);
        assertThat(CommandRecordingSuppressed.class).isAssignableFrom(ReplayableCommandParticipant.class);
        assertThat(CommandRecordingSuppressed.class).isAssignableFrom(CommandLogEntry.class);
    }

    @Test
    void replay_or_retry_disablement_allows_only_pending_ok_or_failed_states() {
        for (ReplayState replayState : ReplayState.values()) {
            ReplayableCommand replayableCommand = replayableCommand(commandLogEntryWithReplayState(replayState));

            String disableReason = replayableCommand.disableReplayOrRetry();

            if (replayState == ReplayState.PENDING || replayState == ReplayState.OK || replayState == ReplayState.FAILED) {
                assertThat(disableReason).as(replayState.name()).isNull();
            } else {
                assertThat(disableReason).as(replayState.name())
                        .isEqualTo("Cannot replay or retry unless replay state is PENDING, OK, or FAILED");
            }
        }
    }

    @Test
    void replay_or_retry_action_delegates_disablement_to_replayable_command() {
        ReplayableCommand undefinedCommand = replayableCommand(commandLogEntryWithReplayState(ReplayState.UNDEFINED));
        ReplayableCommand pendingCommand = replayableCommand(commandLogEntryWithReplayState(ReplayState.PENDING));

        assertThat(new ReplayableCommand_replayOrRetry(undefinedCommand).disableAct())
                .isEqualTo("Cannot replay or retry unless replay state is PENDING, OK, or FAILED");
        assertThat(new ReplayableCommand_replayOrRetry(pendingCommand).disableAct()).isNull();
    }

    @Test
    void replay_or_retry_action_is_disabled_while_background_command_is_pending() {
        UUID interactionId = UUID.randomUUID();
        CommandLogEntry commandLogEntry = commandLogEntryWithReplayState(ReplayState.PENDING);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        when(commandLogEntryRepository.findBackgroundAndNotYetStarted()).thenReturn(List.of(mock(CommandLogEntry.class)));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of());
        ReplayableCommand pendingCommand = new ReplayableCommand(interactionId, replayContext);

        assertThat(new ReplayableCommand_replayOrRetry(pendingCommand).disableAct())
                .isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
    }

    @Test
    void direct_replay_or_retry_invocation_is_guarded_for_undefined_state() {
        UUID interactionId = UUID.randomUUID();
        CommandLogEntry commandLogEntry = commandLogEntryWithReplayState(ReplayState.UNDEFINED);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        TransactionService transactionService = mock(TransactionService.class);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, null, null, List.of());

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().orElse(null)).isNull();
        verify(transactionService, never()).callTransactional(any(Propagation.class), any(Callable.class));
    }

    @Test
    void direct_replay_or_retry_invocation_is_guarded_for_pending_background_work() {
        UUID interactionId = UUID.randomUUID();
        CommandLogEntry commandLogEntry = commandLogEntryWithReplayState(ReplayState.PENDING);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        when(commandLogEntryRepository.findBackgroundAndNotYetStarted()).thenReturn(List.of(mock(CommandLogEntry.class)));
        TransactionService transactionService = mock(TransactionService.class);
        ReplayContext replayContext = new ReplayContext(
                null, null, transactionService, commandLogEntryRepository, null, null, List.of());

        Try<ReplayableCommand> result = new ReplayableCommand(interactionId, replayContext).tryReplayOrRetry();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().orElse(null)).isNull();
        verify(transactionService, never()).callTransactional(any(Propagation.class), any(Callable.class));
    }

    @Test
    void previous_and_next_navigate_to_adjacent_commands_without_mutating_entries() {
        UUID previousInteractionId = UUID.randomUUID();
        UUID currentInteractionId = UUID.randomUUID();
        UUID nextInteractionId = UUID.randomUUID();
        Timestamp previousTimestamp = Timestamp.valueOf("2026-06-08 10:00:00");
        Timestamp currentTimestamp = Timestamp.valueOf("2026-06-08 10:00:01");
        Timestamp nextTimestamp = Timestamp.valueOf("2026-06-08 10:00:02");
        CommandLogEntry previousEntry = commandLogEntry(previousInteractionId, previousTimestamp);
        CommandLogEntry currentEntry = commandLogEntry(currentInteractionId, currentTimestamp);
        CommandLogEntry nextEntry = commandLogEntry(nextInteractionId, nextTimestamp);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(currentInteractionId)).thenReturn(Optional.of(currentEntry));
        when(commandLogEntryRepository.findByInteractionId(previousInteractionId)).thenReturn(Optional.of(previousEntry));
        when(commandLogEntryRepository.findByInteractionId(nextInteractionId)).thenReturn(Optional.of(nextEntry));
        when(commandLogEntryRepository.findForegroundBeforeTimestamp(currentTimestamp, null)).thenReturn(List.of(previousEntry));
        when(commandLogEntryRepository.findForegroundSinceTimestamp(currentTimestamp, null)).thenReturn(List.of(currentEntry, nextEntry));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of());
        ReplayableCommand replayableCommand = new ReplayableCommand(currentInteractionId, replayContext);

        ReplayableCommand previous = replayableCommand.previous();
        ReplayableCommand next = replayableCommand.next();

        assertThat(previous.interactionId()).isEqualTo(previousInteractionId);
        assertThat(previous.replayContext()).isSameAs(replayContext);
        assertThat(next.interactionId()).isEqualTo(nextInteractionId);
        assertThat(next.replayContext()).isSameAs(replayContext);
        assertThat(replayableCommand.disablePrevious()).isNull();
        assertThat(replayableCommand.disableNext()).isNull();
        verify(currentEntry, never()).setReplayState(any());
        verify(previousEntry, never()).setReplayState(any());
        verify(nextEntry, never()).setReplayState(any());
    }

    @Test
    void previous_and_next_skip_ineligible_safe_action_entries() {
        UUID previousInteractionId = UUID.randomUUID();
        UUID omittedInteractionId = UUID.randomUUID();
        UUID currentInteractionId = UUID.randomUUID();
        UUID nextInteractionId = UUID.randomUUID();
        Timestamp previousTimestamp = Timestamp.valueOf("2026-06-08 10:00:00");
        Timestamp omittedTimestamp = Timestamp.valueOf("2026-06-08 10:00:00.500");
        Timestamp currentTimestamp = Timestamp.valueOf("2026-06-08 10:00:01");
        Timestamp nextTimestamp = Timestamp.valueOf("2026-06-08 10:00:02");
        CommandLogEntry previousEntry = commandLogEntry(previousInteractionId, previousTimestamp);
        CommandLogEntry omittedSafeEntry = safeActionCommandLogEntry(omittedInteractionId, omittedTimestamp, null);
        CommandLogEntry currentEntry = commandLogEntry(currentInteractionId, currentTimestamp);
        CommandLogEntry nextEntry = commandLogEntry(nextInteractionId, nextTimestamp);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(currentInteractionId)).thenReturn(Optional.of(currentEntry));
        when(commandLogEntryRepository.findByInteractionId(previousInteractionId)).thenReturn(Optional.of(previousEntry));
        when(commandLogEntryRepository.findByInteractionId(nextInteractionId)).thenReturn(Optional.of(nextEntry));
        when(commandLogEntryRepository.findForegroundBeforeTimestamp(currentTimestamp, null)).thenReturn(List.of(omittedSafeEntry, previousEntry));
        when(commandLogEntryRepository.findForegroundSinceTimestamp(currentTimestamp, null)).thenReturn(List.of(currentEntry, nextEntry));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of(), safeActionSpecificationLoader());
        ReplayableCommand replayableCommand = new ReplayableCommand(currentInteractionId, replayContext);

        ReplayableCommand previous = replayableCommand.previous();
        ReplayableCommand next = replayableCommand.next();

        assertThat(previous.interactionId()).isEqualTo(previousInteractionId);
        assertThat(next.interactionId()).isEqualTo(nextInteractionId);
        assertThat(replayableCommand.disablePrevious()).isNull();
        assertThat(replayableCommand.disableNext()).isNull();
    }

    @Test
    void navigation_actions_disable_at_command_boundaries() {
        UUID currentInteractionId = UUID.randomUUID();
        Timestamp currentTimestamp = Timestamp.valueOf("2026-06-08 10:00:01");
        CommandLogEntry currentEntry = commandLogEntry(currentInteractionId, currentTimestamp);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(currentInteractionId)).thenReturn(Optional.of(currentEntry));
        when(commandLogEntryRepository.findForegroundBeforeTimestamp(currentTimestamp, null)).thenReturn(List.of());
        when(commandLogEntryRepository.findForegroundSinceTimestamp(currentTimestamp, null)).thenReturn(List.of(currentEntry));
        ReplayContext replayContext = new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of());
        ReplayableCommand replayableCommand = new ReplayableCommand(currentInteractionId, replayContext);

        assertThat(replayableCommand.disablePrevious()).isEqualTo("No previous command");
        assertThat(replayableCommand.disableNext()).isEqualTo("No next command");
        assertThat(replayableCommand.previous()).isSameAs(replayableCommand);
        assertThat(replayableCommand.next()).isSameAs(replayableCommand);
    }

    @Test
    void previous_and_next_mixins_delegate_to_replayable_command_navigation() {
        UUID previousInteractionId = UUID.randomUUID();
        UUID currentInteractionId = UUID.randomUUID();
        UUID nextInteractionId = UUID.randomUUID();
        Timestamp previousTimestamp = Timestamp.valueOf("2026-06-08 10:00:00");
        Timestamp currentTimestamp = Timestamp.valueOf("2026-06-08 10:00:01");
        Timestamp nextTimestamp = Timestamp.valueOf("2026-06-08 10:00:02");
        CommandLogEntry previousEntry = commandLogEntry(previousInteractionId, previousTimestamp);
        CommandLogEntry currentEntry = commandLogEntry(currentInteractionId, currentTimestamp);
        CommandLogEntry nextEntry = commandLogEntry(nextInteractionId, nextTimestamp);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(currentInteractionId)).thenReturn(Optional.of(currentEntry));
        when(commandLogEntryRepository.findForegroundBeforeTimestamp(currentTimestamp, null)).thenReturn(List.of(previousEntry));
        when(commandLogEntryRepository.findForegroundSinceTimestamp(currentTimestamp, null)).thenReturn(List.of(currentEntry, nextEntry));
        ReplayableCommand replayableCommand = new ReplayableCommand(currentInteractionId, new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of()));

        assertThat(new ReplayableCommand_previous(replayableCommand).disableAct()).isNull();
        assertThat(new ReplayableCommand_previous(replayableCommand).act().interactionId()).isEqualTo(previousInteractionId);
        assertThat(new ReplayableCommand_next(replayableCommand).disableAct()).isNull();
        assertThat(new ReplayableCommand_next(replayableCommand).act().interactionId()).isEqualTo(nextInteractionId);
    }

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
    void undefined_replay_state_populates_recorded_target_and_argument_bookmarks_and_objects() {
        assertRecordedTargetAndArgumentAreAvailable(ReplayState.UNDEFINED);
    }

    @Test
    void exported_replay_state_populates_recorded_target_and_argument_bookmarks_and_objects() {
        assertRecordedTargetAndArgumentAreAvailable(ReplayState.EXPORTED);
    }

    @Test
    void domain_service_target_is_available_in_every_replay_state() {
        for (ReplayState replayState : ReplayState.values()) {
            UUID interactionId = UUID.randomUUID();
            DomainServiceMenu service = new DomainServiceMenu();
            Bookmark serviceBookmark = Bookmark.forLogicalTypeNameAndIdentifier("demo.DomainServiceMenu", "1");
            CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(commandWithTargetOnly(
                    "demo.DomainServiceMenu", "1"));
            when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
            when(commandLogEntry.getReplayState()).thenReturn(replayState);
            BookmarkService bookmarkService = mock(BookmarkService.class);
            when(bookmarkService.lookup(serviceBookmark)).thenReturn(Optional.of(service));
            ReplayableCommand replayableCommand = replayableCommand(interactionId, commandLogEntry);
            replayableCommand.bookmarkService = bookmarkService;

            ReplayableCommandParticipant targetParticipant = replayableCommand.getParticipants().stream()
                    .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.TARGET)
                    .findFirst()
                    .orElseThrow();

            assertThat(targetParticipant.getActualBookmark()).as(replayState.name()).isEqualTo(serviceBookmark);
            assertThat(targetParticipant.getTarget()).as(replayState.name()).isSameAs(service);
        }
    }

    @Test
    void participant_mementos_are_readable_and_do_not_include_bookmarks() {
        UUID interactionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        ReplayableCommandParticipant targetParticipant = new ReplayableCommandParticipant(
                interactionId,
                ReplayableCommandParticipant.Role.TARGET,
                null,
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "1"),
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "2"));
        ReplayableCommandParticipant parameterParticipant = new ReplayableCommandParticipant(
                interactionId,
                ReplayableCommandParticipant.Role.PARAMETER,
                "customer",
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "1"),
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "2"));
        ReplayableCommandParticipant resultParticipant = new ReplayableCommandParticipant(
                interactionId,
                ReplayableCommandParticipant.Role.RESULT,
                null,
                Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1"),
                Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2"));

        assertThat(targetParticipant.viewModelMemento())
                .isEqualTo("11111111-1111-1111-1111-111111111111--target")
                .doesNotContain("demoCustomer");
        assertThat(parameterParticipant.viewModelMemento())
                .isEqualTo("11111111-1111-1111-1111-111111111111--parameter--customer")
                .doesNotContain("demoCustomer");
        assertThat(resultParticipant.viewModelMemento())
                .isEqualTo("11111111-1111-1111-1111-111111111111--result")
                .doesNotContain("demoInvoice");
    }

    @Test
    void participant_rehydrates_derived_bookmarks_from_readable_memento() {
        UUID interactionId = UUID.randomUUID();
        Bookmark recordedTarget = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1");
        Bookmark actualTarget = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "2");
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.PENDING);
        CommandReplayMappingListener listener = mock(CommandReplayMappingListener.class);
        when(listener.lookup(commandLogEntry, recordedTarget)).thenReturn(Optional.of(actualTarget));
        ReplayContext replayContext = replayContext(interactionId, commandLogEntry, listener);

        ReplayableCommandParticipant participant = new ReplayableCommandParticipant(
                interactionId + "--target", mock(BookmarkService.class), replayContext);

        assertThat(participant.getRole()).isEqualTo(ReplayableCommandParticipant.Role.TARGET);
        assertThat(participant.getRecordedBookmark()).isEqualTo(recordedTarget);
        assertThat(participant.getActualBookmark()).isEqualTo(actualTarget);
        assertThat(participant.getReplayableCommand().viewModelMemento()).isEqualTo(interactionId.toString());
    }

    @Test
    void parameter_participant_memento_preserves_parameter_name_containing_delimiter() {
        UUID interactionId = UUID.randomUUID();
        String parameterName = "customer--primary";
        Bookmark recordedParameter = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3");
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        ((ActionDto) recordedCommandDto.getMember()).getParameters().getParameter().get(0).setName(parameterName);
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.OK);
        ReplayContext replayContext = replayContext(interactionId, commandLogEntry);

        ReplayableCommandParticipant participant = new ReplayableCommandParticipant(
                interactionId + "--parameter--" + parameterName, mock(BookmarkService.class), replayContext);

        assertThat(participant.getParameterName()).isEqualTo(parameterName);
        assertThat(participant.getRecordedBookmark()).isEqualTo(recordedParameter);
        assertThat(participant.getActualBookmark()).isEqualTo(recordedParameter);
    }

    @Test
    void participant_view_model_constructor_uses_string_memento_first_and_injected_services_after() {
        Constructor<?>[] constructors = ReplayableCommandParticipant.class.getConstructors();

        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterTypes()).containsExactly(
                String.class,
                BookmarkService.class,
                ReplayContext.class);
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
    void participant_cosmetics_expose_title_parent_metadata_and_role_specific_visibility() throws Exception {
        UUID interactionId = UUID.randomUUID();
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.OK);
        ReplayableCommand replayableCommand = replayableCommand(interactionId, commandLogEntry);

        ReplayableCommandParticipant parameterParticipant = replayableCommand.getParticipants().stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.PARAMETER)
                .findFirst()
                .orElseThrow();
        ReplayableCommandParticipant targetParticipant = replayableCommand.getParticipants().stream()
                .filter(row -> row.getRole() == ReplayableCommandParticipant.Role.TARGET)
                .findFirst()
                .orElseThrow();
        ReplayableCommandParticipant resultParticipant = new ReplayableCommandParticipant(
                interactionId,
                ReplayableCommandParticipant.Role.RESULT,
                null,
                Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1"),
                Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2"));

        assertThat(parameterParticipant.title())
                .contains("Replay participant", "PARAMETER", "simpleObject", "simple.SimpleObject:3");
        assertThat(parameterParticipant.getReplayableCommand().viewModelMemento()).isEqualTo(interactionId.toString());
        assertThat(parameterParticipant.hideTarget()).isTrue();
        assertThat(parameterParticipant.hideArgument()).isFalse();
        assertThat(parameterParticipant.hideResult()).isTrue();
        assertThat(targetParticipant.hideTarget()).isFalse();
        assertThat(resultParticipant.hideResult()).isFalse();
        assertThat(ReplayableCommandParticipant.class.getMethod("getOwningInteractionId")
                .getAnnotation(PropertyLayout.class).hidden()).isEqualTo(Where.OBJECT_FORMS);

        String layoutXml = Files.readString(replayableCommandParticipantLayoutPath());
        assertThat(layoutXml).contains(
                "<bs:col span=\"4\">",
                "<cpt:fieldSet name=\"General\" id=\"general\">",
                "<cpt:property id=\"replayableCommand\"/>",
                "<cpt:property id=\"role\"/>",
                "<cpt:property id=\"parameterName\"/>",
                "<cpt:fieldSet name=\"Metadata\" id=\"metadata\">",
                "<cpt:property id=\"logicalTypeName\"/>",
                "<cpt:fieldSet name=\"Recorded\" id=\"recorded\">",
                "<cpt:property id=\"recordedBookmark\"/>",
                "<cpt:property id=\"target\"/>",
                "<cpt:property id=\"argument\"/>",
                "<cpt:fieldSet name=\"Actual\" id=\"actual\">",
                "<cpt:property id=\"actualBookmark\"/>",
                "<cpt:property id=\"result\"/>");
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
    void replay_failure_preserves_recorded_command_dto_after_input_remapping() throws Exception {
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
                .thenReturn(Try.failure(new RuntimeException("replay failed")));

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
    }

    @Test
    void has_result_reports_true_when_command_log_entry_stores_result() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(
                new CommandDto(),
                Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "1"));

        assertThat(replayableCommand(commandLogEntry).getHasResult()).isTrue();
    }

    @Test
    void has_result_reports_false_when_command_log_entry_has_no_result() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(new CommandDto(), null);

        assertThat(replayableCommand(commandLogEntry).getHasResult()).isFalse();
    }

    @Test
    void has_result_does_not_require_result_bookmark_to_resolve_locally() {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDtoAndRecordedResult(
                new CommandDto(),
                Bookmark.forLogicalTypeNameAndIdentifier("unresolvable.Customer", "123"));

        assertThat(replayableCommand(commandLogEntry).getHasResult()).isTrue();
    }

    @Test
    void has_result_property_is_ordered_before_exportability_in_tables() throws Exception {
        PropertyLayout hasResultLayout = ReplayableCommand.class.getMethod("getHasResult")
                .getAnnotation(PropertyLayout.class);
        PropertyLayout exportableLayout = ReplayableCommand.class.getMethod("isKnownParticipants")
                .getAnnotation(PropertyLayout.class);

        assertThat(hasResultLayout.sequence()).isEqualTo("4.1");
        assertThat(exportableLayout.sequence()).isEqualTo("4.2");
        assertThat(exportableLayout.hidden()).isEqualTo(Where.OBJECT_FORMS);
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

    private static void assertRecordedTargetAndArgumentAreAvailable(final ReplayState replayState) {
        UUID interactionId = UUID.randomUUID();
        Object targetObject = new Object();
        Object parameterObject = new Object();
        Bookmark targetBookmark = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "1");
        Bookmark parameterBookmark = Bookmark.forLogicalTypeNameAndIdentifier("simple.SimpleObject", "3");
        CommandDto recordedCommandDto = commandWithTargetAndReferenceParameter(
                "simple.SimpleObject", "1", "simple.SimpleObject", "3");
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(recordedCommandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getReplayState()).thenReturn(replayState);
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
        assertThat(targetParticipant.getActualBookmark()).isEqualTo(targetBookmark);
        assertThat(targetParticipant.getTarget()).isSameAs(targetObject);
        assertThat(parameterParticipant.getActualBookmark()).isEqualTo(parameterBookmark);
        assertThat(parameterParticipant.getArgument()).isSameAs(parameterObject);
        verify(commandLogEntry, never()).setReplayState(any());
    }

    @DomainService
    static class DomainServiceMenu {
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
        return new ReplayableCommand(interactionId, replayContext(interactionId, commandLogEntry, listeners));
    }

    private static ReplayContext replayContext(
            final UUID interactionId,
            final CommandLogEntry commandLogEntry,
            final CommandReplayMappingListener... listeners) {
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.findByInteractionId(interactionId)).thenReturn(Optional.of(commandLogEntry));
        return new ReplayContext(
                null, null, null, commandLogEntryRepository, null, null, List.of(listeners));
    }

    private static CommandLogEntry commandLogEntry(
            final UUID interactionId,
            final Timestamp timestamp) {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(commandWithTargetOnly("simple.SimpleObject", "1"));
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getTimestamp()).thenReturn(timestamp);
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.UNDEFINED);
        return commandLogEntry;
    }

    private static CommandLogEntry safeActionCommandLogEntry(
            final UUID interactionId,
            final Timestamp timestamp,
            final Bookmark result) {
        CommandDto commandDto = commandWithTargetOnly("simple.SimpleObject", "1");
        ActionDto action = new ActionDto();
        action.setLogicalMemberIdentifier("simple.SimpleObject#find");
        action.setInteractionType(InteractionType.ACTION_INVOCATION);
        commandDto.setMember(action);
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(commandDto);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        when(commandLogEntry.getTimestamp()).thenReturn(timestamp);
        when(commandLogEntry.getLogicalMemberIdentifier()).thenReturn(action.getLogicalMemberIdentifier());
        when(commandLogEntry.getReplayState()).thenReturn(ReplayState.UNDEFINED);
        when(commandLogEntry.getResult()).thenReturn(result);
        return commandLogEntry;
    }

    private static CommandLogEntry commandLogEntryWithCommandDto(final CommandDto commandDto) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        return commandLogEntry;
    }

    private static SpecificationLoader safeActionSpecificationLoader() {
        final var specificationLoader = mock(SpecificationLoader.class);
        final var objectSpecification = mock(ObjectSpecification.class);
        final var objectAction = mock(ObjectAction.class);
        doReturn(SimpleObject.class).when(objectSpecification).getCorrespondingClass();
        when(objectAction.getSemantics()).thenReturn(SemanticsOf.SAFE);
        when(specificationLoader.specForLogicalTypeNameElseFail("simple.SimpleObject")).thenReturn(objectSpecification);
        when(specificationLoader.loadFeature(any(Identifier.class))).thenReturn(Optional.of(objectAction));
        return specificationLoader;
    }

    private static class SimpleObject {
    }

    private static CommandLogEntry commandLogEntryWithReplayState(final ReplayState replayState) {
        CommandLogEntry commandLogEntry = commandLogEntryWithCommandDto(commandWithTargetOnly("simple.SimpleObject", "1"));
        when(commandLogEntry.getReplayState()).thenReturn(replayState);
        return commandLogEntry;
    }

    private static Path replayableCommandParticipantLayoutPath() {
        Path moduleRelativePath = Path.of("src/main/java/org/apache/causeway/extensions/commandlog/applib/dom/replay/ReplayableCommandParticipant.layout.fallback.xml");
        if (Files.exists(moduleRelativePath)) {
            return moduleRelativePath;
        }
        return Path.of("extensions/core/commandlog/applib").resolve(moduleRelativePath);
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
