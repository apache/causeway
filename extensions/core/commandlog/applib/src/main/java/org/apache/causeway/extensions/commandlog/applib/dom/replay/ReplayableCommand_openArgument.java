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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_openArgument.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "2",
        associateWith = "participants",
        describedAs = "Opens an actual argument from the participants table"
)
@RequiredArgsConstructor
public class ReplayableCommand_openArgument {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_openArgument> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Object act(final String parameterName) {
        return parameterParticipant(parameterName)
                .map(ReplayableCommandParticipant::getArgument)
                .orElse(null);
    }

    @MemberSupport
    public String disableAct() {
        return parameterParticipants().isEmpty()
                ? "No parameter participants available"
                : null;
    }

    @MemberSupport
    public List<String> choicesParameterName() {
        return parameterParticipants().stream()
                .map(ReplayableCommandParticipant::getParameterName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @MemberSupport
    public String defaultParameterName() {
        final List<String> parameterNames = choicesParameterName();
        return parameterNames.size() == 1
                ? parameterNames.get(0)
                : null;
    }

    @MemberSupport
    public String validateParameterName(final String parameterName) {
        if (parameterName == null) {
            return null;
        }
        final Optional<ReplayableCommandParticipant> participant = parameterParticipant(parameterName);
        if (participant.isEmpty()) {
            return "No parameter participant found for " + parameterName;
        }
        return participant.map(ReplayableCommandParticipant::getActualBookmark).isPresent()
                ? null
                : "No actual argument bookmark available for " + parameterName;
    }

    private Optional<ReplayableCommandParticipant> parameterParticipant(final String parameterName) {
        return parameterParticipants().stream()
                .filter(participant -> Objects.equals(participant.getParameterName(), parameterName))
                .findFirst();
    }

    private List<ReplayableCommandParticipant> parameterParticipants() {
        return replayableCommand.getParticipants().stream()
                .filter(participant -> participant.getRole() == ReplayableCommandParticipant.Role.PARAMETER)
                .collect(Collectors.toList());
    }
}
