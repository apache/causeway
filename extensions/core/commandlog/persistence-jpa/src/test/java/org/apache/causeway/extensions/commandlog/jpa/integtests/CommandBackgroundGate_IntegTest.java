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
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_replayOrRetryMultiple;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter;
import org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AppManifest.class,
        properties = "causeway.extensions.command-log.recording-support=ENABLED")
@ActiveProfiles("test")
class CommandBackgroundGate_IntegTest extends CausewayIntegrationTestAbstract {

    @Inject BookmarkService bookmarkService;
    @Inject CommandLogEntryRepository repository;
    @Inject org.apache.causeway.extensions.commandlog.jpa.integtests.model.CounterRepository counterRepository;
    @Inject InteractionService interactionService;
    @Inject ReplayContext replayContext;
    @Inject RepositoryService repositoryService;
    @Inject RunBackgroundCommandsJob runBackgroundCommandsJob;
    @Inject TransactionService transactionService;
    @Inject WrapperFactory wrapperFactory;

    private final JobExecutionContext jobExecutionContext = Mockito.mock(JobExecutionContext.class);
    private Bookmark counterBookmark;

    @BeforeEach
    void setUp() {
        interactionService.nextInteraction();
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            repositoryService.removeAll(CommandLogEntry.class);
            counterRepository.removeAll();
            counterRepository.persist(newCounter("background-gate"));
            counterBookmark = bookmarkService.bookmarkForElseFail(counterRepository.find().get(0));
        }).ifFailureFail();
    }

    @Test
    void recordingAndReplayWaitForCommittedBackgroundCompletion() {
        invoke(Counter::scheduleBumpInBackground);

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            assertThat(repository.findBackgroundAndNotYetStarted()).hasSize(1);
            assertThat(foregroundEntries())
                    .extracting(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry::getLogicalMemberIdentifier)
                    .containsExactly("commandlog.test.Counter#scheduleBumpInBackground");
        }).ifFailureFail();

        final var rejected = transactionService.runTransactional(Propagation.REQUIRES_NEW, () ->
                wrapperFactory.wrap(counter()).bumpUsingDeclaredAction());
        assertThat(rejected.getFailure()).isPresent();
        assertThat(rejected.getFailure().orElseThrow())
                .hasMessageContaining("Cannot continue command-log recording")
                .hasMessageContaining("executed and committed before continuing");
        interactionService.nextInteraction();

        runBackgroundCommands();
        invoke(Counter::bumpUsingDeclaredAction);

        final Timestamp baseline = transactionService.callTransactional(
                Propagation.REQUIRES_NEW, () -> {
                    assertThat(repository.findBackgroundAndNotYetStarted()).isEmpty();
                    final var foreground = foregroundEntries();
                    assertThat(foreground)
                            .extracting(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry::getLogicalMemberIdentifier)
                            .containsExactly(
                                    "commandlog.test.Counter#scheduleBumpInBackground",
                                    "commandlog.test.Counter#bumpUsingDeclaredAction");
                    foreground.forEach(entry -> entry.setReplayState(ReplayState.PENDING));
                    return Timestamp.from(foreground.get(0).getTimestamp().toInstant().minusSeconds(1));
                }).valueAsNonNullElseFail();

        replayAll(baseline);

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            final var foreground = foregroundEntries();
            assertThat(foreground).extracting(
                    org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry::getReplayState)
                    .containsExactly(ReplayState.OK, ReplayState.PENDING);
            assertThat(repository.findBackgroundAndNotYetStarted()).hasSize(1);
        }).ifFailureFail();

        runBackgroundCommands();
        replayAll(baseline);

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            assertThat(repository.findBackgroundAndNotYetStarted()).isEmpty();
            assertThat(foregroundEntries()).extracting(
                    org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry::getReplayState)
                    .containsExactly(ReplayState.OK, ReplayState.OK);
        }).ifFailureFail();
    }

    private void replayAll(final Timestamp baseline) {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            final var manager = new CommandManager(baseline, 50, replayContext);
            new CommandManager_replayOrRetryMultiple(manager)
                    .act(CommandManager_replayOrRetryMultiple.Limit.ALL);
        }).ifFailureFail();
        interactionService.nextInteraction();
    }

    private void runBackgroundCommands() {
        runBackgroundCommandsJob.execute(jobExecutionContext);
        interactionService.nextInteraction();
    }

    private void invoke(final java.util.function.Consumer<Counter> action) {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () ->
                action.accept(wrapperFactory.wrap(counter()))).ifFailureFail();
        interactionService.nextInteraction();
    }

    private Counter counter() {
        return bookmarkService.lookup(counterBookmark, Counter.class).orElseThrow();
    }

    private List<org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry> foregroundEntries() {
        return repository.findForegroundSinceTimestamp(new Timestamp(0));
    }

    private static org.apache.causeway.extensions.commandlog.jpa.integtests.model.Counter newCounter(
            final String name) {
        return org.apache.causeway.extensions.commandlog.jpa.integtests.model.Counter.builder()
                .name(name)
                .build();
    }
}
