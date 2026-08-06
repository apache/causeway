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
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplayableCommandReplayStateTest {

    @ParameterizedTest
    @MethodSource("states")
    void replayAndExclusionUseIndependentStatePredicates(
            final ReplayState state,
            final boolean replayable,
            final boolean excludable) {
        var id = UUID.randomUUID();
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(id);
        when(entry.getReplayState()).thenReturn(state);
        when(entry.getCommandDto()).thenReturn(new CommandDto());
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(id)).thenReturn(Optional.of(entry));
        var context = new ReplayContext(null, null, null, repository, null, null,
                new ResultRemappingService(List.of()), null, null);
        var command = new ReplayableCommand(id, context);

        assertThat(command.disableReplayOrRetry() == null).isEqualTo(replayable);
        assertThat(command.disableExcludeFromReplay() == null).isEqualTo(excludable);
        assertThat(new ReplayableCommand_replayOrRetry(command).disableAct() == null).isEqualTo(replayable);
    }

    private static Stream<Arguments> states() {
        return Stream.of(
                Arguments.of(ReplayState.UNDEFINED, false, false),
                Arguments.of(ReplayState.EXPORTED, false, false),
                Arguments.of(ReplayState.PENDING, true, true),
                Arguments.of(ReplayState.OK, true, false),
                Arguments.of(ReplayState.FAILED, true, true),
                Arguments.of(ReplayState.EXCLUDED, false, false));
    }
}
