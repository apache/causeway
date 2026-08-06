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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerExportSequenceTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-08-06T10:00:00Z"));
    private static final Bookmark SERVICE = bookmark("demo.CustomerMenu", "1");
    private static final Bookmark ORDINARY = bookmark("demo.Customer", "1");
    private static final Bookmark RESULT = bookmark("demo.Invoice", "1");

    @Test
    void exportsOnlyKnownCommandsInManagerOrderWithResultsAndNoStateChanges() throws Exception {
        var first = entry("first", "2026-08-06T10:00:01Z", SERVICE, RESULT, ReplayState.UNDEFINED);
        var unknown = entry("unknown", "2026-08-06T10:00:02Z", ORDINARY, null, ReplayState.UNDEFINED);
        var last = entry("last", "2026-08-06T10:00:03Z", SERVICE, null, ReplayState.EXPORTED);
        var fixture = fixture(List.of(first, unknown, last), List.of());
        var memento = fixture.manager().viewModelMemento();

        var clob = new CommandManager_exportSequence(fixture.manager())
                .act("commands", true, false);
        var imported = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(clob.asString()));

        assertThat(imported).extracting(value -> value.getCommand().getInteractionId())
                .containsExactly(first.getInteractionId().toString(), last.getInteractionId().toString());
        assertThat(imported).extracting(CommandDtoUtils.ImportedCommandDto::getResult)
                .containsExactly(RESULT, null);
        assertThat(clob.name()).startsWith("commands.2026-08-06T10_00_01Z").endsWith(".yaml");
        assertThat(fixture.manager().viewModelMemento()).isEqualTo(memento);
        verify(first, never()).setReplayState(any());
        verify(unknown, never()).setReplayState(any());
        verify(last, never()).setReplayState(any());
    }

    @Test
    void optionalRemappingUsesIndependentEnvelopeAndCanBeDisabled() throws Exception {
        var source = entry("mapped", "2026-08-06T10:00:01Z", SERVICE, RESULT, ReplayState.UNDEFINED);
        CommandReplayMappingListener mapping = new CommandReplayMappingListener() {
            @Override public Optional<Bookmark> lookup(final Bookmark recorded) {
                return Optional.of(bookmark(recorded.logicalTypeName(), "2"));
            }
        };
        var fixture = fixture(List.of(source), List.of(mapping));
        var action = new CommandManager_exportSequence(fixture.manager());

        var recorded = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(
                action.act("recorded", false, false).asString()));
        var remapped = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(
                action.act("remapped", false, true).asString()));

        assertThat(recorded.get(0).getCommand().getTargets().getOid().get(0).getId()).isEqualTo("1");
        assertThat(recorded.get(0).getResult()).isEqualTo(RESULT);
        assertThat(remapped.get(0).getCommand().getTargets().getOid().get(0).getId()).isEqualTo("2");
        assertThat(remapped.get(0).getResult()).isEqualTo(bookmark("demo.Invoice", "2"));
        assertThat(source.getCommandDto().getTargets().getOid().get(0).getId()).isEqualTo("1");
        assertThat(source.getResult()).isEqualTo(RESULT);
    }

    @Test
    void emptyKnownSequenceDisablesExportAndDefaultsAreStable() throws Exception {
        var unknown = entry("unknown", "2026-08-06T10:00:01Z", ORDINARY, null, ReplayState.UNDEFINED);
        var action = new CommandManager_exportSequence(fixture(List.of(unknown), List.of()).manager());

        assertThat(action.disableAct()).contains("known participants");
        assertThat(action.defaultFilenamePrefix()).isEqualTo("commands");
        assertThat(action.defaultFilenameTimestamp()).isTrue();
        assertThat(action.defaultRemapResults()).isFalse();
    }

    private static Fixture fixture(
            final List<CommandLogEntry> entries,
            final List<CommandReplayMappingListener> mappings) {
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findForegroundSinceTimestamp(BASELINE, 50)).thenReturn(entries);
        entries.forEach(entry -> when(repository.findByInteractionId(entry.getInteractionId()))
                .thenReturn(Optional.of(entry)));
        var configuration = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(configuration.extensions().commandLog().recordingSupport()).thenReturn(RecordingSupport.ENABLED);
        var specificationLoader = mock(SpecificationLoader.class);
        var serviceSpecification = mock(ObjectSpecification.class);
        when(serviceSpecification.isDomainService()).thenReturn(true);
        when(specificationLoader.specForLogicalTypeName(SERVICE.logicalTypeName()))
                .thenReturn(Optional.of(serviceSpecification));
        when(specificationLoader.specForLogicalTypeName(ORDINARY.logicalTypeName()))
                .thenReturn(Optional.empty());
        var context = new ReplayContext(
                null, null, null, repository, null, null,
                new ResultRemappingService(mappings), null, null,
                configuration, specificationLoader, List.of());
        return new Fixture(new CommandManager(BASELINE, 50, context));
    }

    private static CommandLogEntry entry(
            final String username,
            final String instant,
            final Bookmark target,
            final Bookmark result,
            final ReplayState replayState) throws Exception {
        var interactionId = UUID.randomUUID();
        var command = new CommandDto();
        command.setInteractionId(interactionId.toString());
        command.setUsername(username);
        command.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar(
                GregorianCalendar.from(Instant.parse(instant).atZone(java.time.ZoneOffset.UTC))));
        var targets = new OidsDto();
        targets.getOid().add(target.toOidDto());
        command.setTargets(targets);
        var action = new ActionDto();
        action.setLogicalMemberIdentifier(target.logicalTypeName() + "#act");
        command.setMember(action);
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(command);
        when(entry.getResult()).thenReturn(result);
        when(entry.getReplayState()).thenReturn(replayState);
        when(entry.getLogicalMemberIdentifier()).thenReturn(action.getLogicalMemberIdentifier());
        return entry;
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
    }

    private record Fixture(CommandManager manager) { }
}
