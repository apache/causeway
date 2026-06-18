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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManagerAbstract.State;
import org.junit.jupiter.api.Test;

class CommandManagerExportStateTest {

    @Test
    void roundtrip_toMemento_and_parseMemento() {
        final var timestamp = Timestamp.from(Instant.parse("2026-06-03T12:34:56.789Z"));
        final var state = new State(timestamp, 25);

        final String memento = state.toMemento();
        final State parsed = CommandManagerAbstract.State.parseMemento(memento, null);

        assertThat(memento).isEqualTo(TimestampMarshallUtil.toString(timestamp) + "--25");
        assertThat(parsed).isNotNull();
        assertThat(parsed.getTimestamp()).isEqualTo(timestamp);
        assertThat(parsed.getLimit()).isEqualTo(25);
    }

    @Test
    void parseMemento_null_returns_fallback() {
        final State fallback = fallbackState();

        final State parsed = CommandManagerAbstract.State.parseMemento(null, fallback);

        assertThat(parsed).isSameAs(fallback);
    }

    @Test
    void parseMemento_empty_returns_fallback() {
        final State fallback = fallbackState();

        final State parsed = CommandManagerAbstract.State.parseMemento("", fallback);

        assertThat(parsed).isSameAs(fallback);
    }

    @Test
    void parseMemento_invalid_part_count_returns_fallback() {
        final State fallback = fallbackState();

        final State parsed = CommandManagerAbstract.State.parseMemento("timestamp--limit--mode", fallback);

        assertThat(parsed).isSameAs(fallback);
    }

    @Test
    void parseMemento_blank_limit_uses_fallback_value() {
        final State fallback = fallbackState();
        final Timestamp timestamp = Timestamp.from(Instant.parse("2026-06-01T00:00:00.000Z"));
        final String memento = TimestampMarshallUtil.toString(timestamp) + "--";

        final State parsed = CommandManagerAbstract.State.parseMemento(memento, fallback);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getTimestamp()).isEqualTo(timestamp);
        assertThat(parsed.getLimit()).isEqualTo(fallback.getLimit());
    }

    @Test
    void parseMemento_invalid_limit_returns_fallback() {
        final State fallback = fallbackState();
        final String memento = TimestampMarshallUtil.toString(fallback.getTimestamp()) + "--abc";

        final State parsed = CommandManagerAbstract.State.parseMemento(memento, fallback);

        assertThat(parsed).isSameAs(fallback);
    }

    @Test
    void parseMemento_invalid_timestamp_uses_fallback_timestamp() {
        final State fallback = fallbackState();

        final State parsed = CommandManagerAbstract.State.parseMemento("not-a-timestamp--10", fallback);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getTimestamp()).isEqualTo(fallback.getTimestamp());
        assertThat(parsed.getLimit()).isEqualTo(10);
    }

    @Test
    void parseMemento_legacy_delimiter_returns_fallback() {
        final State fallback = fallbackState();
        final String memento = TimestampMarshallUtil.toString(fallback.getTimestamp()) + "|10";

        final State parsed = CommandManagerAbstract.State.parseMemento(memento, fallback);

        assertThat(parsed).isSameAs(fallback);
    }

    @Test
    void parseMemento_invalid_and_null_fallback_returns_null() {
        final State parsed = CommandManagerAbstract.State.parseMemento("not-two-parts", null);

        assertThat(parsed).isNull();
    }

    private static State fallbackState() {
        return new State(
                Timestamp.from(Instant.parse("2026-05-30T01:02:03.004Z")),
                77);
    }
}
