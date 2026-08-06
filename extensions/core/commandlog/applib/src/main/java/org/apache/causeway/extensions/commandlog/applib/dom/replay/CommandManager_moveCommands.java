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
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commandsInSequence",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_moveCommands.DomainEvent.class,
        executionPublishing = Publishing.DISABLED)
@ActionLayout(
        associateWith = "commandsInSequence",
        sequence = "1.3",
        cssClass = "btn-secondary",
        describedAs = "Moves selected commands after another command by retimestamping them")
@RequiredArgsConstructor
public class CommandManager_moveCommands {

    public static class DomainEvent
            extends CommandManager.ActionDomainEvent<CommandManager_moveCommands> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(
            final List<ReplayableCommand> selected,
            @ParameterLayout(describedAs = "Command after which the selected commands will be moved")
            final ReplayableCommand target,
            @ParameterLayout(
                    named = "Squash timings",
                    describedAs = "Places each moved command one second after its predecessor")
            final boolean squashTimings) {
        return support().move(selected, target, squashTimings);
    }

    @MemberSupport
    public String disableAct() {
        return support().disableAct();
    }

    @MemberSupport
    public String validateAct(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target,
            final boolean squashTimings) {
        return support().validateAct(selected, target);
    }

    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return support().choicesSelected();
    }

    @MemberSupport
    public List<ReplayableCommand> choicesTarget(final List<ReplayableCommand> selected) {
        return support().choicesTarget(selected);
    }

    @MemberSupport
    public boolean defaultSquashTimings() {
        return false;
    }

    private CommandManagerMovementSupport support() {
        return new CommandManagerMovementSupport(commandManager);
    }
}
