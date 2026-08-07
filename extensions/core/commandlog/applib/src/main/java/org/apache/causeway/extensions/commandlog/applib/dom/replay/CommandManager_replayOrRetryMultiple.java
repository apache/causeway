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
        domainEvent = CommandManager_replayOrRetryMultiple.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "pendingOrFailed",
        sequence = "1.2",
        cssClassFa = "solid forward",
        cssClass = "btn-secondary",
        describedAs = "Executes multiple commands in sequence; review known participants first")
@RequiredArgsConstructor
public class CommandManager_replayOrRetryMultiple {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetryMultiple> { }

    public enum Limit {
        FIVE(5),
        TEN(10),
        TWENTY(20),
        FORTY(40),
        EIGHTY(80),
        ONE_SIXTY(160),
        THREE_TWENTY(320),
        ALL(Integer.MAX_VALUE);

        private final int limit;

        Limit(final int limit) {
            this.limit = limit;
        }

        public String title() {
            return this == ALL ? "All" : Integer.toString(limit);
        }

        long limit() {
            return limit;
        }
    }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(final Limit limit) {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }
        final var replayables = commandManager.getPendingOrFailed().stream()
                .sorted()
                .limit(limit.limit())
                .toList();
        for (var replayable : replayables) {
            final var result = replayable.tryReplayOrRetry();
            if (result.isFailure()
                    || ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(
                            commandManager.replayContext())) {
                return commandManager;
            }
        }
        return commandManager;
    }

    @MemberSupport
    public String disableAct() {
        final var backgroundReason =
                ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (backgroundReason != null) {
            return backgroundReason;
        }
        return commandManager.getPendingOrFailed().isEmpty()
                ? "No commands in collection"
                : null;
    }

    @MemberSupport
    public Limit defaultLimit() {
        return Limit.TEN;
    }
}
