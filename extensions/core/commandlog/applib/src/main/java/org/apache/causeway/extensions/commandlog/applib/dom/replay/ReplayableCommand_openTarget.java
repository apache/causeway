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

import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_openTarget.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "1",
        associateWith = "participants",
        describedAs = "Opens the actual target from the participants table"
)
@RequiredArgsConstructor
public class ReplayableCommand_openTarget {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_openTarget> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Object act() {
        return actualTarget().orElse(null);
    }

    @MemberSupport
    public String disableAct() {
        return actualTarget().isPresent()
                ? null
                : "No actual target available";
    }

    private Optional<Object> actualTarget() {
        return replayableCommand.getParticipants().stream()
                .filter(participant -> participant.getRole() == ReplayableCommandParticipant.Role.TARGET)
                .map(ReplayableCommandParticipant::getTarget)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
