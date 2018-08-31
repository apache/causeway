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

package org.apache.isis.core.runtime.threadpool;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;

/**
 * ThreadPoolSupport is application-scoped, meaning ThreadPoolSupport is closed on
 * application's end of life-cycle.
 * <br/><br/>
 * Implementation Note: ThreadPoolSupport::close is triggered by _Context.clear()
 * when application shuts down.
 *
 */
public final class ThreadPoolSupport implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSupport.class);

    private final ThreadGroup group;
    private final ThreadPoolExecutor concurrentExecutor;
    private final ThreadPoolExecutor sequentialExecutor;

    /**
     * @return the application-scoped singleton ThreadPoolSupport instance
     */
    public static ThreadPoolSupport getInstance() {
        return _Context.computeIfAbsent(ThreadPoolSupport.class, __-> new ThreadPoolSupport());
    }
    
    private ThreadPoolSupport() {

        group = new ThreadGroup(ThreadPoolSupport.class.getName());

        final int corePoolSize = Runtime.getRuntime().availableProcessors();
        final int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        final int keepAliveTimeSecs = 5;
        
        final ThreadFactory threadFactory = (Runnable r) -> new Thread(group, r);

        final int queueCapacity = 25;
        final Supplier<BlockingQueue<Runnable>> workQueueFactory = 
                ()->new LinkedBlockingQueue<>(queueCapacity);
        
        
        concurrentExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTimeSecs, TimeUnit.SECONDS,
                workQueueFactory.get(),
                threadFactory);
        
        sequentialExecutor = new ThreadPoolExecutor(1, 1, // fixed size = 1
                keepAliveTimeSecs, TimeUnit.MILLISECONDS,
                workQueueFactory.get(),
                threadFactory);
        
    }
    
    /*
     * Implementation Note: triggered by _Context.clear() when application shuts down.
     */
    @Override
    public void close() throws Exception {
        try {
            concurrentExecutor.shutdown();
        } finally {
            // in case the previous throws, continue execution here
            sequentialExecutor.shutdown();            
        }
    }

    /**
     * Executes specified {@code callables} on the default executor.  
     * See {@link ThreadPoolExecutor#invokeAll(java.util.Collection)}
     * @param callables nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAll(@Nullable final List<Callable<Object>> callables) {
        return invokeAll(concurrentExecutor, callables);
    }

    /**
     * Executes specified {@code callables} on the sequential executor in sequence, one by one.
     * @param callables nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAllSequential(@Nullable final List<Callable<Object>> callables) {
        return invokeAll(sequentialExecutor, callables);
    }
    
    /**
     * Waits if necessary for the computation to complete. (Suppresses checked exceptions.)
     * @param futures
     * @return list of computation results.
     */
    public static List<Object> join(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.currentTimeMillis();
        try{
            final List<Object> returnValues = _Lists.newArrayList();
            for (Future<Object> future : futures) {
                returnValues.add(join(future));
            }
            return returnValues;
        } finally {
            final long t1 = System.currentTimeMillis();
            LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), (t1-t0));
        }
    }

    /**
     * Waits if necessary for the computation to complete. (Suppresses checked exceptions.)
     * @param future
     * @return the computation result
     */
    public static Object join(final Future<Object> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // ignore
        }
        return null;
    }

    // -- HELPER
    
    private List<Future<Object>> invokeAll(ThreadPoolExecutor executor, @Nullable final List<Callable<Object>> callables) {
        if(isEmpty(callables)) {
            return Collections.emptyList();
        }
        try {
            return executor.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
