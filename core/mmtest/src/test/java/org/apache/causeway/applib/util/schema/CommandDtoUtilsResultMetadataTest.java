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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils.BookmarkDto;
import org.apache.causeway.applib.util.schema.CommandDtoUtils.CommandExportDto;
import org.apache.causeway.applib.util.schema.CommandDtoUtils.ImportedCommandDto;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

class CommandDtoUtilsResultMetadataTest {

    @Test
    void transferFactoriesPreserveCommandAndOptionalBookmark() {
        var command = command("interaction-1");
        var bookmark = bookmark("demoCustomer", "1");

        var export = CommandExportDto.of(command, bookmark);
        var imported = ImportedCommandDto.of(command, bookmark);

        assertThat(export.getCommand()).isSameAs(command);
        assertThat(export.getResult().getType()).isEqualTo("demoCustomer");
        assertThat(export.getResult().getId()).isEqualTo("1");
        assertThat(export.getResult().toBookmark()).isEqualTo(bookmark);
        assertThat(imported.getCommand()).isSameAs(command);
        assertThat(imported.getResult()).isEqualTo(bookmark);
        assertThat(CommandExportDto.of(command, null).getResult()).isNull();
        assertThat(ImportedCommandDto.of(command, null).getResult()).isNull();
        assertThat(BookmarkDto.of(null)).isNull();
    }

    @Test
    void bookmarkDtoConvertsWithoutResolvingDomainObject() {
        var bookmarkDto = new BookmarkDto();
        bookmarkDto.setType("remoteInvoice");
        bookmarkDto.setId("42");

        assertThat(bookmarkDto.toBookmark())
                .isEqualTo(bookmark("remoteInvoice", "42"));
    }

    @Test
    void commandExportIgnoresUnknownLegacyReturnedObjectField() {
        var yaml = """
                command:
                  interactionId: interaction-1
                returnedObject:
                  type: legacyCustomer
                  id: 1
                """;

        var export = YamlUtils.tryRead(
                        CommandExportDto.class,
                        DataSource.ofStringUtf8(yaml),
                        CommandDtoJacksonSupport.yamlReadCustomizer())
                .ifFailureFail()
                .getValue()
                .orElseThrow();

        assertThat(export.getCommand().getInteractionId()).isEqualTo("interaction-1");
        assertThat(export.getResult()).isNull();
    }

    @Test
    void resultBearingExportsUseOrderedMultiDocumentYamlAndOmitNulls() {
        var yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandExportDto.of(command("first"), bookmark("demoCustomer", "1")),
                CommandExportDto.of(command("second"), null)));

        assertThat(yaml)
                .contains("command:")
                .contains("interactionId: \"first\"")
                .contains("interactionId: \"second\"")
                .contains("result:")
                .contains("type: \"demoCustomer\"")
                .contains("id: \"1\"")
                .contains("---")
                .doesNotContain("returnedObject");
        assertThat(yaml.indexOf("first")).isLessThan(yaml.indexOf("second"));
        assertThat(occurrences(yaml, "result:")).isEqualTo(1);
    }

    @Test
    void copyIsStructurallyIndependentAndNullSafe() {
        var original = command("interaction-1");
        var targets = new OidsDto();
        var target = new OidDto();
        target.setType("demoCustomer");
        target.setId("1");
        targets.getOid().add(target);
        original.setTargets(targets);

        var copy = CommandDtoUtils.copy(original);

        assertThat(copy).isNotSameAs(original);
        assertThat(CommandDtoUtils.dtoMapper().toString(copy))
                .isEqualTo(CommandDtoUtils.dtoMapper().toString(original));
        copy.getTargets().getOid().get(0).setId("2");
        assertThat(original.getTargets().getOid().get(0).getId()).isEqualTo("1");
        assertThat(CommandDtoUtils.copy(null)).isNull();
    }

    @Test
    void legacyCommandYamlApisRetainListAndMultiDocumentRoundTrips() {
        var commands = List.of(command("first"), command("second"));

        var listYaml = CommandDtoUtils.toYaml(commands);
        var multiDocYaml = CommandDtoUtils.toMultiDocYaml(commands);

        assertThat(CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(listYaml)))
                .extracting(CommandDto::getInteractionId)
                .containsExactly("first", "second");
        assertThat(CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(multiDocYaml)))
                .extracting(CommandDto::getInteractionId)
                .containsExactly("first", "second");
    }

    private CommandDto command(final String interactionId) {
        var command = new CommandDto();
        command.setMajorVersion("2");
        command.setMinorVersion("0");
        command.setInteractionId(interactionId);
        command.setUsername("sven");
        return command;
    }

    private Bookmark bookmark(final String logicalTypeName, final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, identifier);
    }

    private int occurrences(final String input, final String token) {
        return (input.length() - input.replace(token, "").length()) / token.length();
    }
}
