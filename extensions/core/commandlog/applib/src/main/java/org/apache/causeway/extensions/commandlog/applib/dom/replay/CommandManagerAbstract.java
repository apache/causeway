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

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.TimestampMarshallUtil.fromString;

@RequiredArgsConstructor
public abstract class CommandManagerAbstract implements ViewModel, HasBaseline, HasLimit, CommandRecordingSuppressed {

    final ReplayContext replayContext;

    ReplayContext replayContext() {
        return replayContext;
    }

    @Property
    @PropertyLayout(describedAs = "Only commands after this timestamp are available")
    @Getter java.sql.Timestamp baseline;

    @Property
    @PropertyLayout(describedAs = "Number of commands per page")
    @Getter int limit;


    CommandManagerAbstract(
            final State state,
            final ReplayContext replayContext) {
        this.baseline = state.timestamp;
        this.limit = state.limit;
        this.replayContext = replayContext;
    }


    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return new State(baseline, limit).toMemento();
    }


    @Data
    public static class State {
        private static final String DELIMITER = "--";

        final Timestamp timestamp;
        final int limit;

        public static State from(Timestamp timestamp, final int limit) {
            return new State(timestamp, limit);
        }

        public static State parseMemento(final String memento, final State fallback) {
            if (memento == null || memento.isEmpty()) {
                return fallback;
            }
            try {
                final String[] parts = memento.split(DELIMITER, -1);
                if (parts.length != 2) {
                    return fallback;
                }

                final Timestamp fallbackTimestamp = fallback != null
                        ? fallback.timestamp
                        : Timestamp.from(Instant.now());
                final int fallbackLimit = fallback != null ? fallback.limit : 0;

                final Timestamp timestamp = fromString(parts[0], fallbackTimestamp);
                final int limit = parts[1].isBlank() ? fallbackLimit : Integer.parseInt(parts[1]);

                return new State(timestamp, limit);
            } catch (Exception e) {
                return fallback;
            }
        }


        public String toMemento() {
            return TimestampMarshallUtil.toString(timestamp) + DELIMITER + limit;
        }
    }
}
