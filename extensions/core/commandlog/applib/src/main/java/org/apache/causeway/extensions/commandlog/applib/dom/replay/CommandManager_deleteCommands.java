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

import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "excluded",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_deleteCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "excluded",
        sequence = "1.2",
        cssClass = "btn-danger",
        describedAs = "Permanently deletes selected excluded command-log entries; this cannot be undone")
@RequiredArgsConstructor
public class CommandManager_deleteCommands {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_deleteCommands> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(final List<ReplayableCommand> selected) {
        final var resolved = resolve(selected);
        if (!resolved.isValid()) {
            throw new RecoverableException(resolved.failure());
        }
        resolved.entries().forEach(commandManager.replayContext().repositoryService()::remove);
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        return choicesSelected().isEmpty() ? "No excluded commands" : null;
    }

    @MemberSupport
    public String validateAct(final List<ReplayableCommand> selected) {
        return resolve(selected).failure();
    }

    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandManager.getExcluded();
    }

    private CommandManagerWorkflowSupport.Selection resolve(final List<ReplayableCommand> selected) {
        return CommandManagerWorkflowSupport.resolveSelection(
                selected,
                choicesSelected(),
                "Select at least one command to delete",
                "Selected commands must be excluded commands from the current manager collection");
    }
}
