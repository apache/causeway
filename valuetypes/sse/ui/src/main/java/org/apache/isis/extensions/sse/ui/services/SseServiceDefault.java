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
package org.apache.isis.extensions.sse.ui.services;

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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.sse.applib.service.SseChannel;
import org.apache.isis.extensions.sse.applib.service.SseService;
import org.apache.isis.extensions.sse.applib.annotations.SseSource;
import org.apache.isis.runtime.persistence.transaction.IsisTransactionAspectSupport;
import org.apache.isis.runtime.session.IsisSessionFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Server-sent events.
 *  
 * @see https://www.w3schools.com/html/html5_serversentevents.asp
 * 
 * @since 2.0
 *
 */
@Service
@Named("isisValSse.SseServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class SseServiceDefault implements SseService {

    @Inject TransactionService transactionService;
    @Inject IsisSessionFactory isisSessionFactory;

    private final EventStreamPool eventStreamPool = new EventStreamPool();

    @Override
    public Optional<SseChannel> lookupByType(Class<?> sourceType) {
        return eventStreamPool.lookupByType(sourceType);
    }

    @Override
    public void submit(SseSource task, ExecutionBehavior executionBehavior) {

        Objects.requireNonNull(task);
        Objects.requireNonNull(executionBehavior);
        
        val executor = ForkJoinPool.commonPool();

        switch(executionBehavior) {
        case SIMPLE:
            CompletableFuture.runAsync(()->run(task), executor);
            return;
        case REQUIRES_NEW_SESSION:
            break; // fall through
        }

        val callingThread_TransactionLatch = IsisTransactionAspectSupport.transactionLatch();

        // spawn a new thread that gets its own session
        CompletableFuture.runAsync(()->{

            // wait for calling thread to commit its current transaction 
            callingThread_TransactionLatch.await();

            isisSessionFactory.doInSession(()->run(task));

        }, executor);

    }

    // -- HELPER

    private void run(SseSource task) {

        val sourceType = task.getClass();

        // 'acquires' a possibly new EventStreamLifecycle and increments its running-task-counter
        val eventStreamLifecycle = eventStreamPool.acquireLifecycleForType(sourceType);
        val eventStream = eventStreamLifecycle.getEventStream();

        log.debug("submitting task type='{}' -> stream='{}'", sourceType, eventStream.getId());

        try {

            task.run(eventStream);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            // 'releases' the EventStreamLifecycle meaning it decrements its running-task-counter
            eventStreamLifecycle.release();

        }
    }

    private static class EventStreamPool {

        private final Map<Class<?>,  EventStreamLifecycle> eventStreamsByType = new ConcurrentHashMap<>();

        public Optional<SseChannel> lookupByType(Class<?> sourceType) {
            return Optional.ofNullable(eventStreamsByType.get(sourceType))
                    .map(EventStreamLifecycle::getEventStream);
        }

        public synchronized EventStreamLifecycle acquireLifecycleForType(Class<?> sourceType) {
            val eventStreamLifecycle = eventStreamsByType.computeIfAbsent(sourceType, 
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
                    eventStreamPool.eventStreamsByType.remove(eventStream.getSourceType());    
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

    @Value @Log4j2
    private static class EventStreamDefault implements SseChannel {

        private static final Object $LOCK = new Object[0]; //see https://projectlombok.org/features/Synchronized

        @Getter final UUID id;
        @Getter final Class<?> sourceType;

        private final CountDownLatch latch = new CountDownLatch(1);
        private final Queue<Predicate<SseSource>> listeners = new ConcurrentLinkedQueue<>();

        @Override
        public void fire(SseSource source) {

            final List<Predicate<SseSource>> defensiveCopyOfListeners;

            synchronized ($LOCK) {
                if(!isActive()) {
                    return;
                }
                defensiveCopyOfListeners = _Lists.newArrayList(listeners);

            }

            log.debug("about to fire events to {} listeners", ()->defensiveCopyOfListeners.size());

            final List<Predicate<SseSource>> markedForRemoval = _Lists.newArrayList();

            defensiveCopyOfListeners.forEach(listener->{
                val retain = listener.test(source);
                if(!retain) {
                    markedForRemoval.add(listener);
                }
            });

            synchronized ($LOCK) {
                if(!isActive()) {
                    return;
                }
                listeners.removeAll(markedForRemoval);
            }


        }

        @Override
        public void listenWhile(Predicate<SseSource> listener) {
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
