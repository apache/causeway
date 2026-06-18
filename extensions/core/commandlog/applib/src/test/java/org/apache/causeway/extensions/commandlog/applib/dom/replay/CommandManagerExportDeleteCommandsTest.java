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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;

class CommandManagerExportDeleteCommandsTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Timestamp T1 = Timestamp.from(Instant.parse("2026-06-07T10:00:01Z"));
    private static final Bookmark MENU_SERVICE = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");

    @Test
    void act_deletes_selected_excluded_commands() {
        final var a = entry(ReplayState.EXCLUDED);
        final var b = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(a, b), a, b);

        final var result = fixture.action.act(fixture.commands(a, b));

        assertThat(result).isSameAs(fixture.manager);
        verify(fixture.repositoryService).remove(a);
        verify(fixture.repositoryService).remove(b);
    }

    @Test
    void validates_empty_or_null_selection() {
        final var excluded = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(excluded), excluded);

        assertThat(fixture.action.validateAct(List.of()))
                .isEqualTo("Select at least one command to delete");
        assertThat(fixture.action.validateAct(null))
                .isEqualTo("Select at least one command to delete");
    }

    @Test
    void validates_stale_or_outside_excluded_collection_selection() {
        final var active = entry(ReplayState.UNDEFINED);
        final var excluded = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(active, excluded), active, excluded);

        assertThat(fixture.action.validateAct(fixture.commands(active)))
                .isEqualTo("Selected commands must be excluded commands from the current baseline");
    }

    @Test
    void act_guards_validation_when_ui_is_bypassed() {
        final var excluded = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(excluded), excluded);

        assertThatThrownBy(() -> fixture.action.act(List.of()))
                .isInstanceOf(RecoverableException.class)
                .hasMessage("Select at least one command to delete");
    }

    @Test
    void disable_act_reports_empty_excluded_commands_collection() {
        final var fixture = fixtureWith(List.of());

        assertThat(fixture.action.disableAct())
                .isEqualTo("No excluded commands in collection");
    }

    @Test
    void disable_act_allows_non_empty_excluded_commands_collection() {
        final var excluded = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(excluded), excluded);

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void choices_selected_come_from_excluded_commands_collection() {
        final var active = entry(ReplayState.UNDEFINED);
        final var excluded = entry(ReplayState.EXCLUDED);
        final var fixture = fixtureWith(List.of(active, excluded), active, excluded);

        final var choices = fixture.action.choicesSelected();

        assertThat(interactionIds(choices))
                .containsExactly(excluded.getInteractionId());
    }

    private static Fixture fixtureWith(
            final List<CommandLogEntry> sinceBaseline,
            final CommandLogEntry... entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(sinceBaseline);
        for (final CommandLogEntry entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }

        final var repositoryService = mock(RepositoryService.class);
        final var replayContext = new ReplayContext(repositoryService, null, null, repository, null, null, List.of());
        final var manager = new CommandManagerExport(
                new CommandManagerExport.State(BASELINE, 50),
                replayContext);
        final var action = new CommandManagerExport_deleteCommands(manager);
        return new Fixture(repositoryService, replayContext, manager, action);
    }

    private static CommandLogEntry entry(final ReplayState replayState) {
        final var commandDto = new CommandDto();
        final var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(MENU_SERVICE.getLogicalTypeName() + "#act");
        commandDto.setMember(actionDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(MENU_SERVICE.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(T1);
        when(entry.getTarget()).thenReturn(MENU_SERVICE);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(actionDto.getLogicalMemberIdentifier());
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    private static List<UUID> interactionIds(final List<ReplayableCommand> commands) {
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toList());
    }

    private static class Fixture {
        final RepositoryService repositoryService;
        private final ReplayContext replayContext;
        final CommandManagerExport manager;
        final CommandManagerExport_deleteCommands action;

        Fixture(
                final RepositoryService repositoryService,
                final ReplayContext replayContext,
                final CommandManagerExport manager,
                final CommandManagerExport_deleteCommands action) {
            this.repositoryService = repositoryService;
            this.replayContext = replayContext;
            this.manager = manager;
            this.action = action;
        }

        List<ReplayableCommand> commands(final CommandLogEntry... entries) {
            return java.util.Arrays.stream(entries)
                    .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext))
                    .collect(Collectors.toList());
        }
    }
}
