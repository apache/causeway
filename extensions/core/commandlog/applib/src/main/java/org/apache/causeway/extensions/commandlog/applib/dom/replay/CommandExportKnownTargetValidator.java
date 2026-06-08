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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.ValueType;

/**
 * Validates that an exported command sequence can reach every selected action target and reference parameter
 * from a root service or from a previously recorded result in the same baseline-bounded export sequence.
 */
final class CommandExportKnownTargetValidator {

    private final Predicate<Bookmark> exportRootPredicate;

    CommandExportKnownTargetValidator(final Predicate<Bookmark> exportRootPredicate) {
        this.exportRootPredicate = exportRootPredicate;
    }

    Optional<Failure> validate(
            final Timestamp baseline,
            final List<CommandLogEntry> selectedCommandLogEntries) {
        final Set<Bookmark> knownTargets = new HashSet<>();
        for (final CommandLogEntry entry : selectedCommandLogEntries) {
            if (isBeforeBaseline(baseline, entry)) {
                continue;
            }
            final Optional<Failure> failure = validateParticipants(entry, knownTargets);
            if (failure.isPresent()) {
                return failure;
            }
            Optional.ofNullable(entry.getResult())
                    .ifPresent(knownTargets::add);
        }
        return Optional.empty();
    }

    Optional<Failure> validateParticipants(
            final CommandLogEntry entry,
            final Set<Bookmark> knownTargets) {
        if (entry.getCommandDto() == null) {
            return Optional.empty();
        }
        for (final Participant participant : participantsFor(entry)) {
            if (!isKnownTarget(participant.bookmark, knownTargets)) {
                return Optional.of(new Failure(entry, participant));
            }
        }
        return Optional.empty();
    }

    private boolean isKnownTarget(
            final Bookmark target,
            final Set<Bookmark> knownTargets) {
        return exportRootPredicate.test(target)
                || knownTargets.contains(target);
    }

    private static boolean isBeforeBaseline(
            final Timestamp baseline,
            final CommandLogEntry entry) {
        return baseline != null
                && entry.getTimestamp() != null
                && entry.getTimestamp().before(baseline);
    }

    private static List<Participant> participantsFor(final CommandLogEntry entry) {
        final List<Participant> participants = new ArrayList<>();
        Optional.ofNullable(entry.getCommandDto())
                .map(CommandDto::getTargets)
                .stream()
                .flatMap(oidsDto -> oidsDto.getOid().stream())
                .map(Bookmark::forOidDto)
                .filter(Objects::nonNull)
                .map(Participant::target)
                .forEach(participants::add);
        if (participants.isEmpty() && entry.getTarget() != null) {
            participants.add(Participant.target(entry.getTarget()));
        }
        referenceParametersFor(entry)
                .forEach(participants::add);
        return participants;
    }

    private static List<Participant> referenceParametersFor(final CommandLogEntry entry) {
        final List<Participant> participants = new ArrayList<>();
        Optional.ofNullable(entry.getCommandDto())
                .map(CommandDto::getMember)
                .filter(ActionDto.class::isInstance)
                .map(ActionDto.class::cast)
                .map(ActionDto::getParameters)
                .stream()
                .flatMap(paramsDto -> paramsDto.getParameter().stream())
                .filter(CommandExportKnownTargetValidator::isReferenceParameter)
                .forEach(parameter -> participants.add(Participant.parameter(
                        parameterName(parameter, participants.size()),
                        Bookmark.forOidDto(parameter.getReference()))));
        return participants;
    }

    private static boolean isReferenceParameter(final ParamDto parameter) {
        return parameter.getType() == ValueType.REFERENCE
                && parameter.getReference() != null;
    }

    private static String parameterName(
            final ParamDto parameter,
            final int parameterIndex) {
        return parameter.getName() != null
                ? parameter.getName()
                : "parameter[" + parameterIndex + "]";
    }

    private static final class Participant {
        private final Bookmark bookmark;
        private final String parameterName;

        private Participant(
                final Bookmark bookmark,
                final String parameterName) {
            this.bookmark = bookmark;
            this.parameterName = parameterName;
        }

        static Participant target(final Bookmark bookmark) {
            return new Participant(bookmark, null);
        }

        static Participant parameter(
                final String parameterName,
                final Bookmark bookmark) {
            return new Participant(bookmark, parameterName);
        }

        boolean isParameter() {
            return parameterName != null;
        }
    }

    static final class Failure {
        private final CommandLogEntry commandLogEntry;
        private final Participant participant;

        Failure(
                final CommandLogEntry commandLogEntry,
                final Participant participant) {
            this.commandLogEntry = commandLogEntry;
            this.participant = participant;
        }

        String message() {
            return participant.isParameter()
                    ? parameterMessage()
                    : targetMessage();
        }

        private String targetMessage() {
            return String.format(
                    "%s, target '%s': is unknown.",
                    commandIdentity(commandLogEntry),
                    participant.bookmark
            );
        }

        private String parameterMessage() {
            return String.format(
                    "%s, parameter %s: '%s' is unknown.",
                    commandIdentity(commandLogEntry),
                    participant.parameterName,
                    participant.bookmark
            );
        }

        private static String commandIdentity(final CommandLogEntry entry) {
            return String.format(
                    "%s (at %s)",
                    entry.getLogicalMemberIdentifier(),
                    entry.getTimestamp());
        }
    }
}
