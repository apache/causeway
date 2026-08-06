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
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

final class CommandManagerMovementSupport {

    private static final Duration MINIMUM_GAP = Duration.ofSeconds(1);

    private final CommandManager commandManager;

    CommandManagerMovementSupport(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    String disableAct() {
        if (!commandManager.replayContext().isRecordingSupportEnabled()) {
            return recordingRequired();
        }
        return commandManager.getCommandsInSequence().isEmpty() ? "No commands in sequence" : null;
    }

    String validateAct(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target) {
        if (!commandManager.replayContext().isRecordingSupportEnabled()) {
            return recordingRequired();
        }
        return prepare(selected, target).failure();
    }

    List<ReplayableCommand> choicesSelected() {
        return commandManager.getCommandsInSequence();
    }

    List<ReplayableCommand> choicesTarget(final List<ReplayableCommand> selected) {
        final Set<java.util.UUID> selectedIds = selected == null
                ? Set.of()
                : selected.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(ReplayableCommand::interactionId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
        return choicesSelected().stream()
                .filter(command -> !selectedIds.contains(command.interactionId()))
                .toList();
    }

    CommandManager move(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target,
            final boolean squashTimings) {
        if (!commandManager.replayContext().isRecordingSupportEnabled()) {
            throw new org.apache.causeway.applib.exceptions.RecoverableException(recordingRequired());
        }
        final var movement = prepare(selected, target);
        if (movement.failure() != null) {
            throw new org.apache.causeway.applib.exceptions.RecoverableException(movement.failure());
        }

        final var selectedEntries = movement.selection().entries();
        final var originalTimestamps = selectedEntries.stream()
                .map(CommandLogEntry::getTimestamp)
                .toList();
        var previousNew = Timestamp.from(movement.targetEntry().getTimestamp().toInstant().plus(MINIMUM_GAP));
        Timestamp previousOriginal = null;
        for (var index = 0; index < selectedEntries.size(); index++) {
            final var original = originalTimestamps.get(index);
            final Timestamp replacement;
            if (index == 0) {
                replacement = previousNew;
            } else {
                final var gap = squashTimings
                        ? MINIMUM_GAP
                        : qualifyingGap(previousOriginal, original);
                replacement = Timestamp.from(previousNew.toInstant().plus(gap));
                previousNew = replacement;
            }
            updateTimestamp(selectedEntries.get(index), replacement);
            previousOriginal = original;
        }
        return commandManager;
    }

    private Movement prepare(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target) {
        if (selected == null || selected.isEmpty()) {
            return Movement.failure("Select at least one command to move");
        }
        if (target == null || target.interactionId() == null) {
            return Movement.failure("Select the command to move after");
        }

        final var available = commandManager.getCommandsInSequence();
        final var selectedIds = selected.stream()
                .filter(java.util.Objects::nonNull)
                .map(ReplayableCommand::interactionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (selectedIds.contains(target.interactionId())) {
            return Movement.failure("Cannot move commands after one of the selected commands");
        }
        final var targetCommand = available.stream()
                .filter(command -> target.interactionId().equals(command.interactionId()))
                .findFirst();
        if (targetCommand.isEmpty()) {
            return Movement.failure("Target command must be active in the current manager sequence");
        }

        final var selection = CommandManagerWorkflowSupport.resolveSelection(
                selected,
                available,
                "Select at least one command to move",
                "Selected commands must be active commands from the current manager sequence");
        if (!selection.isValid()) {
            return Movement.failure(selection.failure());
        }
        final var targetEntry = targetCommand.get().commandLogEntry();
        if (targetEntry.isEmpty() || targetEntry.get().getTimestamp() == null) {
            return Movement.failure("Target command is no longer available with a timestamp");
        }
        return new Movement(selection, targetEntry.get(), null);
    }

    private static Duration qualifyingGap(
            final Timestamp previous,
            final Timestamp current) {
        if (previous == null || current == null) {
            return MINIMUM_GAP;
        }
        final var gap = Duration.between(previous.toInstant(), current.toInstant());
        return gap.compareTo(MINIMUM_GAP) >= 0 ? gap : MINIMUM_GAP;
    }

    private static void updateTimestamp(
            final CommandLogEntry entry,
            final Timestamp timestamp) {
        entry.setTimestamp(timestamp);
        final var commandDto = entry.getCommandDto();
        if (commandDto != null) {
            final var copy = CommandDtoUtils.copy(commandDto);
            copy.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
            entry.setCommandDto(copy);
        }
    }

    private static String recordingRequired() {
        return "Command movement requires command-log recording support to be enabled";
    }

    private record Movement(
            CommandManagerWorkflowSupport.Selection selection,
            CommandLogEntry targetEntry,
            String failure) {

        static Movement failure(final String message) {
            return new Movement(null, null, message);
        }
    }
}
