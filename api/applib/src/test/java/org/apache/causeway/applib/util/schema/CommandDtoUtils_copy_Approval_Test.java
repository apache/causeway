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

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.cmd.v2.CommandDto;

class CommandDtoUtils_copy_Approval_Test {

    @Test
    void marshals_command_copy() throws IOException {
        // read YAML sample from classpath (existing test resource)
        final String path = "CommandDtoUtils_copy_Approval_Test.marshals_command_copy.input.yaml";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            final String yaml = StreamUtils.copyToString(is, StandardCharsets.UTF_8);

            final List<CommandDto> commands = CommandDtoUtils.fromYaml(DataSource.ofStringUtf8(yaml));
            Assertions.assertThat(commands).isNotEmpty();

            final CommandDto original = commands.get(0);
            final CommandDto copy = CommandDtoUtils.copy(original);

            // ensure we actually created a different instance
            Assertions.assertThat(copy).isNotSameAs(original);

            // marshal the copy to its XML representation and approve
            final String xml = CommandDtoUtils.dtoMapper().toString(copy);
            Approvals.verify(xml);
        }
    }
}

