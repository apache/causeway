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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

class CommandManager_Replay_importCommandsTest {

    @Test
    void imports_command_export_result_as_command_log_result() {
        CommandDto command = command("with-result");
        Bookmark result = Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "456");
        String yaml = CommandDtoUtils.toYamlExport(List.of(
                CommandDtoUtils.CommandExportDto.of(command, result)));
        Blob commandsYaml = new Blob("commands.yaml", "text", "yaml", yaml.getBytes(StandardCharsets.UTF_8));

        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        CommandLogEntryRepository commandLogEntryRepository = mock(CommandLogEntryRepository.class);
        when(commandLogEntryRepository.saveForReplay(any(CommandDto.class))).thenReturn(commandLogEntry);

        CommandManagerReplay_importCommands importCommands = new CommandManagerReplay_importCommands(null);
        importCommands.replayContext = ReplayContext.builder().commandLogEntryRepository(commandLogEntryRepository).build();

        importCommands.act(commandsYaml, false);

        verify(commandLogEntryRepository).saveForReplay(any(CommandDto.class));
        verify(commandLogEntry).setResult(result);
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
