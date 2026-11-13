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

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.apache.causeway.applib.services.command.Command;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandLogEntryReplaySyncTest {

    @ParameterizedTest
    @EnumSource(value = ReplayState.class, names = {"PENDING", "OK", "FAILED", "EXCLUDED"})
    void replayLifecycleUpdatesTimingWithoutOverwritingImportedFacts(final ReplayState replayState) {
        var entry = mock(CommandLogEntry.class, CALLS_REAL_METHODS);
        var command = mock(Command.class);
        var startedAt = new Timestamp(1000);
        var completedAt = new Timestamp(2000);
        when(entry.getReplayState()).thenReturn(replayState);
        when(command.getStartedAt()).thenReturn(startedAt);
        when(command.getCompletedAt()).thenReturn(completedAt);

        entry.sync(command);

        verify(entry).setStartedAt(startedAt);
        verify(entry).setCompletedAt(completedAt);
        verify(entry, never()).setCommandDto(command.getCommandDto());
        verify(entry, never()).setTarget(command.getTarget());
        verify(entry, never()).setLogicalMemberIdentifier(command.getLogicalMemberIdentifier());
        verify(entry, never()).setResult(command.getResult());
        verify(entry, never()).setException(command.getException());
        verify(entry, never()).setUsername(command.getUsername());
        verify(entry, never()).setTimestamp(command.getTimestamp());
    }

    @Test
    void saveAnalysisRecordsFailedStateReasonAndException() {
        var entry = mock(CommandLogEntry.class, CALLS_REAL_METHODS);

        entry.saveAnalysis("Disabled: not allowed");

        verify(entry).setReplayState(ReplayState.FAILED);
        verify(entry).setReplayStateFailureReason("Disabled: not allowed");
        verify(entry).setException("Disabled: not allowed");
    }

    @Test
    void saveAnalysisWithNullMarksOkAndDoesNotRecordFailure() {
        var entry = mock(CommandLogEntry.class, CALLS_REAL_METHODS);

        entry.saveAnalysis(null);

        verify(entry).setReplayState(ReplayState.OK);
        verify(entry, never()).setReplayStateFailureReason(anyString());
        verify(entry, never()).setException(anyString());
    }
}
