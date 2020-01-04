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
package org.apache.isis.commons.internal.concurrent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.Level;

import org.apache.isis.commons.concurrent.AwaitableLatch;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@RequiredArgsConstructor(staticName = "named") 
@Log4j2
public class _ConcurrentTaskList {

    @Getter private final String name;
    
    private final List<_ConcurrentTask<?>> tasks = _Lists.newArrayList();
    private final AtomicBoolean wasStarted = new AtomicBoolean();
    private final CountDownLatch allFinishedLatch = new CountDownLatch(1);
    private final AwaitableLatch awaitableLatch = AwaitableLatch.of(allFinishedLatch);
    private final LongAdder tasksExecuted = new LongAdder();
    private long executionTimeNanos;
    
    public List<_ConcurrentTask<?>> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // -- ASSEMBLING

    public _ConcurrentTaskList addTask(_ConcurrentTask<?> task) {
        synchronized (tasks) {
            if(wasStarted.get()) {
                val msg = "Tasks already started execution, can no longer modify collection of tasks!"; 
                throw new IllegalStateException(msg);
            }
            tasks.add(task);
        }   
        return this;
    }

    public _ConcurrentTaskList addTasks(Collection<? extends _ConcurrentTask<?>> tasks) {
        synchronized (this.tasks) {
            if(wasStarted.get()) {
                val msg = "Tasks already started execution, can no longer modify collection of tasks!"; 
                throw new IllegalStateException(msg);
            }
            this.tasks.addAll(tasks);
        }
        return this;
    }
    
    // -- EXECUTION
    
    public _ConcurrentTaskList submit(_ConcurrentContext context) {
        
        synchronized (tasks) {
            if(wasStarted.get()) {
                val msg = "Tasks already started execution, can not start again!"; 
                throw new IllegalStateException(msg);
            }
            wasStarted.set(true);
        }
        
        val t0 = System.nanoTime();
        
        if(context.shouldRunSequential()) {
            for(_ConcurrentTask<?> task : tasks) {
                task.run(); // exceptions are swallowed, to be found in the _ConcurrentTask object 
                tasksExecuted.increment();
            }
            executionTimeNanos = System.nanoTime() - t0;
            onFinished(context);
            allFinishedLatch.countDown();
            return this;
        }
        
        // else run with executor ...
        
        val futures = new ArrayList<Future<?>>(tasks.size());
        
        for(_ConcurrentTask<?> task : tasks) {
            futures.add(context.executorService.submit(task));
        }

        // now wait for all futures to complete on a separate thread
        
        val thread = new Thread() {
            
            @Override
            public void run() {
                for(Future<?> future : futures) {
                    try {
                        future.get();
                        tasksExecuted.increment();
                    } catch (InterruptedException | ExecutionException e) {
                        // ignore, continue waiting on tasks
                    }
                    
                }
                executionTimeNanos = System.nanoTime() - t0;
                
                onFinished(context);
                allFinishedLatch.countDown();
            }
            
        };
        
        thread.start();
        
        return this;
        
    }
    
    // -- SYNCHRONICATION

    public AwaitableLatch latch() {
        return awaitableLatch;
    }
    
    public void await() {
        latch().await();
    }
    
    // -- FIELDS/GETTERS
    
    public Duration getExecutionTime() {
        return Duration.of(executionTimeNanos, ChronoUnit.NANOS);
    }
    
    // -- EXECUTION LOGGING
    
    private void onFinished(_ConcurrentContext context) {
        
        for(val task: tasks) {
            if(task.getFailedWith()!=null) {
                log.error("----------------------------------------");
                log.error("Failed TaskList: " + this.getName());
                log.error("Failed Task: " + task.getName());
                log.error("----------------------------------------", task.getFailedWith());
            }
        }
        
        if(!context.enableExecutionLogging) {
            return;
        }
        
        log.printf(Level.INFO, 
                "TaskList '%s' running %d/%d tasks %s, took %.3f milliseconds ",
                getName(),
                tasksExecuted.longValue(),
                tasks.size(),
                context.shouldRunSequential() ? "sequential" : "concurrent",
                        0.000_001 * executionTimeNanos);   
        
    }

    // -- SHORTCUTS

    public _ConcurrentTaskList addRunnable(String name, Runnable runnable) {
        return addTask(_ConcurrentTask.of(runnable).withName(name));
    }
    
    public _ConcurrentTaskList submit(_ConcurrentContext._ConcurrentContextBuilder contextBuilder) {
        return submit(contextBuilder.build());
    }
   

}
