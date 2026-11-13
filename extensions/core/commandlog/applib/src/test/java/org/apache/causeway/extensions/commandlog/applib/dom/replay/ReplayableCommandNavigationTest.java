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
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayableCommandNavigationTest {

    @Test
    void nextAndPreviousSkipIneligibleEntriesAndPreserveContext() throws Exception {
        var timestamp = ZonedDateTime.parse("2026-08-05T10:15:30Z");
        var current = entry(timestamp, false);
        var ineligible = entry(timestamp.plusSeconds(1), true);
        var adjacent = entry(timestamp.plusSeconds(2), false);
        var fixture = fixture(current);
        when(fixture.repository().findForegroundSinceTimestamp(Timestamp.from(timestamp.toInstant())))
                .thenReturn(List.of(current, ineligible, adjacent));
        when(fixture.repository().findForegroundBeforeTimestamp(
                Timestamp.from(timestamp.toInstant()), null))
                .thenReturn(List.of(ineligible, adjacent));

        var next = fixture.command().next();
        var previous = fixture.command().previous();

        assertThat(next.interactionId()).isEqualTo(adjacent.getInteractionId());
        assertThat(previous.interactionId()).isEqualTo(adjacent.getInteractionId());
        assertThat(next.replayContext()).isSameAs(fixture.context());
        assertThat(previous.replayContext()).isSameAs(fixture.context());
        assertThat(fixture.command().disableNext()).isNull();
        assertThat(fixture.command().disablePrevious()).isNull();
        verify(current, never()).setReplayState(any());
        verify(ineligible, never()).setReplayState(any());
        verify(adjacent, never()).setReplayState(any());
    }

    @Test
    void boundariesDisableSafelyAndReturnCurrentCommand() throws Exception {
        var timestamp = ZonedDateTime.parse("2026-08-05T10:15:30Z");
        var current = entry(timestamp, false);
        var fixture = fixture(current);
        when(fixture.repository().findForegroundSinceTimestamp(any())).thenReturn(List.of(current));
        when(fixture.repository().findForegroundBeforeTimestamp(any(), isNull())).thenReturn(List.of());

        assertThat(fixture.command().disablePrevious()).isNotNull();
        assertThat(fixture.command().disableNext()).isNotNull();
        assertThat(fixture.command().previous()).isSameAs(fixture.command());
        assertThat(fixture.command().next()).isSameAs(fixture.command());
    }

    @Test
    void navigationMixinsAreSafeAndSuppressPublishing() {
        assertSafeSuppressed(ReplayableCommand_previous.class);
        assertSafeSuppressed(ReplayableCommand_next.class);
    }

    private static void assertSafeSuppressed(final Class<?> mixinType) {
        var action = mixinType.getAnnotation(Action.class);
        assertThat(action.semantics()).isEqualTo(SemanticsOf.SAFE);
        assertThat(action.commandPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.executionPublishing()).isEqualTo(Publishing.DISABLED);
    }

    private static Fixture fixture(final CommandLogEntry current) {
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(current.getInteractionId())).thenReturn(Optional.of(current));
        var featureRepository = mock(ApplicationFeatureRepository.class);
        var feature = mock(ApplicationFeature.class);
        when(featureRepository.findFeature(any())).thenReturn(feature);
        when(feature.getActionSemantics()).thenReturn(Optional.of(SemanticsOf.SAFE));
        var context = new ReplayContext(
                null, null, null, repository, null, null,
                new ResultRemappingService(List.of()), null, featureRepository);
        return new Fixture(
                new ReplayableCommand(current.getInteractionId(), context),
                context,
                repository);
    }

    private static CommandLogEntry entry(
            final ZonedDateTime timestamp,
            final boolean safeActionWithoutResult) throws Exception {
        var dto = new CommandDto();
        dto.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(timestamp)));
        if (safeActionWithoutResult) {
            dto.setMember(new ActionDto());
        }
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(UUID.randomUUID());
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getReplayState()).thenReturn(ReplayState.PENDING);
        when(entry.getLogicalMemberIdentifier()).thenReturn("demo.Customer#find");
        return entry;
    }

    private record Fixture(
            ReplayableCommand command,
            ReplayContext context,
            CommandLogEntryRepository repository) {
    }
}
