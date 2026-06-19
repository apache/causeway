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

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_unexclude.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "replayState",
        sequence = "2.1",
        describedAs = "Unexcludes command, indicating the desired state"
)
@RequiredArgsConstructor
public class ReplayableCommand_unexclude {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_unexclude> { }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public ReplayableCommand act(final ReplayState replayState) {
        return replayableCommand.unexclude(replayState);
    }

    public List<ReplayState> choices0Act() {
        return ReplayState.nonExcluded();
    }

    @MemberSupport
    public boolean hideAct() {
        return replayableCommand.getReplayState() == ReplayState.EXCLUDED;
    }
}
