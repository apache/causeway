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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;
import org.springframework.lang.Nullable;

import lombok.Getter;

/**
 * Provides supporting functionality for querying {@link CommandLogEntry command log entry} entities.
 *
 * @since 2.0 {@index}
 */
public interface CommandLogEntryRepository {

    class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Command not found");
            this.interactionId = interactionId;
        }
    }

    CommandLogEntry createEntryAndPersist(
            final Command command, final UUID parentInteractionIdIfAny, final ExecuteIn executeIn);

    Optional<CommandLogEntry> findByInteractionId(final UUID interactionId);

    List<CommandLogEntry> findByParent(final CommandLogEntry parent);

    List<CommandLogEntry> findByParentInteractionId(final UUID parentInteractionId);

    List<CommandLogEntry> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to);

    List<CommandLogEntry> findCurrent();

    List<CommandLogEntry> findCompleted();


    List<CommandLogEntry> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to);


    List<CommandLogEntry> findMostRecent();

    List<CommandLogEntry> findMostRecent(final int limit);


    List<CommandLogEntry> findRecentByUsername(final String username);


    List<CommandLogEntry> findRecentByTarget(final Bookmark target);

    List<CommandLogEntry> findRecentByTargetOrResult(final Bookmark targetOrResult);


    /**
     * Intended to support the replay of commands for regression testing on a reference or candidate version of the application.
     *
     * <p>
     * This finder returns all (completed) {@link CommandLogEntry}s started after
     * the command with the specified interactionId.  The number of commands
     * returned can be limited so that they can be applied in batches.
     * </p>
     *
     */
    List<CommandLogEntry> findSince(final UUID interactionId, final Integer batchSize);

    default List<CommandLogEntry> findForegroundSinceTimestamp(final Timestamp since) {
        return findForegroundSinceTimestamp(since, null);
    }

    List<CommandLogEntry> findForegroundSinceTimestamp(final Timestamp since, final Integer limitIfAny);

    List<CommandLogEntry> findForegroundBeforeTimestamp(final Timestamp before, final Integer limitIfAny);


    /**
     * Command Replay feature: Can replay or retry.
     */
    List<CommandLogEntry> findForegroundSinceTimestampAndWithReplayPendingOrFailed(Timestamp since);
    List<CommandLogEntry> findForegroundSinceTimestampAndWithReplayUndefinedOrOk(Timestamp since);
    List<CommandLogEntry> findForegroundSinceTimestampAndWithReplayExcluded(Timestamp since);


    /**
     * Returns any persisted commands that have not yet started.
     *
     * <p>
     * This is to support the notion of background commands (the same as their implementation in v1) whereby a
     * custom executor service for {@link org.apache.causeway.applib.services.wrapper.WrapperFactory} would
     * &quot;execute&quot; a {@link Command} simply by persisting it as a {@link CommandLogEntry}, so that a
     * quartz or similar background job could execute the {@link Command} at some point later.
     * </p>
     */
    List<CommandLogEntry> findBackgroundAndNotYetStarted();

    List<CommandLogEntry> findRecentBackgroundByTarget(final Bookmark target);


    /**
     * The most recent replayed command previously replicated from primary to
     * secondary.
     *
     * <p>
     * This should always exist except for the very first times
     * (after restored the prod DB to secondary).
     * </p>
     */
    Optional<CommandLogEntry> findMostRecentReplayed();

    /**
     * The most recent completed command, as queried on the
     * secondary.
     *
     * <p>
     *     After a restart following the production database being restored
     *     from primary to secondary, would correspond to the last command
     *     run on primary before the production database was restored to the
     *     secondary.
     * </p>
     */
    Optional<CommandLogEntry> findMostRecentCompleted();


    CommandLogEntry saveForReplay(final CommandDto dto);

    default List<CommandLogEntry> saveForReplay(@Nullable final List<CommandDto> commandDtoList) {
        return _NullSafe.stream(commandDtoList)
            .map(this::saveForReplay)
            .collect(Collectors.toList());
    }

    default List<CommandLogEntry> saveForReplay(@Nullable final CommandsDto commandsDto) {
        var commandDtoList = Optional.ofNullable(commandsDto)
            .map(CommandsDto::getCommandDto)
            .orElseGet(Collections::emptyList);
        return saveForReplay(commandDtoList);
    }


    void persist(final CommandLogEntry commandLogEntry);

    void truncateLog();

    // --


    /**
     * intended for testing purposes only
     */
    List<CommandLogEntry> findAll();


    /**
     * intended for testing purposes only
     */
    void removeAll();

}
