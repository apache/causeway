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

import java.util.Arrays;
import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "excluded",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_unexcludeCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "excluded",
        sequence = "1.1",
        cssClass = "btn-secondary",
        describedAs = "Restores selected excluded commands to a replay state")
@RequiredArgsConstructor
public class CommandManager_unexcludeCommands {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_unexcludeCommands> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(
            final List<ReplayableCommand> selected,
            final ReplayState replayState) {
        final var validation = validate(selected, replayState);
        if (validation.failure() != null) {
            throw new RecoverableException(validation.failure());
        }
        validation.selection().entries().forEach(entry -> entry.setReplayState(replayState));
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        if (!commandManager.replayContext().isRecordingSupportEnabled()) {
            return "Command restoration requires command-log recording support to be enabled";
        }
        return choicesSelected().isEmpty() ? "No excluded commands" : null;
    }

    @MemberSupport
    public String validateAct(
            final List<ReplayableCommand> selected,
            final ReplayState replayState) {
        return validate(selected, replayState).failure();
    }

    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandManager.getExcluded();
    }

    @MemberSupport
    public List<ReplayState> choicesReplayState() {
        return Arrays.stream(ReplayState.values())
                .filter(state -> state != ReplayState.EXCLUDED)
                .toList();
    }

    private Validation validate(
            final List<ReplayableCommand> selected,
            final ReplayState replayState) {
        if (replayState == null || replayState == ReplayState.EXCLUDED) {
            return new Validation(null, "Choose a replay state other than EXCLUDED");
        }
        final var selection = CommandManagerWorkflowSupport.resolveSelection(
                selected,
                choicesSelected(),
                "Select at least one command to restore",
                "Selected commands must be excluded commands from the current manager collection");
        return new Validation(selection, selection.failure());
    }

    private record Validation(
            CommandManagerWorkflowSupport.Selection selection,
            String failure) { }
}
