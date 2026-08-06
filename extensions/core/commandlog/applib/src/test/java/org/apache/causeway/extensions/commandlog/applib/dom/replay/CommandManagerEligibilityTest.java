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

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerEligibilityTest {

    @Test
    void generalManagerCollectionsOmitResultlessSafeEntryWithoutMutation() {
        var fixture = fixture();
        when(fixture.repository().findForegroundSinceTimestampAndCanBeExported(fixture.baseline()))
                .thenReturn(fixture.entries());
        when(fixture.repository().findForegroundSinceTimestampAndHasBeenExported(fixture.baseline()))
                .thenReturn(fixture.entries());
        when(fixture.repository().findSinceAndWithReplayOkOrExcluded(fixture.baseline()))
                .thenReturn(fixture.entries());

        var exportManager = new CommandExportManager(fixture.baseline(), fixture.context());
        var replayManager = new CommandReplayManager(fixture.baseline(), fixture.context());

        assertThat(exportManager.getNotYetExported()).extracting(ReplayableCommand::interactionId)
                .containsExactly(fixture.resultBearing().getInteractionId());
        assertThat(exportManager.getExported()).extracting(ReplayableCommand::interactionId)
                .containsExactly(fixture.resultBearing().getInteractionId());
        assertThat(replayManager.getSucceededOrExcluded()).extracting(ReplayableCommand::interactionId)
                .containsExactly(fixture.resultBearing().getInteractionId());
        verifyUnchanged(fixture.resultless());
        verifyUnchanged(fixture.resultBearing());
    }

    @Test
    void pendingOrFailedRetainsEveryRepositoryResult() {
        var fixture = fixture();
        when(fixture.repository().findForegroundSinceTimestampAndWithReplayPendingOrFailed(
                fixture.baseline())).thenReturn(fixture.entries());

        var commands = new CommandReplayManager(fixture.baseline(), fixture.context())
                .getPendingOrFailed();

        assertThat(commands).extracting(ReplayableCommand::interactionId)
                .containsExactly(
                        fixture.resultless().getInteractionId(),
                        fixture.resultBearing().getInteractionId());
        verifyUnchanged(fixture.resultless());
        verifyUnchanged(fixture.resultBearing());
    }

    private static void verifyUnchanged(final CommandLogEntry entry) {
        verify(entry, never()).setReplayState(any());
        verify(entry, never()).setResult(any());
    }

    private static Fixture fixture() {
        var baseline = Timestamp.from(Instant.parse("2026-08-05T10:00:00Z"));
        var resultless = entry(null);
        var resultBearing = entry(Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1"));
        var repository = mock(CommandLogEntryRepository.class);
        var featureRepository = mock(ApplicationFeatureRepository.class);
        var feature = mock(ApplicationFeature.class);
        when(featureRepository.findFeature(any())).thenReturn(feature);
        when(feature.getActionSemantics()).thenReturn(Optional.of(SemanticsOf.SAFE));
        var context = new ReplayContext(
                null, null, null, repository, null, null,
                new ResultRemappingService(List.of()), null, featureRepository);
        return new Fixture(
                baseline, context, repository, resultless, resultBearing,
                List.of(resultless, resultBearing));
    }

    private static CommandLogEntry entry(final Bookmark result) {
        var dto = new CommandDto();
        dto.setMember(new ActionDto());
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(UUID.randomUUID());
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getLogicalMemberIdentifier()).thenReturn("demo.Customer#find");
        when(entry.getResult()).thenReturn(result);
        return entry;
    }

    private record Fixture(
            Timestamp baseline,
            ReplayContext context,
            CommandLogEntryRepository repository,
            CommandLogEntry resultless,
            CommandLogEntry resultBearing,
            List<CommandLogEntry> entries) {
    }
}
