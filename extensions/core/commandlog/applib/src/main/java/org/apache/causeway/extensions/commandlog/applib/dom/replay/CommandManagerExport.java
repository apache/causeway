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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandManagerExport.LOGICAL_TYPE_NAME)
public final class CommandManagerExport
        extends CommandManagerAbstract
        implements ReplayableCommandParticipantTracker {

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
        return activeCommandLogEntries().stream()
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
    Optional<CommandKnownParticipantsValidator.Failure> validateKnownTargets(
            final List<CommandLogEntry> selectedCommandLogEntries) {
        return isRecordingSupportEnabled()
                ? validator().validate(baseline, selectedCommandLogEntries)
                : Optional.empty();
    }

    @Override
    @Programmatic
    public boolean isKnownParticipants(final CommandLogEntry commandLogEntry) {
        if (commandLogEntry == null || !isRecordingSupportEnabled()) {
            return false;
        }
        return validator().validateParticipants(commandLogEntry, knownParticipantsAsOf(commandLogEntry.getInteractionId())).isEmpty();
    }

    @Programmatic
    Set<Bookmark> knownParticipantsAsOf(final UUID interactionId) {
        final Set<Bookmark> knownParticipants = new HashSet<>();
        for (final CommandLogEntry entry : activeCommandLogEntries().stream()
                .sorted()
                .collect(Collectors.toList())) {
            if (sameInteractionId(entry, interactionId)) {
                return knownParticipants;
            }
            Optional.ofNullable(entry.getResult())
                    .ifPresent(knownParticipants::add);
        }
        return knownParticipants;
    }

    private static boolean sameInteractionId(
            final CommandLogEntry entry,
            final UUID interactionId) {
        return entry != null
                && interactionId != null
                && interactionId.equals(entry.getInteractionId());
    }

    private boolean isRecordingSupportEnabled() {
        return replayContext.causewayConfiguration() != null
                && replayContext.causewayConfiguration().getExtensions().getCommandLog().getRecordingSupport().isEnabled();
    }

    @Programmatic
    List<CommandLogEntry> activeCommandLogEntries() {
        return commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(CommandManagerExport::isActiveCommand)
                .filter(this::isDoOp)
                .collect(Collectors.toList());
    }

    private boolean isDoOp(final CommandLogEntry entry) {
        return ReplayableCommand.Util.isDoOp(entry, replayContext.specificationLoader());
    }

    private ReplayableCommand replayableCommandFor(final CommandLogEntry entry) {
        return new ReplayableCommand(
                entry.getInteractionId(),
                replayContext);
    }

    private static boolean isActiveCommand(final CommandLogEntry entry) {
        return entry != null
                && (entry.getReplayState() == ReplayState.UNDEFINED
                || entry.getReplayState() == ReplayState.EXPORTED);
    }

    private static boolean isExcludedCommand(final CommandLogEntry entry) {
        return entry != null && entry.getReplayState() == ReplayState.EXCLUDED;
    }


    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }

}
