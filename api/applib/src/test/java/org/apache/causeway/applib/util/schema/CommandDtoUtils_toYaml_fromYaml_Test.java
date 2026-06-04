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

        Assertions.assertThat(bookmarkDto.getLogicalTypeName()).isEqualTo("demo.Invoice");
        Assertions.assertThat(bookmarkDto.getId()).isEqualTo("456");
    }

    @Test
    void bookmark_metadata_is_absent_for_void_result() {
        CommandDtoUtils.CommandExportDto exportDto = CommandDtoUtils.CommandExportDto.of(command("void-result"), null);

        Assertions.assertThat(exportDto.getReturnedObject()).isNull();
    }

    @Test
    void from_yaml_accepts_wrapped_export_shape_and_ignores_returned_object_metadata() {
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
    void from_yaml_keeps_legacy_command_dto_shape() {
        CommandDto legacy = command("legacy-command");
        String yaml = CommandDtoUtils.toYaml(List.of(legacy));

        List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(commandDtos)
                .singleElement()
                .satisfies(command -> Assertions.assertThat(command.getInteractionId()).isEqualTo("legacy-command"));
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
