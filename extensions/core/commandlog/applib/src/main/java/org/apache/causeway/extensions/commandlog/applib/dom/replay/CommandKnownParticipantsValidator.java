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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueType;

/**
 * Validates recorded targets and reference parameters against roots and earlier results.
 */
final class CommandKnownParticipantsValidator {

    private final Predicate<Bookmark> exportRootPredicate;

    CommandKnownParticipantsValidator(final Predicate<Bookmark> exportRootPredicate) {
        this.exportRootPredicate = exportRootPredicate != null ? exportRootPredicate : __ -> false;
    }

    Optional<Failure> validate(
            final @Nullable Timestamp baseline,
            final List<? extends CommandLogEntry> commandLogEntries) {
        var knownParticipants = new HashSet<Bookmark>();
        if (commandLogEntries == null) {
            return Optional.empty();
        }
        for (var entry : commandLogEntries) {
            if (entry == null || isBeforeBaseline(baseline, entry)) {
                continue;
            }
            var failure = validateParticipants(entry, knownParticipants);
            if (failure.isPresent()) {
                return failure;
            }
            Optional.ofNullable(entry.getResult()).ifPresent(knownParticipants::add);
        }
        return Optional.empty();
    }

    Optional<Failure> validateParticipants(
            final @Nullable CommandLogEntry entry,
            final Set<Bookmark> knownParticipants) {
        if (entry == null) {
            return Optional.empty();
        }
        var known = knownParticipants != null ? knownParticipants : Set.<Bookmark>of();
        for (var participant : participantsFor(entry)) {
            if (!exportRootPredicate.test(participant.bookmark())
                    && !known.contains(participant.bookmark())) {
                return Optional.of(new Failure(entry, participant.bookmark(), participant.parameterName()));
            }
        }
        return Optional.empty();
    }

    private static boolean isBeforeBaseline(
            final @Nullable Timestamp baseline,
            final CommandLogEntry entry) {
        return baseline != null
                && entry.getTimestamp() != null
                && entry.getTimestamp().before(baseline);
    }

    private static List<Participant> participantsFor(final CommandLogEntry entry) {
        var participants = new ArrayList<Participant>();
        var commandDto = entry.getCommandDto();
        Optional.ofNullable(commandDto)
                .map(CommandDto::getTargets)
                .stream()
                .flatMap(targets -> targets.getOid().stream())
                .map(CommandKnownParticipantsValidator::bookmarkFor)
                .flatMap(Optional::stream)
                .map(Participant::target)
                .forEach(participants::add);
        if (participants.isEmpty()) {
            Optional.ofNullable(entry.getTarget())
                    .map(Participant::target)
                    .ifPresent(participants::add);
        }
        participants.addAll(referenceParametersFor(commandDto));
        return participants;
    }

    private static List<Participant> referenceParametersFor(final @Nullable CommandDto commandDto) {
        var participants = new ArrayList<Participant>();
        Optional.ofNullable(commandDto)
                .map(CommandDto::getMember)
                .filter(ActionDto.class::isInstance)
                .map(ActionDto.class::cast)
                .map(ActionDto::getParameters)
                .stream()
                .flatMap(parameters -> parameters.getParameter().stream())
                .filter(CommandKnownParticipantsValidator::isReferenceParameter)
                .forEach(parameter -> bookmarkFor(parameter.getReference())
                        .map(bookmark -> Participant.parameter(
                                parameterName(parameter, participants.size()), bookmark))
                        .ifPresent(participants::add));
        return participants;
    }

    private static boolean isReferenceParameter(final @Nullable ParamDto parameter) {
        return parameter != null
                && parameter.getType() == ValueType.REFERENCE
                && parameter.getReference() != null;
    }

    private static String parameterName(final ParamDto parameter, final int index) {
        return parameter.getName() != null && !parameter.getName().isBlank()
                ? parameter.getName()
                : "parameter[" + index + "]";
    }

    private static Optional<Bookmark> bookmarkFor(final @Nullable OidDto oidDto) {
        return Optional.ofNullable(oidDto)
                .filter(oid -> oid.getType() != null && !oid.getType().isBlank())
                .filter(oid -> oid.getId() != null)
                .map(Bookmark::forOidDto);
    }

    private record Participant(Bookmark bookmark, @Nullable String parameterName) {
        static Participant target(final Bookmark bookmark) {
            return new Participant(bookmark, null);
        }

        static Participant parameter(final String parameterName, final Bookmark bookmark) {
            return new Participant(bookmark, parameterName);
        }
    }

    record Failure(
            CommandLogEntry commandLogEntry,
            Bookmark bookmark,
            @Nullable String parameterName) {

        boolean isParameter() {
            return parameterName != null;
        }

        String message() {
            return isParameter()
                    ? String.format(
                            "%s, parameter %s: '%s' is unknown for command export; select a prior navigation or finder action that makes it known.",
                            commandIdentity(), parameterName, bookmark)
                    : String.format(
                            "%s, target '%s': is unknown for command export; select a prior navigation or finder action that makes it known.",
                            commandIdentity(), bookmark);
        }

        private String commandIdentity() {
            return String.format("%s (at %s)",
                    commandLogEntry.getLogicalMemberIdentifier(), commandLogEntry.getTimestamp());
        }
    }
}
