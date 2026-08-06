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
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid list")
@Named(CommandManager.LOGICAL_TYPE_NAME)
public class CommandManager implements ViewModel, HasBaseline, HasLimit, CommandRecordingSuppressed,
        ReplayableCommandParticipantTracker {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandManager";
    public static final int DEFAULT_LIMIT = 100;

    private final Timestamp baseline;
    private final int limit;
    private final ReplayContext replayContext;

    public CommandManager(final State state, final ReplayContext replayContext) {
        this.baseline = state.timestamp();
        this.limit = normalizeLimit(state.limit());
        this.replayContext = replayContext;
    }

    public CommandManager(final Timestamp baseline, final int limit, final ReplayContext replayContext) {
        this(new State(baseline, limit), replayContext);
    }

    @Inject
    public CommandManager(final String memento, final ReplayContext replayContext) {
        this(State.parseMemento(memento, new State(
                replayContext.clockService().getClock().nowAsJavaSqlTimestamp(), DEFAULT_LIMIT)), replayContext);
    }

    @ObjectSupport public String title() {
        return "Command Manager";
    }

    @Property
    @PropertyLayout(describedAs = "Only foreground commands at or after this timestamp are available")
    @Override public Timestamp getBaseline() {
        return baseline;
    }

    @Property
    @PropertyLayout(describedAs = "Maximum number of commands in the sequence page")
    @Override public int getLimit() {
        return limit;
    }

    @Override
    @Programmatic
    public CommandManager withBaseline(final Timestamp newBaseline) {
        return new CommandManager(newBaseline, limit, replayContext);
    }

    @Override
    @Programmatic
    public CommandManager withLimit(final int newLimit) {
        return new CommandManager(baseline, newLimit, replayContext);
    }

    @Collection
    @CollectionLayout(describedAs = "Eligible foreground commands since the baseline, except excluded commands")
    public List<ReplayableCommand> getCommandsInSequence() {
        return replayContext.commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(entry -> entry.getReplayState() != ReplayState.EXCLUDED)
                .filter(this::isEligible)
                .map(this::wrap)
                .toList();
    }

    @Collection
    @CollectionLayout(sequence = "2", describedAs = "Eligible commands that have been excluded from replay")
    public List<ReplayableCommand> getExcluded() {
        return replayContext.commandLogEntryRepository()
                .findForegroundSinceTimestampAndWithReplayExcluded(baseline).stream()
                .filter(this::isEligible)
                .map(this::wrap)
                .toList();
    }

    @Collection
    @CollectionLayout(describedAs = "Imported commands that can be replayed or retried")
    public List<ReplayableCommand> getPendingOrFailed() {
        return replayContext.commandLogEntryRepository()
                .findForegroundSinceTimestampAndWithReplayPendingOrFailed(baseline).stream()
                .map(this::wrap)
                .toList();
    }

    @Collection
    @CollectionLayout(describedAs = "Commands recorded locally, exported historically, or replayed successfully")
    public List<ReplayableCommand> getRecordedOrReplayed() {
        return replayContext.commandLogEntryRepository()
                .findForegroundSinceTimestampAndWithReplayRecordedOrReplayed(baseline).stream()
                .filter(this::isEligible)
                .map(this::wrap)
                .toList();
    }

    @Override public String viewModelMemento() {
        return new State(baseline, limit).toMemento();
    }

    @Override
    @Programmatic
    public boolean isKnownParticipants(final CommandLogEntry commandLogEntry) {
        if (commandLogEntry == null || !replayContext.isRecordingSupportEnabled()) {
            return false;
        }
        var knownParticipants = new HashSet<Bookmark>();
        for (var entry : commandLogEntries()) {
            if (sameInteractionId(entry, commandLogEntry.getInteractionId())) {
                return validator().validateParticipants(entry, knownParticipants).isEmpty();
            }
            Optional.ofNullable(entry.getResult()).ifPresent(knownParticipants::add);
        }
        return false;
    }

    @Programmatic
    Optional<CommandKnownParticipantsValidator.Failure> validateKnownTargets(
            final List<? extends CommandLogEntry> commandLogEntries) {
        return replayContext.isRecordingSupportEnabled()
                ? validator().validate(baseline, commandLogEntries)
                : Optional.empty();
    }

    private List<CommandLogEntry> commandLogEntries() {
        return replayContext.commandLogEntryRepository().findForegroundSinceTimestamp(baseline, limit).stream()
                .filter(entry -> entry.getReplayState() != ReplayState.EXCLUDED)
                .filter(this::isEligible)
                .toList();
    }

    private CommandKnownParticipantsValidator validator() {
        return new CommandKnownParticipantsValidator(replayContext::isExportRoot);
    }

    private static boolean sameInteractionId(final CommandLogEntry entry, final UUID interactionId) {
        return entry != null
                && interactionId != null
                && interactionId.equals(entry.getInteractionId());
    }

    private boolean isEligible(final CommandLogEntry entry) {
        return ReplayableCommandEligibility.isEligible(entry, replayContext.applicationFeatureRepository());
    }

    private ReplayableCommand wrap(final CommandLogEntry entry) {
        return new ReplayableCommand(entry.getInteractionId(), replayContext, this);
    }

    private static int normalizeLimit(final int candidate) {
        return candidate > 0 ? candidate : DEFAULT_LIMIT;
    }

    public record State(Timestamp timestamp, int limit) {
        private static final String DELIMITER = "--";

        public static State parseMemento(final String memento, final State fallback) {
            if (memento == null || memento.isBlank()) {
                return fallback;
            }
            var delimiterAt = memento.indexOf(DELIMITER);
            var timestampPart = delimiterAt >= 0 ? memento.substring(0, delimiterAt) : memento;
            var limitPart = delimiterAt >= 0 ? memento.substring(delimiterAt + DELIMITER.length()) : "";
            var timestamp = TimestampMarshallUtil.fromString(timestampPart, fallback.timestamp());
            var limit = parseLimit(limitPart, fallback.limit());
            return new State(timestamp, limit);
        }

        private static int parseLimit(final String text, final int fallback) {
            if (text == null || text.isBlank()) {
                return fallback;
            }
            try {
                return normalizeLimit(Integer.parseInt(text));
            } catch (NumberFormatException ex) {
                return fallback;
            }
        }

        public String toMemento() {
            return TimestampMarshallUtil.toString(timestamp) + DELIMITER + normalizeLimit(limit);
        }
    }
}
