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

/**
 * Validates that an exported command sequence can reach every selected action target from a root service
 * or from a previously recorded result in the same baseline-bounded export sequence.
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
            final Optional<Failure> failure = validateTargets(entry, knownTargets);
            if (failure.isPresent()) {
                return failure;
            }
            Optional.ofNullable(entry.getResult())
                    .ifPresent(knownTargets::add);
        }
        return Optional.empty();
    }

    private Optional<Failure> validateTargets(
            final CommandLogEntry entry,
            final Set<Bookmark> knownTargets) {
        if (!isActionCommand(entry.getCommandDto())) {
            return Optional.empty();
        }
        for (final Bookmark target : targetBookmarksFor(entry)) {
            if (!isKnownTarget(target, knownTargets)) {
                return Optional.of(new Failure(entry, target));
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

    private static boolean isActionCommand(final CommandDto commandDto) {
        return commandDto != null
                && commandDto.getMember() instanceof ActionDto;
    }

    private static List<Bookmark> targetBookmarksFor(final CommandLogEntry entry) {
        final List<Bookmark> targets = new ArrayList<>();
        Optional.ofNullable(entry.getCommandDto())
                .map(CommandDto::getTargets)
                .stream()
                .flatMap(oidsDto -> oidsDto.getOid().stream())
                .map(Bookmark::forOidDto)
                .filter(Objects::nonNull)
                .forEach(targets::add);
        if (targets.isEmpty() && entry.getTarget() != null) {
            targets.add(entry.getTarget());
        }
        return targets;
    }

    static final class Failure {
        private final CommandLogEntry commandLogEntry;
        private final Bookmark unknownTarget;

        Failure(
                final CommandLogEntry commandLogEntry,
                final Bookmark unknownTarget) {
            this.commandLogEntry = commandLogEntry;
            this.unknownTarget = unknownTarget;
        }

        String message() {
            return String.format(
                    "Target %s is unknown for command export in command %s. "
                            + "Include an earlier navigation or finder action returning this target in the exportable sequence.",
                    unknownTarget,
                    commandIdentity(commandLogEntry));
        }

        private static String commandIdentity(final CommandLogEntry entry) {
            return String.format(
                    "%s (%s at %s)",
                    entry.getInteractionId(),
                    entry.getLogicalMemberIdentifier(),
                    entry.getTimestamp());
        }
    }
}
