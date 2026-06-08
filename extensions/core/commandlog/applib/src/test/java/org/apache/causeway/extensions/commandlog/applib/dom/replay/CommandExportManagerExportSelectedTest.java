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

import org.junit.jupiter.api.Test;

import javax.inject.Named;

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
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandExportManagerExportSelectedTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Timestamp T1 = Timestamp.from(Instant.parse("2026-06-07T10:00:01Z"));
    private static final Bookmark MENU_SERVICE = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");
    private static final Bookmark CUSTOMER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void validate_selected_accepts_reachable_sequence_before_invocation() {
        final var finder = entry(T1, MENU_SERVICE, CUSTOMER);
        final var actionOnFoundCustomer = entry(Timestamp.from(Instant.parse("2026-06-07T10:00:02Z")), CUSTOMER, null);
        final var fixture = fixtureWith(finder, actionOnFoundCustomer);
        fixture.action.metaModelService = metaModelServiceRecognizingMenuServiceRoot();

        final var validation = fixture.action.validateSelected(fixture.replayableCommands);

        assertThat(validation).isNull();
    }

    @Test
    void validate_selected_reports_unknown_action_target_before_invocation_when_recording_support_is_enabled() {
        final var fixture = fixtureWith(entry(T1, CUSTOMER));

        final var validation = fixture.action.validateSelected(fixture.replayableCommands);

        assertThat(validation)
                .contains(CUSTOMER.toString())
                .contains("unknown for command export")
                .contains("navigation or finder action");
    }

    @Test
    void validate_selected_accepts_unknown_action_target_when_recording_support_is_disabled() {
        final var fixture = fixtureWithRecordingSupport(RecordingSupport.DISABLED, entry(T1, CUSTOMER));

        final var validation = fixture.action.validateSelected(fixture.replayableCommands);

        assertThat(validation).isNull();
    }

    @Test
    void act_guards_unknown_action_target_when_ui_validation_is_bypassed_and_recording_support_is_enabled() {
        final var fixture = fixtureWith(entry(T1, CUSTOMER));

        assertThatThrownBy(() -> fixture.action.act(fixture.replayableCommands, "commands", false))
                .isInstanceOf(RecoverableException.class)
                .hasMessageContaining(CUSTOMER.toString())
                .hasMessageContaining("unknown for command export");
    }

    @Test
    void validate_selected_reports_unknown_action_reference_parameter_before_invocation_when_recording_support_is_enabled() {
        final var fixture = fixtureWith(entryWithReferenceParameter(T1, MENU_SERVICE, "customer", CUSTOMER));
        fixture.action.metaModelService = metaModelServiceRecognizingMenuServiceRoot();

        final var validation = fixture.action.validateSelected(fixture.replayableCommands);

        assertThat(validation)
                .contains("Parameter customer")
                .contains(CUSTOMER.toString())
                .contains("unknown for command export")
                .contains("navigation or finder action");
    }

    @Test
    void validate_selected_accepts_unknown_action_reference_parameter_when_recording_support_is_disabled() {
        final var fixture = fixtureWithRecordingSupport(
                RecordingSupport.DISABLED,
                entryWithReferenceParameter(T1, MENU_SERVICE, "customer", CUSTOMER));
        fixture.action.metaModelService = metaModelServiceRecognizingMenuServiceRoot();

        final var validation = fixture.action.validateSelected(fixture.replayableCommands);

        assertThat(validation).isNull();
    }

    @Test
    void act_guards_unknown_action_reference_parameter_when_ui_validation_is_bypassed_and_recording_support_is_enabled() {
        final var fixture = fixtureWith(entryWithReferenceParameter(T1, MENU_SERVICE, "customer", CUSTOMER));
        fixture.action.metaModelService = metaModelServiceRecognizingMenuServiceRoot();

        assertThatThrownBy(() -> fixture.action.act(fixture.replayableCommands, "commands", false))
                .isInstanceOf(RecoverableException.class)
                .hasMessageContaining("Parameter customer")
                .hasMessageContaining(CUSTOMER.toString())
                .hasMessageContaining("unknown for command export");
    }

    @Test
    void validate_selected_still_requires_at_least_one_command() {
        final var fixture = fixtureWith(entry(T1, CUSTOMER));

        assertThat(fixture.action.validateSelected(List.of()))
                .isEqualTo("Select at least one command to export");
    }

    private static Fixture fixtureWith(final CommandLogEntry... entries) {
        return fixtureWithRecordingSupport(RecordingSupport.ENABLED, entries);
    }

    private static Fixture fixtureWithRecordingSupport(
            final RecordingSupport recordingSupport,
            final CommandLogEntry... entries) {
        final var repository = mock(CommandLogEntryRepository.class);
        for (final CommandLogEntry entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }

        final var replayContext = new ReplayContext(null, null, null, repository, null, null, List.of());
        final var manager = new CommandExportManager(
                new CommandExportManager.State(BASELINE, 50, CommandExportManager.Mode.EXPORT),
                replayContext);
        final var action = new CommandExportManager_exportSelected(manager);
        action.causewayConfiguration = causewayConfigurationWith(recordingSupport);
        final var replayableCommands = java.util.Arrays.stream(entries)
                .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext))
                .collect(java.util.stream.Collectors.toList());
        return new Fixture(action, replayableCommands);
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
            final Bookmark target) {
        return entry(timestamp, target, null);
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        return entry(timestamp, target, result, actionDtoFor(target));
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
        return entry(timestamp, target, null, actionDto);
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
            final ActionDto actionDto) {
        final var commandDto = new CommandDto();
        commandDto.setMember(actionDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        final var interactionId = UUID.randomUUID();
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getTimestamp()).thenReturn(timestamp);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(actionDto.getLogicalMemberIdentifier());
        when(entry.getReplayState()).thenReturn(ReplayState.UNDEFINED);
        return entry;
    }

    @DomainService
    @Named("demo.Customers")
    private static class Customers {
    }

    private static class Fixture {
        final CommandExportManager_exportSelected action;
        final List<ReplayableCommand> replayableCommands;

        Fixture(
                final CommandExportManager_exportSelected action,
                final List<ReplayableCommand> replayableCommands) {
            this.action = action;
            this.replayableCommands = replayableCommands;
        }
    }
}
