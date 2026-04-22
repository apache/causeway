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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.approvaltests.Approvals;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.springframework.util.StreamUtils;

class CommandDtoUtils_toYaml_Approval_Test {

    private static final DatatypeFactory DATATYPE_FACTORY = datatypeFactory();

    @Test
    void marshals_all_date_time_datatypes() {
        withDefaultTimeZone("UTC", () -> {
            String yaml = CommandDtoUtils.toYaml(List.of(commandWithAllDateTimeParams()));
            Approvals.verify(yaml);
        });
    }

    @Test
    void marshals_all_date_time_datatypes_when_default_timezone_is_cest() {
        withDefaultTimeZone("Europe/Paris", () -> {
            String yaml = CommandDtoUtils.toYaml(List.of(commandWithAllDateTimeParams()));
            try {
                Assertions.assertThat(yaml).isEqualTo(readApprovalSnapshot());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static CommandDto commandWithAllDateTimeParams() {
        CommandDto command = new CommandDto();
        command.setMajorVersion("2");
        command.setMinorVersion("0");
        command.setInteractionId("approval-datetime-marshalling");
        command.setUsername("approval-user");
        command.setTargets(targets("demo.Customer", "123"));
        command.setMember(actionWithAllDateTimeParams());
        return command;
    }

    private static void withDefaultTimeZone(final String zoneId, final _Runnable runnable) {
        TimeZone originalDefault = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        try {
            runnable.run();
        } finally {
            TimeZone.setDefault(originalDefault);
        }
    }

    @FunctionalInterface
    private interface _Runnable {
        void run();
    }

    private String readApprovalSnapshot() throws IOException {
        String path = getClass().getSimpleName() + ".marshals_all_date_time_datatypes.approved.txt";
        InputStream stream = getClass().getResourceAsStream(path);
        return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
    }

    private static ActionDto actionWithAllDateTimeParams() {
        ActionDto action = new ActionDto();
        action.setLogicalMemberIdentifier("demo.Customer#allDateTimeTypes");
        action.setInteractionType(InteractionType.ACTION_INVOCATION);

        ParamsDto params = new ParamsDto();
        params.getParameter().add(param("Local Date", ValueType.LOCAL_DATE, "2026-07-01"));
        params.getParameter().add(param("Local Date Time", ValueType.LOCAL_DATE_TIME, "2026-07-01T10:15:30"));
        params.getParameter().add(param("Local Time", ValueType.LOCAL_TIME, "10:15:30"));
        params.getParameter().add(param("Offset Date Time", ValueType.OFFSET_DATE_TIME, "2026-07-01T10:15:30+02:00"));
        params.getParameter().add(param("Offset Time", ValueType.OFFSET_TIME, "10:15:30+02:00"));
        params.getParameter().add(param("Zoned Date Time", ValueType.ZONED_DATE_TIME, "2026-07-01T10:15:30+02:00"));
        action.setParameters(params);
        return action;
    }

    private static ParamDto param(final String name, final ValueType type, final String lexicalValue) {
        ParamDto param = new ParamDto();
        param.setName(name);
        param.setType(type);

        XMLGregorianCalendar value = DATATYPE_FACTORY.newXMLGregorianCalendar(lexicalValue);
        switch (type) {
        case LOCAL_DATE:
            param.setLocalDate(value);
            break;
        case LOCAL_DATE_TIME:
            param.setLocalDateTime(value);
            break;
        case LOCAL_TIME:
            param.setLocalTime(value);
            break;
        case OFFSET_DATE_TIME:
            param.setOffsetDateTime(value);
            break;
        case OFFSET_TIME:
            param.setOffsetTime(value);
            break;
        case ZONED_DATE_TIME:
            param.setZonedDateTime(value);
            break;
        default:
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return param;
    }

    private static OidsDto targets(final String type, final String id) {
        OidDto oid = new OidDto();
        oid.setType(type);
        oid.setId(id);

        OidsDto targets = new OidsDto();
        targets.getOid().add(oid);
        return targets;
    }

    private static DatatypeFactory datatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize DatatypeFactory", ex);
        }
    }
}

