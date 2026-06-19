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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.query.QueryRange;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.springframework.lang.Nullable;

/**
 * Provides supporting functionality for querying {@link CommandLogEntry command log entry} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class CommandLogEntryRepositoryAbstract<C extends CommandLogEntry> implements CommandLogEntryRepository {

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    private final Class<C> commandLogEntryClass;

    protected CommandLogEntryRepositoryAbstract(final Class<C> commandLogEntryClass) {
        this.commandLogEntryClass = commandLogEntryClass;
    }

    public Class<C> getEntityClass() {
        return commandLogEntryClass;
    }

    @Override
    public C createEntryAndPersist(
            final Command command, final UUID parentInteractionIdIfAny, final ExecuteIn executeIn) {
        C c = factoryService.detachedEntity(commandLogEntryClass);
        c.sync(command);
        c.setReplayState(ReplayState.UNDEFINED);
        c.setParentInteractionId(parentInteractionIdIfAny);
        c.setExecuteIn(executeIn);
        persist(c);
        return c;
    }

    @Override
    public Optional<CommandLogEntry> findByInteractionId(final UUID interactionId) {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                Query.named(commandLogEntryClass,  CommandLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId))
        );
    }

    @Override
    public List<CommandLogEntry> findByParent(final CommandLogEntry parent) {
        return findByParentInteractionId(parent.getInteractionId());
    }

    @Override
    public List<CommandLogEntry> findByParentInteractionId(final UUID parentInteractionId) {
        return _Casts.uncheckedCast( repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_PARENT_INTERACTION_ID)
                        .withParameter("parentInteractionId", parentInteractionId))
        );
    }

    @Override
    public List<CommandLogEntry> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<C> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND);
            }
        }
        return _Casts.uncheckedCast(repositoryService().allMatches(query));
    }

    @Override
    public List<CommandLogEntry> findCurrent() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_CURRENT))
        );
    }

    @Override
    public List<CommandLogEntry> findCompleted() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_COMPLETED))
        );
    }

    @Override
    public List<CommandLogEntry> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<C> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target);
            }
        }
        return _Casts.uncheckedCast(repositoryService().allMatches(query));
    }

    @Override
    public List<CommandLogEntry> findMostRecent() {
        return findMostRecent(100);
    }

    @Override
    public List<CommandLogEntry> findMostRecent(final int limit) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(commandLogEntryClass,  CommandLogEntry.Nq.FIND_MOST_RECENT).withLimit(limit))
        );
    }

    @Override
    public List<CommandLogEntry> findRecentByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(30L))
        );
    }

    @Override
    public List<CommandLogEntry> findRecentByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(30L)
                )
        );
    }

    @Override
    public List<CommandLogEntry> findRecentByTargetOrResult(final Bookmark targetOrResult) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_TARGET_OR_RESULT)
                            .withParameter("targetOrResult", targetOrResult)
                            .withLimit(30L)
                    )
        );
    }

    @Override
    public List<CommandLogEntry> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        final C from = findByInteractionIdElseNull(interactionId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<CommandLogEntry> findFirst() {
        Optional<CommandLogEntry> firstCommandIfAny =
                _Casts.uncheckedCast(repositoryService().firstMatch(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FIRST)));
        return firstCommandIfAny
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

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
    @Override
    public List<CommandLogEntry> findBackgroundAndNotYetStarted() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BACKGROUND_AND_NOT_YET_STARTED)));
    }

    @Override
    public List<CommandLogEntry> findRecentBackgroundByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BACKGROUND_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(30L)
                )
        );
    }

    /**
     * The most recent replayed command previously replicated from primary to
     * secondary.
     *
     * <p>
     * This should always exist except for the very first times
     * (after restored the prod DB to secondary).
     * </p>
     */
    @Override
    public Optional<CommandLogEntry> findMostRecentReplayed() {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_MOST_RECENT_REPLAYED))
        );
    }

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
    @Override
    public Optional<CommandLogEntry> findMostRecentCompleted() {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                    Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_MOST_RECENT_COMPLETED))
        );
    }

    @Override
    public C saveForReplay(final CommandDto commandToReplay) {

        final var interactionId = commandToReplay.getInteractionId();
        final var byInteractionId = findByInteractionId(UUID.fromString(interactionId));
        if(byInteractionId.isPresent()) {
            //noinspection unchecked
            return (C) byInteractionId.get();
        }

        final C entity = factoryService.detachedEntity(commandLogEntryClass);
        entity.init(commandToReplay, ReplayState.PENDING, 0);
        entity.setParentInteractionId(null); // n/a for replay
        entity.setExecuteIn(ExecuteIn.FOREGROUND);  // only ever replay foreground commands.

        persist(entity);

        return entity;

    }

    @Override
    public void persist(final CommandLogEntry commandLogEntry) {
        repositoryService().persistAndFlush(commandLogEntry);
    }

    @Override
    public void truncateLog() {
        repositoryService().removeAll(commandLogEntryClass);
    }

    // --


    private C findByInteractionIdElseNull(final UUID interactionId) {
        var q = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_INTERACTION_ID)
                .withParameter("interactionId", interactionId);
        return repositoryService().uniqueMatch(q).orElse(null);
    }

    private List<CommandLogEntry> findSince(
            final Timestamp timestamp,
            final Integer batchSizeIfAny) {

        var query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_SINCE)
                .withParameter("timestamp", timestamp);

        return allMatches(query, batchSizeIfAny);
    }

    @Override
    public List<CommandLogEntry> findForegroundSinceTimestamp(final Timestamp since, Integer batchSizeIfAny) {
        var query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_AFTER)
                .withParameter("from", since);

        return allMatches(query, batchSizeIfAny);
    }

    @Override
    public List<CommandLogEntry> findForegroundBeforeTimestamp(final Timestamp before, Integer batchSizeIfAny) {
        var query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_BEFORE)
                .withParameter("to", before);

        return allMatches(query, batchSizeIfAny);
    }

    @Override
    public List<CommandLogEntry> findForegroundSinceTimestampAndCanBeExported(final Timestamp since, Integer batchSizeIfAny) {
        return findForegroundSinceTimestampWithState(since, ReplayState.UNDEFINED, batchSizeIfAny);
    }

    @Override
    public List<CommandLogEntry> findForegroundSinceTimestampAndWithReplayPendingOrFailed(final Timestamp since) {
        return findForegroundSinceTimestampWithStates(since, ReplayState.PENDING, ReplayState.FAILED);
    }

    /**
     * Command Replay feature: Cannot replay or retry.
     */
    @Override
    public List<CommandLogEntry> findSinceAndWithReplayOkOrExcluded(final Timestamp since) {
        return findForegroundSinceTimestampWithStates(since, ReplayState.OK, ReplayState.EXCLUDED);
    }

    private List<CommandLogEntry> findForegroundSinceTimestampWithState(Timestamp from, ReplayState replayState, Integer batchSizeIfAny) {
        var query = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_AFTER_AND_REPLAY_STATE)
                .withParameter("from", from)
                .withParameter("replayState", replayState);

        return allMatches(query, batchSizeIfAny);
    }


    private List<CommandLogEntry> findForegroundSinceTimestampWithStates(Timestamp from, ReplayState replayState1, ReplayState replayState2) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                        Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_AFTER_AND_REPLAY_STATES)
                                .withParameter("from", from)
                                .withParameter("replayState1", replayState1)
                                .withParameter("replayState2", replayState2)));
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

    private static Timestamp toTimestampStartOfDayWithOffset(
            final @Nullable LocalDate dt,
            final int daysOffset) {

        return dt!=null
                ? new java.sql.Timestamp(
                Instant.from(dt.atStartOfDay().plusDays(daysOffset).atZone(ZoneId.systemDefault()))
                        .toEpochMilli())
                : null;
    }

    private List<CommandLogEntry> allMatches(NamedQuery<C> query, Integer batchSizeIfAny) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        var needsTrimFix = batchSizeIfAny != null && batchSizeIfAny == 1;

        if(batchSizeIfAny != null) {
            query = query.withRange(QueryRange.limit(needsTrimFix ? 2L : batchSizeIfAny));
        }

        final List<CommandLogEntry> commandLogEntries = _Casts.uncheckedCast(repositoryService().allMatches(query));
        return needsTrimFix && commandLogEntries.size() > 1
                ? commandLogEntries.subList(0, 1)
                : commandLogEntries;
    }


    /**
     * intended for testing purposes only
     */
    @Override
    public List<CommandLogEntry> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return _Casts.uncheckedCast(repositoryService().allInstances(commandLogEntryClass));
    }

    /**
     * intended for testing purposes only
     */
    @Override
    public void removeAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'removeAll' in production systems");
        }
        repositoryService().removeAll(commandLogEntryClass);
    }

}
