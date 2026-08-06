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
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerKnownParticipantsTest {

    private static final Timestamp BASELINE = timestamp("2026-08-06T10:00:00Z");
    private static final Timestamp T1 = timestamp("2026-08-06T10:00:01Z");
    private static final Timestamp T2 = timestamp("2026-08-06T10:00:02Z");
    private static final Bookmark SERVICE = bookmark("demo.CustomerMenu", "1");
    private static final Bookmark CUSTOMER = bookmark("demo.Customer", "1");
    private static final Bookmark CATEGORY = bookmark("demo.Category", "STD");

    @Test
    void managerClassifiesServiceReferenceDataAndEarlierResultParticipants() {
        var finder = action(T1, SERVICE, CUSTOMER, ReplayState.OK);
        var later = actionWithReference(T2, CUSTOMER, "category", CATEGORY);
        var commands = fixture(RecordingSupport.ENABLED, finder, later).manager().getCommandsInSequence();

        assertThat(commands).extracting(ReplayableCommand::isKnownParticipants)
                .containsExactly(true, true);
        assertThat(commands.get(1).getReplayState()).isEqualTo(ReplayState.UNDEFINED);
        verify(later, never()).setReplayState(any());
    }

    @Test
    void firstOrdinaryActionAndPropertyTargetsAreUnknown() {
        var action = action(T1, CUSTOMER, null, ReplayState.UNDEFINED);
        var property = property(T2, CUSTOMER);
        var commands = fixture(RecordingSupport.ENABLED, action, property).manager().getCommandsInSequence();

        assertThat(commands).extracting(ReplayableCommand::isKnownParticipants)
                .containsExactly(false, false);
    }

    @Test
    void laterResultCannotEstablishEarlierTarget() {
        var earlier = action(T1, CUSTOMER, null, ReplayState.UNDEFINED);
        var laterFinder = action(T2, SERVICE, CUSTOMER, ReplayState.UNDEFINED);
        var commands = fixture(RecordingSupport.ENABLED, earlier, laterFinder).manager().getCommandsInSequence();

        assertThat(commands).extracting(ReplayableCommand::isKnownParticipants)
                .containsExactly(false, true);
    }

    @Test
    void earlierResultsRemainKnowledgeAcrossIncludedReplayStates() {
        for (var replayState : List.of(
                ReplayState.UNDEFINED,
                ReplayState.EXPORTED,
                ReplayState.PENDING,
                ReplayState.OK,
                ReplayState.FAILED)) {
            var finder = action(T1, SERVICE, CUSTOMER, replayState);
            var target = action(T2, CUSTOMER, null, ReplayState.UNDEFINED);

            assertThat(fixture(RecordingSupport.ENABLED, finder, target).manager()
                    .getCommandsInSequence().get(1).isKnownParticipants())
                    .as("earlier replay state %s", replayState)
                    .isTrue();
        }
    }

    @Test
    void resultBeforeBaselineIsRejectedBySequenceValidator() {
        var beforeBaseline = action(timestamp("2026-08-06T09:59:59Z"), SERVICE,
                CUSTOMER, ReplayState.UNDEFINED);
        var target = action(T2, CUSTOMER, null, ReplayState.UNDEFINED);
        var fixture = fixture(RecordingSupport.ENABLED, beforeBaseline, target);

        assertThat(fixture.manager().validateKnownTargets(List.of(beforeBaseline, target)))
                .isPresent();
    }

    @Test
    void unknownReferenceParameterAndDisabledRecordingAreFalse() {
        var parameterCommand = actionWithReference(T1, SERVICE, "customer", CUSTOMER);

        assertThat(fixture(RecordingSupport.ENABLED, parameterCommand).manager()
                .getCommandsInSequence().get(0).isKnownParticipants()).isFalse();
        assertThat(fixture(RecordingSupport.DISABLED, parameterCommand).manager()
                .getCommandsInSequence().get(0).isKnownParticipants()).isFalse();
    }

    @Test
    void absentAndStandaloneCommandsHaveNoManagerContext() {
        var present = action(T1, SERVICE, null, ReplayState.UNDEFINED);
        var absent = action(T2, SERVICE, null, ReplayState.UNDEFINED);
        var fixture = fixture(RecordingSupport.ENABLED, present);
        when(fixture.repository().findByInteractionId(absent.getInteractionId()))
                .thenReturn(Optional.of(absent));

        assertThat(fixture.manager().isKnownParticipants(absent)).isFalse();
        assertThat(new ReplayableCommand(absent.getInteractionId(), fixture.context())
                .isKnownParticipants()).isFalse();
    }

    @Test
    void excludedAndOutOfPageResultsDoNotEstablishKnowledge() {
        var excludedFinder = action(T1, SERVICE, CUSTOMER, ReplayState.EXCLUDED);
        var target = action(T2, CUSTOMER, null, ReplayState.UNDEFINED);
        var fixture = fixture(RecordingSupport.ENABLED, excludedFinder, target);

        assertThat(fixture.manager().getCommandsInSequence())
                .extracting(ReplayableCommand::isKnownParticipants)
                .containsExactly(false);
    }

    @Test
    void rootClassificationUsesMetamodelAndDoesNotResolveObjects() {
        var fixture = fixture(RecordingSupport.ENABLED, action(T1, SERVICE, null, ReplayState.UNDEFINED));

        assertThat(fixture.context().isDomainService(SERVICE)).isTrue();
        assertThat(fixture.context().isDomainService(CUSTOMER)).isFalse();
        assertThat(fixture.context().isExportRoot(CATEGORY)).isTrue();
        verify(fixture.specificationLoader()).specForLogicalTypeName(SERVICE.logicalTypeName());
    }

    private static Fixture fixture(
            final RecordingSupport recordingSupport,
            final CommandLogEntry... entries) {
        var repository = mock(CommandLogEntryRepository.class);
        for (var entry : entries) {
            when(repository.findByInteractionId(entry.getInteractionId())).thenReturn(Optional.of(entry));
        }
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(List.of(entries));
        var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(recordingSupport);
        var specificationLoader = mock(SpecificationLoader.class);
        var serviceSpecification = mock(ObjectSpecification.class);
        when(serviceSpecification.isDomainService()).thenReturn(true);
        when(specificationLoader.specForLogicalTypeName(SERVICE.logicalTypeName()))
                .thenReturn(Optional.of(serviceSpecification));
        when(specificationLoader.specForLogicalTypeName(CUSTOMER.logicalTypeName()))
                .thenReturn(Optional.empty());
        List<CommandReplayReferenceDataService> referenceDataServices = List.of(CATEGORY::equals);
        var context = new ReplayContext(
                null, null, null, repository, null, null,
                new ResultRemappingService(List.of()), null, null,
                configuration, specificationLoader, referenceDataServices);
        return new Fixture(new CommandManager(BASELINE, 50, context), context, repository, specificationLoader);
    }

    private static CommandLogEntry action(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState) {
        var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.logicalTypeName() + "#act");
        return entry(timestamp, target, result, replayState, actionDto);
    }

    private static CommandLogEntry actionWithReference(
            final Timestamp timestamp,
            final Bookmark target,
            final String parameterName,
            final Bookmark parameterBookmark) {
        var actionDto = new ActionDto();
        actionDto.setLogicalMemberIdentifier(target.logicalTypeName() + "#act");
        actionDto.setParameters(new ParamsDto());
        var parameter = new ParamDto();
        parameter.setName(parameterName);
        parameter.setType(ValueType.REFERENCE);
        parameter.setReference(parameterBookmark.toOidDto());
        actionDto.getParameters().getParameter().add(parameter);
        return entry(timestamp, target, null, ReplayState.UNDEFINED, actionDto);
    }

    private static CommandLogEntry property(final Timestamp timestamp, final Bookmark target) {
        var propertyDto = new PropertyDto();
        propertyDto.setLogicalMemberIdentifier(target.logicalTypeName() + "#name");
        return entry(timestamp, target, null, ReplayState.UNDEFINED, propertyDto);
    }

    private static CommandLogEntry entry(
            final Timestamp timestamp,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState,
            final org.apache.causeway.schema.cmd.v2.MemberDto memberDto) {
        var commandDto = new CommandDto();
        commandDto.setMember(memberDto);
        commandDto.setTargets(new OidsDto());
        commandDto.getTargets().getOid().add(target.toOidDto());
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(UUID.randomUUID());
        when(entry.getTimestamp()).thenReturn(timestamp);
        when(entry.getTarget()).thenReturn(target);
        when(entry.getResult()).thenReturn(result);
        when(entry.getReplayState()).thenReturn(replayState);
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

    private record Fixture(
            CommandManager manager,
            ReplayContext context,
            CommandLogEntryRepository repository,
            SpecificationLoader specificationLoader) {
    }
}
