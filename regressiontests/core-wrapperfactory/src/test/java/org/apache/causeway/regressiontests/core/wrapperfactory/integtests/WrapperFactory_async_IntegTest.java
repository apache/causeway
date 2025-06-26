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
package org.apache.causeway.regressiontests.core.wrapperfactory.integtests;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.testdomain.wrapperfactory.Counter;
import org.apache.causeway.testdomain.wrapperfactory.Counter_bumpUsingMixin;

import lombok.SneakyThrows;

class WrapperFactory_async_IntegTest extends CoreWrapperFactory_IntegTestAbstract {

    @Inject WrapperFactory wrapperFactory;
    @Inject TransactionService transactionService;
    @Inject BookmarkService bookmarkService;

    Bookmark bookmark;

    @BeforeEach
    void setup_counter() {

        runWithNewTransaction(() -> {
            counterRepository.persist(newCounter("fred"));
            List<Counter> counters = counterRepository.find();
            assertThat(counters).hasSize(1);

            bookmark = bookmarkService.bookmarkForElseFail(counters.get(0));
        });

        // given
        assertThat(bookmark).isNotNull();

        var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
        assertThat(counter.getNum()).isNull();
    }

    @SneakyThrows
    @ParameterizedTest(name = "executorService[{index}]: {0}")
    @MethodSource("executorServices")
    void async_using_default_executor_service(final String displayName, final ExecutorService executorService) {

        // when - executing regular action
        runWithNewTransaction(() -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();

            var asyncControl = AsyncControl.defaults()
                    .with(executorService);

            wrapperFactory.asyncWrap(counter, asyncControl)
                .thenApplyAsync(Counter::increment)
                .orTimeout(5_000, TimeUnit.MILLISECONDS)
                .join(); // let's wait max 5 sec to allow executor to complete before continuing
        });

        // then
        runWithNewTransaction(() -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
        });

        // when - executing mixed-in action
        runWithNewTransaction(() -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);

            var asyncControl = AsyncControl.defaults()
                    .with(executorService);

            // when
            wrapperFactory.asyncWrapMixin(Counter_bumpUsingMixin.class, counter, asyncControl)
                .thenApplyAsync(Counter_bumpUsingMixin::act)
                // let's wait max 5 sec to allow executor to complete before continuing
                .orTimeout(5_000, TimeUnit.MILLISECONDS)
                .join(); // wait till done

            assertThat(counter.getNum()).isEqualTo(2L); // verify execution succeeded
        });

        // then
        runWithNewTransaction(() -> {
            var counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter).isNotNull();
            assertThat(counter.getNum()).isEqualTo(2L);
        });
    }

    // -- HELPER

    private static Stream<Arguments> executorServices() {
        return Stream.of(
              Arguments.of("Executors.newSingleThreadExecutor()", Executors.newSingleThreadExecutor()),
              Arguments.of("ForkJoinPool.commonPool()", ForkJoinPool.commonPool()),
              Arguments.of("Executors.newFixedThreadPool(4)", Executors.newFixedThreadPool(4))
        );
    }

}
