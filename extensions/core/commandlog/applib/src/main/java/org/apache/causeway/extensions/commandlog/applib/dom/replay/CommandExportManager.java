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

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.TimestampMarshallUtil.fromString;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.Data;
import lombok.Getter;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandExportManager.LOGICAL_TYPE_NAME)
public final class CommandExportManager implements ViewModel, HasBaseline, CommandRecordingSuppressed {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    ReplayContext replayContext;

    @Inject
    public CommandExportManager(
            final String memento,
            final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), 50)),  replayContext);
    }

    public CommandExportManager(
            final State state,
            final ReplayContext replayContext) {
        this.baseline = state.timestamp;
        this.limit = state.limit;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        return "Command Export Manager";
    }

    @Property
    @PropertyLayout(describedAs = "Only commands after this timestamp are available")
    @Getter
    private java.sql.Timestamp baseline;

    @Property
    @PropertyLayout(describedAs = "Number of commands per page")
    @Getter
    private int limit;

    @Override
    @Programmatic
    public CommandExportManager withBaseline(final Timestamp baseline) {
        return new CommandExportManager(new State(baseline, this.limit), replayContext);
    }

    @Programmatic
    public CommandExportManager withLimit(final int limit) {
        return new CommandExportManager(new State(this.baseline, limit), replayContext);
    }

    // -- COMMANDS

    @Collection
    @CollectionLayout(
            describedAs = "Commands since the baseline"
    )
    public List<ReplayableCommand> getCommands() {
        return commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .map(entry -> new ReplayableCommand(
                        entry.getInteractionId(),
                        replayContext))
                .collect(Collectors.toList());
    }

    @Programmatic
    public List<ReplayableCommand> getCommandsPrevious() {
        return commandLogEntryRepository().findForegroundBeforeTimestamp(baseline, limit).stream()
                .map(entry -> new ReplayableCommand(
                        entry.getInteractionId(),
                        replayContext))
                .collect(Collectors.toList());
    }

    public enum Direction {
        NEXT, PREVIOUS
    }

    @Programmatic
    public List<ReplayableCommand> commands(final Direction direction) {
        switch (direction) {
            case NEXT:
                return getCommands();
            case PREVIOUS:
            default:
                return getCommandsPrevious();
        }
    }

    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return new State(baseline, limit).toMemento();
    }

    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }

    @Data
    public static class State {
        private static final String DELIMITER = "--";

        private final Timestamp timestamp;
        private final int limit;

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
