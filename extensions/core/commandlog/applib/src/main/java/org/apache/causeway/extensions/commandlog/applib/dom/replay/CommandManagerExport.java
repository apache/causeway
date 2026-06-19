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
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandManagerExport.LOGICAL_TYPE_NAME)
public final class CommandManagerExport
        extends CommandManagerAbstract {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    @Inject
    public CommandManagerExport(
            final String memento,
            final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), 50)),  replayContext);
    }

    public CommandManagerExport(
            final State state,
            final ReplayContext replayContext) {
        super(state, replayContext);
    }

    @Override
    @Programmatic
    public CommandManagerExport withBaseline(final Timestamp baseline) {
        return new CommandManagerExport(new State(baseline, this.limit), replayContext);
    }

    @Override
    @Programmatic
    public CommandManagerExport withLimit(final int limit) {
        return new CommandManagerExport(new State(this.baseline, limit), replayContext);
    }

    // -- COMMANDS

    @Collection
    @CollectionLayout(
            describedAs = "Commands since the baseline"
    )
    public List<ReplayableCommand> getCommands() {
        ReplayableCommandParticipantTracker.putTrackerOnScratchpad(this, replayContext.scratchpad());
        return commandLogEntries().stream()
                .filter(this::isDoOp)
                .map(this::replayableCommandFor)
                .collect(Collectors.toList());
    }

    @Collection
    @CollectionLayout(
            sequence = "2",
            describedAs = "Commands since the baseline that have been excluded from the active export sequence"
    )
    public List<ReplayableCommand> getExcludedCommands() {
        return commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(CommandManagerExport::isExcludedCommand)
                .filter(this::isDoOp)
                .map(entry -> new ReplayableCommand(
                        entry.getInteractionId(),
                        replayContext))
                .collect(Collectors.toList());
    }

    @Programmatic
    public List<ReplayableCommand> getCommandsPrevious() {
        return commandLogEntryRepository().findForegroundBeforeTimestamp(baseline, limit).stream()
                .filter(this::isDoOp)
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


    @Programmatic
    List<CommandLogEntry> commandLogEntries() {
        return commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(CommandManagerExport::wasExecutedOk)
                .filter(this::isDoOp)
                .collect(Collectors.toList());
    }

    private boolean isDoOp(final CommandLogEntry entry) {
        return ReplayableCommand.Util.isDoOp(entry, replayContext.specificationLoader());
    }

    private static boolean wasExecutedOk(final CommandLogEntry entry) {
        return entry != null
                && (entry.getReplayState() == ReplayState.UNDEFINED
                || entry.getReplayState() == ReplayState.OK
        );
    }

    private static boolean isExcludedCommand(final CommandLogEntry entry) {
        return entry != null && entry.getReplayState() == ReplayState.EXCLUDED;
    }


    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }

}
