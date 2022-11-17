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
package org.apache.causeway.extensions.commandlog.applib.integtest;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.CounterRepository;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.SneakyThrows;
import lombok.val;

@ExtendWith(MockitoExtension.class)
public abstract class BackgroundService_IntegTestAbstract extends CausewayIntegrationTestAbstract {

    @Mock JobExecutionContext mockQuartzJobExecutionContext;

    Bookmark bookmark;


    protected abstract Counter newCounter(String name);

    private static boolean prototypingOrig;

    @BeforeAll
    static void setup_environment() {
        prototypingOrig = new CausewaySystemEnvironment().isPrototyping();
        new CausewaySystemEnvironment().setPrototyping(true);
    }

    @AfterAll
    static void reset_environment() {
        new CausewaySystemEnvironment().setPrototyping(prototypingOrig);
    }

    @BeforeEach
    void setup_counter() {

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            counterRepository.removeAll();

            counterRepository.persist(newCounter("fred"));
            List<Counter> counters = counterRepository.find();
            assertThat(counters).hasSize(1);

            bookmark = bookmarkService.bookmarkForElseFail(counters.get(0));
        }).ifFailureFail();

        // given
        assertThat(bookmark).isNotNull();

        val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
        assertThat(counter.getNum()).isNull();
    }

    @Test
    void async_using_default_executor_service() {

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();

            wrapperFactory.asyncWrap(counter, AsyncControl.returning(Counter.class)).bumpUsingDeclaredAction();

            Thread.sleep(1_000);// horrid, but let's just wait 1 sec to allow executor to complete before continuing
        }).ifFailureFail();

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
        }).ifFailureFail();

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);

            // when
            wrapperFactory.asyncWrapMixin(Counter_bumpUsingMixin.class, counter, AsyncControl.returning(Counter.class)).act();

            Thread.sleep(1_000);// horrid, but let's just wait 1 sec to allow executor to complete before continuing
        }).ifFailureFail();

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(2L);
        }).ifFailureFail();

    }


    @SneakyThrows
    @Test
    void using_background_service() {

        // given
        removeAllCommandLogEntriesAndCounters();

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isNull();

            // when
            backgroundService.execute(counter).bumpUsingDeclaredAction();

            Thread.sleep(1_000);// horrid, but let's just wait 1 sec before testing
        }).ifFailureFail();

        // then no change to the counter
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isNull();   // still null
        }).ifFailureFail();

        // but then instead a background command is persisted
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val all = commandLogEntryRepository.findAll();
            assertThat(all).hasSize(1);
            CommandLogEntry commandLogEntry = all.get(0);

            assertThat(commandLogEntry)
                    .satisfies(x -> assertThat(x.getTarget()).isEqualTo(bookmark))
                    .satisfies(x -> assertThat(x.getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#bumpUsingDeclaredAction"))
                    .satisfies(x -> assertThat(x.getTimestamp()).isNotNull())
                    .satisfies(x -> assertThat(x.getExecuteIn()).isEqualTo(ExecuteIn.BACKGROUND))
                    .satisfies(x -> assertThat(x.getParentInteractionId()).isNotNull())
                    .satisfies(x -> assertThat(x.getCommandDto()).isNotNull())
                    .satisfies(x -> assertThat(x.getStartedAt()).isNull())
                    .satisfies(x -> assertThat(x.getCompletedAt()).isNull())
                    .satisfies(x -> assertThat(x.getResult()).isNull())
                    .satisfies(x -> assertThat(x.getException()).isNullOrEmpty())
                    .satisfies(x -> assertThat(x.getResultSummary()).isNullOrEmpty())
                    .satisfies(x -> assertThat(x.getReplayState()).isEqualTo(ReplayState.UNDEFINED))
                    .satisfies(x -> assertThat(x.getReplayStateFailureReason()).isNull());
        }).ifFailureFail();



        // when (simulate quartz running in the background)
        runBackgroundCommandsJob.execute(mockQuartzJobExecutionContext);

        // then bumped
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
        }).ifFailureFail();

        // and marked as started and completed
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val after = commandLogEntryRepository.findAll();
            assertThat(after).hasSize(1);
            CommandLogEntry commandLogEntryAfter = after.get(0);

            assertThat(commandLogEntryAfter)
                    .satisfies(x -> assertThat(x.getStartedAt()).isNotNull()) // changed
                    .satisfies(x -> assertThat(x.getCompletedAt()).isNotNull()) // changed
                    .satisfies(x -> assertThat(x.getResult()).isNotNull()) // changed
                    .satisfies(x -> assertThat(x.getResultSummary()).isNotNull()) // changed
                    ;
        }).ifFailureFail();


    }

    private void removeAllCommandLogEntriesAndCounters() {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            commandLogEntryRepository.removeAll();
            assertThat(commandLogEntryRepository.findAll()).isEmpty();
        }).ifFailureFail();
    }

    @Inject BackgroundService backgroundService;
    @Inject BackgroundService.PersistCommandExecutorService persistCommandExecutorService;
    @Inject WrapperFactory wrapperFactory;
    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject TransactionService transactionService;
    @Inject RunBackgroundCommandsJob runBackgroundCommandsJob;
    @Inject BookmarkService bookmarkService;
    @Inject CounterRepository counterRepository;

}
