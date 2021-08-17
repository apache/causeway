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
package org.apache.isis.extensions.commandlog.jpa.entities;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.extensions.commandlog.model.command.CommandModelRepository;
import org.apache.isis.extensions.commandlog.model.command.ReplayState;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides supporting functionality for querying and persisting
 * {@link CommandJdo command} entities.
 */
@Service
@Named("isis.ext.commandLog.CommandJdoRepository")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jpa")
@RequiredArgsConstructor
//@Log4j2
public class CommandJpaRepository
implements CommandModelRepository<CommandJpa> {

    @Inject final Provider<InteractionProvider> interactionProviderProvider;
    @Inject final Provider<RepositoryService> repositoryServiceProvider;


    @Override
    public List<CommandJpa> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<CommandJpa> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(CommandJpa.class, "findByTimestampBetween")
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(CommandJpa.class, "findByTimestampAfter")
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(CommandJpa.class, "findByTimestampBefore")
                        .withParameter("to", toTs);
            } else {
                query = Query.named(CommandJpa.class, "find");
            }
        }
        return repositoryService().allMatches(query);
    }

    @Override
    public Optional<CommandJpa> findByInteractionId(final UUID interactionId) {
        return repositoryService().firstMatch(
                Query.named(CommandJpa.class, "findByInteractionIdStr")
                    .withParameter("interactionIdStr", interactionId.toString()));
    }

    @Override
    public List<CommandJpa> findByParent(final CommandModel parent) {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findByParent")
                    .withParameter("parent", parent));
    }

    @Override
    public List<CommandJpa> findCurrent() {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findCurrent"));
    }

    @Override
    public List<CommandJpa> findCompleted() {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findCompleted"));
    }

    @Override
    public List<CommandJpa> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<CommandJpa> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(CommandJpa.class, "findByTargetAndTimestampBetween")
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(CommandJpa.class, "findByTargetAndTimestampAfter")
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(CommandJpa.class, "findByTargetAndTimestampBefore")
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(CommandJpa.class, "findByTarget")
                        .withParameter("target", target);
            }
        }
        return repositoryService().allMatches(query);
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

    @Override
    public List<CommandJpa> findRecentByUsername(final String username) {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findRecentByUsername")
                    .withParameter("username", username));
    }

    @Override
    public List<CommandJpa> findRecentByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findRecentByTarget")
                    .withParameter("target", target));
    }

    @Override
    public List<CommandJpa> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        final CommandJpa from = findByInteractionIdElseNull(interactionId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<CommandJpa> findFirst() {
        Optional<CommandJpa> firstCommandIfAny = repositoryService().firstMatch(
                Query.named(CommandJpa.class, "findFirst"));
        return firstCommandIfAny
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }


    private CommandJpa findByInteractionIdElseNull(final UUID interactionId) {
        return null;
        //TODO migrate to JPA
//        val tsq = jdoSupport.newTypesafeQuery(CommandJpa.class);
//        val cand = QCommandJdo.candidate();
//        val q = tsq.filter(
//                cand.interactionIdStr.eq(tsq.parameter("interactionIdStr", String.class))
//        );
//        q.setParameter("interactionIdStr", interactionId.toString());
//        return q.executeUnique();
    }

    private List<CommandJpa> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        val needsTrimFix = batchSize != null && batchSize == 1;

        val q = Query.named(CommandJpa.class, "findSince")
                .withParameter("timestamp", timestamp)
                .withRange(QueryRange.limit(
                        needsTrimFix ? 2L : batchSize
                ));

        final List<CommandJpa> commandJdos = repositoryService().allMatches(q);
        return needsTrimFix && commandJdos.size() > 1
                    ? commandJdos.subList(0,1)
                    : commandJdos;
    }


    @Override
    public Optional<CommandJpa> findMostRecentReplayed() {

        return repositoryService().firstMatch(
                Query.named(CommandJpa.class, "findMostRecentReplayed"));
    }

    @Override
    public Optional<CommandJpa> findMostRecentCompleted() {
        return repositoryService().firstMatch(
                Query.named(CommandJpa.class, "findMostRecentCompleted"));
    }

    @Override
    public List<CommandJpa> findNotYetReplayed() {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findNotYetReplayed"));
    }

    @Override
    public List<CommandJpa> findReplayedOnSecondary() {
        return repositoryService().allMatches(
                Query.named(CommandJpa.class, "findReplayableMostRecentStarted"));
    }

    @Override
    public List<CommandJpa> saveForReplay(final CommandsDto commandsDto) {
        List<CommandDto> commandDto = commandsDto.getCommandDto();
        List<CommandJpa> commands = new ArrayList<>();
        for (final CommandDto dto : commandDto) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }

    @Programmatic
    @Override
    public CommandJpa saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final CommandJpa commandJdo = new CommandJpa();

        commandJdo.setInteractionIdStr(dto.getInteractionId());
        commandJdo.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()));
        commandJdo.setUsername(dto.getUser());

        commandJdo.setReplayState(ReplayState.PENDING);

        final OidDto firstTarget = dto.getTargets().getOid().get(0);
        commandJdo.setTarget(Bookmark.forOidDto(firstTarget));
        commandJdo.setCommandDto(dto);
        commandJdo.setLogicalMemberIdentifier(dto.getMember().getLogicalMemberIdentifier());

        persist(commandJdo);

        return commandJdo;
    }

    @Override
    public void persist(final CommandJpa commandJdo) {
        repositoryService().persist(commandJdo);
    }

    @Override
    public void truncateLog() {
        repositoryService().removeAll(CommandJpa.class);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

}
