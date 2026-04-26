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
import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.util.StreamUtils;

import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandDtoUtils_fromYaml_Approval_Test {

	@Test
    void unmarshals_all_date_time_datatypes_from_approved_toYaml_snapshot() throws IOException {
        String yaml = readApprovalSnapshot();

        List<CommandDto> commands = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));

        Assertions.assertThat(commands).singleElement().satisfies(command -> {
            Assertions.assertThat(command.getInteractionId()).isEqualTo("approval-datetime-marshalling");

            ActionDto action = (ActionDto) command.getMember();
            Assertions.assertThat(action.getLogicalMemberIdentifier())
                    .isEqualTo("demo.Customer#allDateTimeTypes");

            List<ParamDto> params = action.getParameters().getParameter();
            Assertions.assertThat(params).hasSize(6);

            ParamDto localDate = params.get(0);
            Assertions.assertThat(localDate.getType()).isEqualTo(ValueType.LOCAL_DATE);
            Assertions.assertThat(localDate.getLocalDate().toXMLFormat()).isEqualTo("2026-07-01");

            ParamDto localDateTime = params.get(1);
            Assertions.assertThat(localDateTime.getType()).isEqualTo(ValueType.LOCAL_DATE_TIME);
            Assertions.assertThat(localDateTime.getLocalDateTime().toXMLFormat()).isEqualTo("2026-07-01T10:15:30");

            ParamDto localTime = params.get(2);
            Assertions.assertThat(localTime.getType()).isEqualTo(ValueType.LOCAL_TIME);
            Assertions.assertThat(localTime.getLocalTime().toXMLFormat()).isEqualTo("10:15:30");

            ParamDto offsetDateTime = params.get(3);
            Assertions.assertThat(offsetDateTime.getType()).isEqualTo(ValueType.OFFSET_DATE_TIME);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getYear()).isEqualTo(2026);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getMonth()).isEqualTo(7);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getDay()).isEqualTo(1);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getHour()).isEqualTo(8);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getMinute()).isEqualTo(15);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getSecond()).isEqualTo(30);
            Assertions.assertThat(offsetDateTime.getOffsetDateTime().getTimezone()).isEqualTo(0);

            ParamDto offsetTime = params.get(4);
            Assertions.assertThat(offsetTime.getType()).isEqualTo(ValueType.OFFSET_TIME);
            Assertions.assertThat(offsetTime.getOffsetTime()).isNotNull();
            Assertions.assertThat(offsetTime.getOffsetTime().getHour()).isEqualTo(8);
            Assertions.assertThat(offsetTime.getOffsetTime().getMinute()).isEqualTo(15);
            Assertions.assertThat(offsetTime.getOffsetTime().getSecond()).isEqualTo(30);
            Assertions.assertThat(offsetTime.getOffsetTime().getTimezone()).isEqualTo(0);

            ParamDto zonedDateTime = params.get(5);
            Assertions.assertThat(zonedDateTime.getType()).isEqualTo(ValueType.ZONED_DATE_TIME);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getYear()).isEqualTo(2026);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getMonth()).isEqualTo(7);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getDay()).isEqualTo(1);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getHour()).isEqualTo(8);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getMinute()).isEqualTo(15);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getSecond()).isEqualTo(30);
            Assertions.assertThat(zonedDateTime.getZonedDateTime().getTimezone())
                    .isEqualTo(0);
        });
    }

    private String readApprovalSnapshot() throws IOException {
        String path = CommandDtoUtils_toYaml_Approval_Test.class.getSimpleName() + ".marshals_all_date_time_datatypes.approved.txt";
        InputStream stream = CommandDtoUtils_toYaml_Approval_Test.class.getResourceAsStream(path);
        return StreamUtils.copyToString(stream, java.nio.charset.StandardCharsets.UTF_8);
    }
	
}
