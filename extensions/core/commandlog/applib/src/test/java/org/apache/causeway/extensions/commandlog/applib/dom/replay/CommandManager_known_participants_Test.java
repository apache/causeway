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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import org.apache.causeway.core.runtimeservices.scratchpad.ScratchpadDefault;

import org.junit.jupiter.api.Test;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandManager_known_participants_Test {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Timestamp T1 = Timestamp.from(Instant.parse("2026-06-07T10:00:01Z"));

    private static final Bookmark MENU_SERVICE_BOOKMARK = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");
    public static final LogicalType MENU_SERVICE_LOGICAL_TYPE_NAME = LogicalType.eager(Customers.class, MENU_SERVICE_BOOKMARK.getLogicalTypeName());
    public static final ObjectSpecification MENU_SERVICE_OBJECT_SPEC = mock(ObjectSpecification.class);

    private static final Bookmark CUSTOMER_BOOKMARK = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");
    private static final Bookmark CATEGORY_BOOKMARK = Bookmark.forLogicalTypeNameAndIdentifier("demo.Category", "STD");

    @Test
    void export_sequence_is_enabled_for_first_domain_service_target() {
        final var fixture = fixtureWith(entry(T1, MENU_SERVICE_BOOKMARK));

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void export_sequence_is_enabled_for_reachable_sequence() {
        final var finder = entry(T1, MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), CUSTOMER_BOOKMARK, null);
        final var fixture = fixtureWith(finder, actionOnFoundCustomer);

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void export_sequence_is_enabled_for_reference_data_target() {
        final var fixture = fixtureWith(entry(T1, CATEGORY_BOOKMARK));

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void export_sequence_is_disabled_for_unknown_action_target_when_recording_support_is_enabled() {
        final var fixture = fixtureWith(entry(T1, CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void export_sequence_is_disabled_for_unknown_property_edit_target_when_recording_support_is_enabled() {
        final var fixture = fixtureWith(propertyEditEntry(T1, CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void export_sequence_is_disabled_when_recording_support_is_disabled() {
        final var fixture = fixtureWithRecordingSupport(RecordingSupport.DISABLED, entry(T1, CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void export_sequence_is_enabled_for_reference_data_reference_parameter() {
        final var fixture = fixtureWith(entryWithReferenceParameter(T1, MENU_SERVICE_BOOKMARK, "category", CATEGORY_BOOKMARK));

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void export_sequence_is_disabled_for_unknown_action_reference_parameter_when_recording_support_is_enabled() {
        final var fixture = fixtureWith(entryWithReferenceParameter(T1, MENU_SERVICE_BOOKMARK, "customer", CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void export_sequence_is_disabled_for_unknown_action_reference_parameter_when_recording_support_is_disabled() {
        final var fixture = fixtureWithRecordingSupport(
                RecordingSupport.DISABLED,
                entryWithReferenceParameter(T1, MENU_SERVICE_BOOKMARK, "customer", CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void exportable_is_true_for_first_domain_service_target_in_export_manager_context() {
        final var commands = exportManagerCommandsWith(entry(T1, MENU_SERVICE_BOOKMARK));

        assertThat(commands.get(0).isKnownParticipants()).isTrue();
    }

    @Test
    void exportable_is_true_for_command_with_known_target_in_export_manager_context() {
        final var finder = entry(T1, MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), CUSTOMER_BOOKMARK, null);
        final var commands = exportManagerCommandsWith(finder, actionOnFoundCustomer);

        assertThat(commands.get(1).isKnownParticipants()).isTrue();
    }

    @Test
    void exportable_is_false_for_command_with_unknown_target_in_export_manager_context() {
        final var commands = exportManagerCommandsWith(entry(T1, CUSTOMER_BOOKMARK));

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
    }

    @Test
    void exportable_is_false_for_property_edit_with_unknown_target_in_export_manager_context() {
        final var commands = exportManagerCommandsWith(propertyEditEntry(T1, CUSTOMER_BOOKMARK));

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
    }

    @Test
    void exportable_is_false_when_required_result_is_later_in_export_order() {
        final var actionOnCustomer = entry(T1, CUSTOMER_BOOKMARK, null);
        final var laterFinder = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var commands = exportManagerCommandsWith(actionOnCustomer, laterFinder);

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
    }

    @Test
    void earlier_non_exportable_command_does_not_make_later_known_command_non_exportable() {
        final var unknownCustomerAction = entry(T1, CUSTOMER_BOOKMARK, null);
        final var finder = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:03Z")), CUSTOMER_BOOKMARK, null);
        final var commands = exportManagerCommandsWith(unknownCustomerAction, finder, actionOnFoundCustomer);

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
        assertThat(commands.get(1).isKnownParticipants()).isTrue();
        assertThat(commands.get(2).isKnownParticipants()).isTrue();
    }

    @Test
    void exportable_is_false_when_recording_support_is_disabled() {
        final var repository = repositoryWith(entry(T1, CUSTOMER_BOOKMARK));
        final var replayContext = ReplayContext.builder()
                                        .commandLogEntryRepository(repository)
                                        .scratchpad(new ScratchpadDefault())
                                        .commandReplayReferenceDataService(referenceDataServiceFor(CATEGORY_BOOKMARK))
                                        .metaModelService(metaModelServiceRecognizingMenuServiceRoot())
                                        .specificationLoader(specificationLoaderRecognizingMenuServiceRoot())
                                        .causewayConfiguration(causewayConfigurationWith(RecordingSupport.DISABLED))
                                        .build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);
        final var commands = manager.getCommandsInSequence();

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
    }

    @Test
    void exportable_is_false_outside_export_manager_context() {
        final var entry = entry(T1, CUSTOMER_BOOKMARK);
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        final var replayContext = ReplayContext.builder()
                .commandLogEntryRepository(repository)
                .scratchpad(new ScratchpadDefault())
                .commandReplayReferenceDataService(referenceDataServiceFor(CATEGORY_BOOKMARK))
                .build();

        final var command = new ReplayableCommand(entry.getInteractionId(), replayContext);

        assertThat(command.isKnownParticipants()).isFalse();
    }

    @Test
    void exportable_computation_does_not_modify_replay_state() {
        final var entry = entry(T1, CUSTOMER_BOOKMARK);
        final var commands = exportManagerCommandsWith(entry);

        assertThat(commands.get(0).isKnownParticipants()).isFalse();
        verify(entry, org.mockito.Mockito.never()).setReplayState(org.mockito.Mockito.any());
    }

    @Test
    void export_sequence_disable_act_requires_at_least_one_command_with_known_participants() {
        final var fixture = fixtureWith(entry(T1, CUSTOMER_BOOKMARK));

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void export_sequence_is_enabled_for_reference_data_target_in_sequence() {
        final var fixture = fixtureWith(entry(T1, CATEGORY_BOOKMARK, null));

        assertThat(fixture.action.disableAct()).isNull();
    }

    @Test
    void command_sequence_marks_only_known_participant_commands_as_known() {
        final var unknownCustomerAction = entry(T1, CUSTOMER_BOOKMARK, null);
        final var finder = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:03Z")), CUSTOMER_BOOKMARK, null);
        final var commands = exportManagerCommandsWith(unknownCustomerAction, finder, actionOnFoundCustomer);

        assertThat(commands)
                .extracting(ReplayableCommand::isKnownParticipants)
                .containsExactly(false, true, true);
    }

    @Test
    void export_sequence_is_disabled_when_recording_support_makes_known_participants_false() {
        final var entry = entry(T1, CUSTOMER_BOOKMARK);
        final var fixture = fixtureWithRecordingSupport(RecordingSupport.DISABLED, entry);

        assertThat(fixture.action.disableAct())
                .isEqualTo("No commands (with known participants) in this sequence.");
    }

    @Test
    void fixture_exposes_full_command_sequence_for_review() {
        final var finder = entry(T1, MENU_SERVICE_BOOKMARK, CUSTOMER_BOOKMARK);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), CUSTOMER_BOOKMARK, null);
        final var fixture = fixtureWith(finder, actionOnFoundCustomer);

        assertThat(fixture.replayableCommands)
                .extracting(ReplayableCommand::interactionId)
                .containsExactly(finder.getInteractionId(), actionOnFoundCustomer.getInteractionId());
    }

    @Test
    void export_sequence_disablement_does_not_modify_replay_state() {
        final var entry = entry(T1, MENU_SERVICE_BOOKMARK);
        final var fixture = fixtureWith(entry);

        assertThat(fixture.action.disableAct()).isNull();
        verify(entry, org.mockito.Mockito.never()).setReplayState(org.mockito.Mockito.any());
    }

    private static Fixture fixtureWith(final CommandLogEntry... entries) {
        return fixtureWithRecordingSupport(RecordingSupport.ENABLED, entries);
    }

    private static List<ReplayableCommand> exportManagerCommandsWith(final CommandLogEntry... entries) {
        final var repository = repositoryWith(entries);
        final var replayContext = ReplayContext.builder()
                                        .commandLogEntryRepository(repository)
                                        .scratchpad(new ScratchpadDefault())
                                        .commandReplayReferenceDataService(referenceDataServiceFor(CATEGORY_BOOKMARK))
                                        .metaModelService(metaModelServiceRecognizingMenuServiceRoot())
                                        .specificationLoader(specificationLoaderRecognizingMenuServiceRoot())
                                        .causewayConfiguration(causewayConfigurationWith(RecordingSupport.ENABLED))
                                        .build();
        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);
        return manager.getCommandsInSequence();
    }

    private static Fixture fixtureWithRecordingSupport(
            final RecordingSupport recordingSupport,
            final CommandLogEntry... entries) {
        final var repository = repositoryWith(entries);
        final var replayContext = ReplayContext.builder()
                .commandLogEntryRepository(repository)
                .causewayConfiguration(causewayConfigurationWith(recordingSupport))
                .scratchpad(new ScratchpadDefault())
                .metaModelService(metaModelServiceRecognizingMenuServiceRoot())
                .specificationLoader(specificationLoaderRecognizingMenuServiceRoot())
                .commandReplayReferenceDataService(referenceDataServiceFor(CATEGORY_BOOKMARK))
                .build();

        final var manager = new CommandManager(new CommandManager.State(BASELINE, 50), replayContext);
        final var action = new CommandManager_exportSequence(manager);
        final var replayableCommands = java.util.Arrays.stream(entries)
                .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext))
                .collect(java.util.stream.Collectors.toList());
        return new Fixture(action, replayableCommands);
    }

    private static CommandLogEntryRepository repositoryWith(final CommandLogEntry... entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        for (final CommandLogEntry entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(List.of(entries));
        return repository;
    }

    private static CausewayConfiguration causewayConfigurationWith(final RecordingSupport recordingSupport) {
        final var causewayConfiguration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(causewayConfiguration.getExtensions().getCommandLog().getRecordingSupport()).thenReturn(recordingSupport);
        return causewayConfiguration;
    }

    private static MetaModelService metaModelServiceRecognizingMenuServiceRoot() {
        final var metaModelService = mock(MetaModelService.class);
        when(metaModelService.lookupLogicalTypeByName(MENU_SERVICE_BOOKMARK.getLogicalTypeName()))
                .thenReturn(Optional.of(MENU_SERVICE_LOGICAL_TYPE_NAME));
        return metaModelService;
    }

    private static SpecificationLoader specificationLoaderRecognizingMenuServiceRoot() {
        final var specificationLoader = mock(SpecificationLoader.class);
        when(specificationLoader.specForLogicalType(MENU_SERVICE_LOGICAL_TYPE_NAME))
                .thenReturn(Optional.of(MENU_SERVICE_OBJECT_SPEC));
        when(MENU_SERVICE_OBJECT_SPEC.isDomainService()).thenReturn(true);
        return specificationLoader;
    }

    private static CommandReplayReferenceDataService referenceDataServiceFor(final Bookmark referenceDataBookmark) {
        return referenceDataBookmark::equals;
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target) {
        return entry(timestamp, target, null);
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        return entry(timestamp, target, result, ReplayState.UNDEFINED, actionDtoFor(target));
    }

    private static CommandLogEntry propertyEditEntry(
            final Timestamp timestamp,
            final Bookmark target) {
        final var propertyDto = new PropertyDto();
        propertyDto.setLogicalMemberIdentifier(target.getLogicalTypeName() + "#email");
        return entry(timestamp, target, null, ReplayState.UNDEFINED, propertyDto);
    }

    private static CommandLogEntry entryWithReferenceParameter(
            final Timestamp timestamp,
            final Bookmark target,
            final String parameterName,
            final Bookmark parameterBookmark) {
        final var actionDto = actionDtoFor(target);
        actionDto.setParameters(new ParamsDto());
        final var parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(parameterBookmark.toOidDto());
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, null, ReplayState.UNDEFINED, actionDto);
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
            final ReplayState replayState) {
        return entry(timestamp, target, result, replayState, actionDtoFor(target));
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState,
            final org.apache.causeway.schema.cmd.v2.MemberDto memberDto) {
        final var commandDto = new CommandDto();
        commandDto.setMember(memberDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(timestamp);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(memberDto.getLogicalMemberIdentifier());
        when(entry.getReplayState()).thenReturn(replayState);
        return entry;
    }

    @DomainService
    @Named("demo.Customers")
    private static class Customers {
    }

    private static class Fixture {
        final CommandManager_exportSequence action;
        final List<ReplayableCommand> replayableCommands;

        Fixture(
                final CommandManager_exportSequence action,
                final List<ReplayableCommand> replayableCommands) {
            this.action = action;
            this.replayableCommands = replayableCommands;
        }
    }
}
