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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommandManagerWorkflowTest {

    private static final Timestamp BASELINE = timestamp("2026-08-06T10:00:00Z");
    private static final int LIMIT = 50;
    private static final Bookmark SERVICE = bookmark("demo.CustomerMenu", "1");
    private static final Bookmark ORDINARY = bookmark("demo.Customer", "1");

    @Test
    void exclusionDefaultsUnknownCommandsAndRefreshesCollectionsWithoutChangingManagerState() {
        var known = mutableEntry("2026-08-06T10:00:01Z", ReplayState.UNDEFINED, SERVICE);
        var unknown = mutableEntry("2026-08-06T10:00:02Z", ReplayState.EXPORTED, ORDINARY);
        var fixture = fixture(true, known, unknown);
        var action = new CommandManager_excludeCommands(fixture.manager());
        var memento = fixture.manager().viewModelMemento();

        assertThat(action.defaultSelected()).extracting(ReplayableCommand::interactionId)
                .containsExactly(unknown.id());
        var returned = action.act(action.defaultSelected());

        assertThat(returned).isSameAs(fixture.manager());
        assertThat(unknown.replayState()).isEqualTo(ReplayState.EXCLUDED);
        assertThat(fixture.manager().getCommandsInSequence())
                .extracting(ReplayableCommand::interactionId).containsExactly(known.id());
        assertThat(fixture.manager().getExcluded())
                .extracting(ReplayableCommand::interactionId).containsExactly(unknown.id());
        assertThat(fixture.manager().viewModelMemento()).isEqualTo(memento);
    }

    @Test
    void selectionValidationUsesIdsAcceptsReconstructionAndRejectsInvalidSelectionsAtomically() {
        var first = mutableEntry("2026-08-06T10:00:01Z", ReplayState.UNDEFINED, SERVICE);
        var second = mutableEntry("2026-08-06T10:00:02Z", ReplayState.EXPORTED, SERVICE);
        var fixture = fixture(true, first, second);
        var action = new CommandManager_excludeCommands(fixture.manager());
        var reconstructed = new ReplayableCommand(first.id().toString(), fixture.context());
        var foreign = new ReplayableCommand(UUID.randomUUID().toString(), fixture.context());

        assertThat(action.validateAct(List.of(reconstructed))).isNull();
        for (var invalid : List.of(
                List.<ReplayableCommand>of(),
                Arrays.asList((ReplayableCommand) null),
                List.of(reconstructed, reconstructed),
                List.of(reconstructed, foreign))) {
            assertThat(action.validateAct(invalid)).isNotNull();
            assertThat(first.replayState()).isEqualTo(ReplayState.UNDEFINED);
            assertThat(second.replayState()).isEqualTo(ReplayState.EXPORTED);
        }
        assertThat(action.validateAct(null)).isNotNull();
    }

    @Test
    void recordingGateAppliesToExclusionAndMovementButNotDirectRestorationOrDeletion() {
        var active = mutableEntry("2026-08-06T10:00:01Z", ReplayState.UNDEFINED, SERVICE);
        var excluded = mutableEntry("2026-08-06T10:00:02Z", ReplayState.EXCLUDED, SERVICE);
        var fixture = fixture(false, active, excluded);
        var activeCommand = fixture.manager().getCommandsInSequence().get(0);
        var excludedCommand = fixture.manager().getExcluded().get(0);

        assertThat(new CommandManager_excludeCommands(fixture.manager()).disableAct()).contains("recording");
        assertThat(new CommandManager_excludeCommands(fixture.manager())
                .validateAct(List.of(activeCommand))).contains("recording");
        assertThat(new CommandManager_moveCommands(fixture.manager()).disableAct()).contains("recording");
        assertThatThrownBy(() -> new CommandManager_excludeCommands(fixture.manager())
                .act(List.of(activeCommand))).hasMessageContaining("recording");
        assertThatThrownBy(() -> new CommandManager_moveCommands(fixture.manager())
                .act(List.of(activeCommand), excludedCommand, false)).hasMessageContaining("recording");

        var restore = new CommandManager_unexcludeCommands(fixture.manager());
        assertThat(restore.disableAct()).contains("recording");
        assertThat(restore.validateAct(List.of(excludedCommand), ReplayState.PENDING)).isNull();
        restore.act(List.of(excludedCommand), ReplayState.PENDING);
        assertThat(excluded.replayState()).isEqualTo(ReplayState.PENDING);
    }

    @Test
    void restorationOffersEveryNonExcludedStateAndRejectsDestinationOrMembershipBeforeMutation() {
        var excludedA = mutableEntry("2026-08-06T10:00:01Z", ReplayState.EXCLUDED, SERVICE);
        var excludedB = mutableEntry("2026-08-06T10:00:02Z", ReplayState.EXCLUDED, SERVICE);
        var active = mutableEntry("2026-08-06T10:00:03Z", ReplayState.OK, SERVICE);
        var fixture = fixture(true, excludedA, excludedB, active);
        var action = new CommandManager_unexcludeCommands(fixture.manager());
        var excluded = action.choicesSelected();
        var activeCommand = fixture.manager().getCommandsInSequence().stream()
                .filter(command -> command.interactionId().equals(active.id())).findFirst().orElseThrow();

        assertThat(action.choicesReplayState()).containsExactly(
                ReplayState.UNDEFINED, ReplayState.EXPORTED, ReplayState.PENDING,
                ReplayState.OK, ReplayState.FAILED);
        assertThat(action.validateAct(excluded, ReplayState.EXCLUDED)).isNotNull();
        assertThat(action.validateAct(excluded, null)).isNotNull();
        assertThat(action.validateAct(List.of(excluded.get(0), activeCommand), ReplayState.PENDING)).isNotNull();
        assertThat(excludedA.replayState()).isEqualTo(ReplayState.EXCLUDED);
        assertThat(excludedB.replayState()).isEqualTo(ReplayState.EXCLUDED);

        action.act(excluded, ReplayState.UNDEFINED);
        assertThat(excludedA.replayState()).isEqualTo(ReplayState.UNDEFINED);
        assertThat(excludedB.replayState()).isEqualTo(ReplayState.UNDEFINED);
        assertThat(fixture.manager().getExcluded()).isEmpty();
    }

    @Test
    void deletionRemovesOnlyAValidatedExcludedSelectionAndLeavesIndependentServicesUntouched() {
        var excludedA = mutableEntry("2026-08-06T10:00:01Z", ReplayState.EXCLUDED, SERVICE);
        var excludedB = mutableEntry("2026-08-06T10:00:02Z", ReplayState.EXCLUDED, SERVICE);
        var active = mutableEntry("2026-08-06T10:00:03Z", ReplayState.FAILED, SERVICE);
        var fixture = fixture(false, excludedA, excludedB, active);
        var action = new CommandManager_deleteCommands(fixture.manager());
        var excluded = action.choicesSelected();
        var activeCommand = fixture.manager().getCommandsInSequence().stream()
                .filter(command -> command.interactionId().equals(active.id())).findFirst().orElseThrow();

        assertThat(action.disableAct()).isNull();
        assertThat(action.validateAct(List.of(excluded.get(0), activeCommand))).isNotNull();
        verify(fixture.repositoryService(), never()).remove(any());

        action.act(excluded);
        assertThat(excludedA.removed()).isTrue();
        assertThat(excludedB.removed()).isTrue();
        assertThat(active.removed()).isFalse();
        assertThat(action.disableAct()).contains("No excluded");
        verifyNoInteractions(fixture.resultRemappingService());
    }

    @Test
    void deletionRejectsEveryNonExcludedReplayStateAndStaleCommandsAtomically() {
        var entries = Arrays.stream(ReplayState.values())
                .map(state -> mutableEntry(
                        "2026-08-06T10:00:0" + state.ordinal() + "Z", state, SERVICE))
                .toList();
        var fixture = fixture(false, entries.toArray(MutableEntry[]::new));
        var action = new CommandManager_deleteCommands(fixture.manager());
        var excluded = action.choicesSelected().get(0);

        for (var active : fixture.manager().getCommandsInSequence()) {
            assertThat(action.validateAct(List.of(excluded, active))).isNotNull();
        }
        var stale = new ReplayableCommand(UUID.randomUUID().toString(), fixture.context());
        assertThat(action.validateAct(List.of(excluded, stale))).isNotNull();
        assertThat(action.validateAct(List.of())).isNotNull();
        assertThat(entries).noneMatch(MutableEntry::removed);
    }

    @Test
    void movementDerivesManagerOrderPreservesQualifyingGapsAndUpdatesEntryAndDtoOnly() {
        var target = mutableEntry("2026-08-06T10:00:00Z", ReplayState.UNDEFINED, SERVICE);
        var first = mutableEntry("2026-08-06T10:01:00Z", ReplayState.EXPORTED, SERVICE);
        var second = mutableEntry("2026-08-06T10:01:02.500Z", ReplayState.PENDING, SERVICE);
        var untouched = mutableEntry("2026-08-06T10:02:00Z", ReplayState.OK, SERVICE);
        var fixture = fixture(true, target, first, second, untouched);
        var action = new CommandManager_moveCommands(fixture.manager());
        var commands = action.choicesSelected();
        var memento = fixture.manager().viewModelMemento();

        action.act(List.of(commands.get(2), commands.get(1)), commands.get(0), false);

        assertThat(first.timestamp()).isEqualTo(timestamp("2026-08-06T10:00:01Z"));
        assertThat(second.timestamp()).isEqualTo(timestamp("2026-08-06T10:00:03.500Z"));
        assertThat(dtoTimestamp(first)).isEqualTo(first.timestamp());
        assertThat(dtoTimestamp(second)).isEqualTo(second.timestamp());
        assertThat(target.timestamp()).isEqualTo(timestamp("2026-08-06T10:00:00Z"));
        assertThat(untouched.timestamp()).isEqualTo(timestamp("2026-08-06T10:02:00Z"));
        assertThat(first.replayState()).isEqualTo(ReplayState.EXPORTED);
        assertThat(second.replayState()).isEqualTo(ReplayState.PENDING);
        assertThat(fixture.manager().viewModelMemento()).isEqualTo(memento);
        assertThat(fixture.manager().getCommandsInSequence())
                .extracting(ReplayableCommand::interactionId)
                .containsExactly(target.id(), first.id(), second.id(), untouched.id());
    }

    @Test
    void movementUsesMinimumForShortReversedAndMissingGapsAndCanSquash() {
        for (var timestamps : List.of(
                Arrays.asList("2026-08-06T10:01:00Z", "2026-08-06T10:01:00Z", "2026-08-06T10:00:59Z"),
                Arrays.asList("2026-08-06T10:01:00Z", "2026-08-06T10:01:00.500Z", null))) {
            var target = mutableEntry("2026-08-06T10:00:00Z", ReplayState.UNDEFINED, SERVICE);
            var first = mutableEntry(timestamps.get(0), ReplayState.UNDEFINED, SERVICE);
            var second = mutableEntry(timestamps.get(1), ReplayState.UNDEFINED, SERVICE);
            var third = mutableEntry(timestamps.get(2), ReplayState.UNDEFINED, SERVICE);
            var fixture = fixture(true, target, first, second, third);
            var commands = fixture.manager().getCommandsInSequence();
            var selected = commands.subList(1, 4);

            new CommandManager_moveCommands(fixture.manager())
                    .act(selected, commands.get(0), false);

            var entries = List.of(first, second, third);
            assertThat(selected.stream()
                    .map(ReplayableCommand::interactionId)
                    .map(id -> entries.stream().filter(entry -> entry.id().equals(id)).findFirst().orElseThrow())
                    .map(MutableEntry::timestamp)).containsExactly(
                    timestamp("2026-08-06T10:00:01Z"),
                    timestamp("2026-08-06T10:00:02Z"),
                    timestamp("2026-08-06T10:00:03Z"));
        }

        var target = mutableEntry("2026-08-06T10:00:00Z", ReplayState.UNDEFINED, SERVICE);
        var first = mutableEntry("2026-08-06T11:00:00Z", ReplayState.UNDEFINED, SERVICE);
        var second = mutableEntry("2026-08-06T12:00:00Z", ReplayState.UNDEFINED, SERVICE);
        var fixture = fixture(true, target, first, second);
        var action = new CommandManager_moveCommands(fixture.manager());
        var commands = action.choicesSelected();

        assertThat(action.defaultSquashTimings()).isFalse();
        action.act(commands.subList(1, 3), commands.get(0), true);
        assertThat(first.timestamp()).isEqualTo(timestamp("2026-08-06T10:00:01Z"));
        assertThat(second.timestamp()).isEqualTo(timestamp("2026-08-06T10:00:02Z"));
    }

    @Test
    void movementChoicesExcludeSelectionAndEveryInvalidTargetIsAtomic() {
        var first = mutableEntry("2026-08-06T10:00:01Z", ReplayState.UNDEFINED, SERVICE);
        var second = mutableEntry("2026-08-06T10:00:02Z", ReplayState.UNDEFINED, SERVICE);
        var excluded = mutableEntry("2026-08-06T10:00:03Z", ReplayState.EXCLUDED, SERVICE);
        var fixture = fixture(true, first, second, excluded);
        var action = new CommandManager_moveCommands(fixture.manager());
        var active = action.choicesSelected();
        var excludedCommand = fixture.manager().getExcluded().get(0);
        var originals = List.of(first.timestamp(), second.timestamp(), excluded.timestamp());

        assertThat(action.choicesTarget(List.of(active.get(0))))
                .extracting(ReplayableCommand::interactionId).containsExactly(second.id());
        assertThat(action.validateAct(List.of(active.get(0)), active.get(0), false)).isNotNull();
        assertThat(action.validateAct(List.of(active.get(0)), excludedCommand, false)).isNotNull();
        assertThat(action.validateAct(List.of(excludedCommand), active.get(0), false)).isNotNull();
        assertThat(action.validateAct(List.of(), active.get(0), false)).isNotNull();
        assertThat(action.validateAct(List.of(active.get(0)), null, false)).isNotNull();
        assertThat(List.of(first.timestamp(), second.timestamp(), excluded.timestamp()))
                .containsExactlyElementsOf(originals);
    }

    @Test
    void movingLaterIsObservedByKnownParticipantsAndSequenceExport() throws Exception {
        var first = mutableEntry("2026-08-06T10:00:01Z", ReplayState.UNDEFINED, SERVICE);
        var second = mutableEntry("2026-08-06T10:00:02Z", ReplayState.UNDEFINED, SERVICE);
        var last = mutableEntry("2026-08-06T10:00:03Z", ReplayState.EXPORTED, SERVICE);
        var fixture = fixture(true, first, second, last);
        var move = new CommandManager_moveCommands(fixture.manager());
        var original = move.choicesSelected();

        move.act(List.of(original.get(0)), original.get(2), false);

        var refreshed = fixture.manager().getCommandsInSequence();
        assertThat(refreshed).extracting(ReplayableCommand::interactionId)
                .containsExactly(second.id(), last.id(), first.id());
        assertThat(refreshed).allMatch(ReplayableCommand::isKnownParticipants);
        var yaml = new CommandManager_exportSequence(fixture.manager())
                .act("commands", false, false).asString();
        assertThat(CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml)))
                .extracting(value -> value.getCommand().getInteractionId())
                .containsExactly(
                        second.id().toString(), last.id().toString(), first.id().toString());
    }

    private static Fixture fixture(
            final boolean recordingEnabled,
            final MutableEntry... mutableEntries) {
        final var entries = new ArrayList<>(List.of(mutableEntries));
        final var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, LIMIT)).thenAnswer(invocation -> entries.stream()
                .filter(entry -> !entry.removed())
                .map(MutableEntry::entry)
                .sorted(java.util.Comparator.comparing(
                        CommandLogEntry::getTimestamp,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .limit(LIMIT)
                .toList());
        when(repository.findForegroundSinceTimestampAndWithReplayExcluded(BASELINE)).thenAnswer(invocation -> entries.stream()
                .filter(entry -> !entry.removed() && entry.replayState() == ReplayState.EXCLUDED)
                .map(MutableEntry::entry)
                .sorted(java.util.Comparator.comparing(
                        CommandLogEntry::getTimestamp,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList());
        when(repository.findByInteractionId(any())).thenAnswer(invocation -> {
            final UUID id = invocation.getArgument(0);
            return entries.stream()
                    .filter(entry -> !entry.removed() && entry.id().equals(id))
                    .map(MutableEntry::entry)
                    .findFirst();
        });

        final var repositoryService = mock(RepositoryService.class);
        doAnswer(invocation -> {
            final Object removed = invocation.getArgument(0);
            entries.stream().filter(entry -> entry.entry() == removed)
                    .forEach(entry -> entry.removedRef().set(true));
            return null;
        }).when(repositoryService).remove(any());

        final var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(
                recordingEnabled ? RecordingSupport.ENABLED : RecordingSupport.DISABLED);
        final var specificationLoader = mock(SpecificationLoader.class);
        final var serviceSpecification = mock(ObjectSpecification.class);
        when(serviceSpecification.isDomainService()).thenReturn(true);
        when(specificationLoader.specForLogicalTypeName(SERVICE.logicalTypeName()))
                .thenReturn(Optional.of(serviceSpecification));
        when(specificationLoader.specForLogicalTypeName(ORDINARY.logicalTypeName()))
                .thenReturn(Optional.empty());
        final var resultRemappingService = mock(ResultRemappingService.class);
        final var context = new ReplayContext(
                repositoryService, null, null, repository, null, null,
                resultRemappingService, null, mock(ApplicationFeatureRepository.class),
                configuration, specificationLoader, List.of());
        return new Fixture(
                new CommandManager(BASELINE, LIMIT, context), context,
                repositoryService, resultRemappingService);
    }

    private static MutableEntry mutableEntry(
            final String instant,
            final ReplayState replayState,
            final Bookmark target) {
        final var id = UUID.randomUUID();
        final var timestamp = new AtomicReference<Timestamp>(instant != null ? timestamp(instant) : null);
        final var state = new AtomicReference<>(replayState);
        final var dto = new AtomicReference<>(commandDto(id, timestamp.get(), target));
        final var removed = new AtomicBoolean();
        final var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(id);
        when(entry.getTimestamp()).thenAnswer(invocation -> timestamp.get());
        doAnswer(invocation -> {
            timestamp.set(invocation.getArgument(0));
            return null;
        }).when(entry).setTimestamp(any());
        when(entry.getReplayState()).thenAnswer(invocation -> state.get());
        doAnswer(invocation -> {
            state.set(invocation.getArgument(0));
            return null;
        }).when(entry).setReplayState(any());
        when(entry.getCommandDto()).thenAnswer(invocation -> dto.get());
        doAnswer(invocation -> {
            dto.set(invocation.getArgument(0));
            return null;
        }).when(entry).setCommandDto(any());
        when(entry.getTarget()).thenReturn(target);
        when(entry.getLogicalMemberIdentifier()).thenReturn(target.logicalTypeName() + "#act");
        return new MutableEntry(id, entry, timestamp, state, dto, removed);
    }

    private static CommandDto commandDto(
            final UUID id,
            final Timestamp timestamp,
            final Bookmark target) {
        final var dto = new CommandDto();
        dto.setInteractionId(id.toString());
        if (timestamp != null) {
            dto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
        }
        final var targets = new OidsDto();
        targets.getOid().add(target.toOidDto());
        dto.setTargets(targets);
        final var action = new ActionDto();
        action.setLogicalMemberIdentifier(target.logicalTypeName() + "#act");
        dto.setMember(action);
        return dto;
    }

    private static Timestamp dtoTimestamp(final MutableEntry entry) {
        return JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(entry.dto().getTimestamp());
    }

    private static Timestamp timestamp(final String instant) {
        return Timestamp.from(Instant.parse(instant));
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
    }

    private record Fixture(
            CommandManager manager,
            ReplayContext context,
            RepositoryService repositoryService,
            ResultRemappingService resultRemappingService) { }

    private record MutableEntry(
            UUID id,
            CommandLogEntry entry,
            AtomicReference<Timestamp> timestampRef,
            AtomicReference<ReplayState> replayStateRef,
            AtomicReference<CommandDto> dtoRef,
            AtomicBoolean removedRef) {

        Timestamp timestamp() { return timestampRef.get(); }
        ReplayState replayState() { return replayStateRef.get(); }
        CommandDto dto() { return dtoRef.get(); }
        boolean removed() { return removedRef.get(); }
    }
}
