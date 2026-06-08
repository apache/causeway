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

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.core.config.CausewayConfiguration;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commands",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        domainEvent = CommandExportManager_moveCommandsUp.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commands", sequence = "1.2",
        cssClassFa = "angles-up",
        describedAs = "Moves selected Commands upward after another command by retimestamping them. "
                + "The first moved command is placed after the target; subsequent moved commands either preserve their original timing gaps or, when requested, are squashed to 1 second increments."
)
public class CommandExportManager_moveCommandsUp {

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_moveCommandsUp> {
    }

    private final CommandExportManager commandExportManager;

    @Inject CausewayConfiguration causewayConfiguration;

    public CommandExportManager_moveCommandsUp(final CommandExportManager commandExportManager) {
        this.commandExportManager = commandExportManager;
    }

    @MemberSupport
    public CommandExportManager act(
            final List<ReplayableCommand> selected,
            @ParameterLayout(describedAs = "Command after which the selected commands will be moved.") final ReplayableCommand target,
            @ParameterLayout(
                    named = "Squash timings",
                    describedAs = "Discard original timing gaps between selected commands and place each moved command 1 second after the preceding moved command.") final boolean squashTimings) {
        final String validation = validateAct(selected, target, squashTimings);
        if (validation != null) {
            throw new RecoverableException(validation);
        }
        return movementSupport().move(selected, target, squashTimings);
    }

    @MemberSupport
    public String disableAct() {
        return movementSupport().disableAct();
    }

    @MemberSupport
    public String validateAct(
            final List<ReplayableCommand> selected,
            final ReplayableCommand target,
            final boolean squashTimings) {
        return movementSupport().validateAct(selected, target);
    }

    @MemberSupport
    public List<ReplayableCommand> choicesTarget(final List<ReplayableCommand> selected) {
        return movementSupport().choicesTarget(selected);
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return movementSupport().choicesSelected();
    }

    @MemberSupport
    public boolean defaultSquashTimings() {
        return false;
    }

    private CommandExportManagerMovementSupport movementSupport() {
        return new CommandExportManagerMovementSupport(
                commandExportManager,
                causewayConfiguration,
                CommandExportManagerMovementSupport.Direction.UP);
    }
}
