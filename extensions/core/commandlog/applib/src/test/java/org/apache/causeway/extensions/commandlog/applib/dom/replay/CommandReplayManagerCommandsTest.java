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

class CommandReplayManagerCommandsTest {

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
    void pending_or_failed_omits_safe_action_without_result() {
        final var safeAction = safeActionEntry(ReplayState.PENDING, null);
        final var repository = repositoryReturningPendingOrFailed(List.of(safeAction));
        final var manager = manager(repository, safeActionSpecificationLoader());

        final var commands = manager.getPendingOrFailed();

        assertThat(commands).isEmpty();
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

    private static CommandReplayManager manager(
            final CommandLogEntryRepository repository,
            final SpecificationLoader specificationLoader) {
        final var replayContext = new ReplayContext(null, null, null, repository, null, null, List.of(), specificationLoader);
        return new CommandReplayManager(BASELINE, replayContext);
    }

    private static CommandLogEntryRepository repositoryReturningPendingOrFailed(final List<CommandLogEntry> entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(BASELINE)).thenReturn(entries);
        entries.forEach(entry -> when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry)));
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
}
