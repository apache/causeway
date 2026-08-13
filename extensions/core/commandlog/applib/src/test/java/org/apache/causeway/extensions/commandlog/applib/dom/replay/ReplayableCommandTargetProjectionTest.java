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
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplayableCommandTargetProjectionTest {

    // an id longer than the 10-char abbreviation used by getTargetId()
    private static final String LONG_ID = "0123456789-abcdef";

    @Test
    void getTargetExposesTheFullRecordedBookmark() {
        var command = fixture(ReplayState.OK, null);

        assertThat(command.getTarget()).isEqualTo("demo.Customer:" + LONG_ID);
        // the abbreviated id used for table columns remains truncated (10 chars including the ellipsis)
        assertThat(command.getTargetId()).isEqualTo("0123456...");
    }

    @Test
    void titleUsesTheFullRecordedTargetNotTheAbbreviatedId() {
        var command = fixture(ReplayState.OK, null);

        assertThat(command.title()).contains("demo.Customer:" + LONG_ID);
        assertThat(command.title()).doesNotContain("0123456...");
    }

    @Test
    void recordedOnlyCommandExposesItsRecordedTargetAsActual() {
        // UNDEFINED (recorded-only, never replayed) is executed-ok, so with no remapping the actual target
        // falls back to the recorded target (MA-7).
        var command = fixture(ReplayState.UNDEFINED, null);

        assertThat(command.getActualTarget()).isEqualTo("demo.Customer:" + LONG_ID);
    }

    @Test
    void dtoViewIncludesTheRecordedResult() {
        var command = fixture(ReplayState.OK, Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "42"));

        assertThat(command.getDto().getAdoc()).contains("demo.Invoice", "42");
    }

    private static ReplayableCommand fixture(final ReplayState replayState, final Bookmark result) {
        var interactionId = UUID.randomUUID();
        var dto = command();
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getReplayState()).thenReturn(replayState);
        when(entry.getResult()).thenReturn(result);

        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(interactionId)).thenReturn(Optional.of(entry));

        var context = new ReplayContext(
                null, null, null, repository, null, null, new ResultRemappingService(List.of()));
        return new ReplayableCommand(interactionId, context);
    }

    private static CommandDto command() {
        var dto = new CommandDto();
        var targetOid = new OidDto();
        targetOid.setType("demo.Customer");
        targetOid.setId(LONG_ID);
        var targets = new OidsDto();
        targets.getOid().add(targetOid);
        dto.setTargets(targets);
        var member = new ActionDto();
        member.setLogicalMemberIdentifier("demo.Customer#placeOrder");
        dto.setMember(member);
        return dto;
    }
}
