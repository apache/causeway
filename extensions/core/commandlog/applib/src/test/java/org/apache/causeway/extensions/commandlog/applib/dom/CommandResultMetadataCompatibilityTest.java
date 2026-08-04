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
package org.apache.causeway.extensions.commandlog.applib.dom;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils.CommandExportDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;

class CommandResultMetadataCompatibilityTest {

    @Test
    void recordedCommandAndResultCanFormPortableExportValue() {
        var commandDto = new CommandDto();
        commandDto.setInteractionId("interaction-1");
        var result = Bookmark.forLogicalTypeNameAndIdentifier("demoCustomer", "1");
        var commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getCommandDto()).thenReturn(commandDto);
        when(commandLogEntry.getResult()).thenReturn(result);

        var commandExport = CommandExportDto.of(
                commandLogEntry.getCommandDto(),
                commandLogEntry.getResult());

        assertThat(commandExport.getCommand()).isSameAs(commandDto);
        assertThat(commandExport.getResult().getType()).isEqualTo("demoCustomer");
        assertThat(commandExport.getResult().getId()).isEqualTo("1");
    }
}
