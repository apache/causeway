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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandDtoUtils_toYaml_fromYaml_Test {

    @Test
    void bookmark_metadata_maps_returned_object_bookmark() {
        CommandDtoUtils.BookmarkDto bookmarkDto = CommandDtoUtils.BookmarkDto.of(
                Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456"));

        Assertions.assertThat(bookmarkDto.getType()).isEqualTo("demo.Invoice");
        Assertions.assertThat(bookmarkDto.getId()).isEqualTo("456");
    }

    @Test
    void bookmark_metadata_is_absent_for_void_result() {
        CommandDtoUtils.CommandExportDto exportDto = CommandDtoUtils.CommandExportDto.of(command("void-result"), null);

        Assertions.assertThat(exportDto.getResult()).isNull();
    }

    @Test
    void from_yaml_accepts_wrapped_export_shape_and_ignores_result_metadata() {
        CommandDto withResult = command("with-result");
        String yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(
                        withResult,
                        Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456"))));

        List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(commandDtos)
                .singleElement()
                .satisfies(command -> Assertions.assertThat(command.getInteractionId()).isEqualTo("with-result"));
    }

    @Test
    void from_yaml_for_replay_accepts_wrapped_export_shape_and_keeps_result_metadata() {
        CommandDto withResult = command("with-result");
        String yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(
                        withResult,
                        Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456"))));

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId()).isEqualTo("with-result");
                    Assertions.assertThat(importedCommandDto.getResult())
                            .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456"));
                });
    }

    @Test
    void from_yaml_for_replay_accepts_multi_document_export_shape_with_result_type() {
        String yaml = "command:\n"
                + "  majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"first-result-type\"\n"
                + "  username: \"sven\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"demo.Customer\"\n"
                + "      id: \"123\"\n"
                + "  member: !<ACT>\n"
                + "    logicalMemberIdentifier: \"demo.Customer#noop\"\n"
                + "    interactionType: \"action_invocation\"\n"
                + "result:\n"
                + "  type: \"demo.Invoice\"\n"
                + "  id: \"456\"\n"
                + "---\n"
                + "command:\n"
                + "  majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"second-result-type\"\n"
                + "  username: \"sven\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"demo.Customer\"\n"
                + "      id: \"789\"\n"
                + "  member: !<ACT>\n"
                + "    logicalMemberIdentifier: \"demo.Customer#noop\"\n"
                + "    interactionType: \"action_invocation\"\n"
                + "result:\n"
                + "  type: \"demo.Invoice\"\n"
                + "  id: \"987\"\n";

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos).hasSize(2);
        Assertions.assertThat(importedCommandDtos.get(0).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456"));
        Assertions.assertThat(importedCommandDtos.get(1).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "987"));
    }

    @Test
    void from_yaml_for_replay_accepts_wrapped_export_shape_without_result_metadata() {
        CommandDto voidResult = command("void-result");
        String yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(voidResult, null)));

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId()).isEqualTo("void-result");
                    Assertions.assertThat(importedCommandDto.getResult()).isNull();
                });
    }

    @Test
    void from_yaml_for_replay_ignores_old_returned_object_field() {
        String yaml = "command:\n"
                + "  majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"old-field\"\n"
                + "  username: \"sven\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"demo.Customer\"\n"
                + "      id: \"123\"\n"
                + "  member: !<ACT>\n"
                + "    logicalMemberIdentifier: \"demo.Customer#noop\"\n"
                + "    interactionType: \"action_invocation\"\n"
                + "returnedObject:\n"
                + "  type: \"demo.Invoice\"\n"
                + "  id: \"456\"\n";

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId()).isEqualTo("old-field");
                    Assertions.assertThat(importedCommandDto.getResult()).isNull();
                });
    }

    @Test
    void from_yaml_for_replay_accepts_real_exported_result_type_yaml() throws IOException {
        String yaml = Files.readString(Path.of(
                "src/test/resources/org/apache/causeway/applib/util/schema/CommandDtoUtils_toYaml_fromYaml_Test.replay-export-with-result-type.yaml"));

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos).hasSize(4);
        Assertions.assertThat(importedCommandDtos.get(0).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.PetOwner", "237"));
        Assertions.assertThat(importedCommandDtos.get(1).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.PetOwner", "714"));
        Assertions.assertThat(importedCommandDtos.get(2).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.PetOwner", "714"));
        Assertions.assertThat(importedCommandDtos.get(3).getResult())
                .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.Pet", "723"));
    }

    @Test
    void from_yaml_for_replay_accepts_exported_result_type_with_empty_reference_parameter() {
        String yaml = "command:\n"
                + "  majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"6f0c92b0-a680-4877-af90-5b6177c3dc02\"\n"
                + "  timestamp: \"2026-06-06T06:37:45.219+00:00\"\n"
                + "  username: \"__system\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"petowner.PetOwner\"\n"
                + "      id: \"237\"\n"
                + "  member: !<ACT>\n"
                + "    parameters:\n"
                + "      parameter:\n"
                + "      - type: \"reference\"\n"
                + "        name: \"pet\"\n"
                + "      - localDateTime: \"2026-01-12T16:45:00.000\"\n"
                + "        type: \"localDateTime\"\n"
                + "        name: \"visitAt\"\n"
                + "    logicalMemberIdentifier: \"petowner.PetOwner#bookVisit\"\n"
                + "    interactionType: \"action_invocation\"\n"
                + "result:\n"
                + "  type: \"petowner.PetOwner\"\n"
                + "  id: \"237\"\n";

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId())
                            .isEqualTo("6f0c92b0-a680-4877-af90-5b6177c3dc02");
                    Assertions.assertThat(importedCommandDto.getResult())
                            .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.PetOwner", "237"));
                });
    }

    @Test
    void from_yaml_for_replay_accepts_exported_result_type_with_enum_parameter() {
        String yaml = "command:\n"
                + "  majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"f136b8af-dfeb-4d69-a94d-0354c6a4615a\"\n"
                + "  timestamp: \"2026-06-06T06:45:04.616+00:00\"\n"
                + "  username: \"sven\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"petowner.PetOwner\"\n"
                + "      id: \"714\"\n"
                + "  member: !<ACT>\n"
                + "    parameters:\n"
                + "      parameter:\n"
                + "      - string: \"Bob\"\n"
                + "        type: \"string\"\n"
                + "        name: \"name\"\n"
                + "      - enum:\n"
                + "          enumType: \"domainapp.modules.petowner.dom.pet.PetSpecies\"\n"
                + "          enumName: \"Cat\"\n"
                + "        type: \"enum\"\n"
                + "        name: \"species\"\n"
                + "    logicalMemberIdentifier: \"petowner.PetOwner#addPet\"\n"
                + "    interactionType: \"action_invocation\"\n"
                + "result:\n"
                + "  type: \"petowner.PetOwner\"\n"
                + "  id: \"714\"\n";

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId())
                            .isEqualTo("f136b8af-dfeb-4d69-a94d-0354c6a4615a");
                    Assertions.assertThat(importedCommandDto.getResult())
                            .isEqualTo(Bookmark.forLogicalTypeNameAndIdentifier("petowner.PetOwner", "714"));
                });
    }

    @Test
    void from_yaml_keeps_legacy_command_dto_shape() {
        CommandDto legacy = command("legacy-command");
        String yaml = CommandDtoUtils.toYaml(List.of(legacy));

        List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(commandDtos)
                .singleElement()
                .satisfies(command -> Assertions.assertThat(command.getInteractionId()).isEqualTo("legacy-command"));
    }

    @Test
    void from_yaml_for_replay_keeps_legacy_multi_document_command_dto_shape() {
        CommandDto legacy = command("legacy-command");
        String yaml = CommandDtoUtils.toYaml(List.of(legacy));

        List<CommandDtoUtils.ImportedCommandDto> importedCommandDtos = CommandDtoUtils.fromYamlForReplay(
                DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(importedCommandDtos)
                .singleElement()
                .satisfies(importedCommandDto -> {
                    Assertions.assertThat(importedCommandDto.getCommand().getInteractionId()).isEqualTo("legacy-command");
                    Assertions.assertThat(importedCommandDto.getResult()).isNull();
                });
    }

    @Test
    void from_yaml_for_replay_rejects_legacy_command_dto_list_shape() {
        String yaml = "- majorVersion: \"2\"\n"
                + "  minorVersion: \"0\"\n"
                + "  interactionId: \"list-command\"\n"
                + "  username: \"sven\"\n"
                + "  targets:\n"
                + "    oid:\n"
                + "    - type: \"demo.Customer\"\n"
                + "      id: \"123\"\n"
                + "  member: !<ACT>\n"
                + "    logicalMemberIdentifier: \"demo.Customer#noop\"\n"
                + "    interactionType: \"action_invocation\"\n";

        Throwable thrown = Assertions.catchThrowable(() -> CommandDtoUtils.fromYamlForReplay(DataSource.ofStringUtf8(yaml)));

        Assertions.assertThat(thrown).isNotNull();
    }

    @Test
    void localDate_roundtrips_as_date_only_without_timezone() throws Exception {
        TimeZone originalDefault = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar originalDate = datatypeFactory
                    .newXMLGregorianCalendarDate(2026, 7, 1, DatatypeConstants.FIELD_UNDEFINED);
            XMLGregorianCalendar originalDateTime = datatypeFactory
                    .newXMLGregorianCalendar("2026-07-01T10:15:30");
            XMLGregorianCalendar originalTime = datatypeFactory
                    .newXMLGregorianCalendar("10:15:30");

            CommandDto command = command("localdate-roundtrip-bug");

            ParamDto localDateParam = new ParamDto();
            localDateParam.setName("Invoice Date");
            localDateParam.setType(ValueType.LOCAL_DATE);
            localDateParam.setLocalDate(originalDate);

            ParamDto localDateTimeParam = new ParamDto();
            localDateTimeParam.setName("Invoice Date Time");
            localDateTimeParam.setType(ValueType.LOCAL_DATE_TIME);
            localDateTimeParam.setLocalDateTime(originalDateTime);

            ParamDto localTimeParam = new ParamDto();
            localTimeParam.setName("Invoice Time");
            localTimeParam.setType(ValueType.LOCAL_TIME);
            localTimeParam.setLocalTime(originalTime);

            ParamsDto params = new ParamsDto();
            params.getParameter().add(localDateParam);
            params.getParameter().add(localDateTimeParam);
            params.getParameter().add(localTimeParam);

            ActionDto action = new ActionDto();
            action.setLogicalMemberIdentifier("demo.Customer#invoice");
            action.setInteractionType(InteractionType.ACTION_INVOCATION);
            action.setParameters(params);
            command.setMember(action);

            String yaml = CommandDtoUtils.toYaml(List.of(command));
            List<CommandDto> roundtripped = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));

            XMLGregorianCalendar roundtrippedDate = ((ActionDto) roundtripped.get(0).getMember())
                    .getParameters()
                    .getParameter()
                    .get(0)
                    .getLocalDate();
            XMLGregorianCalendar roundtrippedDateTime = ((ActionDto) roundtripped.get(0).getMember())
                    .getParameters()
                    .getParameter()
                    .get(1)
                    .getLocalDateTime();
            XMLGregorianCalendar roundtrippedTime = ((ActionDto) roundtripped.get(0).getMember())
                    .getParameters()
                    .getParameter()
                    .get(2)
                    .getLocalTime();

            // Verify fixed behavior: local date/time values are emitted and roundtripped without timezone.
            Assertions.assertThat(yaml)
                    .contains("localDate: \"2026-07-01\"")
                    .contains("localDateTime: \"2026-07-01T10:15:30\"")
                    .contains("localTime: \"10:15:30\"")
                    .doesNotContain("localDate: \"2026-06-30T22:00:00.000+00:00\"")
                    .doesNotContain("localDateTime: \"2026-07-01T10:15:30.000+00:00\"")
                    .doesNotContain("localTime: \"1970-01-01T10:15:30.000+00:00\"");
            Assertions.assertThat(roundtrippedDate.toXMLFormat())
                    .isEqualTo(originalDate.toXMLFormat());
            Assertions.assertThat(roundtrippedDateTime.toXMLFormat())
                    .isEqualTo(originalDateTime.toXMLFormat());
            Assertions.assertThat(roundtrippedTime.toXMLFormat())
                    .isEqualTo(originalTime.toXMLFormat());
        } finally {
            TimeZone.setDefault(originalDefault);
        }
    }

    private static CommandDto command(final String interactionId) {
        CommandDto command = new CommandDto();
        command.setMajorVersion("2");
        command.setMinorVersion("0");
        command.setInteractionId(interactionId);
        command.setUsername("sven");

        OidDto oid = new OidDto();
        oid.setType("demo.Customer");
        oid.setId("123");
        OidsDto targets = new OidsDto();
        targets.getOid().add(oid);
        command.setTargets(targets);

        ActionDto action = new ActionDto();
        action.setLogicalMemberIdentifier("demo.Customer#noop");
        action.setInteractionType(InteractionType.ACTION_INVOCATION);
        command.setMember(action);
        return command;
    }
}
