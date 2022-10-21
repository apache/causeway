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
package org.apache.causeway.extensions.commandreplay.secondary.mixins;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;


/**
 * @since 2.0 {@index}
 */
@Action(
    semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE,
    domainEvent = CommandLogEntry_exclude.ActionDomainEvent.class
)
@ActionLayout(associateWith = "executeIn", sequence = "2")
@RequiredArgsConstructor
//@Log4j2
public class CommandLogEntry_exclude {

    public static class ActionDomainEvent
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<CommandLogEntry_exclude> { }

    final CommandLogEntry commandLogEntry;

    @MemberSupport
    public CommandLogEntry act() {
        commandLogEntry.setReplayState(ReplayState.EXCLUDED);
        return commandLogEntry;
    }
    @MemberSupport public boolean hideAct() {
        return !secondaryConfig.isPresent() || !secondaryConfig.get().isConfigured() ;
    }
    @MemberSupport public String disableAct() {
        final boolean notInError =
                commandLogEntry.getReplayState() == null || !commandLogEntry.getReplayState().isFailed();
        return notInError
                ? "This command is not in error, so cannot be excluded."
                : null;
    }

    @Inject Optional<SecondaryConfig> secondaryConfig;

}
