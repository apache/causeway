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
package org.apache.causeway.applib.util.schema;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils.CommandExportDto;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.cmd.v2.CommandDto;

class CommandDtoUtilsReplayImportTest {

    @Test
    void decodesWrappedDocumentsInOrderWithOptionalUnresolvedResults() {
        var yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandExportDto.of(command("first"), bookmark("remote.Invoice", "1")),
                CommandExportDto.of(command("second"), null)));

        var imported = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml));

        assertThat(imported)
                .extracting(value -> value.getCommand().getInteractionId())
                .containsExactly("first", "second");
        assertThat(imported.get(0).getResult()).isEqualTo(bookmark("remote.Invoice", "1"));
        assertThat(imported.get(1).getResult()).isNull();
    }

    @Test
    void ignoresEnvelopeDocumentsWithoutCommandsAndLegacyReturnedObject() {
        var yaml = """
                result:
                  type: "ignored.Type"
                  id: "1"
                ---
                command:
                  majorVersion: "2"
                  minorVersion: "0"
                  interactionId: "valid"
                  username: "sven"
                returnedObject:
                  type: "legacy.Type"
                  id: "2"
                """;

        var imported = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml));

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getCommand().getInteractionId()).isEqualTo("valid");
        assertThat(imported.get(0).getResult()).isNull();
    }

    @Test
    void fallsBackToLegacyMultiDocumentCommands() {
        var yaml = CommandDtoUtils.toMultiDocYaml(List.of(command("first"), command("second")));

        var imported = CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml));

        assertThat(imported)
                .extracting(value -> value.getCommand().getInteractionId())
                .containsExactly("first", "second");
        assertThat(imported).allMatch(value -> value.getResult() == null);
    }

    @Test
    void rejectsListRootWithoutChangingGeneralYamlCompatibility() {
        var yaml = CommandDtoUtils.toYaml(List.of(command("first"), command("second")));

        assertThatThrownBy(() -> CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multi-document YAML");
        assertThat(CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml)))
                .extracting(CommandDto::getInteractionId)
                .containsExactly("first", "second");
    }

    @Test
    void emptyInputIsAnEmptyLegacyStreamAndMalformedInputRetainsBothFailures() {
        assertThat(CommandDtoUtils.fromYamlForReplay(DataSource.empty())).isEmpty();

        assertThatThrownBy(() -> CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8("command: [\n")))
                .satisfies(failure -> assertThat(failure.getSuppressed()).isNotEmpty());
    }

    private static CommandDto command(final String interactionId) {
        var command = new CommandDto();
        command.setMajorVersion("2");
        command.setMinorVersion("0");
        command.setInteractionId(interactionId);
        command.setUsername("sven");
        return command;
    }

    private static Bookmark bookmark(final String logicalTypeName, final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, identifier);
    }
}
