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
import java.util.Objects;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_importCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "pendingOrFailed",
        sequence = "1.1",
        cssClass = "btn-secondary",
        describedAs = "Imports result-bearing or legacy multi-document YAML for replay")
@RequiredArgsConstructor
public class CommandManager_importCommands {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_importCommands> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(
            @Parameter(fileAccept = ".yml,.yaml")
            final Blob commandsYaml,
            @ParameterLayout(describedAs = "Move the baseline to the oldest imported command timestamp")
            final boolean moveBaselineToOldest) {
        final var imported = CommandDtoUtils.fromYamlForReplay(commandsYaml.asDataSource());
        imported.forEach(value -> {
            final CommandLogEntry entry = commandManager.replayContext()
                    .commandLogEntryRepository().saveForReplay(value.getCommand());
            if (value.getResult() != null) {
                entry.setResult(value.getResult());
            }
        });
        if (!moveBaselineToOldest) {
            return commandManager;
        }
        return imported.stream()
                .map(value -> value.getCommand().getTimestamp())
                .filter(Objects::nonNull)
                .map(JavaSqlXMLGregorianCalendarMarshalling::toTimestamp)
                .filter(Objects::nonNull)
                .min(Timestamp::compareTo)
                .map(commandManager::withBaseline)
                .map(CommandManager.class::cast)
                .orElse(commandManager);
    }

    @MemberSupport public boolean defaultMoveBaselineToOldest() {
        return true;
    }
}
