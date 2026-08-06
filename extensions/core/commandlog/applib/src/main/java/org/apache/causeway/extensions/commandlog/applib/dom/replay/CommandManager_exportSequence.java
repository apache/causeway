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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_exportSequence.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "commandsInSequence",
        sequence = "1.1",
        cssClassFa = "solid share-from-square",
        cssClass = "btn-primary",
        describedAs = "Exports commands with known participants as result-bearing YAML")
@RequiredArgsConstructor
public class CommandManager_exportSequence {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_exportSequence> { }

    private final CommandManager commandManager;

    @MemberSupport
    public Clob act(
            @ParameterLayout(describedAs = "File name prefix for the exported YAML")
            final String filenamePrefix,
            @ParameterLayout(describedAs = "Add the first exported command timestamp to the filename")
            final boolean filenameTimestamp,
            @ParameterLayout(describedAs = "Replace recorded bookmark identities with replay mappings")
            final boolean remapResults) {
        final List<ReplayableCommand> exportable = exportableCommands();
        final var exports = exportable.stream()
                .map(ReplayableCommand::commandLogEntry)
                .flatMap(Optional::stream)
                .map(entry -> CommandDtoUtils.CommandExportDto.of(
                        entry.getCommandDto(), entry.getResult()))
                .map(export -> remapResults
                        ? commandManager.replayContext().resultRemappingService().remapped(export)
                        : export)
                .toList();
        final String yaml = CommandDtoUtils.toYamlExport(exports);
        final String filename = filenamePrefix + timestampSuffix(exportable, filenameTimestamp);
        return Clob.of(filename, CommonMimeType.YAML, yaml);
    }

    @MemberSupport
    public String disableAct() {
        return exportableCommands().isEmpty()
                ? "No commands (with known participants) in this sequence."
                : null;
    }

    @MemberSupport public String defaultFilenamePrefix() {
        return "commands";
    }

    @MemberSupport public boolean defaultFilenameTimestamp() {
        return true;
    }

    @MemberSupport public boolean defaultRemapResults() {
        return false;
    }

    private List<ReplayableCommand> exportableCommands() {
        return commandManager.getCommandsInSequence().stream()
                .filter(ReplayableCommand::isKnownParticipants)
                .toList();
    }

    private static String timestampSuffix(
            final List<ReplayableCommand> exportable,
            final boolean enabled) {
        if (!enabled || exportable.isEmpty()) {
            return "";
        }
        return exportable.get(0).getTimestampIfAny()
                .map(timestamp -> Instant.from(timestamp).toString())
                .map(value -> "." + value.replaceAll("[^A-Za-z0-9._-]", "_"))
                .orElse("");
    }
}
