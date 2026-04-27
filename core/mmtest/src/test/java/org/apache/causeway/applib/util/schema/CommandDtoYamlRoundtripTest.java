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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

class CommandDtoYamlRoundtripTest {

    @Test
    void list() {
        var yaml = CommandDtoUtils.toYaml(List.of(commandDtoSample(), commandDtoSample()));
        var afterRoundtrip = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));
        assertEquals(yaml, CommandDtoUtils.toYaml(afterRoundtrip));
    }

    @Test
    void multiDoc() {
        var yaml = CommandDtoUtils.toMultiDocYaml(List.of(commandDtoSample(), commandDtoSample()));
        var afterRoundtrip = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));
        assertEquals(yaml, CommandDtoUtils.toMultiDocYaml(afterRoundtrip));
    }

    private CommandDto commandDtoSample() {

        var commandDto = new CommandDto();
        commandDto.setInteractionId(UUID.randomUUID().toString());
        commandDto.setMajorVersion("2");
        commandDto.setMinorVersion("0");
        commandDto.setUsername("sven");

        long instant = Instant.now().toEpochMilli();

        var timestamp1 = new Timestamp(instant);
        var timestamp2 = new Timestamp(instant + 1234L);
        var timings = CommandDtoUtils.timingsFor(commandDto);
        timings.setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp1));
        timings.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp2));
        commandDto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp1));

        var action = new ActionDto();
        commandDto.setMember(action);
        action.setInteractionType(InteractionType.ACTION_INVOCATION);
        action.setLogicalMemberIdentifier("demo.ActionCommandPublishingPage#changeNamePublished");

        var params = new ParamsDto();
        action.setParameters(params);

        var param1 = new ParamDto();
        params.getParameter().add(param1);

        var ref = new OidDto();
        param1.setReference(ref);
        param1.setName("entity");
        param1.setType(ValueType.REFERENCE);
        ref.setType("demo.ActionCommandPublishingEntity");
        ref.setId("63");

        var param2 = new ParamDto();
        params.getParameter().add(param2);

        param2.setString("Monica2");
        param2.setName("newName");
        param2.setType(ValueType.STRING);

        var targets = new OidsDto();
        commandDto.setTargets(targets);

        var target = new OidDto();
        targets.getOid().add(target);
        target.setType("demo.ActionCommandPublishingPage");
        target.setId("PAAAg94peru5Z0Cw2FOcp1qflAATk66cZj_bmXAeSadQ1nHY8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJVVEYtOCI_Pjxyb290Lz4K");

        return commandDto;
    }

}
