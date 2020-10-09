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
package org.apache.isis.extensions.commandlog.impl.jdo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.persistence.jdo.applib.services.IsisJdoSupport_v3_2;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Provides supporting functionality for querying and persisting
 * {@link CommandJdo command} entities.
 */
@Service
@Named("isisExtensionsCommandLog.CommandJdoRepository")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Jdo")
@RequiredArgsConstructor
@Log4j2
public class CommandJdoRepository {

    @Inject final Provider<InteractionContext> interactionContextProvider;
    @Inject final RepositoryService repositoryService;
    @Inject final IsisJdoSupport_v3_2 isisJdoSupport;

    public List<CommandJdo> findByFromAndTo(
            @Nullable final LocalDate from,
            @Nullable final LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<CommandJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTimestampBetween", 
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTimestampAfter", 
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTimestampBefore", 
                        "to", toTs);
            } else {
                query = new QueryDefault<>(CommandJdo.class,
                        "find");
            }
        }
        return repositoryService.allMatches(query);
    }


    public Optional<CommandJdo> findByUniqueId(final UUID uniqueId) {
        return repositoryService.firstMatch(
                new QueryDefault<>(CommandJdo.class,
                        "findByUniqueIdStr",
                        "uniqueIdStr", uniqueId.toString()));
    }

    public List<CommandJdo> findByParent(final CommandJdo parent) {
        return repositoryService.allMatches(
                new QueryDefault<>(CommandJdo.class,
                        "findByParent",
                        "parent", parent));
    }


    public List<CommandJdo> findCurrent() {
        return repositoryService.allMatches(
                new QueryDefault<>(CommandJdo.class, "findCurrent"));
    }


    public List<CommandJdo> findCompleted() {
        return repositoryService.allMatches(
                new QueryDefault<>(CommandJdo.class, "findCompleted"));
    }


    public List<CommandJdo> findByTargetAndFromAndTo(
            final Bookmark target
            , final LocalDate from
            , final LocalDate to) {
        final String targetStr = target.toString();
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<CommandJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTargetAndTimestampBetween", 
                        "targetStr", targetStr,
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTargetAndTimestampAfter", 
                        "targetStr", targetStr,
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTargetAndTimestampBefore", 
                        "targetStr", targetStr,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(CommandJdo.class,
                        "findByTarget", 
                        "targetStr", targetStr);
            }
        }
        return repositoryService.allMatches(query);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, int daysOffset) {
        return dt!=null
                ?new java.sql.Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }


    public List<CommandJdo> findRecentByUsername(final String username) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        CommandJdo.class,
                        "findRecentByUsername",
                        "username", username));
    }


    public List<CommandJdo> findRecentByTarget(final Bookmark target) {
        final String targetStr = target.toString();
        return repositoryService.allMatches(
                new QueryDefault<>(
                        CommandJdo.class,
                        "findRecentByTarget",
                        "targetStr", targetStr));
    }


    /**
     * Intended to support the replay of commands on a seconadry instance of
     * the application.
     *
     * This finder returns all (completed) {@link CommandJdo}s started after
     * the command with the specified uniqueId.  The number of commands
     * returned can be limited so that they can be applied in batches.
     *
     * If the provided uniqueId is null, then only a single
     * {@link CommandJdo command} is returned.  This is intended to support
     * the case when the secondary does not yet have any
     * {@link CommandJdo command}s replicated.  In practice this is unlikely;
     * typically we expect that the secondary will be set up to run against a
     * copy of the primary instance's DB (restored from a backup), in which
     * case there will already be a {@link CommandJdo command} representing the
     * current high water mark on the secondary system.
     *
     * If the unique id is not null but the corresponding
     * {@link CommandJdo command} is not found, then <tt>null</tt> is returned.
     * In the replay scenario the caller will probably interpret this as an
     * error because it means that the high water mark on the secondary is
     * inaccurate, referring to a non-existent {@link CommandJdo command} on
     * the primary.
     *
     * @param uniqueId - the identifier of the {@link CommandJdo command} being
     *                   the replay hwm (using {@link #findMostRecentReplayed()} on the
     *                   secondary), or null if no HWM was found there.
     * @param batchSize - to restrict the number returned (so that replay
     *                   commands can be batched).
     *
     * @return
     */
    public List<CommandJdo> findSince(final UUID uniqueId, final Integer batchSize) {
        if(uniqueId == null) {
            return findFirst();
        }
        final CommandJdo from = findByUniqueIdElseNull(uniqueId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<CommandJdo> findFirst() {
        Optional<CommandJdo> firstCommandIfAny = repositoryService.firstMatch(
                new QueryDefault<>(CommandJdo.class, "findFirst"));
        return firstCommandIfAny
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }


    private CommandJdo findByUniqueIdElseNull(final UUID uniqueId) {
        val tsq = isisJdoSupport.newTypesafeQuery(CommandJdo.class);
        val cand = QCommandJdo.candidate();
        val q = tsq.filter(
                cand.uniqueIdStr.eq(tsq.parameter("uniqueIdStr", String.class))
        );
        q.setParameter("uniqueIdStr", uniqueId.toString());
        return q.executeUnique();
    }

    private List<CommandJdo> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {
        val q = new QueryDefault<>(
                CommandJdo.class,
                "findSince",
                "timestamp", timestamp);

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        if(batchSize != null) {
            q.withCount(batchSize == 1 ? 2 : batchSize);
        }
        final List<CommandJdo> commandJdos = repositoryService.allMatches(q);
        return batchSize != null && batchSize == 1 && commandJdos.size() > 1
                    ? commandJdos.subList(0,1)
                    : commandJdos;
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
    public Optional<CommandJdo> findMostRecentReplayed() {

        return repositoryService.firstMatch(
                new QueryDefault<>(
                        CommandJdo.class, "findMostRecentReplayed"));
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
    public Optional<CommandJdo> findMostRecentCompleted() {
        return repositoryService.firstMatch(
                new QueryDefault<>(CommandJdo.class, "findMostRecentCompleted"));
    }


    public List<CommandJdo> findNotYetReplayed() {
        return repositoryService.allMatches(
                new QueryDefault<>(CommandJdo.class,
                        "findNotYetReplayed"));
    }



    public List<CommandJdo> findReplayedOnSecondary() {
        return repositoryService.allMatches(
                new QueryDefault<>(CommandJdo.class, "findReplayableMostRecentStarted"));
    }


    public List<CommandJdo> saveForReplay(final CommandsDto commandsDto) {
        List<CommandDto> commandDto = commandsDto.getCommandDto();
        List<CommandJdo> commands = new ArrayList<>();
        for (final CommandDto dto : commandDto) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }

    @Programmatic
    public CommandJdo saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final CommandJdo commandJdo = new CommandJdo();

        commandJdo.setUniqueIdStr(dto.getTransactionId());
        commandJdo.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()));
        commandJdo.setUsername(dto.getUser());

        commandJdo.setReplayState(ReplayState.PENDING);

        final OidDto firstTarget = dto.getTargets().getOid().get(0);
        commandJdo.setTarget(Bookmark.from(firstTarget));
        commandJdo.setCommandDto(dto);
        commandJdo.setLogicalMemberIdentifier(dto.getMember().getLogicalMemberIdentifier());

        persist(commandJdo);

        return commandJdo;
    }

    public void persist(final CommandJdo commandJdo) {
        repositoryService.persist(commandJdo);
    }

    public void truncateLog() {
        repositoryService.removeAll(CommandJdo.class);
    }

}
