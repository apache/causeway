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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;

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
     * Intended to support the replay of commands on a secondary instance of
     * the application.
     *
     * This finder returns all (completed) {@link CommandLogEntry}s started after
     * the command with the specified interactionId.  The number of commands
     * returned can be limited so that they can be applied in batches.
     *
     * If the provided interactionId is null, then only a single
     * {@link CommandLogEntry command} is returned.  This is intended to support
     * the case when the secondary does not yet have any
     * {@link CommandLogEntry command}s replicated.  In practice this is unlikely;
     * typically we expect that the secondary will be set up to run against a
     * copy of the primary instance's DB (restored from a backup), in which
     * case there will already be a {@link CommandLogEntry command} representing the
     * current high water mark on the secondary system.
     *
     * If the interactionId is not null but the corresponding
     * {@link CommandLogEntry command} is not found, then <tt>null</tt> is returned.
     * In the replay scenario the caller will probably interpret this as an
     * error because it means that the high water mark on the secondary is
     * inaccurate, referring to a non-existent {@link CommandLogEntry command} on
     * the primary.
     *
     * @param interactionId - the identifier of the {@link CommandLogEntry command} being
     *                   the replay HWM (using {@link #findMostRecentReplayed()} on the
     *                   secondary), or null if no HWM was found there.
     * @param batchSize - to restrict the number returned (so that replay
     *                   commands can be batched).
     */
    List<CommandLogEntry> findSince(final UUID interactionId, final Integer batchSize);

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

    List<CommandLogEntry> findNotYetReplayed();

    CommandLogEntry saveForReplay(final CommandDto dto);

    List<CommandLogEntry> saveForReplay(final CommandsDto commandsDto);

    void persist(final CommandLogEntry commandLogEntry);

    void truncateLog();

    // --

    List<CommandLogEntry> findCommandsOnPrimaryElseFail(
            final @Nullable UUID interactionId,
            final @Nullable Integer batchSize) throws NotFoundException;

    /**
     * intended for testing purposes only
     */
    List<CommandLogEntry> findAll();

    /**
     * intended for testing purposes only
     */
    void removeAll();

}
