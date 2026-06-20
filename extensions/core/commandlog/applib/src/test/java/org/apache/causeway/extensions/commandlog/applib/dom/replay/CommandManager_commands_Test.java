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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.causeway.core.runtimeservices.scratchpad.ScratchpadDefault;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.InteractionType;

class CommandManager_commands_Test {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Bookmark RESULT = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void commands_collection_includes_undefined_replay_states_since_baseline() {
        final var undefined = entry(ReplayState.UNDEFINED);
        final var exported = entry(ReplayState.UNDEFINED);
        final var repository = repositoryReturning(List.of(undefined, exported), List.of());
        final var replayContext = ReplayContext.builder().commandLogEntryRepository(repository).specificationLoader(null).build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getCommandsForExport();

        assertThat(interactionIds(commands))
                .containsExactly(undefined.getInteractionId(), exported.getInteractionId());
    }

    @Test @Disabled// TODO: reinstate or remove
    void commands_collection_omits_excluded_and_replay_execution_states_since_baseline() {
        final var undefined = entry(ReplayState.UNDEFINED);
        final var undefined2 = entry(ReplayState.UNDEFINED);
        final var excluded = entry(ReplayState.EXCLUDED);
        final var pending = entry(ReplayState.PENDING);
        final var ok = entry(ReplayState.OK);
        final var failed = entry(ReplayState.FAILED);

        final var repository = repositoryReturning(List.of(undefined, undefined2, excluded, pending, ok, failed), List.of());

        final var replayContext = ReplayContext.builder()
                .commandLogEntryRepository(repository)
                .scratchpad(new ScratchpadDefault())
                .build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getCommandsForExport();

        assertThat(interactionIds(commands))
                .containsExactly(undefined.getInteractionId(), undefined2.getInteractionId(), ok.getInteractionId());
    }

    @Test @Disabled // TO reinstate or delete
    void excluded_commands_collection_includes_only_excluded_replay_states_since_baseline() {
        final var undefined = entry(ReplayState.UNDEFINED);
        final var exported = entry(ReplayState.UNDEFINED);
        final var excluded = entry(ReplayState.EXCLUDED);
        final var pending = entry(ReplayState.PENDING);
        final var repository = repositoryReturning(List.of(undefined, exported, excluded, pending), List.of());
        final var replayContext = ReplayContext.builder().commandLogEntryRepository(repository).specificationLoader(null).build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getExcludedCommands();

        assertThat(interactionIds(commands))
                .containsExactly(excluded.getInteractionId());
    }

    @Test
    void commands_collection_includes_safe_action_with_single_result() {
        final var safeAction = safeActionEntry(ReplayState.UNDEFINED, RESULT);
        final var repository = repositoryReturning(List.of(safeAction), List.of());
        final SpecificationLoader specificationLoader = safeActionSpecificationLoader();
        final var replayContext = ReplayContext.builder().commandLogEntryRepository(repository).specificationLoader(specificationLoader).build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getCommandsForExport();

        assertThat(interactionIds(commands))
                .containsExactly(safeAction.getInteractionId());
    }

    @Test
    void commands_collection_omits_safe_action_without_result_but_entry_remains_available_from_repository() {
        final var safeAction = safeActionEntry(ReplayState.UNDEFINED, null);
        final var repository = repositoryReturning(List.of(safeAction), List.of());
        final SpecificationLoader specificationLoader = safeActionSpecificationLoader();
        final var replayContext = ReplayContext.builder().commandLogEntryRepository(repository).specificationLoader(specificationLoader).build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getCommandsForExport();

        assertThat(commands).isEmpty();
        assertThat(repository.findForegroundSinceTimestamp(BASELINE, 50)).containsExactly(safeAction);
    }

    @Test @Disabled// TODO: reinstate or remove
    void previous_page_uses_unified_unfiltered_query() {
        final var excluded = entry(ReplayState.EXCLUDED);
        final var exported = entry(ReplayState.UNDEFINED);
        final var repository = repositoryReturning(List.of(), List.of(exported, excluded));
        final var replayContext = ReplayContext.builder().commandLogEntryRepository(repository).specificationLoader(null).build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);

        final var commands = manager.getCommandsForExport();

        assertThat(interactionIds(commands))
                .containsExactly(exported.getInteractionId(), excluded.getInteractionId());
    }

    private static CommandLogEntryRepository repositoryReturning(
            final List<CommandLogEntry> next,
            final List<CommandLogEntry> previous) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(next);
        when(repository.findForegroundBeforeTimestamp(BASELINE, 50)).thenReturn(previous);
        java.util.stream.Stream.concat(next.stream(), previous.stream())
                .forEach(entry -> when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry)));
        return repository;
    }

    private static CommandLogEntry entry(final ReplayState replayState) {
        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getReplayState()).thenReturn(replayState);
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

    private static List<UUID> interactionIds(final List<ReplayableCommand> commands) {
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toList());
    }
}
