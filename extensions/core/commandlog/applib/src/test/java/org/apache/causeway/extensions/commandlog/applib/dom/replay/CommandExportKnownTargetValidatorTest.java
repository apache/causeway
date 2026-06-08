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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandExportKnownTargetValidatorTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-06-07T10:00:00Z"));
    private static final Timestamp BEFORE_BASELINE = Timestamp.from(Instant.parse("2026-06-07T09:59:59Z"));
    private static final Timestamp T1 = Timestamp.from(Instant.parse("2026-06-07T10:00:01Z"));
    private static final Timestamp T2 = Timestamp.from(Instant.parse("2026-06-07T10:00:02Z"));

    private static final Bookmark MENU_SERVICE = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customers", "1");
    private static final Bookmark CUSTOMER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");
    private static final Bookmark CATEGORY = Bookmark.forLogicalTypeNameAndIdentifier("demo.Category", "STD");
    private static final Bookmark ORDER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Order", "1");

    @Test
    void accepts_root_menu_service_target() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(action(T1, MENU_SERVICE, null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_target_returned_by_earlier_selected_command() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(T1, MENU_SERVICE, CUSTOMER),
                action(T2, CUSTOMER, null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_target_returned_by_earlier_singleton_container_command() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var singletonContainerResult = CUSTOMER;
        final var failure = validator.validate(BASELINE, List.of(
                action(T1, MENU_SERVICE, singletonContainerResult),
                action(T2, CUSTOMER, null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_reference_data_target_without_prior_result() {
        final var validator = new CommandExportKnownTargetValidator(bookmark -> MENU_SERVICE.equals(bookmark) || CATEGORY.equals(bookmark));

        final var failure = validator.validate(BASELINE, List.of(action(T1, CATEGORY, null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void rejects_unknown_non_root_target_and_reports_command_and_bookmark() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);
        final var selected = action(T1, CUSTOMER, null);

        final var failure = validator.validate(BASELINE, List.of(selected));

        assertThat(failure).isPresent();
        assertThat(failure.get().message())
                .contains(CUSTOMER.toString())
                .doesNotContain("[")
                .contains("unknown for command export")
                .contains("navigation or finder action");
    }

    @Test
    void ignores_result_before_baseline_when_validating_later_target() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(BEFORE_BASELINE, MENU_SERVICE, CUSTOMER),
                action(T2, CUSTOMER, null)));

        assertThat(failure).isPresent();
        assertThat(failure.get().message()).contains(CUSTOMER.toString());
    }

    @Test
    void does_not_treat_locally_resolvable_object_as_root() {
        final var validator = new CommandExportKnownTargetValidator(__ -> false);

        final var failure = validator.validate(BASELINE, List.of(action(T1, CUSTOMER, null)));

        assertThat(failure).isPresent();
    }

    @Test
    void later_result_does_not_validate_earlier_target() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(T1, CUSTOMER, null),
                action(T2, MENU_SERVICE, CUSTOMER)));

        assertThat(failure).isPresent();
        assertThat(failure.get().message()).contains(CUSTOMER.toString());
    }

    @Test
    void safe_action_without_result_does_not_establish_unrelated_target() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(T1, MENU_SERVICE, null),
                action(T2, ORDER, null)));

        assertThat(failure).isPresent();
        assertThat(failure.get().message()).contains(ORDER.toString());
    }

    @Test
    void property_commands_with_known_targets_establish_result_as_later_known_target() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                property(T1, MENU_SERVICE, CUSTOMER),
                action(T2, CUSTOMER, null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_reference_parameter_returned_by_earlier_selected_command() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(T1, MENU_SERVICE, CUSTOMER),
                actionWithReferenceParameter(T2, MENU_SERVICE, null, "customer", CUSTOMER)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_reference_parameter_that_is_root_menu_service() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                actionWithReferenceParameter(T1, MENU_SERVICE, null, "menu", MENU_SERVICE)));

        assertThat(failure).isEmpty();
    }

    @Test
    void accepts_reference_data_reference_parameter_without_prior_result() {
        final var validator = new CommandExportKnownTargetValidator(bookmark -> MENU_SERVICE.equals(bookmark) || CATEGORY.equals(bookmark));

        final var failure = validator.validate(BASELINE, List.of(
                actionWithReferenceParameter(T1, MENU_SERVICE, null, "category", CATEGORY)));

        assertThat(failure).isEmpty();
    }

    @Test
    void rejects_unknown_reference_parameter_and_reports_command_parameter_and_bookmark() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);
        final var selected = actionWithReferenceParameter(T1, MENU_SERVICE, null, "customer", CUSTOMER);

        final var failure = validator.validate(BASELINE, List.of(selected));

        assertThat(failure).isPresent();
        assertThat(failure.get().message())
                .contains(CUSTOMER.toString())
                .doesNotContain("[")
                .contains("parameter customer")
                .contains("unknown for command export")
                .contains("navigation or finder action");
    }

    @Test
    void later_result_does_not_validate_earlier_reference_parameter() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                actionWithReferenceParameter(T1, MENU_SERVICE, null, "customer", CUSTOMER),
                action(T2, MENU_SERVICE, CUSTOMER)));

        assertThat(failure).isPresent();
        assertThat(failure.get().message())
                .contains("parameter customer")
                .contains(CUSTOMER.toString());
    }

    @Test
    void result_before_baseline_does_not_validate_reference_parameter() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                action(BEFORE_BASELINE, MENU_SERVICE, CUSTOMER),
                actionWithReferenceParameter(T2, MENU_SERVICE, null, "customer", CUSTOMER)));

        assertThat(failure).isPresent();
        assertThat(failure.get().message())
                .contains("parameter customer")
                .contains(CUSTOMER.toString());
    }

    @Test
    void ignores_scalar_parameters_for_export_path_validation() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                actionWithScalarParameter(T1, MENU_SERVICE, null, "name")));

        assertThat(failure).isEmpty();
    }

    @Test
    void ignores_reference_parameters_without_reference_bookmark() {
        final var validator = new CommandExportKnownTargetValidator(MENU_SERVICE::equals);

        final var failure = validator.validate(BASELINE, List.of(
                actionWithReferenceParameter(T1, MENU_SERVICE, null, "customer", null)));

        assertThat(failure).isEmpty();
    }

    @Test
    void reference_data_service_accepts_when_any_implementation_accepts_bookmark() {
        final var services = List.<CommandReplayReferenceDataService>of(
                bookmark -> false,
                CATEGORY::equals);

        final var referenceData = CommandReplayReferenceDataService.isReferenceData(services, CATEGORY);

        assertThat(referenceData).isTrue();
    }

    @Test
    void reference_data_service_rejects_when_no_implementation_accepts_bookmark() {
        final var services = List.<CommandReplayReferenceDataService>of(
                bookmark -> false,
                bookmark -> MENU_SERVICE.equals(bookmark));

        final var referenceData = CommandReplayReferenceDataService.isReferenceData(services, CATEGORY);

        assertThat(referenceData).isFalse();
    }

    @Test
    void reference_data_service_rejects_when_no_implementations_are_registered() {
        final var referenceData = CommandReplayReferenceDataService.isReferenceData(List.of(), CATEGORY);

        assertThat(referenceData).isFalse();
    }

    private static CommandLogEntry action(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        return entry(timestamp, target, result, actionDtoFor(target));
    }

    private static CommandLogEntry actionWithReferenceParameter(
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
        if (parameterBookmark != null) {
            parameter.setReference(parameterBookmark.toOidDto());
        }
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, result, actionDto);
    }

    private static CommandLogEntry actionWithScalarParameter(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final String parameterName) {
        final var actionDto = actionDtoFor(target);
        actionDto.setParameters(new ParamsDto());
        final var parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.STRING);
        parameter.setString("Alice");
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, result, actionDto);
    }

    private static ActionDto actionDtoFor(final Bookmark target) {
        final var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.getLogicalTypeName() + "#act");
        return actionDto;
    }

    private static CommandLogEntry property(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        final var propertyDto = new PropertyDto();
        propertyDto.setLogicalMemberIdentifier("demo.PropertyHolder#name");
        return entry(timestamp, target, result, propertyDto);
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final MemberDto memberDto) {
        final var commandDto = new CommandDto();
        commandDto.setMember(memberDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());

        final var entry = mock(CommandLogEntry.class);
        when(entry.getTimestamp()).thenReturn(timestamp);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(memberDto.getLogicalMemberIdentifier());
        return entry;
    }
}
