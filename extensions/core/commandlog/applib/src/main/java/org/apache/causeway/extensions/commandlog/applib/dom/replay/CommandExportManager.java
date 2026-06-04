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
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandExportManager.LOGICAL_TYPE_NAME)
public final class CommandExportManager implements ViewModel, HasBaseline {

	public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    ReplayContext replayContext;

    @Inject
    public CommandExportManager(
            final String memento,
            final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), 50, Mode.EXPORT)),  replayContext);
    }

    public CommandExportManager(
            final State state,
            final ReplayContext replayContext) {
        this.baseline = state.timestamp;
        this.limit = state.limit;
        this.mode = state.mode;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        return "Command Export Manager";
    }

    @RequiredArgsConstructor
    public enum Mode {
        EXPORT("Export"),
        UNEXPORT("Unexport");

        private final String title;

        @ObjectSupport
        public String title() {
            return title;
        }


        Mode toggle() {
            return this == UNEXPORT ? EXPORT : UNEXPORT;
        }
    }


    @Property
    @PropertyLayout(describedAs = "Only commands after this timestamp are available")
    @Getter
    private java.sql.Timestamp baseline;

    @Property
    @PropertyLayout(describedAs = "Number of commands per page")
    @Getter
    private int limit;

    @Programmatic
    @Getter
    private Mode mode;



    @Override
    @Programmatic
    public CommandExportManager withBaseline(Timestamp baseline) {
        return new CommandExportManager(new State(baseline, this.limit, this.mode), replayContext);
    }

    @Programmatic
    public CommandExportManager withLimit(int limit) {
        return new CommandExportManager(new State(this.baseline, limit, this.mode), replayContext);
    }

    @Programmatic
    public CommandExportManager withMode(Mode mode) {
        return new CommandExportManager(new State(this.baseline, this.limit, mode), replayContext);
    }



    // -- NOT YET EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that can be exported"
    )
    public List<ReplayableCommand> getNotYetExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndCanBeExported(baseline, limit).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }
    @MemberSupport
    public boolean hideNotYetExported() {
        return this.mode == Mode.UNEXPORT;
    }
    @Programmatic
    public List<ReplayableCommand> getNotYetExportedPrevious() {
        return commandLogEntryRepository().findForegroundBeforeTimestampAndCanBeExported(baseline, limit).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }



    // -- HAVE BEEN EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that have been exported"
    )
    public List<ReplayableCommand> getExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndHasBeenExported(baseline, limit).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }
    @MemberSupport
    public boolean hideExported() {
        return this.mode == Mode.EXPORT;
    }

    @Programmatic
    private List<ReplayableCommand> getExportedPrevious() {
        return commandLogEntryRepository().findForegroundBeforeTimestampAndHasBeenExported(baseline, limit).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    public enum Direction {
        NEXT, PREVIOUS
    }

    @Programmatic
    public List<ReplayableCommand> commands(Direction direction) {
        switch (mode) {
            case EXPORT:
                switch (direction) {
                    case NEXT:
                        return getNotYetExported();
                    case PREVIOUS:
                    default:
                        return getNotYetExportedPrevious();
                }
            case UNEXPORT:
            default:
                switch (direction) {
                    case NEXT:
                        return getExported();
                    case PREVIOUS:
                    default:
                        return getExportedPrevious();
                }
        }
    }




    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return new State(baseline, limit, mode).toMemento();
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
        private final Mode mode;

        public static State parseMemento(String memento, State fallback) {
            if(memento == null || memento.isEmpty()) {
                return fallback;
            }
            try {
                String[] parts = memento.split(DELIMITER, -1);
                if(parts.length != 3) {
                    return fallback;
                }

                final Timestamp fallbackTimestamp = fallback != null
                        ? fallback.timestamp
                        : Timestamp.from(Instant.now());
                final int fallbackLimit = fallback != null ? fallback.limit : 0;
                final Mode fallbackMode = fallback != null ? fallback.mode : Mode.EXPORT;

                final Timestamp timestamp = fromString(parts[0], fallbackTimestamp);
                final int limit = parts[1].isBlank() ? fallbackLimit : Integer.parseInt(parts[1]);
                final Mode mode = parts[2].isBlank() ? fallbackMode : Mode.valueOf(parts[2]);

                return new State(timestamp, limit, mode);
            } catch (Exception e) {
                return fallback;
            }
        }

        public String toMemento() {
            return TimestampMarshallUtil.toString(timestamp) + DELIMITER + limit + DELIMITER + mode;
        }
    }

}
