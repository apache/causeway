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

import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplayPendingBackgroundCommandsTest {

    @Test
    void emptyRepositoryDoesNotDisableReplay() {
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of());

        assertThat(ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(context(repository))).isFalse();
        assertThat(ReplayPendingBackgroundCommands.disableReason(context(repository))).isNull();
    }

    @Test
    void pendingRepositoryUsesSharedWaitMessage() {
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findBackgroundAndNotYetStarted()).thenReturn(List.of(mock(CommandLogEntry.class)));

        assertThat(ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(context(repository))).isTrue();
        assertThat(ReplayPendingBackgroundCommands.disableReason(context(repository)))
                .isEqualTo(ReplayPendingBackgroundCommands.WAIT_MESSAGE);
    }

    private static ReplayContext context(final CommandLogEntryRepository repository) {
        return new ReplayContext(null, null, null, repository, null, null, null);
    }
}
