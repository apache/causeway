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

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;

import static org.assertj.core.api.Assertions.assertThat;

class CommandManagerStateTest {

    private static final Timestamp BASELINE = Timestamp.valueOf("2026-08-06 10:00:00");
    private static final Timestamp FALLBACK_BASELINE = Timestamp.valueOf("2026-08-06 12:00:00");
    private static final CommandManager.State FALLBACK =
            new CommandManager.State(FALLBACK_BASELINE, CommandManager.DEFAULT_LIMIT);

    @Test
    void canonicalMementoRoundTrips() {
        var state = new CommandManager.State(BASELINE, 50);

        assertThat(CommandManager.State.parseMemento(state.toMemento(), FALLBACK)).isEqualTo(state);
        assertThat(state.toMemento()).endsWith("--50");
    }

    @Test
    void timestampOnlyMementoUsesDefaultLimit() {
        var state = CommandManager.State.parseMemento(TimestampMarshallUtil.toString(BASELINE), FALLBACK);

        assertThat(state.timestamp()).isEqualTo(BASELINE);
        assertThat(state.limit()).isEqualTo(CommandManager.DEFAULT_LIMIT);
    }

    @Test
    void malformedComponentsFallBackIndependently() {
        var validTimestampInvalidLimit = CommandManager.State.parseMemento(
                TimestampMarshallUtil.toString(BASELINE) + "--not-a-number", FALLBACK);
        var invalidTimestampValidLimit = CommandManager.State.parseMemento("not-a-timestamp--25", FALLBACK);
        var nonPositiveLimit = CommandManager.State.parseMemento(
                TimestampMarshallUtil.toString(BASELINE) + "--0", FALLBACK);

        assertThat(validTimestampInvalidLimit).isEqualTo(new CommandManager.State(BASELINE, 100));
        assertThat(invalidTimestampValidLimit).isEqualTo(new CommandManager.State(FALLBACK_BASELINE, 25));
        assertThat(nonPositiveLimit).isEqualTo(new CommandManager.State(BASELINE, 100));
        assertThat(CommandManager.State.parseMemento("", FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void controlsPreserveTheOtherStateAndSuppressPublishing() {
        var manager = new CommandManager(BASELINE, 50, null);

        assertThat(((CommandManager) new HasBaseline_previousHour(manager).act()).getBaseline())
                .isEqualTo(Timestamp.valueOf("2026-08-06 09:00:00"));
        assertThat(((CommandManager) new HasBaseline_nextHour(manager).act()).getLimit()).isEqualTo(50);
        assertThat(((CommandManager) new HasBaseline_changeBaseline(manager)
                .act(FALLBACK_BASELINE)).getLimit()).isEqualTo(50);
        assertThat(((CommandManager) new HasLimit_changeLimit(manager).act(25)).getBaseline())
                .isEqualTo(BASELINE);
        assertThat(((CommandManager) manager.withLimit(0)).getLimit()).isEqualTo(100);

        assertSafeSuppressed(HasBaseline_previousHour.class);
        assertSafeSuppressed(HasBaseline_nextHour.class);
        assertSafeSuppressed(HasBaseline_changeBaseline.class);
        assertSafeSuppressed(HasLimit_changeLimit.class);
    }

    @Test
    void limitAboveMaximumIsCappedAndNonPositiveUsesDefault() {
        assertThat(new CommandManager(BASELINE, 500, null).getLimit()).isEqualTo(CommandManager.MAX_LIMIT);
        assertThat(new CommandManager(BASELINE, CommandManager.MAX_LIMIT, null).getLimit())
                .isEqualTo(CommandManager.MAX_LIMIT);
        assertThat(new CommandManager(BASELINE, 50, null).getLimit()).isEqualTo(50);
        assertThat(new CommandManager(BASELINE, 0, null).getLimit()).isEqualTo(CommandManager.DEFAULT_LIMIT);
    }

    @Test
    void changeLimitAcceptsOneToMaximumAndRejectsOutOfRange() {
        var changeLimit = new HasLimit_changeLimit(new CommandManager(BASELINE, 50, null));

        assertThat(changeLimit.validateNewLimit(1)).isNull();
        assertThat(changeLimit.validateNewLimit(CommandManager.MAX_LIMIT)).isNull();
        assertThat(changeLimit.validateNewLimit(0)).isNotNull();
        assertThat(changeLimit.validateNewLimit(CommandManager.MAX_LIMIT + 1)).isNotNull();
    }

    private static void assertSafeSuppressed(final Class<?> type) {
        var action = type.getAnnotation(Action.class);
        assertThat(action.semantics()).isEqualTo(SemanticsOf.SAFE);
        assertThat(action.commandPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.executionPublishing()).isEqualTo(Publishing.DISABLED);
    }
}
