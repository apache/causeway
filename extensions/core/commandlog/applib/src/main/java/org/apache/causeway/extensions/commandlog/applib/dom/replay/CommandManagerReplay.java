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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import org.jspecify.annotations.NonNull;

import lombok.Getter;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid circle-play")
@Named(CommandManagerReplay.LOGICAL_TYPE_NAME)
public final class CommandManagerReplay
        extends CommandManagerAbstract {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    @Inject
    public CommandManagerReplay(
            final String memento,
            final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), 50)),  replayContext);
    }

    CommandManagerReplay(
            final State state,
            final ReplayContext replayContext) {
        super(state, replayContext);
    }

    @ObjectSupport public String title() {
        return "Command Replay Manager";
    }


    @Override
    @Programmatic
    public CommandManagerReplay withBaseline(final Timestamp baseline) {
        return new CommandManagerReplay(new State(baseline, this.limit), replayContext);
    }

    @Override
    @Programmatic
    public CommandManagerReplay withLimit(final int limit) {
        return new CommandManagerReplay(new State(this.baseline, limit), replayContext);
    }


    // -- PENDING OR FAILED

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that can be either replayed (if PENDING) or retried (if FAILED)"
    )
    public List<ReplayableCommand> getPendingOrFailed() {
        return streamPendingOrFailed()
            .collect(Collectors.toList());
    }

    @NonNull Stream<ReplayableCommand> streamPendingOrFailed() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndWithReplayPendingOrFailed(baseline).stream()
                .filter(this::isReplayable)
                .map(entry -> new ReplayableCommand(
                        entry.getInteractionId(),
                        replayContext));
    }

    long sizePendingOrFailed() {
        return streamPendingOrFailed().count();
    }


    // -- OK OR EXCLUDE

    @Collection
    @CollectionLayout(
            describedAs = "Imported Commands that were either replayed with success (replayState=OK) "
                    + "or marked to be excluded from replay (replayState=EXCLUDE)"
    )
    public List<ReplayableCommand> getSucceededOrExcluded() {
        return commandLogEntryRepository().findSinceAndWithReplayOkOrExcluded(baseline).stream()
            .filter(this::isReplayable)
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    // -- HELPER
    private boolean isReplayable(final CommandLogEntry entry) {
        return ReplayableCommandEligibility.isReplayable(entry, replayContext.specificationLoader());
    }

    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }
}
