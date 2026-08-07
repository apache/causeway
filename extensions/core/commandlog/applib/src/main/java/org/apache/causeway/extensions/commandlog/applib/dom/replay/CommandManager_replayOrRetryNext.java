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

import java.util.Optional;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_replayOrRetryNext.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "pendingOrFailed",
        sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the oldest command")
@RequiredArgsConstructor
public class CommandManager_replayOrRetryNext {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetryNext> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act() {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }
        commandInSequence(oldestPendingOrFailed())
                .filter(ReplayableCommand::isKnownParticipants)
                .ifPresent(ReplayableCommand::tryReplayOrRetry);
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        final var backgroundReason =
                ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (backgroundReason != null) {
            return backgroundReason;
        }
        final var oldest = oldestPendingOrFailed();
        if (oldest.isEmpty()) {
            return "No commands to execute";
        }
        final var inSequence = commandInSequence(oldest);
        if (inSequence.isEmpty()) {
            return "Unable to find command in sequence (in order to check its known participants)";
        }
        return inSequence.filter(ReplayableCommand::isKnownParticipants).isEmpty()
                ? "Unknown participants (target and/or action args)"
                : null;
    }

    private Optional<ReplayableCommand> oldestPendingOrFailed() {
        return commandManager.getPendingOrFailed().stream().sorted().findFirst();
    }

    private Optional<ReplayableCommand> commandInSequence(final Optional<ReplayableCommand> candidate) {
        return candidate.stream()
                .flatMap(next -> commandManager.getCommandsInSequence().stream()
                        .filter(command -> command.interactionId().equals(next.interactionId())))
                .findFirst();
    }
}
