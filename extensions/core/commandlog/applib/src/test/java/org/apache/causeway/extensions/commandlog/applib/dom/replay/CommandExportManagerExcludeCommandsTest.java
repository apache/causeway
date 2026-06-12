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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;

class CommandExportManagerExcludeCommandsTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Timestamp T1 = Timestamp.from(Instant.parse("2026-06-07T10:00:01Z"));
    private static final Bookmark MENU_SERVICE = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");
    private static final Bookmark CUSTOMER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void act_marks_selected_active_commands_excluded() {
        final var a = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var b = entry(MENU_SERVICE, ReplayState.EXPORTED);
        final var fixture = fixtureWith(RecordingSupport.ENABLED, List.of(a, b), a, b);

        final var result = fixture.action.act(fixture.commands(a, b));

        assertThat(result).isSameAs(fixture.manager);
        verify(a).setReplayState(ReplayState.EXCLUDED);
        verify(b).setReplayState(ReplayState.EXCLUDED);
    }

    @Test
    void validates_empty_or_null_selection() {
        final var a = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(RecordingSupport.ENABLED, List.of(a), a);

        assertThat(fixture.action.validateAct(List.of()))
                .isEqualTo("Select at least one command to exclude");
        assertThat(fixture.action.validateAct(null))
                .isEqualTo("Select at least one command to exclude");
    }

    @Test
    void validates_stale_or_outside_active_collection_selection() {
        final var active = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var excluded = entry(MENU_SERVICE, ReplayState.EXCLUDED);
        final var fixture = fixtureWith(RecordingSupport.ENABLED, List.of(active, excluded), active, excluded);

        assertThat(fixture.action.validateAct(fixture.commands(excluded)))
                .isEqualTo("Selected commands must be active commands from the current baseline");
    }

    @Test
    void act_guards_validation_when_ui_is_bypassed() {
        final var a = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(RecordingSupport.ENABLED, List.of(a), a);

        assertThatThrownBy(() -> fixture.action.act(List.of()))
                .isInstanceOf(RecoverableException.class)
                .hasMessage("Select at least one command to exclude");
    }

    @Test
    void defaults_selected_to_non_exportable_active_commands_only() {
        final var exportable = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var nonExportable = entry(CUSTOMER, ReplayState.UNDEFINED);
        final var unknownExportability = entry(CUSTOMER, ReplayState.EXPORTED);
        final var excluded = entry(CUSTOMER, ReplayState.EXCLUDED);
        final var fixture = fixtureWith(
                RecordingSupport.ENABLED,
                List.of(exportable, nonExportable, unknownExportability, excluded),
                exportable,
                nonExportable,
                unknownExportability,
                excluded);
        fixture.manager.metaModelService = metaModelServiceRecognizingMenuServiceRoot();
        fixture.manager.causewayConfiguration = causewayConfigurationWith(RecordingSupport.ENABLED);
        when(fixture.repository.findByInteractionId(unknownExportability.getInteractionId())).thenReturn(Optional.empty());

        final var defaults = fixture.action.defaultSelected();

        assertThat(interactionIds(defaults))
                .containsExactly(nonExportable.getInteractionId());
    }

    @Test
    void disable_act_reports_recording_support_disabled() {
        final var a = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(RecordingSupport.DISABLED, List.of(a), a);

        assertThat(fixture.action.disableAct())
                .isEqualTo("Command exclusion requires command-log recording support to be enabled");
    }

    @Test
    void direct_invocation_is_guarded_when_recording_support_disabled() {
        final var a = entry(MENU_SERVICE, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(RecordingSupport.DISABLED, List.of(a), a);

        assertThatThrownBy(() -> fixture.action.act(fixture.commands(a)))
                .isInstanceOf(RecoverableException.class)
                .hasMessage("Command exclusion requires command-log recording support to be enabled");
    }

    private static Fixture fixtureWith(
            final RecordingSupport recordingSupport,
            final List<CommandLogEntry> sinceBaseline,
            final CommandLogEntry... entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(sinceBaseline);
        for (final CommandLogEntry entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }

        final var replayContext = new ReplayContext(null, null, null, repository, null, null, List.of());
        final var manager = new CommandExportManager(
                new CommandExportManager.State(BASELINE, 50),
                replayContext);
        final var action = new CommandExportManager_excludeCommands(manager);
        action.causewayConfiguration = causewayConfigurationWith(recordingSupport);
        return new Fixture(repository, replayContext, manager, action);
    }

    private static CommandLogEntry entry(
            final Bookmark target,
            final ReplayState replayState) {
        final var commandDto = new CommandDto();
        final var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.getLogicalTypeName() + "#act");
        commandDto.setMember(actionDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(T1);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(actionDto.getLogicalMemberIdentifier());
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    private static CausewayConfiguration causewayConfigurationWith(final RecordingSupport recordingSupport) {
        final var causewayConfiguration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(causewayConfiguration.getExtensions().getCommandLog().getRecordingSupport()).thenReturn(recordingSupport);
        return causewayConfiguration;
    }

    private static MetaModelService metaModelServiceRecognizingMenuServiceRoot() {
        final var metaModelService = mock(MetaModelService.class);
        when(metaModelService.lookupLogicalTypeByName(MENU_SERVICE.getLogicalTypeName()))
                .thenReturn(Optional.of(LogicalType.eager(Customers.class, MENU_SERVICE.getLogicalTypeName())));
        return metaModelService;
    }

    private static List<UUID> interactionIds(final List<ReplayableCommand> commands) {
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toList());
    }

    @DomainService
    @Named("demo.Customers")
    private static class Customers {
    }

    private static class Fixture {
        final CommandLogEntryRepository repository;
        private final ReplayContext replayContext;
        final CommandExportManager manager;
        final CommandExportManager_excludeCommands action;

        Fixture(
                final CommandLogEntryRepository repository,
                final ReplayContext replayContext,
                final CommandExportManager manager,
                final CommandExportManager_excludeCommands action) {
            this.repository = repository;
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
