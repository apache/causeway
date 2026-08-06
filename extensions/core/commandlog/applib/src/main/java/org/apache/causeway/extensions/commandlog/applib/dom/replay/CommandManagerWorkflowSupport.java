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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

final class CommandManagerWorkflowSupport {

    private CommandManagerWorkflowSupport() { }

    static Selection resolveSelection(
            final List<ReplayableCommand> selected,
            final List<ReplayableCommand> available,
            final String emptyMessage,
            final String outsideMessage) {
        if (selected == null || selected.isEmpty()) {
            return Selection.failure(emptyMessage);
        }

        final var selectedIds = new LinkedHashSet<UUID>();
        for (var command : selected) {
            if (command == null || command.interactionId() == null) {
                return Selection.failure(outsideMessage);
            }
            if (!selectedIds.add(command.interactionId())) {
                return Selection.failure("The same command cannot be selected more than once");
            }
        }

        final var availableById = new LinkedHashMap<UUID, ReplayableCommand>();
        for (var command : available) {
            if (command != null && command.interactionId() != null) {
                availableById.putIfAbsent(command.interactionId(), command);
            }
        }
        if (!availableById.keySet().containsAll(selectedIds)) {
            return Selection.failure(outsideMessage);
        }

        final var commands = availableById.entrySet().stream()
                .filter(entry -> selectedIds.contains(entry.getKey()))
                .map(java.util.Map.Entry::getValue)
                .toList();
        final var entries = new ArrayList<CommandLogEntry>(commands.size());
        for (var command : commands) {
            final var entry = command.commandLogEntry();
            if (entry.isEmpty()) {
                return Selection.failure("A selected command is no longer available");
            }
            entries.add(entry.get());
        }
        return Selection.success(commands, entries);
    }

    record Selection(
            List<ReplayableCommand> commands,
            List<CommandLogEntry> entries,
            String failure) {

        static Selection success(
                final List<ReplayableCommand> commands,
                final List<CommandLogEntry> entries) {
            return new Selection(List.copyOf(commands), List.copyOf(entries), null);
        }

        static Selection failure(final String message) {
            return new Selection(List.of(), List.of(), message);
        }

        boolean isValid() {
            return failure == null;
        }
    }
}
