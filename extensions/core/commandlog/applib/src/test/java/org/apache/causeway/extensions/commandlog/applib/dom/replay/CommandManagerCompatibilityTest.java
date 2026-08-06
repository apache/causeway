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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import jakarta.inject.Named;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandManagerCompatibilityTest {

    private static final Timestamp BASELINE = Timestamp.valueOf("2026-08-06 10:00:00");

    @Test
    void legacyMementosRemainLoadableAndNavigateToUnifiedManager() {
        var context = context();
        var legacyMemento = TimestampMarshallUtil.toString(BASELINE);
        var exportManager = new CommandExportManager(legacyMemento, context);
        var replayManager = new CommandReplayManager(legacyMemento, context);

        assertThat(exportManager.baseline()).isEqualTo(BASELINE);
        assertThat(replayManager.baseline()).isEqualTo(BASELINE);
        assertUnified(exportManager.new openCommandManager().act());
        assertUnified(replayManager.new openCommandManager().act());
        assertSafeSuppressed(CommandExportManager.openCommandManager.class);
        assertSafeSuppressed(CommandReplayManager.openCommandManager.class);
    }

    @Test
    void logicalTypeAndModuleRegistrationAreStable() {
        assertThat(CommandManager.class.getAnnotation(Named.class).value())
                .isEqualTo("causeway.ext.commandLog.CommandManager");
        assertThat(CommandExportManager.LOGICAL_TYPE_NAME)
                .isEqualTo("causeway.ext.commandLog.CommandExportManager");
        assertThat(CommandReplayManager.LOGICAL_TYPE_NAME)
                .isEqualTo("causeway.ext.commandLog.CommandReplayManager");

        var registered = List.of(CausewayModuleExtCommandLogApplib.class
                .getAnnotation(Import.class).value());
        assertThat(registered).contains(
                CommandManager.class,
                HasBaseline_changeBaseline.class,
                HasBaseline_previousHour.class,
                HasBaseline_nextHour.class,
                HasLimit_changeLimit.class,
                CommandExportManager.openCommandManager.class,
                CommandReplayManager.openCommandManager.class);
    }

    private static void assertUnified(final CommandManager manager) {
        assertThat(manager.getBaseline()).isEqualTo(BASELINE);
        assertThat(manager.getLimit()).isEqualTo(CommandManager.DEFAULT_LIMIT);
    }

    private static void assertSafeSuppressed(final Class<?> type) {
        var action = type.getAnnotation(Action.class);
        assertThat(action.semantics()).isEqualTo(SemanticsOf.SAFE);
        assertThat(action.commandPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.executionPublishing()).isEqualTo(Publishing.DISABLED);
    }

    private static ReplayContext context() {
        var clockService = mock(ClockService.class);
        var clock = mock(VirtualClock.class);
        when(clock.nowAsJavaSqlTimestamp()).thenReturn(Timestamp.from(Instant.parse("2026-08-06T12:00:00Z")));
        when(clockService.getClock()).thenReturn(clock);
        return new ReplayContext(null, null, null, mock(CommandLogEntryRepository.class), null,
                clockService, new ResultRemappingService(List.of()), null, null);
    }
}
