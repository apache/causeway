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
package org.apache.causeway.extensions.commandlog.jpa.integtests;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_deleteCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_excludeCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_moveCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_unexcludeCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AppManifest.class,
        properties = "causeway.extensions.command-log.recording-support=ENABLED")
@ActiveProfiles("test")
class CommandManagerWorkflow_IntegTest extends CausewayIntegrationTestAbstract {

    private static final Timestamp BASELINE = timestamp("2026-08-06T10:00:00Z");

    @Inject CommandLogEntryRepository repository;
    @Inject InteractionService interactionService;
    @Inject RepositoryService repositoryService;
    @Inject ReplayContext replayContext;
    @Inject TransactionService transactionService;

    @BeforeEach
    void setUp() {
        interactionService.nextInteraction();
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () ->
                repositoryService.removeAll(CommandLogEntry.class)).ifFailureFail();
    }

    @Test
    void workflowMutationsCommitAndReloadThroughExistingJpaQueries() {
        final var targetId = UUID.randomUUID();
        final var firstId = UUID.randomUUID();
        final var secondId = UUID.randomUUID();
        final var deletedId = UUID.randomUUID();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            persist(targetId, "2026-08-06T10:00:00Z", ReplayState.UNDEFINED);
            persist(firstId, "2026-08-06T10:01:00Z", ReplayState.UNDEFINED);
            persist(secondId, "2026-08-06T10:01:02.500Z", ReplayState.EXPORTED);
            persist(deletedId, "2026-08-06T10:03:00Z", ReplayState.EXCLUDED);
        }).ifFailureFail();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            final var manager = manager();
            new CommandManager_excludeCommands(manager).act(List.of(command(manager, firstId)));
        }).ifFailureFail();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            assertThat(repository.findByInteractionId(firstId).orElseThrow().getReplayState())
                    .isEqualTo(ReplayState.EXCLUDED);
            final var manager = manager();
            new CommandManager_unexcludeCommands(manager)
                    .act(List.of(command(manager.getExcluded(), firstId)), ReplayState.PENDING);
        }).ifFailureFail();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            assertThat(repository.findByInteractionId(firstId).orElseThrow().getReplayState())
                    .isEqualTo(ReplayState.PENDING);
            final var manager = manager();
            new CommandManager_deleteCommands(manager)
                    .act(List.of(command(manager.getExcluded(), deletedId)));
        }).ifFailureFail();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            assertThat(repository.findByInteractionId(deletedId)).isEmpty();
            final var manager = manager();
            final var selected = List.of(command(manager, secondId), command(manager, firstId));
            new CommandManager_moveCommands(manager)
                    .act(selected, command(manager, targetId), false);
        }).ifFailureFail();

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            final var first = repository.findByInteractionId(firstId).orElseThrow();
            final var second = repository.findByInteractionId(secondId).orElseThrow();
            assertThat(first.getTimestamp()).isEqualTo(timestamp("2026-08-06T10:00:01Z"));
            assertThat(second.getTimestamp()).isEqualTo(timestamp("2026-08-06T10:00:03.500Z"));
            assertThat(dtoTimestamp(first)).isEqualTo(first.getTimestamp());
            assertThat(dtoTimestamp(second)).isEqualTo(second.getTimestamp());
            assertThat(repository.findForegroundSinceTimestamp(BASELINE, 3))
                    .extracting(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry::getInteractionId)
                    .containsExactly(targetId, firstId, secondId);
        }).ifFailureFail();
    }

    private CommandManager manager() {
        return new CommandManager(BASELINE, 50, replayContext);
    }

    private static ReplayableCommand command(
            final CommandManager manager,
            final UUID interactionId) {
        return command(manager.getCommandsInSequence(), interactionId);
    }

    private static ReplayableCommand command(
            final List<ReplayableCommand> commands,
            final UUID interactionId) {
        return commands.stream()
                .filter(command -> command.interactionId().equals(interactionId))
                .findFirst()
                .orElseThrow();
    }

    private void persist(
            final UUID interactionId,
            final String instant,
            final ReplayState replayState) {
        final var timestamp = timestamp(instant);
        final var target = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");
        final var entry = new CommandLogEntry();
        entry.setInteractionId(interactionId);
        entry.setUsername("tester");
        entry.setTimestamp(timestamp);
        entry.setTarget(target);
        entry.setExecuteIn(ExecuteIn.FOREGROUND);
        entry.setLogicalMemberIdentifier("demo.Customer#update");
        entry.setReplayState(replayState);
        entry.setCommandDto(commandDto(interactionId, timestamp, target));
        repository.persist(entry);
    }

    private static CommandDto commandDto(
            final UUID interactionId,
            final Timestamp timestamp,
            final Bookmark target) {
        final var dto = new CommandDto();
        dto.setInteractionId(interactionId.toString());
        dto.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(timestamp));
        final var targets = new OidsDto();
        targets.getOid().add(target.toOidDto());
        dto.setTargets(targets);
        final var action = new ActionDto();
        action.setLogicalMemberIdentifier("demo.Customer#update");
        dto.setMember(action);
        return dto;
    }

    private static Timestamp dtoTimestamp(
            final org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry entry) {
        return JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(entry.getCommandDto().getTimestamp());
    }

    private static Timestamp timestamp(final String instant) {
        return Timestamp.from(Instant.parse(instant));
    }
}
