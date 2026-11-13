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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayableCommandExportTest {

    @Test
    void exportsSingleCommandAsResultBearingYamlWithoutMutatingState() {
        var interactionId = UUID.randomUUID();
        var dto = command(interactionId);
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getReplayState()).thenReturn(ReplayState.OK);
        when(entry.getResult()).thenReturn(Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "42"));

        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(interactionId)).thenReturn(Optional.of(entry));
        var context = new ReplayContext(
                null, null, null, repository, null, null, new ResultRemappingService(List.of()));
        var replayableCommand = new ReplayableCommand(interactionId, context);

        var clob = new ReplayableCommand_export(replayableCommand).act("myexport", false);

        assertThat(clob.name()).isEqualTo("myexport.yaml");
        // the exported YAML carries the recorded result envelope (not just the raw command DTO)
        assertThat(clob.asString()).contains("demo.Invoice", "42");
        // exporting does not mutate replay state
        verify(entry, org.mockito.Mockito.never()).setReplayState(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void exportIsDisabledWhenThereIsNoCommandDto() {
        var interactionId = UUID.randomUUID();
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(null);

        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(interactionId)).thenReturn(Optional.of(entry));
        var context = new ReplayContext(
                null, null, null, repository, null, null, new ResultRemappingService(List.of()));
        var replayableCommand = new ReplayableCommand(interactionId, context);

        assertThat(new ReplayableCommand_export(replayableCommand).disableAct())
                .isEqualTo("No command DTO to export.");
    }

    private static CommandDto command(final UUID interactionId) {
        var dto = new CommandDto();
        dto.setInteractionId(interactionId.toString());
        var targetOid = new OidDto();
        targetOid.setType("demo.Customer");
        targetOid.setId("1");
        var targets = new OidsDto();
        targets.getOid().add(targetOid);
        dto.setTargets(targets);
        return dto;
    }
}
