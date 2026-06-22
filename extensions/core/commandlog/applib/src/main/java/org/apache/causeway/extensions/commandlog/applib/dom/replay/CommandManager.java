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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;

import org.jspecify.annotations.NonNull;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.TimestampMarshallUtil.fromString;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandManager.LOGICAL_TYPE_NAME)
@RequiredArgsConstructor
public class CommandManager
        implements ViewModel, HasBaseline, HasLimit, CommandRecordingSuppressed, ReplayableCommandParticipantTracker {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    final ReplayContext replayContext;
    ReplayContext replayContext() {
        return replayContext;
    }

    public CommandManager(
            final State state,
            final ReplayContext replayContext) {
        this.baseline = state.timestamp;
        this.limit = state.limit;
        this.replayContext = replayContext;
    }


    @Inject
    public CommandManager(
            final String memento,
            final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), 50)),  replayContext);
    }

    @Override
    public String viewModelMemento() {
        return new State(baseline, limit).toMemento();
    }


    @ObjectSupport
    public String title() {
        return "Command Manager";
    }



    @Property
    @PropertyLayout(describedAs = "Only commands after this timestamp are available")
    @Getter java.sql.Timestamp baseline;

    @Override
    @Programmatic
    public CommandManager withBaseline(final Timestamp baseline) {
        return new CommandManager(new State(baseline, this.limit), replayContext);
    }



    @Property
    @PropertyLayout(describedAs = "Number of commands per page")
    @Getter int limit;

    @Override
    @Programmatic
    public CommandManager withLimit(final int limit) {
        return new CommandManager(new State(this.baseline, limit), replayContext);
    }



    @Collection
    @CollectionLayout(
            describedAs = "Commands since the baseline (except those that have been excluded)."
    )
    public List<ReplayableCommand> getCommandsInSequence() {
        ReplayableCommandParticipantTracker.putTrackerOnScratchpad(this, replayContext.scratchpad());
        return commandLogEntries().stream()
                .filter(this::isDoOp)
                .map(this::replayableCommandFor)
                .collect(Collectors.toList());
    }

    @Programmatic
    public Stream<ReplayableCommand> streamCommandsInSequence() {
        return getCommandsInSequence().stream();
    }


    @Collection
    @CollectionLayout(
            sequence = "2",
            describedAs = "Commands since the baseline that have been excluded"
    )
    public List<ReplayableCommand> getExcluded() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndWithReplayExcluded(baseline)
                .stream()
                .filter(this::isDoOp)
                .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext))
                .collect(Collectors.toList());
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
        return commandLogEntryRepository().findForegroundSinceTimestampAndWithReplayPendingOrFailed(baseline)
                .stream()
                .map(entry -> new ReplayableCommand(entry.getInteractionId(), replayContext));
    }

    long sizePendingOrFailed() {
        return streamPendingOrFailed().count();
    }



    // -- OK OR EXCLUDE

    @Collection
    @CollectionLayout(
            describedAs = "Commands that have been executed successfully, " +
                    "either directly as a recording or imported and replayed."
    )
    public List<ReplayableCommand> getRecordedOrReplayed() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndWithReplayUndefinedOrOk(baseline).stream()
                .filter(this::isDoOp)
                .map(entry->new ReplayableCommand(entry.getInteractionId(), replayContext))
                .collect(Collectors.toList());
    }



    /**
     * The sequence of {@link CommandLogEntry}s to be evaluated, specifically with respect to having known participants.
     */
    @Programmatic
    List<CommandLogEntry> commandLogEntries() {
        return commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(this::isDoOp)
                .filter(entry -> entry.getReplayState().isNotExcluded())
                .collect(Collectors.toList());
    }


    // -- HELPERS

    private boolean isDoOp(final CommandLogEntry entry) {
        return ReplayableCommand.Util.isDoOp(entry, replayContext.specificationLoader());
    }

    private static boolean isExcludedCommand(final CommandLogEntry entry) {
        return entry != null && entry.getReplayState().isExcluded();
    }


    CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }


    @Programmatic
    Optional<CommandKnownParticipantsValidator.Failure> validateKnownTargets(
            final List<CommandLogEntry> commandLogEntries) {
        return replayContext.isRecordingSupportEnabled()
                ? validator().validate(baseline, commandLogEntries)
                : Optional.empty();
    }

    @Override
    @Programmatic
    public boolean isKnownParticipants(final CommandLogEntry commandLogEntry) {
        if (commandLogEntry == null || !replayContext.isRecordingSupportEnabled()) {
            return false;
        }
        return validator().validateParticipants(commandLogEntry, knownParticipantsAsOf(commandLogEntry.getInteractionId())).isEmpty();
    }

    @Programmatic
    Set<Bookmark> knownParticipantsAsOf(final UUID interactionId) {
        final Set<Bookmark> knownParticipants = new HashSet<>();
        for (final CommandLogEntry entry : commandLogEntries().stream()
                .sorted()   // by timestamp
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

    ReplayableCommand replayableCommandFor(final CommandLogEntry entry) {
        return new ReplayableCommand(entry.getInteractionId(), replayContext);
    }

    CommandKnownParticipantsValidator validator() {
        return new CommandKnownParticipantsValidator(this::isDomainServiceOrReferenceData);
    }

    private boolean isDomainServiceOrReferenceData(final Bookmark bookmark) {
        return replayContext.isDomainService(bookmark)
                || CommandReplayReferenceDataService.isReferenceData(replayContext.commandReplayReferenceDataServices(), bookmark);
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
