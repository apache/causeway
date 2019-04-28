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
package org.apache.isis.core.runtime.services.sse;

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
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.events.sse.EventStream;
import org.apache.isis.applib.events.sse.EventStreamService;
import org.apache.isis.applib.events.sse.EventStreamSource;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

/**
 * Server-sent events.
 *  
 * @see https://www.w3schools.com/html/html5_serversentevents.asp
 * 
 * @since 2.0.0-M3
 *
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class EventStreamServiceDefault implements EventStreamService {

    @Inject TransactionService transactionService;

    private final EventStreamPool eventStreamPool = new EventStreamPool();

    @Override
    public Optional<EventStream> lookupByType(Class<?> sourceType) {
        return eventStreamPool.lookupByType(sourceType);
    }
    
    @Override
    public void submit(EventStreamSource task, ExecutionBehavior executionBehavior) {

        Objects.requireNonNull(task);
        Objects.requireNonNull(executionBehavior);
        
        val threadPool = ThreadPoolSupport.getInstance();
        
        switch(executionBehavior) {
        case SIMPLE:
            CompletableFuture.runAsync(()->run(task), threadPool.getExecutor());
            return;
        case REQUIRES_NEW_SESSION:
            break; // fall through
        }

        val callingThread_TransactionLatch = transactionService.currentTransactionLatch();

        // spawn a new thread that gets its own session
        CompletableFuture.runAsync(()->{
          
         // wait for calling thread to commit its current transaction 
            try {
                callingThread_TransactionLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
      
            IsisContext.getSessionFactory().doInSession(()->run(task));
            
        }, threadPool.getExecutor());
        
//        ForkJoinPool.commonPool().submit(()->{
//
//        });

    }

    // -- HELPER

    private void run(EventStreamSource task) {

        val sourceType = task.getClass();

        // 'acquires' a possibly new EventStreamLifecycle and increments its running-task-counter
        val eventStreamLifecycle = eventStreamPool.acquireLifecycleForType(sourceType);
        val eventStream = eventStreamLifecycle.getEventStream();

        probe.println("submitting task type='%s' -> stream='%s'", sourceType, eventStream.getId());


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

        public Optional<EventStream> lookupByType(Class<?> sourceType) {
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

        @Getter private final EventStream eventStream;
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

    private final static _Probe probe = _Probe.unlimited().label("BackgroundExecutionService"); 

    @Value
    private static class EventStreamDefault implements EventStream {

        private final static _Probe probe = _Probe.unlimited().label("EventStreamDefault");
        private static final Object $LOCK = new Object[0]; //see https://projectlombok.org/features/Synchronized

        @Getter final UUID id;
        @Getter final Class<?> sourceType;

        private final CountDownLatch latch = new CountDownLatch(1);
        private final Queue<Predicate<EventStreamSource>> listeners = new ConcurrentLinkedQueue<>();

        @Override
        public void fire(EventStreamSource source) {

            final List<Predicate<EventStreamSource>> defensiveCopyOfListeners;

            synchronized ($LOCK) {
                if(!isActive()) {
                    return;
                }
                defensiveCopyOfListeners = _Lists.newArrayList(listeners);

            }

            probe.println("fire listeners=%d", defensiveCopyOfListeners.size());

            final List<Predicate<EventStreamSource>> markedForRemoval = _Lists.newArrayList();

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
        public void listenWhile(Predicate<EventStreamSource> listener) {
            synchronized ($LOCK) {
                if(isActive()) {
                    listeners.add(listener);   
                }
            }
        }

        @Override
        public void close() {
            probe.println("close");
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
