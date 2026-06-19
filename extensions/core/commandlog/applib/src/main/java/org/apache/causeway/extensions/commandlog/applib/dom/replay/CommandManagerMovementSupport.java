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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

class CommandManagerMovementSupport {

    static final long MINIMUM_GAP_MILLIS = 10L;
    static final long SQUASH_GAP_MILLIS = 1000L;

    private final CommandManager commandManager;
    private final ReplayContext replayContext;

    CommandManagerMovementSupport(
            final CommandManager commandManager,
            final ReplayContext replayContext) {
        this.commandManager = commandManager;
        this.replayContext = replayContext;
    }

    String disableAct() {
        if (!isRecordingSupportEnabled()) {
            return "Command movement requires command-log recording support to be enabled";
        }
        return commandManager.getCommandsForExport().isEmpty() ? "No commands in collection" : null;
    }

    String validateAct(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target) {
        if (selected == null || selected.isEmpty()) {
            return "Select at least one command to move";
        }
        if (target == null) {
            return "Select the command to move after";
        }

        final Set<UUID> selectedIds = interactionIds(selected);
        if (selectedIds.contains(target.interactionId())) {
            return "Cannot move commands after one of the selected commands";
        }

        final List<ReplayableCommand> availableCommands = commandManager.getCommandsForExport();
        final Set<UUID> availableIds = interactionIds(availableCommands);
        if (!availableIds.contains(target.interactionId())) {
            return "Target command is not available for export from the current baseline";
        }
        if (!availableIds.containsAll(selectedIds)) {
            return "Selected commands must be available for export from the current baseline";
        }
        return null;
    }

    List<ReplayableCommand> choicesTarget(final List<ReplayableCommand> selected) {
        return choicesTarget(selected, commandManager.getCommandsForExport());
    }

    private List<ReplayableCommand> choicesTarget(
            final List<ReplayableCommand> selected,
            final List<ReplayableCommand> availableCommands) {
        final Set<UUID> selectedIds = interactionIds(selected);
        if (selectedIds.isEmpty()) {
            return availableCommands;
        }
        final int firstSelectedIndex = firstSelectedIndex(availableCommands, selectedIds);
        final int lastSelectedIndex = lastSelectedIndex(availableCommands, selectedIds);
        if (firstSelectedIndex < 0 || lastSelectedIndex < 0) {
            return availableCommands.stream()
                    .filter(command -> !selectedIds.contains(command.interactionId()))
                    .collect(Collectors.toList());
        }
        return availableCommands.subList(lastSelectedIndex + 1, availableCommands.size());
    }

    List<ReplayableCommand> choicesSelected() {
        return commandManager.getCommandsForExport();
    }

    CommandManager move(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target,
            final boolean squashTimings) {
        final CommandLogEntry targetEntry = commandLogEntry(target).orElseThrow();
        final List<CommandLogEntry> selectedEntries = selected.stream()
                .map(this::commandLogEntry)
                .flatMap(Optional::stream)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        moveAfter(selectedEntries, targetEntry, squashTimings);
        return commandManager;
    }

    private boolean isRecordingSupportEnabled() {
        return replayContext.causewayConfiguration().getExtensions().getCommandLog().getRecordingSupport().isEnabled();
    }

    private void moveAfter(
            final List<CommandLogEntry> selectedEntries,
            final CommandLogEntry targetEntry,
            final boolean squashTimings) {
        final long gapMillis = squashTimings ? SQUASH_GAP_MILLIS : MINIMUM_GAP_MILLIS;
        Timestamp nextTimestamp = addMillis(targetEntry.getTimestamp(), gapMillis);
        Timestamp previousOriginalTimestamp = null;
        Timestamp previousNewTimestamp = null;

        for (final CommandLogEntry selectedEntry : selectedEntries) {
            final Timestamp originalTimestamp = selectedEntry.getTimestamp();
            final Timestamp newTimestamp;
            if (previousNewTimestamp == null) {
                newTimestamp = nextTimestamp;
            } else {
                final long originalGap = squashTimings || originalTimestamp == null || previousOriginalTimestamp == null
                        ? gapMillis
                        : originalTimestamp.getTime() - previousOriginalTimestamp.getTime();
                newTimestamp = addMillis(previousNewTimestamp, Math.max(originalGap, gapMillis));
            }
            setTimestamp(selectedEntry, newTimestamp);
            previousOriginalTimestamp = originalTimestamp;
            previousNewTimestamp = newTimestamp;
            nextTimestamp = addMillis(newTimestamp, gapMillis);
        }
    }

    private static Timestamp addMillis(
            final Timestamp timestamp,
            final long millis) {
        final Timestamp base = timestamp != null ? timestamp : new Timestamp(0L);
        return new Timestamp(base.getTime() + millis);
    }

    private static void setTimestamp(
            final CommandLogEntry entry,
            final Timestamp timestamp) {
        entry.setTimestamp(timestamp);
        updateCommandDtoTimestamp(entry, timestamp);
    }

    private static void updateCommandDtoTimestamp(
            final CommandLogEntry entry,
            final Timestamp timestamp) {
        final var commandDto = entry.getCommandDto();
        if (commandDto != null) {
            var commandDtoCopy = CommandDtoUtils.copy(commandDto);
            commandDtoCopy.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
            entry.setCommandDto(commandDtoCopy);
        }
    }

    private Optional<CommandLogEntry> commandLogEntry(final ReplayableCommand command) {
        return command != null ? command.commandLogEntry() : Optional.empty();
    }

    private static int firstSelectedIndex(
            final List<ReplayableCommand> commands,
            final Set<UUID> selectedIds) {
        for (int i = 0; i < commands.size(); i++) {
            if (selectedIds.contains(commands.get(i).interactionId())) {
                return i;
            }
        }
        return -1;
    }

    private static int lastSelectedIndex(
            final List<ReplayableCommand> commands,
            final Set<UUID> selectedIds) {
        for (int i = commands.size() - 1; i >= 0; i--) {
            if (selectedIds.contains(commands.get(i).interactionId())) {
                return i;
            }
        }
        return -1;
    }

    private static Set<UUID> interactionIds(final List<ReplayableCommand> commands) {
        if (commands == null) {
            return java.util.Collections.emptySet();        }
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
