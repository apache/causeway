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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandKnownParticipantsValidatorTest {

    private static final Timestamp BASELINE = timestamp("2026-08-06T10:00:00Z");
    private static final Timestamp BEFORE = timestamp("2026-08-06T09:59:59Z");
    private static final Timestamp T1 = timestamp("2026-08-06T10:00:01Z");
    private static final Timestamp T2 = timestamp("2026-08-06T10:00:02Z");
    private static final Bookmark SERVICE = bookmark("demo.CustomerMenu", "1");
    private static final Bookmark CUSTOMER = bookmark("demo.Customer", "1");
    private static final Bookmark CATEGORY = bookmark("demo.Category", "STD");

    @Test
    void acceptsRootsReferenceDataAndEarlierResults() {
        var validator = new CommandKnownParticipantsValidator(
                bookmark -> SERVICE.equals(bookmark) || CATEGORY.equals(bookmark));

        assertThat(validator.validate(BASELINE, List.of(
                action(T1, SERVICE, CUSTOMER),
                actionWithReference(T2, CUSTOMER, "category", CATEGORY)))).isEmpty();
    }

    @Test
    void laterAndPreBaselineResultsDoNotEstablishKnowledge() {
        var validator = new CommandKnownParticipantsValidator(SERVICE::equals);

        assertThat(validator.validate(BASELINE, List.of(
                action(BEFORE, SERVICE, CUSTOMER),
                action(T1, CUSTOMER, null),
                action(T2, SERVICE, CUSTOMER))))
                .get().extracting(CommandKnownParticipantsValidator.Failure::bookmark)
                .isEqualTo(CUSTOMER);
    }

    @Test
    void reportsFirstTargetFailureBeforeParameterFailure() {
        var validator = new CommandKnownParticipantsValidator(__ -> false);
        var failure = validator.validate(BASELINE, List.of(
                actionWithReference(T1, CUSTOMER, "category", CATEGORY))).orElseThrow();

        assertThat(failure.isParameter()).isFalse();
        assertThat(failure.message()).contains("target", CUSTOMER.toString(), "unknown for command export");
    }

    @Test
    void reportsNamedAndPositionalParameterFailures() {
        var validator = new CommandKnownParticipantsValidator(SERVICE::equals);
        var named = validator.validate(BASELINE, List.of(
                actionWithReference(T1, SERVICE, "customer", CUSTOMER))).orElseThrow();
        var positional = validator.validate(BASELINE, List.of(
                actionWithReference(T1, SERVICE, null, CUSTOMER))).orElseThrow();

        assertThat(named.message()).contains("parameter customer", CUSTOMER.toString());
        assertThat(positional.message()).contains("parameter parameter[0]", CUSTOMER.toString());
    }

    @Test
    void ignoresScalarAndMalformedReferenceParameters() {
        var validator = new CommandKnownParticipantsValidator(SERVICE::equals);
        var dto = actionDto(SERVICE);
        dto.setParameters(new ParamsDto());
        var scalar = new ParamDto();
        scalar.setName("quantity");
        scalar.setType(ValueType.INT);
        var missingReference = new ParamDto();
        missingReference.setType(ValueType.REFERENCE);
        dto.getParameters().getParameter().add(scalar);
        dto.getParameters().getParameter().add(missingReference);

        assertThat(validator.validate(BASELINE, List.of(entry(T1, SERVICE, null, dto)))).isEmpty();
    }

    @Test
    void usesLegacyTargetFallbackAndToleratesAbsentDto() {
        var validator = new CommandKnownParticipantsValidator(SERVICE::equals);
        var legacy = mock(CommandLogEntry.class);
        when(legacy.getTimestamp()).thenReturn(T1);
        when(legacy.getTarget()).thenReturn(CUSTOMER);
        var absent = mock(CommandLogEntry.class);
        when(absent.getTimestamp()).thenReturn(T2);

        assertThat(validator.validate(BASELINE, List.of(legacy))).isPresent();
        assertThat(validator.validate(BASELINE, List.of(absent))).isEmpty();
    }

    @Test
    void nullInputAndNullRootPredicateFailSafely() {
        var validator = new CommandKnownParticipantsValidator(null);

        assertThat(validator.validate(BASELINE, null)).isEmpty();
        assertThat(validator.validateParticipants(null, null)).isEmpty();
        assertThat(validator.validate(BASELINE, List.of(action(T1, CUSTOMER, null)))).isPresent();
    }

    private static CommandLogEntry action(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result) {
        return entry(timestamp, target, result, actionDto(target));
    }

    private static CommandLogEntry actionWithReference(
            final Timestamp timestamp,
            final Bookmark target,
            final String parameterName,
            final Bookmark parameterBookmark) {
        var actionDto = actionDto(target);
        actionDto.setParameters(new ParamsDto());
        var parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(parameterBookmark.toOidDto());
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, null, actionDto);
    }

    private static ActionDto actionDto(final Bookmark target) {
        var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.logicalTypeName() + "#act");
        return actionDto;
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final MemberDto memberDto) {
        var commandDto = new CommandDto();
        commandDto.setMember(memberDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());
        var entry = mock(CommandLogEntry.class);
        when(entry.getTimestamp()).thenReturn(timestamp);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(memberDto.getLogicalMemberIdentifier());
        return entry;
    }

    private static Timestamp timestamp(final String value) {
        return Timestamp.from(Instant.parse(value));
    }

    private static Bookmark bookmark(final String type, final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, identifier);
    }
}
