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

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandManagerImportCommandsTest {

    private static final Timestamp BASELINE = Timestamp.from(Instant.parse("2026-08-06T12:00:00Z"));
    private static final Bookmark RESULT = bookmark("demo.Invoice", "missing-locally");

    @Test
    void importsCanonicalResultsAndMovesBaselineToOldestTimestamp() throws Exception {
        var newer = command("newer", "2026-08-06T10:00:00Z");
        var older = command("older", "2026-08-06T09:00:00Z");
        var repository = mock(CommandLogEntryRepository.class);
        var newerEntry = mock(CommandLogEntry.class);
        var olderEntry = mock(CommandLogEntry.class);
        when(repository.saveForReplay(any(CommandDto.class))).thenReturn(newerEntry, olderEntry);
        var manager = manager(repository);
        var yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(newer, RESULT),
                CommandDtoUtils.CommandExportDto.of(older, null)));

        var returned = new CommandManager_importCommands(manager).act(blob(yaml), true);

        verify(repository).saveForReplay(argThat(
                (CommandDto command) -> "newer".equals(command.getUsername())));
        verify(repository).saveForReplay(argThat(
                (CommandDto command) -> "older".equals(command.getUsername())));
        verify(newerEntry).setResult(RESULT);
        verify(olderEntry, never()).setResult(org.mockito.ArgumentMatchers.any());
        assertThat(returned.getBaseline()).isEqualTo(Timestamp.from(Instant.parse("2026-08-06T09:00:00Z")));
        assertThat(returned.getLimit()).isEqualTo(50);
        assertThat(manager.getBaseline()).isEqualTo(BASELINE);
        assertThat(manager.getLimit()).isEqualTo(50);
    }

    @Test
    void legacyAndRepeatedImportDelegateEveryCommandToRepository() throws Exception {
        var command = command("legacy", "2026-08-06T10:00:00Z");
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.saveForReplay(any(CommandDto.class))).thenReturn(mock(CommandLogEntry.class));
        var manager = manager(repository);
        var yaml = CommandDtoUtils.toMultiDocYaml(List.of(command));
        var action = new CommandManager_importCommands(manager);

        assertThat(action.act(blob(yaml), false)).isSameAs(manager);
        assertThat(action.act(blob(yaml), false)).isSameAs(manager);

        verify(repository, times(2)).saveForReplay(argThat(
                (CommandDto imported) -> "legacy".equals(imported.getUsername())));
        assertThat(action.defaultMoveBaselineToOldest()).isTrue();
    }

    @Test
    void nullTimestampsAndEmptyInputRetainManagerState() throws Exception {
        var withoutTimestamp = command("untimed", null);
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.saveForReplay(any(CommandDto.class))).thenReturn(mock(CommandLogEntry.class));
        var manager = manager(repository);
        var action = new CommandManager_importCommands(manager);

        assertThat(action.act(blob(CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(withoutTimestamp, null)))), true))
                .isSameAs(manager);
        assertThat(action.act(blob(""), true)).isSameAs(manager);
        verify(repository).saveForReplay(argThat(
                (CommandDto imported) -> "untimed".equals(imported.getUsername())));
    }

    @Test
    void replayImportRejectsYamlListRootBeforePersistence() {
        var repository = mock(CommandLogEntryRepository.class);
        var action = new CommandManager_importCommands(manager(repository));

        assertThatThrownBy(() -> action.act(blob("- interactionId: one\n- interactionId: two\n"), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multi-document YAML");
        verify(repository, never()).saveForReplay(org.mockito.ArgumentMatchers.any(CommandDto.class));
    }

    private static CommandManager manager(final CommandLogEntryRepository repository) {
        var context = new ReplayContext(
                null, null, null, repository, null, null,
                new ResultRemappingService(List.of()));
        return new CommandManager(BASELINE, 50, context);
    }

    private static CommandDto command(final String username, final String instant) throws Exception {
        var command = new CommandDto();
        command.setInteractionId(UUID.randomUUID().toString());
        command.setUsername(username);
        if (instant != null) {
            command.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    GregorianCalendar.from(Instant.parse(instant).atZone(java.time.ZoneOffset.UTC))));
        }
        var targets = new OidsDto();
        targets.getOid().add(bookmark("demo.CustomerMenu", "1").toOidDto());
        command.setTargets(targets);
        var action = new ActionDto();
        action.setLogicalMemberIdentifier("demo.CustomerMenu#find");
        command.setMember(action);
        return command;
    }

    private static Blob blob(final String yaml) {
        return Blob.of("commands.yaml", CommonMimeType.YAML, yaml.getBytes(StandardCharsets.UTF_8));
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
    }
}
