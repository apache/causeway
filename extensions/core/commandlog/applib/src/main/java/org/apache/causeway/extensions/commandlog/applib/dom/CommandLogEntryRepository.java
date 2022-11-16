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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.query.QueryRange;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.CommandsDto;
import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.common.v2.InteractionType;

import lombok.Getter;
import lombok.val;

/**
 * Provides supporting functionality for querying {@link CommandLogEntry command log entry} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class CommandLogEntryRepository<C extends CommandLogEntry> {

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Command not found");
            this.interactionId = interactionId;
        }
    }

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    private final Class<C> commandLogEntryClass;


    protected CommandLogEntryRepository(Class<C> commandLogEntryClass) {
        this.commandLogEntryClass = commandLogEntryClass;
    }

    public Class<C> getEntityClass() {
        return commandLogEntryClass;
    }

    public C createEntryAndPersist(
            final Command command, final UUID parentInteractionIdIfAny, final ExecuteIn executeIn) {
        C c = factoryService.detachedEntity(commandLogEntryClass);
        c.init(command);
        c.setParentInteractionId(parentInteractionIdIfAny);
        c.setExecuteIn(executeIn);
        persist(c);
        return c;
    }

    public Optional<C> findByInteractionId(final UUID interactionId) {
        return repositoryService().firstMatch(
                Query.named(commandLogEntryClass,  CommandLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId));
    }

    public List<C> findByParent(final CommandLogEntry parent) {
        return findByParentInteractionId(parent.getInteractionId());
    }

    public List<C> findByParentInteractionId(final UUID parentInteractionId) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_PARENT_INTERACTION_ID)
                        .withParameter("parentInteractionId", parentInteractionId));
    }

    public List<C> findByFromAndTo(
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
        return repositoryService().allMatches(query);
    }

    public List<C> findCurrent() {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_CURRENT));
    }

    public List<C> findCompleted() {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_COMPLETED));
    }


    public List<C> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

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
        return repositoryService().allMatches(query);
    }


    public List<C> findMostRecent() {
        return findMostRecent(100);
    }

    public List<C> findMostRecent(final int limit) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass,  CommandLogEntry.Nq.FIND_MOST_RECENT).withLimit(limit));
    }


    public List<C> findRecentByUsername(final String username) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(30L));
    }



    public List<C> findRecentByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(30L)
        );
    }

    public List<C> findRecentByTargetOrResult(final Bookmark targetOrResult) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BY_TARGET_OR_RESULT)
                        .withParameter("targetOrResult", targetOrResult)
                        .withLimit(30L)
        );
    }


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
    public List<C> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        final C from = findByInteractionIdElseNull(interactionId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<C> findFirst() {
        Optional<C> firstCommandIfAny = repositoryService().firstMatch(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_FIRST));
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
    public List<C> findBackgroundAndNotYetStarted() {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BACKGROUND_AND_NOT_YET_STARTED));
    }

    public List<C> findRecentBackgroundByTarget(Bookmark target) {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_RECENT_BACKGROUND_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(30L)
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
    public Optional<C> findMostRecentReplayed() {
        return repositoryService().firstMatch(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_MOST_RECENT_REPLAYED));
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
    public Optional<C> findMostRecentCompleted() {
        return repositoryService().firstMatch(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_MOST_RECENT_COMPLETED));
    }

    public List<C> findNotYetReplayed() {
        return repositoryService().allMatches(
                Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_REPLAY_STATE)
                        .withParameter("replayState", ReplayState.PENDING)
                        .withLimit(10));
    }


    public C saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final C commandJdo = factoryService.detachedEntity(commandLogEntryClass);

        commandJdo.setInteractionId(UUID.fromString(dto.getInteractionId()));
        commandJdo.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()));
        commandJdo.setUsername(dto.getUsername());

        commandJdo.setReplayState(ReplayState.PENDING);

        val firstTargetOidDto = dto.getTargets().getOid().get(0);
        commandJdo.setTarget(Bookmark.forOidDto(firstTargetOidDto));
        commandJdo.setCommandDto(dto);
        commandJdo.setLogicalMemberIdentifier(dto.getMember().getLogicalMemberIdentifier());

        persist(commandJdo);

        return commandJdo;
    }


    public List<C> saveForReplay(final CommandsDto commandsDto) {
        val commandDtos = commandsDto.getCommandDto();
        val commands = new ArrayList<C>();
        for (val dto : commandDtos) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }


    public void persist(final C commandLogEntry) {
        repositoryService().persist(commandLogEntry);
    }

    public void truncateLog() {
        repositoryService().removeAll(commandLogEntryClass);
    }

    // --


    public List<C> findCommandsOnPrimaryElseFail(
            final @Nullable UUID interactionId,
            final @Nullable Integer batchSize) throws NotFoundException {

        final List<C> commands = findSince(interactionId, batchSize);
        if(commands == null) {
            throw new NotFoundException(interactionId);
        }
        return commands;
    }



    private C findByInteractionIdElseNull(final UUID interactionId) {
        val q = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_BY_INTERACTION_ID)
                .withParameter("interactionId", interactionId);
        return repositoryService().uniqueMatch(q).orElse(null);
    }

    private List<C> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        val needsTrimFix = batchSize != null && batchSize == 1;

        val q = Query.named(commandLogEntryClass, CommandLogEntry.Nq.FIND_SINCE)
                .withParameter("timestamp", timestamp)
                .withRange(QueryRange.limit(
                        needsTrimFix ? 2L : batchSize
                ));

        final List<C> commandJdos = repositoryService().allMatches(q);
        return needsTrimFix && commandJdos.size() > 1
                ? commandJdos.subList(0,1)
                : commandJdos;
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

    /**
     * intended for testing purposes only
     */
    public List<C> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return repositoryService().allInstances(commandLogEntryClass);
    }


    /**
     * intended for testing purposes only
     */
    public void removeAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'removeAll' in production systems");
        }
        repositoryService().removeAll(commandLogEntryClass);
    }


}
