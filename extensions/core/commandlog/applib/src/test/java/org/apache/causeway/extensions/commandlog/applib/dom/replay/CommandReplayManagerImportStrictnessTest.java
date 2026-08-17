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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.junit.jupiter.api.Test;

class CommandReplayManagerImportStrictnessTest {

    @Test
    void malformedYamlIsRejectedAndNothingIsImported() {
        var repository = mock(CommandLogEntryRepository.class);
        var manager = new CommandReplayManager("2026-08-06T10:00:00Z", context(repository));

        // strict decode throws on unparseable input (rather than silently importing nothing); the exact
        // exception type is the underlying parser/decoder failure, so we only require that it is raised.
        assertThatThrownBy(() -> manager.new importCommands().act(blob("not: [valid"), false))
                .isInstanceOf(RuntimeException.class);
        verify(repository, never()).saveForReplay(any(CommandDto.class));
    }

    @Test
    void validLegacyMultiDocumentStreamImportsThroughStrictDecoder() {
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.saveForReplay(any(CommandDto.class))).thenReturn(mock(CommandLogEntry.class));
        var manager = new CommandReplayManager("2026-08-06T10:00:00Z", context(repository));
        var yaml = CommandDtoUtils.toMultiDocYaml(List.of(command()));

        manager.new importCommands().act(blob(yaml), false);

        verify(repository).saveForReplay(any(CommandDto.class));
    }

    private static ReplayContext context(final CommandLogEntryRepository repository) {
        var clockService = mock(ClockService.class);
        var clock = mock(VirtualClock.class);
        when(clock.nowAsJavaSqlTimestamp()).thenReturn(Timestamp.from(Instant.parse("2026-08-06T12:00:00Z")));
        when(clockService.getClock()).thenReturn(clock);
        return new ReplayContext(
                null, null, null, repository, null, clockService,
                new ResultRemappingService(List.of()), null, null);
    }

    private static CommandDto command() {
        var dto = new CommandDto();
        dto.setInteractionId(UUID.randomUUID().toString());
        var targetOid = new OidDto();
        targetOid.setType("demo.Customer");
        targetOid.setId("1");
        var targets = new OidsDto();
        targets.getOid().add(targetOid);
        dto.setTargets(targets);
        return dto;
    }

    private static Blob blob(final String yaml) {
        return Blob.of("commands.yaml", CommonMimeType.YAML, yaml.getBytes(StandardCharsets.UTF_8));
    }
}
