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

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "notYetExported",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        domainEvent = CommandExportManager_moveCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "notYetExported", sequence = "1.2",
        describedAs = "Moves selected Commands after another command by retimestamping them. "
                + "The first moved command is placed 10ms after the target and subsequent moved commands preserve their original timing gaps."
)
public class CommandExportManager_moveCommands {

    private static final long MINIMUM_GAP_MILLIS = 10L;

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_moveCommands> {
    }

    private final CommandExportManager commandExportManager;

    @Inject CausewayConfiguration causewayConfiguration;

    public CommandExportManager_moveCommands(final CommandExportManager commandExportManager) {
        this.commandExportManager = commandExportManager;
    }

    @MemberSupport
    public CommandExportManager act(
            final List<ReplayableCommand> selected,
            @ParameterLayout(describedAs = "Command after which the selected commands will be moved.") final ReplayableCommand target) {
        final String validation = validateAct(selected, target);
        if (validation != null) {
            throw new RecoverableException(validation);
        }

        final CommandLogEntry targetEntry = commandLogEntry(target).orElseThrow();
        final List<CommandLogEntry> selectedEntries = selected.stream()
                .map(this::commandLogEntry)
                .flatMap(Optional::stream)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        moveAfter(selectedEntries, targetEntry);
        return commandExportManager;
    }

    @MemberSupport
    public String disableAct() {
        if (!isRecordingSupportEnabled()) {
            return "Command movement requires command-log recording support to be enabled";
        }
        return commandExportManager.getNotYetExported().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateAct(
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

        final Set<UUID> availableIds = interactionIds(commandExportManager.getNotYetExported());
        if (!availableIds.contains(target.interactionId())) {
            return "Target command is not available for export from the current baseline";
        }
        if (!availableIds.containsAll(selectedIds)) {
            return "Selected commands must be available for export from the current baseline";
        }
        return null;
    }

    @MemberSupport
    public List<ReplayableCommand> choicesTarget(final List<ReplayableCommand> selected) {
        final Set<UUID> selectedIds = interactionIds(selected);
        return commandExportManager.getNotYetExported().stream()
                .filter(command -> !selectedIds.contains(command.interactionId()))
                .collect(Collectors.toList());
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandExportManager.getNotYetExported();
    }

    private boolean isRecordingSupportEnabled() {
        return causewayConfiguration != null
                && causewayConfiguration.getExtensions().getCommandLog().getRecordingSupport().isEnabled();
    }

    private void moveAfter(
            final List<CommandLogEntry> selectedEntries,
            final CommandLogEntry targetEntry) {
        Timestamp nextTimestamp = addMillis(targetEntry.getTimestamp(), MINIMUM_GAP_MILLIS);
        Timestamp previousOriginalTimestamp = null;
        Timestamp previousNewTimestamp = null;

        for (final CommandLogEntry selectedEntry : selectedEntries) {
            final Timestamp originalTimestamp = selectedEntry.getTimestamp();
            final Timestamp newTimestamp;
            if (previousOriginalTimestamp == null) {
                newTimestamp = nextTimestamp;
            } else {
                final long originalGap = originalTimestamp != null
                        ? originalTimestamp.getTime() - previousOriginalTimestamp.getTime()
                        : MINIMUM_GAP_MILLIS;
                newTimestamp = addMillis(previousNewTimestamp, Math.max(originalGap, MINIMUM_GAP_MILLIS));
            }
            selectedEntry.setTimestamp(newTimestamp);
            updateCommandDtoTimestamp(selectedEntry, newTimestamp);
            previousOriginalTimestamp = originalTimestamp;
            previousNewTimestamp = newTimestamp;
            nextTimestamp = addMillis(newTimestamp, MINIMUM_GAP_MILLIS);
        }
    }

    private static Timestamp addMillis(
            final Timestamp timestamp,
            final long millis) {
        final Timestamp base = timestamp != null ? timestamp : new Timestamp(0L);
        return new Timestamp(base.getTime() + millis);
    }

    private static void updateCommandDtoTimestamp(
            final CommandLogEntry entry,
            final Timestamp timestamp) {
        if (entry.getCommandDto() != null) {
            entry.getCommandDto().setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
        }
    }

    private Optional<CommandLogEntry> commandLogEntry(final ReplayableCommand command) {
        return command != null ? command.commandLogEntry() : Optional.empty();
    }

    private static Set<UUID> interactionIds(final List<ReplayableCommand> commands) {
        if (commands == null) {
            return Set.of();
        }
        return commands.stream()
                .map(ReplayableCommand::interactionId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
