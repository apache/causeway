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

import java.util.Arrays;
import java.util.Collection;
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

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadPoolSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSupport.class);

    private final static int KEEP_ALIVE_TIME_SECS = 5;
    private final static int QUEUE_CAPACITY = 5000;

    private static final int MIN_CORE_POOL_SIZE = 4;
    private static final int MIN_MAX_POOL_SIZE = 4;

    private final ThreadGroup group;
    private final ThreadPoolExecutor concurrentExecutor;
    private final ThreadPoolExecutor sequentialExecutor;

    private static ThreadPoolSupport threadPoolSupport;

    public static synchronized ThreadPoolSupport getInstance() {
        if (threadPoolSupport == null) {
            threadPoolSupport = new ThreadPoolSupport();
        }
        return threadPoolSupport;
    }

    private ThreadPoolSupport() {
        group = new ThreadGroup(ThreadPoolSupport.class.getName());

        final int corePoolSize = Math.min(Runtime.getRuntime().availableProcessors(), MIN_CORE_POOL_SIZE);
        final int maximumPoolSize = Math.min(Runtime.getRuntime().availableProcessors(), MIN_MAX_POOL_SIZE);

        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(group, r);
            }
        };

        Supplier<BlockingQueue<Runnable>> workQueueFactory = new Supplier<BlockingQueue<Runnable>>() {
            @Override
            public BlockingQueue<Runnable> get() {
                return new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            }
        };

        concurrentExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                KEEP_ALIVE_TIME_SECS,
                TimeUnit.SECONDS,
                workQueueFactory.get(),
                threadFactory);

        sequentialExecutor = new ThreadPoolExecutor(
                1,
                1,
                KEEP_ALIVE_TIME_SECS,
                TimeUnit.SECONDS,
                workQueueFactory.get(),
                threadFactory);

    }

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
     * Executes specified {@code callables} on the default executor.
     * See {@link ThreadPoolExecutor#invokeAll(java.util.Collection)}
     * @param callables nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAll(final Callable<Object>... callables) {
        return invokeAll(Arrays.asList(callables));
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
     * Executes specified {@code callables} on the sequential executor in sequence, one by one.
     * @param callables nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAllSequential(final Callable<Object>... callables) {
        return invokeAllSequential(Arrays.asList(callables));
    }


    public List<Object> join(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.currentTimeMillis();
        try{
            final List<Object> returnValues = Lists.newArrayList();
            for (Future<Object> future : futures) {
                Object result;
                try {
                    result = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    // ignore
                    result = null;
                }
                returnValues.add(result);
            }
            return returnValues;
        } finally {
            final long t1 = System.currentTimeMillis();
            if(LOG.isInfoEnabled()) {
                LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), (t1-t0));
            }
        }
    }

    public List<Object> joinGatherFailures(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.currentTimeMillis();
        try{
            final List<Object> returnValues = Lists.newArrayList();
            for (Future<Object> future : futures) {
                final Object result;
                try {
                    result = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                returnValues.add(result);
            }
            return returnValues;
        } finally {
            final long t1 = System.currentTimeMillis();
            LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), (t1-t0));
        }
    }

    public Object join(final Future<Object> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // ignore
            return null;
        }
    }

    // -- HELPER

    private List<Future<Object>> invokeAll(ThreadPoolExecutor executor, @Nullable final List<Callable<Object>> callables) {
        if(isEmpty(callables)) {
            return Collections.emptyList();
        }
        try {
            return executor.invokeAll(timed(executor, callables));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Callable<Object>> timed(
            final ThreadPoolExecutor executor,
            final List<Callable<Object>> callables) {
        final long queuedAt = System.currentTimeMillis();
        return FluentIterable.from(callables).transform(
                new Function<Callable<Object>, Callable<Object>>() {
                    @Override
                    public Callable<Object> apply(final Callable<Object> callable) {
                        final int queueSize = executor.getQueue().size();
                        return timed(callable, queueSize, queuedAt);
                    }
                }).toList();
    }

    private static Callable<Object> timed(
            final Callable<Object> callable,
            final int queueSize,
            final long queuedAt) {
        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                final long startedAt = System.currentTimeMillis();
                if(LOG.isDebugEnabled()) {
                    LOG.debug("START: workQueue.size: {}, waited for: {}ms, {}",
                            queueSize,
                            startedAt - queuedAt,
                            callable.toString());
                }
                try {
                    return callable.call();
                } finally {
                    final long completedAt = System.currentTimeMillis();
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("END: completed in: {}ms, {}",
                                completedAt - startedAt,
                                callable.toString());
                    }
                }
            }
        };
    }

    private static boolean isEmpty(Collection<?> x) { return x==null || x.size() == 0; }


}
