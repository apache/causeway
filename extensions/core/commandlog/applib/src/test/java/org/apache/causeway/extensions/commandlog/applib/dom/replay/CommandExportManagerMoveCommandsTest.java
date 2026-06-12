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
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandExportManagerMoveCommandsTest {

    private static final Timestamp BASELINE = timestamp("2026-06-07T10:00:00Z");
    private static final Timestamp BEFORE_BASELINE = timestamp("2026-06-07T09:59:59Z");
    private static final Timestamp T0 = timestamp("2026-06-07T10:00:00.500Z");
    private static final Timestamp T1 = timestamp("2026-06-07T10:00:01Z");
    private static final Timestamp T1_250 = timestamp("2026-06-07T10:00:01.250Z");
    private static final Timestamp T2 = timestamp("2026-06-07T10:00:02Z");
    private static final Timestamp T5 = timestamp("2026-06-07T10:00:05Z");

    private static final Bookmark MENU_SERVICE = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");
    private static final Bookmark CUSTOMER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void choices_target_excludes_selected_commands() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        final var choices = fixture.moveUpAction.choicesTarget(fixture.commands(b, c));

        assertThat(interactionIds(choices))
                .containsExactly(a.getInteractionId());
    }

    @Test
    void choices_target_includes_exported_commands() {
        final var a = entry(T1, MENU_SERVICE, null, ReplayState.EXPORTED);
        final var b = entry(T2, MENU_SERVICE, null, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(a, b);

        final var choices = fixture.moveUpAction.choicesTarget(fixture.commands(b));

        assertThat(interactionIds(choices))
                .containsExactly(a.getInteractionId());
    }

    @Test
    void choices_target_excludes_excluded_commands() {
        final var a = entry(T1, MENU_SERVICE, null, ReplayState.UNDEFINED);
        final var b = entry(T2, MENU_SERVICE, null, ReplayState.EXCLUDED);
        final var fixture = fixtureWith(a, b);

        final var choices = fixture.moveUpAction.choicesTarget(fixture.commands(a));

        assertThat(interactionIds(choices))
                .isEmpty();
    }

    @Test
    void validates_excluded_commands_are_outside_active_collection() {
        final var a = entry(T1, MENU_SERVICE, null, ReplayState.EXCLUDED);
        final var b = entry(T2, MENU_SERVICE, null, ReplayState.UNDEFINED);
        final var fixture = fixtureWith(a, b);

        assertThat(fixture.moveUpAction.validateAct(fixture.commands(a), fixture.command(b), false))
                .isEqualTo("Selected commands must be available for export from the current baseline");
    }

    @Test
    void validates_empty_selection_missing_target_selected_target_and_outside_baseline() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var beforeBaseline = entry(BEFORE_BASELINE, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b);

        assertThat(fixture.moveUpAction.validateAct(List.of(), fixture.command(b), false))
                .isEqualTo("Select at least one command to move");
        assertThat(fixture.moveUpAction.validateAct(fixture.commands(a), null, false))
                .isEqualTo("Select the command to move after");
        assertThat(fixture.moveUpAction.validateAct(fixture.commands(a), fixture.command(a), false))
                .isEqualTo("Cannot move commands after one of the selected commands");
        assertThat(fixture.moveUpAction.validateAct(fixture.commands(a), fixture.command(b), false))
                .isEqualTo("Target command must be before the first selected command");
        assertThat(fixture.moveUpAction.validateAct(fixture.commands(beforeBaseline), fixture.command(b), false))
                .isEqualTo("Selected commands must be available for export from the current baseline");
    }

    @Test
    void act_guards_validation_when_ui_is_bypassed() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var fixture = fixtureWith(a);

        assertThatThrownBy(() -> fixture.moveUpAction.act(List.of(), fixture.command(a), false))
                .isInstanceOf(RecoverableException.class)
                .hasMessage("Select at least one command to move");
    }

    @Test
    void disable_act_reports_recording_support_disabled() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var fixture = fixtureWithRecordingSupport(RecordingSupport.DISABLED, a);

        assertThat(fixture.moveUpAction.disableAct())
                .isEqualTo("Command movement requires command-log recording support to be enabled");
    }

    @Test
    void default_squash_timings_is_false() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var fixture = fixtureWith(a);

        assertThat(fixture.moveUpAction.defaultSquashTimings()).isFalse();
    }

    @Test
    void moves_single_command_to_target_plus_ten_milliseconds() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b);

        fixture.moveUpAction.act(fixture.commands(b), fixture.command(a), false);

        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:01.010Z"));
        assertThat(a.getTimestamp()).isEqualTo(T1);
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(b.getCommandDto().getTimestamp()))
                .isEqualTo(b.getTimestamp());
    }

    @Test
    void moves_multiple_commands_preserving_original_internal_gaps() {
        final var a = entry(T0, MENU_SERVICE, null);
        final var b = entry(T1, MENU_SERVICE, null);
        final var c = entry(T1_250, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveUpAction.act(fixture.commands(b, c), fixture.command(a), false);

        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:00.510Z"));
        assertThat(c.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:00.760Z"));
        assertThat(a.getTimestamp()).isEqualTo(T0);
    }

    @Test
    void moves_multiple_commands_squashing_original_internal_gaps_to_one_second_apart() {
        final var a = entry(T0, MENU_SERVICE, null);
        final var b = entry(T1, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveUpAction.act(fixture.commands(b, c), fixture.command(a), true);

        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:01.500Z"));
        assertThat(c.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:02.500Z"));
        assertThat(a.getTimestamp()).isEqualTo(T0);
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(b.getCommandDto().getTimestamp()))
                .isEqualTo(b.getTimestamp());
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(c.getCommandDto().getTimestamp()))
                .isEqualTo(c.getTimestamp());
    }

    @Test
    void moves_multiple_commands_with_minimum_gap_when_original_gap_is_not_positive() {
        final var a = entry(T0, MENU_SERVICE, null);
        final var b = entry(T1, MENU_SERVICE, null);
        final var c = entry(T1, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveUpAction.act(fixture.commands(b, c), fixture.command(a), false);

        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:00.510Z"));
        assertThat(c.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:00.520Z"));
    }

    @Test
    void does_not_retimestamp_unselected_commands() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveUpAction.act(fixture.commands(c), fixture.command(a), false);

        assertThat(b.getTimestamp()).isEqualTo(T2);
    }

    @Test
    void down_choices_target_excludes_selected_commands() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        final var choices = fixture.moveDownAction.choicesTarget(fixture.commands(a, b));

        assertThat(interactionIds(choices))
                .containsExactly(c.getInteractionId());
    }

    @Test
    void down_validates_empty_selection_missing_target_selected_target_and_outside_baseline() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var beforeBaseline = entry(BEFORE_BASELINE, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b);

        assertThat(fixture.moveDownAction.validateAct(List.of(), fixture.command(b), false))
                .isEqualTo("Select at least one command to move");
        assertThat(fixture.moveDownAction.validateAct(fixture.commands(a), null, false))
                .isEqualTo("Select the command to move after");
        assertThat(fixture.moveDownAction.validateAct(fixture.commands(a), fixture.command(a), false))
                .isEqualTo("Cannot move commands after one of the selected commands");
        assertThat(fixture.moveDownAction.validateAct(fixture.commands(b), fixture.command(a), false))
                .isEqualTo("Target command must be after the last selected command");
        assertThat(fixture.moveDownAction.validateAct(fixture.commands(beforeBaseline), fixture.command(b), false))
                .isEqualTo("Selected commands must be available for export from the current baseline");
    }

    @Test
    void down_moves_single_command_to_target_plus_ten_milliseconds() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b);

        fixture.moveDownAction.act(fixture.commands(a), fixture.command(b), false);

        assertThat(a.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:05.010Z"));
        assertThat(b.getTimestamp()).isEqualTo(T5);
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(a.getCommandDto().getTimestamp()))
                .isEqualTo(a.getTimestamp());
    }

    @Test
    void down_moves_multiple_commands_preserving_original_internal_gaps() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T1_250, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveDownAction.act(fixture.commands(a, b), fixture.command(c), false);

        assertThat(a.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:05.010Z"));
        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:05.260Z"));
        assertThat(c.getTimestamp()).isEqualTo(T5);
    }

    @Test
    void down_moves_multiple_commands_squashing_original_internal_gaps_to_one_second_apart() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T5, MENU_SERVICE, null);
        final var c = entry(timestamp("2026-06-07T10:00:20Z"), MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveDownAction.act(fixture.commands(a, b), fixture.command(c), true);

        assertThat(a.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:21Z"));
        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:22Z"));
        assertThat(c.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:20Z"));
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(a.getCommandDto().getTimestamp()))
                .isEqualTo(a.getTimestamp());
        assertThat(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(b.getCommandDto().getTimestamp()))
                .isEqualTo(b.getTimestamp());
    }

    @Test
    void down_moves_multiple_commands_with_minimum_gap_when_original_gap_is_not_positive() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T1, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveDownAction.act(fixture.commands(a, b), fixture.command(c), false);

        assertThat(a.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:05.010Z"));
        assertThat(b.getTimestamp()).isEqualTo(timestamp("2026-06-07T10:00:05.020Z"));
    }

    @Test
    void down_does_not_retimestamp_unselected_commands() {
        final var a = entry(T1, MENU_SERVICE, null);
        final var b = entry(T2, MENU_SERVICE, null);
        final var c = entry(T5, MENU_SERVICE, null);
        final var fixture = fixtureWith(a, b, c);

        fixture.moveDownAction.act(fixture.commands(a), fixture.command(c), false);

        assertThat(b.getTimestamp()).isEqualTo(T2);
    }

    @Test
    void moved_finder_result_validates_later_selected_action_target() {
        final var predecessor = entry(T0, MENU_SERVICE, null);
        final var actionOnUnknownCustomer = entry(T1, CUSTOMER, null);
        final var laterFinder = entry(T5, MENU_SERVICE, CUSTOMER);
        final var fixture = fixtureWith(predecessor, actionOnUnknownCustomer, laterFinder);
        fixture.exportAction.metaModelService = metaModelServiceRecognizingMenuServiceRoot();
        fixture.exportAction.causewayConfiguration = causewayConfigurationWith(RecordingSupport.ENABLED);

        fixture.moveUpAction.act(fixture.commands(laterFinder), fixture.command(predecessor), false);
        final var validation = fixture.exportAction.validateSelected(fixture.commands(predecessor, actionOnUnknownCustomer, laterFinder));

        assertThat(validation).isNull();
    }

    @Test
    void moved_navigation_result_validates_later_selected_reference_parameter() {
        final var predecessor = entry(T0, MENU_SERVICE, null);
        final var actionWithUnknownParameter = entryWithReferenceParameter(T1, MENU_SERVICE, null, "customer", CUSTOMER);
        final var laterNavigation = entry(T5, MENU_SERVICE, CUSTOMER);
        final var fixture = fixtureWith(predecessor, actionWithUnknownParameter, laterNavigation);
        fixture.exportAction.metaModelService = metaModelServiceRecognizingMenuServiceRoot();
        fixture.exportAction.causewayConfiguration = causewayConfigurationWith(RecordingSupport.ENABLED);

        fixture.moveUpAction.act(fixture.commands(laterNavigation), fixture.command(predecessor), false);
        final var validation = fixture.exportAction.validateSelected(fixture.commands(predecessor, actionWithUnknownParameter, laterNavigation));

        assertThat(validation).isNull();
    }

    private static Fixture fixtureWith(final CommandLogEntry... entries) {
        return fixtureWithRecordingSupport(RecordingSupport.ENABLED, entries);
    }

    private static Fixture fixtureWithRecordingSupport(
            final RecordingSupport recordingSupport,
            final CommandLogEntry... entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        final var availableEntries = List.of(entries);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(availableEntries);
        for (final CommandLogEntry entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }

        final var replayContext = new ReplayContext(null, null, null, repository, null, null, List.of());
        final var manager = new CommandExportManager(
                new CommandExportManager.State(BASELINE, 50),
                replayContext);
        final var moveUpAction = new CommandExportManager_moveCommandsUp(manager);
        moveUpAction.causewayConfiguration = causewayConfigurationWith(recordingSupport);
        final var moveDownAction = new CommandExportManager_moveCommandsDown(manager);
        moveDownAction.causewayConfiguration = causewayConfigurationWith(recordingSupport);
        final var exportAction = new CommandExportManager_exportSelected(manager);
        exportAction.causewayConfiguration = causewayConfigurationWith(recordingSupport);
        return new Fixture(
                replayContext,
                moveUpAction,
                moveDownAction,
                exportAction);
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

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        return entry(timestamp, target, result, ReplayState.UNDEFINED);
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState) {
        return entry(timestamp, target, result, replayState, actionDtoFor(target));
    }

    private static CommandLogEntry entryWithReferenceParameter(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final String parameterName,
            final Bookmark parameterBookmark) {
        final var actionDto = actionDtoFor(target);
        actionDto.setParameters(new ParamsDto());
        final var parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(parameterBookmark.toOidDto());
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, result, ReplayState.UNDEFINED, actionDto);
    }

    private static ActionDto actionDtoFor(final Bookmark target) {
        final var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.getLogicalTypeName() + "#act");
        return actionDto;
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState,
            final ActionDto actionDto) {
        final AtomicReference<Timestamp> timestampRef = new AtomicReference<>(timestamp);
        final var commandDto = new CommandDto();
        commandDto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
        commandDto.setMember(actionDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenAnswer(__ -> timestampRef.get());
        Mockito.doCallRealMethod().when(entry).compareTo(Mockito.any(CommandLogEntry.class));
        Mockito.doAnswer(invocation -> {
            timestampRef.set(invocation.getArgument(0));
            return null;
        }).when(entry).setTimestamp(Mockito.any(Timestamp.class));
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(actionDto.getLogicalMemberIdentifier());
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    private static Timestamp timestamp(final String instant) {
        return Timestamp.from(Instant.parse(instant));
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
        private final ReplayContext replayContext;
        final CommandExportManager_moveCommandsUp moveUpAction;
        final CommandExportManager_moveCommandsDown moveDownAction;
        final CommandExportManager_exportSelected exportAction;

        Fixture(
                final ReplayContext replayContext,
                final CommandExportManager_moveCommandsUp moveUpAction,
                final CommandExportManager_moveCommandsDown moveDownAction,
                final CommandExportManager_exportSelected exportAction) {
            this.replayContext = replayContext;
            this.moveUpAction = moveUpAction;
            this.moveDownAction = moveDownAction;
            this.exportAction = exportAction;
        }

        ReplayableCommand command(final CommandLogEntry entry) {
            return new ReplayableCommand(entry.getInteractionId(), replayContext);
        }

        List<ReplayableCommand> commands(final CommandLogEntry... entries) {
            return java.util.Arrays.stream(entries)
                    .map(this::command)
                    .collect(Collectors.toList());
        }
    }
}
