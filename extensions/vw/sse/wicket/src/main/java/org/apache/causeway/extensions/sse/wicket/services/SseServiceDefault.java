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
package org.apache.causeway.extensions.sse.wicket.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.extensions.sse.applib.annotations.SseSource;
import org.apache.causeway.extensions.sse.applib.service.SseChannel;
import org.apache.causeway.extensions.sse.applib.service.SseService;
import org.apache.causeway.extensions.sse.wicket.CausewayModuleExtSseWicket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Server-sent events.
 *
 * @see <a href="https://www.w3schools.com/html/html5_serversentevents.asp">www.w3schools.com</a>
 *
 * @since 2.0 {@index}
 */
@Service
@Named(SseServiceDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
public class SseServiceDefault implements SseService {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSseWicket.NAMESPACE + ".SseServiceDefault";

    @Inject private TransactionService transactionService;
    @Inject private InteractionService interactionService;

    private final EventStreamPool eventStreamPool = new EventStreamPool();

    @Override
    public Optional<SseChannel> lookupByType(final Class<?> sourceType) {
        return eventStreamPool.lookupByType(sourceType);
    }

    @Override
    public void submit(final SseSource task, final ExecutionBehavior executionBehavior) {

        Objects.requireNonNull(task);
        Objects.requireNonNull(executionBehavior);

        var executor = ForkJoinPool.commonPool();

        switch(executionBehavior) {
        case SIMPLE:
            CompletableFuture.runAsync(()->run(task), executor);
            return;
        case REQUIRES_NEW_SESSION:
            break; // fall through
        }

        // spawn a new thread that gets its own session
        CompletableFuture.runAsync(()->{

            interactionService.runAnonymous(()->{
                transactionService.runWithinCurrentTransactionElseCreateNew(()->run(task));
            });

        }, executor);

    }

    // -- HELPER

    private void run(final SseSource task) {

        var sourceType = task.getClass();

        // 'acquires' a possibly new EventStreamLifecycle and increments its running-task-counter
        var eventStreamLifecycle = eventStreamPool.acquireLifecycleForType(sourceType);
        var eventStream = eventStreamLifecycle.getEventStream();

        log.debug("submitting task type='{}' -> stream='{}'", sourceType, eventStream.uuid());

        try {

            task.run(eventStream);

        } catch (Exception e) {

            log.warn("task run failed on source type {} failed", sourceType, e);

        } finally {

            // 'releases' the EventStreamLifecycle meaning it decrements its running-task-counter
            eventStreamLifecycle.release();

        }
    }

    private static class EventStreamPool {

        private final Map<Class<?>,  EventStreamLifecycle> eventStreamsByType = new ConcurrentHashMap<>();

        public Optional<SseChannel> lookupByType(final Class<?> sourceType) {
            return Optional.ofNullable(eventStreamsByType.get(sourceType))
                    .map(EventStreamLifecycle::getEventStream);
        }

        public synchronized EventStreamLifecycle acquireLifecycleForType(final Class<?> sourceType) {
            var eventStreamLifecycle = eventStreamsByType.computeIfAbsent(sourceType,
                    __->EventStreamLifecycle.of(
                            new EventStreamDefault(UUID.randomUUID(), sourceType),
                            this));
            eventStreamLifecycle.acquire();
            return eventStreamLifecycle;
        }

    }

    @RequiredArgsConstructor(staticName="of")
    private static class EventStreamLifecycle {

        private static final Object $LOCK = new Object[0]; //see https://projectlombok.org/features/Synchronized

        @Getter private final SseChannel eventStream;
        private final EventStreamPool eventStreamPool;

        private int runningTasksCounter;

        public void acquire() {
            synchronized ($LOCK) {
                ++runningTasksCounter;
            }
        }

        public void release() {
            int remaining;

            synchronized ($LOCK) {
                remaining = --runningTasksCounter;
                if(remaining<1) {
                    eventStreamPool.eventStreamsByType.remove(eventStream.sourceType());
                }
            }

            // to keep the synchronized block concise, we run this outside the block,
            // because it does not require synchronization
            if(remaining<1) {
                eventStream.close();
            }
        }

    }

    // -- EVENT STREAM DEFAULT IMPLEMENTATION

    @Slf4j
    private record EventStreamDefault(
            UUID uuid,
            Class<?> sourceType,
            CountDownLatch latch,
            Queue<Predicate<SseSource>> listeners
            ) implements SseChannel {

        private static final Object $LOCK = new Object[0]; //see https://projectlombok.org/features/Synchronized

        public EventStreamDefault(final UUID uuid, final Class<?> sourceType) {
            this(uuid, sourceType, new CountDownLatch(1), new ConcurrentLinkedQueue<>());
        }

        @Override
        public void fire(final SseSource source) {

            final List<Predicate<SseSource>> defensiveCopyOfListeners;

            synchronized ($LOCK) {
                if(!isActive()) return;
                defensiveCopyOfListeners = _Lists.newArrayList(listeners);

            }

            if(log.isDebugEnabled()) {
                log.debug("about to fire events to {} listeners", defensiveCopyOfListeners.size());
            }

            final List<Predicate<SseSource>> markedForRemoval = _Lists.newArrayList();

            defensiveCopyOfListeners.forEach(listener->{
                var retain = listener.test(source);
                if(!retain) {
                    markedForRemoval.add(listener);
                }
            });

            synchronized ($LOCK) {
                if(!isActive()) return;
                listeners.removeAll(markedForRemoval);
            }

        }

        @Override
        public void listenWhile(final Predicate<SseSource> listener) {
            synchronized ($LOCK) {
                if(isActive()) {
                    listeners.add(listener);
                }
            }
        }

        @Override
        public void close() {
            synchronized ($LOCK) {
                listeners.clear();
                latch.countDown();
            }
        }

        private boolean isActive() {
            return latch.getCount()>0L;
        }

        @Override
        public void awaitClose() throws InterruptedException {
            latch.await();
        }

    }

}
