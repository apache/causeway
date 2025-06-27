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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.WrapperFactory.AsyncProxy;
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

public abstract class BackgroundService_IntegTestAbstract extends CausewayIntegrationTestAbstract {

    @Inject InteractionService interactionService;
    @Inject BackgroundService backgroundService;
    @Inject WrapperFactory wrapperFactory;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject TransactionService transactionService;
    @Inject RunBackgroundCommandsJob runBackgroundCommandsJob;
    @Inject BookmarkService bookmarkService;
    @Inject CounterRepository<? extends Counter> counterRepository;

    JobExecutionContext mockQuartzJobExecutionContext = Mockito.mock(JobExecutionContext.class);

    Bookmark bookmark;

    protected abstract <T extends Counter> T newCounter(String name);

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
            List<? extends Counter> counters = counterRepository.find();
            assertThat(counters).hasSize(1);

            bookmark = bookmarkService.bookmarkForElseFail(counters.get(0));
        }).ifFailureFail();

        // given
        assertThat(bookmark).isNotNull();

        var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
        assertThat(counter.getNum()).isNull();
    }

    @Test
    void async_using_default_executor_service() {

        final AtomicReference<AsyncProxy<Counter>> asyncProxyUnderTest1 = new AtomicReference<>();
        final AtomicReference<AsyncProxy<Counter_bumpUsingMixin>> asyncProxyUnderTest2 = new AtomicReference<>();

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();

            var control = AsyncControl.defaults();

            asyncProxyUnderTest1.set(wrapperFactory.asyncWrap(counter, control));

        }).ifFailureFail();

        // execute async and wait till done
        {
            asyncProxyUnderTest1.get()
                .thenApplyAsync(Counter::bumpUsingDeclaredAction)
                .orTimeout(5, TimeUnit.SECONDS)
                .join(); // wait till done
        }

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
            var control = AsyncControl.defaults();

            // store the async proxy for later use below
            asyncProxyUnderTest2.set(wrapperFactory.asyncWrapMixin(Counter_bumpUsingMixin.class, counter, control));

        }).ifFailureFail();

        // execute async and wait till done
        {
            // returns the detached counter entity, so we can immediately check whether the action was executed
            var counter = asyncProxyUnderTest2.get()
                    .thenApplyAsync(Counter_bumpUsingMixin::act)
                    // let's wait max 5 sec to allow executor to complete before continuing
                    .orTimeout(5, TimeUnit.SECONDS)
                    .join(); // wait till done
            assertThat(counter.getNum()).isEqualTo(2L);
        }

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(2L);
        }).ifFailureFail();

    }

    @SneakyThrows
    @Test
    void using_background_service() {

        final AtomicReference<AsyncProxy<Counter>> asyncProxyUnderTest = new AtomicReference<>();

        // given
        removeAllCommandLogEntriesAndCounters();

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isNull();

            // when
            asyncProxyUnderTest.set(backgroundService.execute(counter));

        }).ifFailureFail();

        // execute async and wait till done
        {
            asyncProxyUnderTest.get()
                .thenAcceptAsync(Counter::bumpUsingDeclaredAction)
                // let's wait max 5 sec to allow executor to complete before continuing
                .orTimeout(5, TimeUnit.SECONDS)
                .join(); // wait till done
        }

        // then no change to the counter
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isNull();   // still null
        }).ifFailureFail();

        // but then instead a background command is persisted
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var all = commandLogEntryRepository.findAll();
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
        interactionService.nextInteraction();

        // then bumped
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
        }).ifFailureFail();

        // and marked as started and completed
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            var after = commandLogEntryRepository.findAll();
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

}
